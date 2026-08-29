package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.model.Rank;
import dev.smnl.smessential.service.RankService;
import dev.smnl.smessential.service.UserService;
import dev.smnl.smessential.service.WhitelistService;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WhitelistCommand extends EssentialCommand {

  private final WhitelistService whitelistService;
  private final RankService rankService;
  private final UserService userService;

  public WhitelistCommand(
      @NotNull WhitelistService whitelistService,
      @Nullable RankService rankService,
      @Nullable UserService userService) {
    super(
        "Whitelist",
        "Administration",
        "Manages the server database-backed whitelist",
        "smessential.command.administration",
        false,
        new String[] {"wl"});
    this.whitelistService = whitelistService;
    this.rankService = rankService;
    this.userService = userService;
  }

  public WhitelistCommand(@NotNull WhitelistService whitelistService) {
    this(whitelistService, null, null);
  }

  private static final List<HelpEntry> HELP_ENTRIES =
      List.of(
          new HelpEntry("/whitelist on", "Enables the server whitelist"),
          new HelpEntry("/whitelist off", "Disables the server whitelist"),
          new HelpEntry("/whitelist add <player>", "Adds a player to the whitelist"),
          new HelpEntry("/whitelist remove <player>", "Removes a player from the whitelist"),
          new HelpEntry("/whitelist list [page]", "Lists all whitelisted players"),
          new HelpEntry("/whitelist status", "Displays current whitelist status"));

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
      case "on", "enable" -> {
        whitelistService.setEnabled(true);
        sender.sendMessage(
            MessageFormatter.formatInfo(getToolName(), "Server whitelist has been enabled."));
      }
      case "off", "disable" -> {
        whitelistService.setEnabled(false);
        sender.sendMessage(
            MessageFormatter.formatInfo(getToolName(), "Server whitelist has been disabled."));
      }
      case "add" -> {
        if (args.length < 2) {
          sendUsage(sender, "/whitelist add <player>");
          return;
        }
        handleAdd(sender, args[1]);
      }
      case "remove" -> {
        if (args.length < 2) {
          sendUsage(sender, "/whitelist remove <player>");
          return;
        }
        handleRemove(sender, args[1]);
      }
      case "list" -> handleList(sender, args);
      case "status" -> {
        boolean enabled = whitelistService.isEnabled();
        int count = whitelistService.getWhitelistEntries().size();
        Component msg =
            Component.text("Whitelist is currently ", NamedTextColor.GRAY)
                .append(
                    Component.text(
                        enabled ? "ENABLED" : "DISABLED",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(" with ", NamedTextColor.GRAY))
                .append(Component.text(count, NamedTextColor.WHITE))
                .append(
                    Component.text(
                        " whitelisted player" + (count == 1 ? "" : "s") + ".",
                        NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      }
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

  private void sendHelp(@NotNull CommandSender sender, int page) {
    sendPaginatedHelp(sender, "Whitelist commands", "whitelist", HELP_ENTRIES, page);
  }

  private void handleAdd(@NotNull CommandSender sender, @NotNull String targetName) {
    String addedBy = sender.getName();
    DatabaseManager.WhitelistData entry = whitelistService.addPlayer(targetName, addedBy);
    Component msg =
        Component.text("Added ", NamedTextColor.GRAY)
            .append(Component.text(entry.getDisplayName(), NamedTextColor.WHITE))
            .append(Component.text(" to the whitelist.", NamedTextColor.GRAY));
    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private void handleRemove(@NotNull CommandSender sender, @NotNull String targetName) {
    DatabaseManager.WhitelistData removed = whitelistService.removePlayer(targetName);
    if (removed != null) {
      Component msg =
          Component.text("Removed ", NamedTextColor.GRAY)
              .append(Component.text(removed.getDisplayName(), NamedTextColor.WHITE))
              .append(Component.text(" from the whitelist.", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
    } else {
      sendError(sender, "'" + targetName + "' is not whitelisted.");
    }
  }

  private void handleList(@NotNull CommandSender sender, @NotNull String[] args) {
    int page = 1;
    if (args.length >= 2) {
      try {
        page = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        sendError(sender, "Page must be a valid integer.");
        return;
      }
    }

    Collection<DatabaseManager.WhitelistData> entries = whitelistService.getWhitelistEntries();
    if (entries.isEmpty()) {
      sender.sendMessage(
          MessageFormatter.formatInfo(
              getToolName(),
              Component.text("The whitelist is currently empty.", NamedTextColor.GRAY)));
      return;
    }

    List<DatabaseManager.WhitelistData> sorted = new ArrayList<>(entries);
    sorted.sort((e1, e2) -> e1.getDisplayName().compareToIgnoreCase(e2.getDisplayName()));

    int pageSize = 10;
    int totalPages = Math.max(1, (int) Math.ceil((double) sorted.size() / pageSize));
    if (page < 1 || page > totalPages) {
      sendError(sender, "Page " + page + " does not exist (1-" + totalPages + ").");
      return;
    }

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, sorted.size());

    String headerText =
        totalPages > 1
            ? "Whitelisted Players (" + sorted.size() + ") (Page " + page + "/" + totalPages + "):"
            : "Whitelisted Players (" + sorted.size() + "):";

    net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
    builder.append(Component.newline());
    builder.append(
        MessageFormatter.format(
            getToolName(), headerText, MessageFormatter.MessageType.INFO, false));

    for (int i = startIndex; i < endIndex; i++) {
      DatabaseManager.WhitelistData data = sorted.get(i);
      Component line =
          Component.text("- ", NamedTextColor.DARK_GRAY).append(formatPlayerEntry(data));
      builder.append(Component.newline()).append(line);
    }

    if (page < totalPages) {
      builder
          .append(Component.newline())
          .append(
              Component.text(
                  "Type /whitelist list " + (page + 1) + " for the next page.",
                  NamedTextColor.DARK_GRAY));
    }

    builder.append(Component.newline());
    sender.sendMessage(builder.build());
  }

  private @NotNull Component formatPlayerEntry(@NotNull DatabaseManager.WhitelistData data) {
    if (rankService != null) {
      UUID uuid = data.uuid();
      if (uuid == null && userService != null) {
        uuid = userService.findUuidByUsername(data.getDisplayName());
      }
      if (uuid != null) {
        Rank rank = rankService.getPlayerRank(uuid);
        return rank.formatPlayerDisplayName(data.getDisplayName());
      }
      Rank defaultRank = rankService.getDefaultRank();
      if (defaultRank != null) {
        return defaultRank.formatPlayerDisplayName(data.getDisplayName());
      }
    }
    return Component.text(data.getDisplayName(), NamedTextColor.WHITE);
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase() : "";
      List<String> subs = List.of("on", "off", "add", "remove", "list", "status", "help");
      return subs.stream().filter(s -> s.startsWith(prefix)).toList();
    }

    if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
      String prefix = args[1];
      return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), prefix);
    }

    if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
      String prefix = args[1].toLowerCase();
      return whitelistService.getWhitelistEntries().stream()
          .map(DatabaseManager.WhitelistData::getDisplayName)
          .filter(name -> name.toLowerCase().startsWith(prefix))
          .toList();
    }

    if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
      Collection<DatabaseManager.WhitelistData> entries = whitelistService.getWhitelistEntries();
      int totalPages = Math.max(1, (int) Math.ceil((double) entries.size() / 10));
      List<String> pages = new ArrayList<>();
      for (int i = 1; i <= totalPages; i++) {
        pages.add(String.valueOf(i));
      }
      String prefix = args[1].toLowerCase(Locale.ROOT);
      return pages.stream().filter(s -> s.startsWith(prefix)).toList();
    }

    return Collections.emptyList();
  }
}
