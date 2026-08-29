package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.model.Rank;
import dev.smnl.smessential.service.RankService;
import dev.smnl.smessential.service.UserService;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.MessageFormatter.MessageType;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankCommand extends EssentialCommand {

  private final RankService rankService;
  private final UserService userService;

  public RankCommand(@NotNull RankService rankService, @Nullable UserService userService) {
    super(
        "Rank",
        "Administration",
        "Manages player ranks and permissions",
        "smessential.command.administration",
        false,
        new String[] {"ranks", "group", "permission", "perm"});
    this.rankService = rankService;
    this.userService = userService;
  }

  public RankCommand(@NotNull RankService rankService) {
    this(rankService, null);
  }

  private static final List<HelpEntry> HELP_ENTRIES =
      List.of(
          new HelpEntry("/rank list [primary|secondary]", "Lists all ranks or filters by type"),
          new HelpEntry("/rank info <rank>", "Shows detailed info about a rank"),
          new HelpEntry("/rank get <player>", "Shows player's primary & secondary ranks"),
          new HelpEntry(
              "/rank set <player> <primary_rank> [--clear]",
              "Sets primary rank (optionally clearing secondary ranks)"),
          new HelpEntry(
              "/rank addsub <player> <secondary_rank>", "Attaches a secondary rank to a player"),
          new HelpEntry(
              "/rank removesub <player> <secondary_rank>",
              "Removes a secondary rank from a player"),
          new HelpEntry(
              "/rank clearsubs <player>", "Clears all attached secondary ranks from a player"),
          new HelpEntry(
              "/rank setdisplay <player> <rank|reset>",
              "Sets or resets which assigned rank to display (prefix/color)"),
          new HelpEntry(
              "/rank create <id> <name> <color> <weight> [primary|secondary] [prefix]",
              "Creates a rank"),
          new HelpEntry("/rank delete <id>", "Deletes a rank"),
          new HelpEntry("/rank setprefix <rank> [prefix]", "Sets or clears a rank's prefix"),
          new HelpEntry("/rank setcolor <rank> <color>", "Sets a rank's color (name or hex #)"),
          new HelpEntry("/rank setweight <rank> <weight>", "Sets a rank's weight priority"),
          new HelpEntry("/rank setname <rank> <display_name>", "Sets a rank's display name"),
          new HelpEntry(
              "/rank setdefault <primary_rank>", "Sets the default primary rank for new players"),
          new HelpEntry(
              "/rank setparent <primary_rank> <parent>",
              "Sets parent for primary rank inheritance"),
          new HelpEntry(
              "/rank removeparent <primary_rank> <parent>", "Removes parent from primary rank"),
          new HelpEntry(
              "/rank perm <add|remove|list> <rank> [permission]",
              "Manages rank direct permissions"),
          new HelpEntry("/rank reload", "Reloads ranks and permissions from database"),
          new HelpEntry("/rank debug", "Previews chat formatting for all ranks"));

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
      case "list" -> handleList(sender, args);
      case "info" -> handleInfo(sender, args);
      case "get", "user" -> handleGet(sender, args);
      case "set" -> handleSetPrimary(sender, args);
      case "addsub", "add", "attach" -> handleAddSecondary(sender, args);
      case "removesub", "remove", "detach" -> handleRemoveSecondary(sender, args);
      case "clearsubs", "clearsub", "clearsubranks" -> handleClearSecondary(sender, args);
      case "setdisplay", "display", "settag", "tag", "show" -> handleSetDisplay(sender, args);
      case "create" -> handleCreate(sender, args);
      case "delete" -> handleDelete(sender, args);
      case "setprefix", "prefix" -> handleSetPrefix(sender, args);
      case "setcolor", "color" -> handleSetColor(sender, args);
      case "setweight", "weight" -> handleSetWeight(sender, args);
      case "setname", "name", "setdisplayname" -> handleSetName(sender, args);
      case "setdefault", "default" -> handleSetDefault(sender, args);
      case "setparent" -> handleSetParent(sender, args);
      case "removeparent" -> handleRemoveParent(sender, args);
      case "perm", "permission" -> handlePermission(sender, args);
      case "reload", "refresh" -> handleReload(sender);
      case "debug" -> handleDebug(sender);
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
    sendPaginatedHelp(sender, "Rank commands", "rank", HELP_ENTRIES, page);
  }

  private void handleList(@NotNull CommandSender sender, @NotNull String[] args) {
    String filter = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "all";

    List<Rank> primaryRanks = rankService.getPrimaryRanks();
    List<Rank> secondaryRanks = rankService.getSecondaryRanks();
    List<Rank> allRanks = new ArrayList<>(rankService.getAllRanks());

    if (allRanks.isEmpty()) {
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), "No ranks exist."));
      return;
    }

    if (filter.equals("primary")) {
      renderRankList(sender, "Primary Ranks (" + primaryRanks.size() + "):", primaryRanks);
      return;
    }

    if (filter.equals("secondary") || filter.equals("sub")) {
      renderRankList(sender, "Secondary Ranks (" + secondaryRanks.size() + "):", secondaryRanks);
      return;
    }

    renderRankList(sender, "Ranks (" + allRanks.size() + "):", allRanks);
  }

  private void renderRankList(
      @NotNull CommandSender sender, @NotNull String title, @NotNull List<Rank> ranks) {
    TextComponent.Builder builder = Component.text();
    builder.append(Component.newline());
    builder.append(MessageFormatter.format(getToolName(), title, MessageType.INFO, false));

    for (Rank rank : ranks) {
      Component line =
          Component.text("- ", NamedTextColor.DARK_GRAY)
              .append(Component.text(rank.getId(), NamedTextColor.WHITE))
              .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
              .append(rank.getRankDisplayNameComponent())
              .append(
                  Component.text(
                      String.format(
                          Locale.US,
                          " (Weight: %d, Perms: %d%s%s)",
                          rank.getWeight(),
                          rank.getPermissions().size(),
                          rank.isSecondary()
                              ? ", Secondary"
                              : (rank.isDefault() ? ", Default" : ""),
                          rank.getParents().isEmpty()
                              ? ""
                              : ", Parents: " + String.join(", ", rank.getParents())),
                      NamedTextColor.DARK_GRAY));
      builder.append(Component.newline()).append(line);
    }

    builder.append(Component.newline());
    sender.sendMessage(builder.build());
  }

  private void handleInfo(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 2) {
      sendUsage(sender, "/rank info <rank>");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    Rank rank = rankService.getRank(id);
    if (rank == null) {
      handleGet(sender, args);
      return;
    }

    Set<String> parents = rank.getParents();
    Set<String> effectivePerms = rankService.getEffectivePermissions(rank);

    TextComponent.Builder builder = Component.text();
    builder.append(Component.newline());
    builder.append(
        MessageFormatter.format(
            getToolName(), "Rank details for " + rank.getId() + ":", MessageType.INFO, false));
    builder
        .append(Component.newline())
        .append(
            formatInfoLine(
                "Type",
                Component.text(rank.isPrimary() ? "Primary" : "Secondary", NamedTextColor.WHITE)));
    builder
        .append(Component.newline())
        .append(formatInfoLine("Display Name", rank.getRankDisplayNameComponent()));
    builder
        .append(Component.newline())
        .append(formatInfoLine("Weight", Component.text(rank.getWeight(), NamedTextColor.WHITE)));
    builder
        .append(Component.newline())
        .append(formatInfoLine("Color", Component.text(rank.getColor(), rank.getTextColor())));
    builder
        .append(Component.newline())
        .append(
            formatInfoLine(
                "Prefix",
                rank.getPrefix().isEmpty()
                    ? Component.text("None", NamedTextColor.DARK_GRAY)
                    : rank.getPrefixComponent()
                        .append(
                            Component.text(
                                " (" + rank.getPrefix() + ")", NamedTextColor.DARK_GRAY))));
    if (rank.isPrimary()) {
      builder
          .append(Component.newline())
          .append(
              formatInfoLine(
                  "Default",
                  Component.text(String.valueOf(rank.isDefault()), NamedTextColor.WHITE)));
      builder
          .append(Component.newline())
          .append(
              formatInfoLine(
                  "Parents",
                  Component.text(
                      parents.isEmpty() ? "None" : String.join(", ", parents),
                      NamedTextColor.WHITE)));
    }
    builder
        .append(Component.newline())
        .append(
            formatInfoLine(
                "Permissions ("
                    + rank.getPermissions().size()
                    + " direct, "
                    + effectivePerms.size()
                    + " effective)",
                Component.text(
                    rank.getPermissions().isEmpty()
                        ? "None"
                        : String.join(", ", rank.getPermissions()),
                    NamedTextColor.WHITE)));
    builder.append(Component.newline());

    sender.sendMessage(builder.build());
  }

  private void handleGet(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 2) {
      sendUsage(sender, "/rank get <player>");
      return;
    }

    String targetName = args[1];
    Player target = PlayerUtils.findOnlinePlayer(targetName, sender);

    if (target != null) {
      UUID uuid = target.getUniqueId();
      renderPlayerRanks(sender, PlayerUtils.getStaffVisibleDisplayName(target), uuid);
      return;
    }

    if (userService != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              rankService.getPlugin(),
              () -> {
                UUID uuid = userService.findUuidByUsername(targetName);
                if (uuid == null) {
                  sendError(sender, "User '" + targetName + "' was never seen.");
                  return;
                }
                renderPlayerRanks(
                    sender, Component.text(targetName + " (Offline)", NamedTextColor.WHITE), uuid);
              });
      return;
    }

    sendError(sender, "Player '" + targetName + "' not found.");
  }

  private void renderPlayerRanks(
      @NotNull CommandSender sender, @NotNull Component targetNameComp, @NotNull UUID uuid) {
    Rank primary = rankService.getPrimaryRank(uuid);
    Set<Rank> secondaries = rankService.getSecondaryRanks(uuid);
    Rank displayRank = rankService.getDisplayRank(uuid);
    String customDisplayId = rankService.getCustomDisplayRankId(uuid);

    TextComponent.Builder builder = Component.text();
    builder.append(Component.newline());
    builder.append(
        MessageFormatter.formatInfo(
            getToolName(),
            Component.text("Rank details for ", NamedTextColor.GRAY)
                .append(targetNameComp)
                .append(Component.text(":", NamedTextColor.GRAY))));

    builder
        .append(Component.newline())
        .append(
            formatInfoLine(
                "Primary Rank",
                primary
                    .getRankDisplayNameComponent()
                    .append(
                        Component.text(
                            " (Weight: " + primary.getWeight() + ")", NamedTextColor.DARK_GRAY))));

    TextComponent.Builder secBuilder = Component.text();
    if (secondaries.isEmpty()) {
      secBuilder.append(Component.text("None", NamedTextColor.DARK_GRAY));
    } else {
      List<Rank> sortedSec = new ArrayList<>(secondaries);
      Collections.sort(sortedSec);
      for (int i = 0; i < sortedSec.size(); i++) {
        Rank r = sortedSec.get(i);
        secBuilder
            .append(r.getRankDisplayNameComponent())
            .append(Component.text(" (Weight: " + r.getWeight() + ")", NamedTextColor.DARK_GRAY));
        if (i < sortedSec.size() - 1) {
          secBuilder.append(Component.text(", ", NamedTextColor.DARK_GRAY));
        }
      }
    }
    builder
        .append(Component.newline())
        .append(formatInfoLine("Secondary Ranks", secBuilder.build()));

    builder
        .append(Component.newline())
        .append(
            formatInfoLine(
                "Active Appearance",
                displayRank
                    .getRankDisplayNameComponent()
                    .append(
                        customDisplayId != null
                            ? Component.text(
                                " (Custom: " + customDisplayId + ")", NamedTextColor.DARK_GRAY)
                            : Component.text(" (Default: Primary)", NamedTextColor.DARK_GRAY))));

    builder.append(Component.newline());
    sender.sendMessage(builder.build());
  }

  private @NotNull Component formatInfoLine(@NotNull String label, @NotNull Component value) {
    return Component.text("- ", NamedTextColor.DARK_GRAY)
        .append(Component.text(label + ": ", NamedTextColor.GRAY))
        .append(value);
  }

  private void handleSetPrimary(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank set <player> <primary_rank> [--clear]");
      return;
    }

    String targetName = args[1];
    String rankName = args[2];
    Rank targetRank = rankService.getRank(rankName);

    if (targetRank == null) {
      sendError(sender, "Rank '" + rankName + "' does not exist. Use /rank list.");
      return;
    }

    if (!targetRank.isPrimary()) {
      sendError(
          sender,
          "Rank '"
              + targetRank.getName()
              + "' is a secondary rank. Use '/rank addsub "
              + targetName
              + " "
              + targetRank.getId()
              + "' to attach it.");
      return;
    }

    boolean clearSubs =
        args.length >= 4
            && (args[3].equalsIgnoreCase("--clear")
                || args[3].equalsIgnoreCase("-c")
                || args[3].equalsIgnoreCase("clear")
                || args[3].equalsIgnoreCase("clearsubs"));

    Player onlineTarget = PlayerUtils.findOnlinePlayer(targetName, sender);
    if (onlineTarget != null) {
      rankService.setPlayerPrimaryRank(onlineTarget.getUniqueId(), targetRank.getId(), clearSubs);

      Component notifySender =
          Component.text("Set primary rank of ", NamedTextColor.GRAY)
              .append(PlayerUtils.getStaffVisibleDisplayName(onlineTarget))
              .append(Component.text(" to ", NamedTextColor.GRAY))
              .append(targetRank.getRankDisplayNameComponent())
              .append(
                  clearSubs
                      ? Component.text(" and cleared all secondary ranks.", NamedTextColor.GRAY)
                      : Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notifySender));
      return;
    }

    if (userService != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              rankService.getPlugin(),
              () -> {
                UUID uuid = userService.findUuidByUsername(targetName);
                if (uuid == null) {
                  sendError(sender, "User '" + targetName + "' was never seen.");
                  return;
                }
                rankService.setPlayerPrimaryRank(uuid, targetRank.getId(), clearSubs);
                Component notifySender =
                    Component.text("Set primary rank of (Offline) ", NamedTextColor.GRAY)
                        .append(Component.text(targetName, NamedTextColor.WHITE))
                        .append(Component.text(" to ", NamedTextColor.GRAY))
                        .append(targetRank.getRankDisplayNameComponent())
                        .append(
                            clearSubs
                                ? Component.text(
                                    " and cleared all secondary ranks.", NamedTextColor.GRAY)
                                : Component.text(".", NamedTextColor.GRAY));
                sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notifySender));
              });
    } else {
      sendError(sender, "'" + targetName + "' is not online.");
    }
  }

  private void handleClearSecondary(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 2) {
      sendUsage(sender, "/rank clearsubs <player>");
      return;
    }

    String targetName = args[1];
    Player onlineTarget = PlayerUtils.findOnlinePlayer(targetName, sender);

    if (onlineTarget != null) {
      boolean cleared = rankService.clearPlayerSecondaryRanks(onlineTarget.getUniqueId());
      if (cleared) {
        Component notifySender =
            Component.text("Cleared all secondary ranks for ", NamedTextColor.GRAY)
                .append(PlayerUtils.getStaffVisibleDisplayName(onlineTarget))
                .append(Component.text(".", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notifySender));
      } else {
        sendError(sender, "Player has no secondary ranks assigned.");
      }
      return;
    }

    if (userService != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              rankService.getPlugin(),
              () -> {
                UUID uuid = userService.findUuidByUsername(targetName);
                if (uuid == null) {
                  sendError(sender, "User '" + targetName + "' was never seen.");
                  return;
                }
                boolean cleared = rankService.clearPlayerSecondaryRanks(uuid);
                if (cleared) {
                  Component notifySender =
                      Component.text(
                              "Cleared all secondary ranks for (Offline) ", NamedTextColor.GRAY)
                          .append(Component.text(targetName, NamedTextColor.WHITE))
                          .append(Component.text(".", NamedTextColor.GRAY));
                  sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notifySender));
                } else {
                  sendError(sender, "Player has no secondary ranks assigned.");
                }
              });
      return;
    }

    sendError(sender, "'" + targetName + "' is not online.");
  }

  private void handleSetDisplay(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank setdisplay <player> <rank|reset>");
      return;
    }

    String targetName = args[1];
    String choice = args[2].toLowerCase(Locale.ROOT);
    boolean isReset =
        choice.equals("reset")
            || choice.equals("default")
            || choice.equals("primary")
            || choice.equals("none")
            || choice.equals("clear");

    Player onlineTarget = PlayerUtils.findOnlinePlayer(targetName, sender);
    if (onlineTarget != null) {
      UUID uuid = onlineTarget.getUniqueId();
      if (isReset) {
        rankService.resetPlayerDisplayRank(uuid);
        Rank primary = rankService.getPrimaryRank(uuid);
        Component notify =
            Component.text("Reset display appearance for ", NamedTextColor.GRAY)
                .append(PlayerUtils.getStaffVisibleDisplayName(onlineTarget))
                .append(Component.text(" to primary rank ", NamedTextColor.GRAY))
                .append(primary.getRankDisplayNameComponent())
                .append(Component.text(".", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notify));
        return;
      }

      Rank targetRank = rankService.getRank(choice);
      if (targetRank == null) {
        sendError(sender, "Rank '" + choice + "' does not exist. Use /rank list.");
        return;
      }

      if (!rankService.hasRankAssigned(uuid, targetRank.getId())) {
        sendError(
            sender,
            "Player does not have rank '"
                + targetRank.getName()
                + "' assigned (must be their primary or secondary rank).");
        return;
      }

      rankService.setPlayerDisplayRank(uuid, targetRank.getId());
      Component notify =
          Component.text("Set display appearance of ", NamedTextColor.GRAY)
              .append(PlayerUtils.getStaffVisibleDisplayName(onlineTarget))
              .append(Component.text(" to ", NamedTextColor.GRAY))
              .append(targetRank.getRankDisplayNameComponent())
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notify));
      return;
    }

    if (userService != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              rankService.getPlugin(),
              () -> {
                UUID uuid = userService.findUuidByUsername(targetName);
                if (uuid == null) {
                  sendError(sender, "User '" + targetName + "' was never seen.");
                  return;
                }

                if (isReset) {
                  rankService.resetPlayerDisplayRank(uuid);
                  Rank primary = rankService.getPrimaryRank(uuid);
                  Component notify =
                      Component.text("Reset display appearance for (Offline) ", NamedTextColor.GRAY)
                          .append(Component.text(targetName, NamedTextColor.WHITE))
                          .append(Component.text(" to primary rank ", NamedTextColor.GRAY))
                          .append(primary.getRankDisplayNameComponent())
                          .append(Component.text(".", NamedTextColor.GRAY));
                  sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notify));
                  return;
                }

                Rank targetRank = rankService.getRank(choice);
                if (targetRank == null) {
                  sendError(sender, "Rank '" + choice + "' does not exist. Use /rank list.");
                  return;
                }

                if (!rankService.hasRankAssigned(uuid, targetRank.getId())) {
                  sendError(
                      sender,
                      "Player does not have rank '"
                          + targetRank.getName()
                          + "' assigned (must be their primary or secondary rank).");
                  return;
                }

                rankService.setPlayerDisplayRank(uuid, targetRank.getId());
                Component notify =
                    Component.text("Set display appearance of (Offline) ", NamedTextColor.GRAY)
                        .append(Component.text(targetName, NamedTextColor.WHITE))
                        .append(Component.text(" to ", NamedTextColor.GRAY))
                        .append(targetRank.getRankDisplayNameComponent())
                        .append(Component.text(".", NamedTextColor.GRAY));
                sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notify));
              });
      return;
    }

    sendError(sender, "'" + targetName + "' is not online.");
  }

  private void handleAddSecondary(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank addsub <player> <secondary_rank>");
      return;
    }

    String targetName = args[1];
    String rankName = args[2];
    Rank targetRank = rankService.getRank(rankName);

    if (targetRank == null) {
      sendError(sender, "Rank '" + rankName + "' does not exist. Use /rank list.");
      return;
    }

    if (!targetRank.isSecondary()) {
      sendError(
          sender,
          "Rank '"
              + targetRank.getName()
              + "' is a primary rank. Use '/rank set "
              + targetName
              + " "
              + targetRank.getId()
              + "' to set the primary rank.");
      return;
    }

    Player onlineTarget = PlayerUtils.findOnlinePlayer(targetName, sender);
    if (onlineTarget != null) {
      boolean added =
          rankService.addPlayerSecondaryRank(onlineTarget.getUniqueId(), targetRank.getId());
      if (added) {
        Component notifySender =
            Component.text("Added secondary rank ", NamedTextColor.GRAY)
                .append(targetRank.getRankDisplayNameComponent())
                .append(Component.text(" to ", NamedTextColor.GRAY))
                .append(PlayerUtils.getStaffVisibleDisplayName(onlineTarget))
                .append(Component.text(".", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notifySender));
      } else {
        sendError(sender, "Player already has that secondary rank.");
      }
      return;
    }

    if (userService != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              rankService.getPlugin(),
              () -> {
                UUID uuid = userService.findUuidByUsername(targetName);
                if (uuid == null) {
                  sendError(sender, "User '" + targetName + "' was never seen.");
                  return;
                }
                boolean added = rankService.addPlayerSecondaryRank(uuid, targetRank.getId());
                if (added) {
                  Component notifySender =
                      Component.text("Added secondary rank ", NamedTextColor.GRAY)
                          .append(targetRank.getRankDisplayNameComponent())
                          .append(Component.text(" to (Offline) ", NamedTextColor.GRAY))
                          .append(Component.text(targetName, NamedTextColor.WHITE))
                          .append(Component.text(".", NamedTextColor.GRAY));
                  sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notifySender));
                } else {
                  sendError(sender, "Player already has that secondary rank.");
                }
              });
    } else {
      sendError(sender, "'" + targetName + "' is not online.");
    }
  }

  private void handleRemoveSecondary(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank removesub <player> <secondary_rank>");
      return;
    }

    String targetName = args[1];
    String rankName = args[2];
    Rank targetRank = rankService.getRank(rankName);

    if (targetRank == null) {
      sendError(sender, "Rank '" + rankName + "' does not exist. Use /rank list.");
      return;
    }

    Player onlineTarget = PlayerUtils.findOnlinePlayer(targetName, sender);
    if (onlineTarget != null) {
      boolean removed =
          rankService.removePlayerSecondaryRank(onlineTarget.getUniqueId(), targetRank.getId());
      if (removed) {
        Component notifySender =
            Component.text("Removed secondary rank ", NamedTextColor.GRAY)
                .append(targetRank.getRankDisplayNameComponent())
                .append(Component.text(" from ", NamedTextColor.GRAY))
                .append(PlayerUtils.getStaffVisibleDisplayName(onlineTarget))
                .append(Component.text(".", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notifySender));
      } else {
        sendError(sender, "Player does not have that secondary rank.");
      }
      return;
    }

    if (userService != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              rankService.getPlugin(),
              () -> {
                UUID uuid = userService.findUuidByUsername(targetName);
                if (uuid == null) {
                  sendError(sender, "User '" + targetName + "' was never seen.");
                  return;
                }
                boolean removed = rankService.removePlayerSecondaryRank(uuid, targetRank.getId());
                if (removed) {
                  Component notifySender =
                      Component.text("Removed secondary rank ", NamedTextColor.GRAY)
                          .append(targetRank.getRankDisplayNameComponent())
                          .append(Component.text(" from (Offline) ", NamedTextColor.GRAY))
                          .append(Component.text(targetName, NamedTextColor.WHITE))
                          .append(Component.text(".", NamedTextColor.GRAY));
                  sender.sendMessage(MessageFormatter.formatInfo(getToolName(), notifySender));
                } else {
                  sendError(sender, "Player does not have that secondary rank.");
                }
              });
    } else {
      sendError(sender, "'" + targetName + "' is not online.");
    }
  }

  private void handleCreate(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 5) {
      sendUsage(sender, "/rank create <id> <name> <color> <weight> [primary|secondary] [prefix]");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    if (!id.matches("^[a-zA-Z0-9_-]+$")) {
      sendError(
          sender, "Rank ID must only contain alphanumeric characters, hyphens, or underscores.");
      return;
    }

    if (rankService.getRank(id) != null) {
      sendError(sender, "A rank with ID '" + id + "' already exists.");
      return;
    }

    String name = args[2];
    String color = args[3];
    int weight;
    try {
      weight = Integer.parseInt(args[4]);
    } catch (NumberFormatException e) {
      sendError(sender, "Invalid weight number.");
      return;
    }

    boolean isPrimary = true;
    String prefix = "[" + name + "]";

    if (args.length >= 6) {
      String arg5 = args[5].toLowerCase(Locale.ROOT);
      if (arg5.equals("secondary") || arg5.equals("sub")) {
        isPrimary = false;
        if (args.length >= 7) {
          prefix = joinArgs(args, 6);
        }
      } else if (arg5.equals("primary")) {
        isPrimary = true;
        if (args.length >= 7) {
          prefix = joinArgs(args, 6);
        }
      } else {
        String lastArg = args[args.length - 1].toLowerCase(Locale.ROOT);
        if (lastArg.equals("secondary") || lastArg.equals("sub")) {
          isPrimary = false;
          prefix = String.join(" ", Arrays.copyOfRange(args, 5, args.length - 1));
        } else if (lastArg.equals("primary")) {
          isPrimary = true;
          prefix = String.join(" ", Arrays.copyOfRange(args, 5, args.length - 1));
        } else {
          isPrimary = true;
          prefix = joinArgs(args, 5);
        }
      }
    }

    prefix = prefix.trim();
    if (prefix.equalsIgnoreCase("none") || prefix.equalsIgnoreCase("clear")) {
      prefix = "";
    } else if (prefix.isEmpty()) {
      prefix = "[" + name + "]";
    }

    Rank newRank = new Rank(id, name, color, prefix, weight, false, isPrimary, new HashSet<>());
    rankService.createRank(newRank);

    Component msg =
        Component.text(
                "Created " + (isPrimary ? "primary" : "secondary") + " rank ", NamedTextColor.GRAY)
            .append(newRank.getRankDisplayNameComponent())
            .append(
                Component.text(
                    " (Weight: " + weight + (prefix.isEmpty() ? "" : ", Prefix: " + prefix) + ").",
                    NamedTextColor.GRAY));
    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private void handleDelete(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 2) {
      sendUsage(sender, "/rank delete <id>");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    Rank rank = rankService.getRank(id);
    if (rank == null) {
      sendError(sender, "Rank '" + id + "' does not exist.");
      return;
    }

    if (rank.isDefault()) {
      sendError(sender, "Cannot delete the default rank.");
      return;
    }

    boolean deleted = rankService.deleteRank(id);
    if (deleted) {
      sender.sendMessage(
          MessageFormatter.formatInfo(
              getToolName(), "Rank '" + id + "' and all player associations were deleted."));
    } else {
      sendError(sender, "Could not delete rank '" + id + "'.");
    }
  }

  private void handleSetPrefix(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 2) {
      sendUsage(sender, "/rank setprefix <rank> [prefix]");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    Rank rank = rankService.getRank(id);
    if (rank == null) {
      sendError(sender, "Rank '" + id + "' does not exist.");
      return;
    }

    String newPrefix = "";
    if (args.length >= 3) {
      String raw = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
      if (!raw.equalsIgnoreCase("none")
          && !raw.equalsIgnoreCase("clear")
          && !raw.equalsIgnoreCase("empty")) {
        newPrefix = raw;
      }
    }

    rank.setPrefix(newPrefix);
    rankService.saveRankChanges(rank);

    Component msg =
        Component.text("Updated prefix for rank ", NamedTextColor.GRAY)
            .append(rank.getRankDisplayNameComponent())
            .append(Component.text(" to: ", NamedTextColor.GRAY))
            .append(
                newPrefix.isEmpty()
                    ? Component.text("None", NamedTextColor.DARK_GRAY)
                    : rank.getPrefixComponent());
    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private void handleSetColor(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank setcolor <rank> <color>");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    Rank rank = rankService.getRank(id);
    if (rank == null) {
      sendError(sender, "Rank '" + id + "' does not exist.");
      return;
    }

    String colorStr = args[2].trim();
    rank.setColor(colorStr);
    rankService.saveRankChanges(rank);

    Component msg =
        Component.text("Updated color for rank ", NamedTextColor.GRAY)
            .append(rank.getRankDisplayNameComponent())
            .append(Component.text(" (", NamedTextColor.GRAY))
            .append(Component.text(colorStr, rank.getTextColor()))
            .append(Component.text(").", NamedTextColor.GRAY));
    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private void handleSetWeight(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank setweight <rank> <weight>");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    Rank rank = rankService.getRank(id);
    if (rank == null) {
      sendError(sender, "Rank '" + id + "' does not exist.");
      return;
    }

    int weight;
    try {
      weight = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      sendError(sender, "Weight must be an integer.");
      return;
    }

    rank.setWeight(weight);
    rankService.saveRankChanges(rank);

    Component msg =
        Component.text("Updated weight for rank ", NamedTextColor.GRAY)
            .append(rank.getRankDisplayNameComponent())
            .append(Component.text(" to ", NamedTextColor.GRAY))
            .append(Component.text(weight, NamedTextColor.WHITE))
            .append(Component.text(".", NamedTextColor.GRAY));
    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private void handleSetName(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank setname <rank> <display_name>");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    Rank rank = rankService.getRank(id);
    if (rank == null) {
      sendError(sender, "Rank '" + id + "' does not exist.");
      return;
    }

    String newName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
    rank.setName(newName);
    rankService.saveRankChanges(rank);

    Component msg =
        Component.text("Updated display name for rank '", NamedTextColor.GRAY)
            .append(Component.text(id, NamedTextColor.WHITE))
            .append(Component.text("' to ", NamedTextColor.GRAY))
            .append(rank.getRankDisplayNameComponent())
            .append(Component.text(".", NamedTextColor.GRAY));
    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private void handleSetDefault(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 2) {
      sendUsage(sender, "/rank setdefault <primary_rank>");
      return;
    }

    String id = args[1].toLowerCase(Locale.ROOT);
    Rank rank = rankService.getRank(id);
    if (rank == null) {
      sendError(sender, "Rank '" + id + "' does not exist.");
      return;
    }

    if (!rank.isPrimary()) {
      sendError(sender, "Only primary ranks can be set as the default rank.");
      return;
    }

    rankService.setDefaultRank(rank.getId());

    Component msg =
        Component.text("Set default rank for new players to ", NamedTextColor.GRAY)
            .append(rank.getRankDisplayNameComponent())
            .append(Component.text(".", NamedTextColor.GRAY));
    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private void handleSetParent(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank setparent <primary_rank> <parent_primary_rank>");
      return;
    }

    String rankId = args[1];
    String parentId = args[2];

    Rank rank = rankService.getRank(rankId);
    Rank parent = rankService.getRank(parentId);

    if (rank == null) {
      sendError(sender, "Rank '" + rankId + "' does not exist.");
      return;
    }
    if (parent == null) {
      sendError(sender, "Rank '" + parentId + "' does not exist.");
      return;
    }

    if (!rank.isPrimary() || !parent.isPrimary()) {
      sendError(
          sender,
          "Inheritance is only supported for primary ranks. Secondary ranks cannot inherit or be inherited.");
      return;
    }

    boolean success = rankService.addParentToRank(rankId, parentId);
    if (success) {
      Component msg =
          Component.text("Rank ", NamedTextColor.GRAY)
              .append(rank.getRankDisplayNameComponent())
              .append(Component.text(" now inherits from ", NamedTextColor.GRAY))
              .append(parent.getRankDisplayNameComponent())
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
    } else {
      sendError(sender, "Could not set parent (cycle detected or already parent).");
    }
  }

  private void handleRemoveParent(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank removeparent <primary_rank> <parent_primary_rank>");
      return;
    }

    String rankId = args[1];
    String parentId = args[2];

    boolean removed = rankService.removeParentFromRank(rankId, parentId);
    if (removed) {
      sender.sendMessage(
          MessageFormatter.formatInfo(
              getToolName(), "Removed parent '" + parentId + "' from rank '" + rankId + "'."));
    } else {
      sendError(sender, "Rank '" + rankId + "' does not inherit from '" + parentId + "'.");
    }
  }

  private void handlePermission(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 3) {
      sendUsage(sender, "/rank perm <add|remove|list> <rank> [permission]");
      return;
    }

    String action = args[1].toLowerCase(Locale.ROOT);
    String rankId = args[2];
    Rank rank = rankService.getRank(rankId);

    if (rank == null) {
      sendError(sender, "Rank '" + rankId + "' does not exist.");
      return;
    }

    switch (action) {
      case "list" -> {
        Set<String> perms = rank.getPermissions();
        TextComponent.Builder builder = Component.text();
        builder.append(Component.newline());
        builder.append(
            MessageFormatter.formatInfo(
                getToolName(),
                Component.text("Direct permissions for ", NamedTextColor.GRAY)
                    .append(rank.getRankDisplayNameComponent())
                    .append(Component.text(" (" + perms.size() + "):", NamedTextColor.GRAY))));
        if (perms.isEmpty()) {
          builder
              .append(Component.newline())
              .append(Component.text("- None", NamedTextColor.DARK_GRAY));
        } else {
          List<String> sorted = new ArrayList<>(perms);
          Collections.sort(sorted);
          for (String perm : sorted) {
            builder
                .append(Component.newline())
                .append(
                    Component.text("- ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(perm, NamedTextColor.WHITE)));
          }
        }
        builder.append(Component.newline());
        sender.sendMessage(builder.build());
      }
      case "add" -> {
        if (args.length < 4) {
          sendUsage(sender, "/rank perm add <rank> <permission>");
          return;
        }
        String perm = args[3];
        rankService.addPermissionToRank(rank.getId(), perm);
        Component msg =
            Component.text("Added permission '", NamedTextColor.GRAY)
                .append(Component.text(perm, NamedTextColor.WHITE))
                .append(Component.text("' to rank ", NamedTextColor.GRAY))
                .append(rank.getRankDisplayNameComponent())
                .append(Component.text(".", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      }
      case "remove" -> {
        if (args.length < 4) {
          sendUsage(sender, "/rank perm remove <rank> <permission>");
          return;
        }
        String perm = args[3];
        rankService.removePermissionFromRank(rank.getId(), perm);
        Component msg =
            Component.text("Removed permission '", NamedTextColor.GRAY)
                .append(Component.text(perm, NamedTextColor.WHITE))
                .append(Component.text("' from rank ", NamedTextColor.GRAY))
                .append(rank.getRankDisplayNameComponent())
                .append(Component.text(".", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      }
      default -> sendUsage(sender, "/rank perm <add|remove|list> <rank> [permission]");
    }
  }

  private void handleReload(@NotNull CommandSender sender) {
    if (userService != null) {
      userService.reload();
    }
    rankService.reload();
    sendInfo(sender, "Ranks, permissions, and player ranks reloaded from database successfully.");
  }

  private void handleDebug(@NotNull CommandSender sender) {
    Collection<Rank> ranks = rankService.getAllRanks();
    if (ranks.isEmpty()) {
      sendInfo(sender, "No ranks exist.");
      return;
    }

    String username = sender.getName();
    net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
    boolean first = true;
    for (Rank rank : ranks) {
      if (!first) {
        builder.append(Component.newline());
      }
      Component nameWithPrefix = rank.formatPlayerDisplayName(username);
      Component line =
          nameWithPrefix
              .append(Component.text(": ", NamedTextColor.WHITE))
              .append(Component.text("Hello, world!", NamedTextColor.WHITE));
      builder.append(line);
      first = false;
    }

    sender.sendMessage(builder.build());
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
      List<String> subs =
          new ArrayList<>(
              List.of(
                  "list",
                  "get",
                  "set",
                  "addsub",
                  "removesub",
                  "clearsubs",
                  "setdisplay",
                  "create",
                  "delete",
                  "setprefix",
                  "setcolor",
                  "setweight",
                  "setname",
                  "setdefault",
                  "setparent",
                  "removeparent",
                  "perm",
                  "reload",
                  "debug",
                  "help"));
      int totalPages = Math.max(1, (int) Math.ceil((double) HELP_ENTRIES.size() / 10));
      for (int i = 1; i <= totalPages; i++) {
        subs.add(String.valueOf(i));
      }
      return subs.stream().filter(s -> s.startsWith(prefix)).toList();
    }

    if (args.length == 2) {
      String sub = args[0].toLowerCase(Locale.ROOT);
      String prefix = args[1].toLowerCase(Locale.ROOT);

      if (sub.equals("help") || sub.equals("h") || sub.equals("?")) {
        int totalPages = Math.max(1, (int) Math.ceil((double) HELP_ENTRIES.size() / 10));
        List<String> pages = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
          pages.add(String.valueOf(i));
        }
        return pages.stream().filter(s -> s.startsWith(prefix)).toList();
      }

      if (sub.equals("get")
          || sub.equals("user")
          || sub.equals("set")
          || sub.equals("addsub")
          || sub.equals("removesub")
          || sub.equals("clearsubs")
          || sub.equals("clearsub")
          || sub.equals("clearsubranks")
          || sub.equals("setdisplay")
          || sub.equals("display")
          || sub.equals("settag")
          || sub.equals("tag")
          || sub.equals("show")) {
        return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), args[1]);
      }
      if (sub.equals("list")) {
        return List.of("all", "primary", "secondary").stream()
            .filter(s -> s.startsWith(prefix))
            .toList();
      }
      if (sub.equals("setdefault") || sub.equals("default")) {
        return rankService.getPrimaryRanks().stream()
            .map(Rank::getId)
            .filter(id -> id.startsWith(prefix))
            .toList();
      }
      if (sub.equals("info")
          || sub.equals("delete")
          || sub.equals("setprefix")
          || sub.equals("prefix")
          || sub.equals("setcolor")
          || sub.equals("color")
          || sub.equals("setweight")
          || sub.equals("weight")
          || sub.equals("setname")
          || sub.equals("name")
          || sub.equals("setdisplayname")) {
        return rankService.getAllRanks().stream()
            .map(Rank::getId)
            .filter(id -> id.startsWith(prefix))
            .toList();
      }
      if (sub.equals("perm") || sub.equals("permission")) {
        return List.of("add", "remove", "list").stream().filter(s -> s.startsWith(prefix)).toList();
      }
    }

    if (args.length == 3) {
      String sub = args[0].toLowerCase(Locale.ROOT);
      String prefix = args[2].toLowerCase(Locale.ROOT);

      if (sub.equals("set")) {
        return rankService.getPrimaryRanks().stream()
            .map(Rank::getId)
            .filter(id -> id.startsWith(prefix))
            .toList();
      }
      if (sub.equals("setdisplay")
          || sub.equals("display")
          || sub.equals("settag")
          || sub.equals("tag")
          || sub.equals("show")) {
        Player targetPlayer = PlayerUtils.findOnlinePlayer(args[1], commandSourceStack.getSender());
        List<String> options = new ArrayList<>();
        if (targetPlayer != null) {
          for (Rank r : rankService.getAllRanksForPlayer(targetPlayer.getUniqueId())) {
            options.add(r.getId());
          }
        } else {
          for (Rank r : rankService.getAllRanks()) {
            options.add(r.getId());
          }
        }
        options.add("reset");
        options.add("default");
        return options.stream().filter(s -> s.startsWith(prefix)).toList();
      }
      if (sub.equals("addsub") || sub.equals("removesub")) {
        return rankService.getSecondaryRanks().stream()
            .map(Rank::getId)
            .filter(id -> id.startsWith(prefix))
            .toList();
      }
      if (sub.equals("setparent") || sub.equals("removeparent")) {
        return rankService.getPrimaryRanks().stream()
            .map(Rank::getId)
            .filter(id -> id.startsWith(prefix))
            .toList();
      }
      if (sub.equals("setcolor") || sub.equals("color")) {
        return List.of(
                "red",
                "gold",
                "yellow",
                "green",
                "aqua",
                "blue",
                "light_purple",
                "dark_purple",
                "white",
                "gray",
                "dark_gray",
                "black")
            .stream()
            .filter(s -> s.startsWith(prefix))
            .toList();
      }
      if (sub.equals("setweight") || sub.equals("weight")) {
        return List.of("0", "10", "50", "100", "500", "1000").stream()
            .filter(s -> s.startsWith(prefix))
            .toList();
      }
      if (sub.equals("setprefix") || sub.equals("prefix")) {
        return List.of("[Prefix]", "none").stream().filter(s -> s.startsWith(prefix)).toList();
      }
      if (sub.equals("perm") || sub.equals("permission")) {
        return rankService.getAllRanks().stream()
            .map(Rank::getId)
            .filter(id -> id.startsWith(prefix))
            .toList();
      }
    }

    if (args.length == 4) {
      String sub = args[0].toLowerCase(Locale.ROOT);
      String prefix = args[3].toLowerCase(Locale.ROOT);

      if (sub.equals("set")) {
        return List.of("--clear").stream().filter(s -> s.startsWith(prefix)).toList();
      }
      if (sub.equals("create")) {
        return List.of(
                "red",
                "gold",
                "yellow",
                "green",
                "aqua",
                "blue",
                "light_purple",
                "dark_purple",
                "white",
                "gray",
                "dark_gray",
                "black")
            .stream()
            .filter(s -> s.startsWith(prefix))
            .toList();
      }
      if (sub.equals("perm") || sub.equals("permission")) {
        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
          Rank rank = rankService.getRank(args[2]);
          if (rank != null) {
            return rank.getPermissions().stream()
                .filter(p -> p.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
          }
        }
      }
    }

    if (args.length == 5 && args[0].equalsIgnoreCase("create")) {
      String prefix = args[4].toLowerCase(Locale.ROOT);
      return List.of("0", "10", "50", "100", "500", "1000").stream()
          .filter(s -> s.startsWith(prefix))
          .toList();
    }

    if (args.length == 6 && args[0].equalsIgnoreCase("create")) {
      String prefix = args[5].toLowerCase(Locale.ROOT);
      return List.of("primary", "secondary", "[Prefix]", "none").stream()
          .filter(s -> s.startsWith(prefix))
          .toList();
    }

    if (args.length == 7 && args[0].equalsIgnoreCase("create")) {
      String prefix = args[6].toLowerCase(Locale.ROOT);
      return List.of("[Prefix]", "none").stream().filter(s -> s.startsWith(prefix)).toList();
    }

    return Collections.emptyList();
  }
}
