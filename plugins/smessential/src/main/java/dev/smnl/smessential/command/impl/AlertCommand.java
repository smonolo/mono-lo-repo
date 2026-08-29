package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.gui.AlertGUIManager;
import dev.smnl.smessential.model.AlertTarget;
import dev.smnl.smessential.service.AlertService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AlertCommand extends EssentialCommand {

  private final AlertGUIManager alertGUIManager;
  private final AlertService alertService;

  public AlertCommand(AlertGUIManager alertGUIManager, AlertService alertService) {
    super(
        "Alert",
        "Administration",
        "Dispatches or clears alerts (top banner, chat, all)",
        "smessential.command.administration",
        false,
        new String[] {"announce"});
    this.alertGUIManager = alertGUIManager;
    this.alertService = alertService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
      alertService.clearAlert();
      sendInfo(sender, "Active alert has been cleared.");
      return;
    }

    if (sender instanceof Player player) {
      String message = args.length > 0 ? joinArgs(args, 0) : "";
      alertGUIManager.openAlertGUI(player, message);
    } else {
      if (args.length == 0) {
        sendUsage(sender, "/alert <message> | /alert clear");
        return;
      }
      String message = joinArgs(args, 0);
      alertService.sendAlert(AlertTarget.ALL, message);
      sendInfo(sender, "Alert sent.");
    }
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase() : "";
      if ("clear".startsWith(prefix)) {
        return List.of("clear");
      }
    }
    return Collections.emptyList();
  }
}
