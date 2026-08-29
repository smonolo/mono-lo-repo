package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.gui.StatsGUIManager;
import dev.smnl.smessential.service.UserService;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatsCommand extends EssentialCommand {

  private final StatsGUIManager statsGUIManager;
  private final UserService userService;

  public StatsCommand(@NotNull StatsGUIManager statsGUIManager, @Nullable UserService userService) {
    super(
        "Stats",
        "World",
        "Views player gameplay statistics",
        "smessential.command.stats",
        true,
        new String[] {"stat", "statistic", "statistics"});
    this.statsGUIManager = statsGUIManager;
    this.userService = userService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    Player player = (Player) sender;

    if (args.length == 0) {
      statsGUIManager.openStatsGUI(player, player);
      return;
    }

    String targetName = args[0];
    Player targetOnline = PlayerUtils.findOnlinePlayer(targetName, sender);
    if (targetOnline != null) {
      statsGUIManager.openStatsGUI(player, targetOnline);
      return;
    }

    if (userService != null) {
      UUID offlineUuid = userService.findUuidByUsername(targetName);
      if (offlineUuid != null) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offlineUuid);
        statsGUIManager.openStatsGUI(player, offlinePlayer);
        return;
      }
    }

    OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
    if (offline.hasPlayedBefore() || offline.getName() != null) {
      statsGUIManager.openStatsGUI(player, offline);
      return;
    }

    sendError(sender, "'" + targetName + "' has never played on this server.");
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0] : "";
      return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), prefix);
    }
    return Collections.emptyList();
  }
}
