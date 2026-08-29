package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.model.LeaderboardData;
import dev.smnl.smessential.model.StatisticType;
import dev.smnl.smessential.service.LeaderboardService;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.MessageFormatter.MessageType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeaderboardCommand extends EssentialCommand {

  private static final Pattern SIZE_PATTERN = Pattern.compile("^(\\d+)[xX](\\d+)$");
  private final LeaderboardService leaderboardService;

  public LeaderboardCommand(@NotNull LeaderboardService leaderboardService) {
    super(
        "leaderboard",
        "Administration",
        "Manages in-game stat leaderboard displays",
        "smessential.command.administration",
        false,
        new String[] {"lb", "top"});
    this.leaderboardService = leaderboardService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      sendHelp(sender, 1);
      return;
    }

    String sub = args[0].toLowerCase(Locale.ROOT);
    try {
      int page = Integer.parseInt(sub);
      sendHelp(sender, page);
      return;
    } catch (NumberFormatException ignored) {
    }

    switch (sub) {
      case "create", "place", "add" -> handleCreate(sender, args);
      case "remove", "delete" -> handleRemove(sender, args);
      case "list" -> handleList(sender);
      case "tp", "teleport" -> handleTeleport(sender, args);
      case "reload" -> handleReload(sender);
      case "refresh" -> handleRefresh(sender);
      case "help", "h", "?" -> {
        int page = 1;
        if (args.length > 1) {
          try {
            page = Integer.parseInt(args[1]);
          } catch (NumberFormatException ignored) {
          }
        }
        sendHelp(sender, page);
      }
      default -> sendHelp(sender, 1);
    }
  }

  private record BoardSize(int width, int height) {
    public static @NotNull BoardSize parse(@Nullable String arg) {
      if (arg == null) return new BoardSize(1, 1);
      Matcher matcher = SIZE_PATTERN.matcher(arg.trim());
      if (matcher.matches()) {
        try {
          int w = Math.max(1, Math.min(5, Integer.parseInt(matcher.group(1))));
          int h = Math.max(1, Math.min(5, Integer.parseInt(matcher.group(2))));
          return new BoardSize(w, h);
        } catch (NumberFormatException ignored) {
        }
      }
      return new BoardSize(1, 1);
    }

    public static boolean isSizePattern(@Nullable String arg) {
      if (arg == null) return false;
      return SIZE_PATTERN.matcher(arg.trim()).matches();
    }
  }

  private void handleCreate(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendError(
          sender,
          "Usage: /leaderboard create <id> <stat> [size: 2x2] [limit] OR /leaderboard create <id> <stat> <x> <y> <z> [world] [size] [limit]");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    if (!id.matches("^[a-zA-Z0-9_-]+$")) {
      sendError(
          sender, "Leaderboard ID must only contain letters, numbers, hyphens, or underscores.");
      return;
    }

    StatisticType statType = StatisticType.fromKey(args[2]);
    if (statType == null) {
      StringBuilder valid = new StringBuilder();
      for (StatisticType type : StatisticType.values()) {
        if (!valid.isEmpty()) valid.append(", ");
        valid.append(type.getKey());
      }
      sendError(sender, "Unknown statistic '" + args[2] + "'. Available stats: " + valid);
      return;
    }

    Location loc;
    int limit = 10;
    BoardSize size = new BoardSize(1, 1);

    if (args.length >= 6 && isCoordinate(args[3])) {
      // Coordinates provided explicitly: /leaderboard create <id> <stat> <x> <y> <z> [world] [size]
      // [limit]
      World targetWorld = null;
      int nextArg = 6;
      if (args.length > nextArg
          && !BoardSize.isSizePattern(args[nextArg])
          && !isInteger(args[nextArg])) {
        targetWorld = Bukkit.getWorld(args[nextArg]);
        if (targetWorld == null) {
          sendError(sender, "World '" + args[nextArg] + "' not found.");
          return;
        }
        nextArg++;
      } else if (sender instanceof Player player) {
        targetWorld = player.getWorld();
      } else {
        List<World> worlds = Bukkit.getWorlds();
        if (!worlds.isEmpty()) {
          targetWorld = worlds.get(0);
        }
      }

      if (targetWorld == null) {
        sendError(sender, "Could not determine world for leaderboard location.");
        return;
      }

      try {
        double x =
            parseCoordinate(args[3], sender instanceof Player p ? p.getLocation().getX() : 0.0);
        double y =
            parseCoordinate(args[4], sender instanceof Player p ? p.getLocation().getY() : 0.0);
        double z =
            parseCoordinate(args[5], sender instanceof Player p ? p.getLocation().getZ() : 0.0);
        float yaw = sender instanceof Player p ? p.getLocation().getYaw() : 0.0f;
        float pitch = sender instanceof Player p ? p.getLocation().getPitch() : 0.0f;
        loc = new Location(targetWorld, x, y, z, yaw, pitch);
      } catch (NumberFormatException e) {
        sendError(sender, "Invalid numeric coordinates specified.");
        return;
      }

      for (int i = nextArg; i < args.length; i++) {
        if (BoardSize.isSizePattern(args[i])) {
          size = BoardSize.parse(args[i]);
        } else if (isInteger(args[i])) {
          limit = Math.max(1, Math.min(30, Integer.parseInt(args[i])));
        }
      }
    } else {
      // In-game wall target creation: /leaderboard create <id> <stat> [size] [limit]
      if (!(sender instanceof Player player)) {
        sendError(
            sender,
            "Console must specify coordinates: /leaderboard create <id> <stat> <x> <y> <z> [world] [size] [limit]");
        return;
      }

      Block targetBlock = player.getTargetBlockExact(5);
      BlockFace targetFace = targetBlock != null ? player.getTargetBlockFace(5) : null;
      if (targetBlock != null
          && targetFace != null
          && targetFace != BlockFace.UP
          && targetFace != BlockFace.DOWN) {
        Block frameBlock = targetBlock.getRelative(targetFace);
        loc = frameBlock.getLocation();
        loc.setYaw(LeaderboardService.yawFromBlockFace(targetFace));
        loc.setPitch(0.0f);
      } else {
        loc = player.getLocation().getBlock().getLocation();
        loc.setYaw(LeaderboardService.yawFromBlockFace(player.getFacing().getOppositeFace()));
        loc.setPitch(0.0f);
      }

      for (int i = 3; i < args.length; i++) {
        if (BoardSize.isSizePattern(args[i])) {
          size = BoardSize.parse(args[i]);
        } else if (isInteger(args[i])) {
          limit = Math.max(1, Math.min(30, Integer.parseInt(args[i])));
        }
      }
    }

    leaderboardService.createOrUpdateLeaderboard(
        id, statType, loc, limit, size.width(), size.height());

    Component successMessage =
        Component.text("Created leaderboard ", NamedTextColor.GRAY)
            .append(Component.text(id, NamedTextColor.WHITE))
            .append(Component.text(" for ", NamedTextColor.GRAY))
            .append(Component.text(statType.getDisplayName(), NamedTextColor.GOLD))
            .append(Component.text(" at ", NamedTextColor.GRAY))
            .append(
                Component.text(
                    String.format(
                        Locale.US,
                        "%.1f, %.1f, %.1f (%s)",
                        loc.getX(),
                        loc.getY(),
                        loc.getZ(),
                        loc.getWorld().getName()),
                    NamedTextColor.WHITE))
            .append(
                Component.text(
                    String.format(
                        Locale.US,
                        " (Top %d • %dx%d Painting)",
                        limit,
                        size.width(),
                        size.height()),
                    NamedTextColor.GRAY));

    sender.sendMessage(MessageFormatter.formatInfo("Administration", successMessage));
  }

  private void handleRemove(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 2) {
      sendError(sender, "Usage: /leaderboard remove <id>");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    boolean removed = leaderboardService.removeLeaderboard(id);
    if (!removed) {
      sendError(sender, "Leaderboard '" + id + "' does not exist.");
      return;
    }

    sender.sendMessage(
        MessageFormatter.formatInfo(
            "Administration",
            Component.text("Leaderboard ", NamedTextColor.GRAY)
                .append(Component.text(id, NamedTextColor.WHITE))
                .append(Component.text(" has been removed.", NamedTextColor.GRAY))));
  }

  private void handleList(@NotNull CommandSender sender) {
    Collection<LeaderboardData> all = leaderboardService.getAllLeaderboards();
    if (all.isEmpty()) {
      sender.sendMessage(
          MessageFormatter.formatInfo("Administration", "No leaderboards currently exist."));
      return;
    }

    TextComponent.Builder builder = Component.text();
    builder.append(Component.newline());
    builder.append(
        MessageFormatter.format(
            getToolName(), "Leaderboards (" + all.size() + "):", MessageType.INFO, false));

    for (LeaderboardData data : all) {
      Component line =
          Component.text("- ", NamedTextColor.DARK_GRAY)
              .append(Component.text(data.id(), NamedTextColor.WHITE))
              .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
              .append(Component.text(data.statType().getDisplayName(), NamedTextColor.GRAY))
              .append(
                  Component.text(
                      String.format(
                          Locale.US,
                          " @ %s (%.1f, %.1f, %.1f) [Top %d • %dx%d Painting]",
                          data.worldName(),
                          data.x(),
                          data.y(),
                          data.z(),
                          data.limit(),
                          data.width(),
                          data.height()),
                      NamedTextColor.DARK_GRAY));
      builder.append(Component.newline()).append(line);
    }

    builder.append(Component.newline());
    sender.sendMessage(builder.build());
  }

  private void handleTeleport(@NotNull CommandSender sender, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      sendError(sender, "Only players can teleport to a leaderboard.");
      return;
    }

    if (args.length < 2) {
      sendError(sender, "Usage: /leaderboard tp <id>");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    LeaderboardData data = leaderboardService.getLeaderboard(id);
    if (data == null) {
      sendError(sender, "Leaderboard '" + id + "' does not exist.");
      return;
    }

    Location loc = data.toLocation();
    if (loc == null || loc.getWorld() == null) {
      sendError(sender, "Leaderboard world '" + data.worldName() + "' is not loaded.");
      return;
    }

    player.teleport(loc);
    sender.sendMessage(
        MessageFormatter.formatInfo(
            "Administration",
            Component.text("Teleported to leaderboard ", NamedTextColor.GRAY)
                .append(Component.text(id, NamedTextColor.WHITE))));
  }

  private void handleReload(@NotNull CommandSender sender) {
    leaderboardService.reload();
    sender.sendMessage(
        MessageFormatter.formatInfo(
            "Administration", "Leaderboards reloaded from database successfully."));
  }

  private void handleRefresh(@NotNull CommandSender sender) {
    leaderboardService.refreshAllDisplays();
    sender.sendMessage(
        MessageFormatter.formatInfo("Administration", "Refreshed all leaderboard map displays."));
  }

  private static final List<HelpEntry> HELP_ENTRIES =
      List.of(
          new HelpEntry(
              "/leaderboard create <id> <stat> [size: 2x2] [limit]",
              "Creates or updates a leaderboard painting"),
          new HelpEntry("/leaderboard remove <id>", "Removes a leaderboard"),
          new HelpEntry("/leaderboard list", "Lists all active leaderboards"),
          new HelpEntry("/leaderboard tp <id>", "Teleports to a leaderboard"),
          new HelpEntry("/leaderboard reload", "Reloads leaderboards from database"),
          new HelpEntry("/leaderboard refresh", "Refreshes all leaderboard displays"));

  private void sendHelp(@NotNull CommandSender sender, int page) {
    sendPaginatedHelp(sender, "Leaderboard commands", "leaderboard", HELP_ENTRIES, page);
  }

  private boolean isCoordinate(@Nullable String arg) {
    if (arg == null || arg.isBlank()) return false;
    if (arg.startsWith("~")) return true;
    try {
      Double.parseDouble(arg);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean isInteger(@Nullable String arg) {
    if (arg == null || arg.isBlank()) return false;
    try {
      Integer.parseInt(arg);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private double parseCoordinate(String arg, double relativeTo) throws NumberFormatException {
    if (arg.startsWith("~")) {
      if (arg.length() == 1) {
        return relativeTo;
      }
      return relativeTo + Double.parseDouble(arg.substring(1));
    }
    return Double.parseDouble(arg);
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack stack, @NotNull String[] args) {
    if (args.length == 0 || args.length == 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
      List<String> subcommands =
          List.of("create", "place", "remove", "delete", "list", "tp", "reload", "refresh", "help");
      return subcommands.stream().filter(s -> s.startsWith(prefix)).toList();
    }

    String sub = args[0].toLowerCase(Locale.ROOT);

    if (args.length == 2) {
      String prefix = args[1].toLowerCase(Locale.ROOT);
      if (sub.equals("remove")
          || sub.equals("delete")
          || sub.equals("tp")
          || sub.equals("teleport")) {
        return leaderboardService.getAllLeaderboards().stream()
            .map(LeaderboardData::id)
            .filter(id -> id.startsWith(prefix))
            .toList();
      }
    }

    if ((sub.equals("create") || sub.equals("place") || sub.equals("add")) && args.length == 3) {
      String prefix = args[2].toLowerCase(Locale.ROOT);
      return Arrays.stream(StatisticType.values())
          .map(StatisticType::getKey)
          .filter(k -> k.startsWith(prefix))
          .toList();
    }

    if ((sub.equals("create") || sub.equals("place") || sub.equals("add")) && args.length == 4) {
      String prefix = args[3].toLowerCase(Locale.ROOT);
      List<String> suggestions =
          List.of("1x1", "2x1", "1x2", "2x2", "3x2", "3x3", "~", "5", "10", "15", "20");
      return suggestions.stream().filter(s -> s.startsWith(prefix)).toList();
    }

    if ((sub.equals("create") || sub.equals("place") || sub.equals("add")) && args.length == 5) {
      String prefix = args[4].toLowerCase(Locale.ROOT);
      List<String> suggestions =
          List.of("1x1", "2x1", "1x2", "2x2", "3x2", "3x3", "5", "10", "15", "20");
      return suggestions.stream().filter(s -> s.startsWith(prefix)).toList();
    }

    if ((sub.equals("create") || sub.equals("place") || sub.equals("add")) && args.length == 7) {
      String prefix = args[6].toLowerCase(Locale.ROOT);
      return Bukkit.getWorlds().stream()
          .map(World::getName)
          .filter(w -> w.toLowerCase(Locale.ROOT).startsWith(prefix))
          .toList();
    }

    return Collections.emptyList();
  }
}
