package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.AlertService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AlertCommand extends EssentialCommand {

  private final AlertService alertService;

  public AlertCommand(AlertService alertService) {
    super(
        "Alert",
        "Administration",
        "Broadcasts an alert message to chat",
        "smessential.command.administration",
        false,
        new String[] {"announce"});
    this.alertService = alertService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      sendUsage(sender, "/alert <message>");
      return;
    }

    String message = joinArgs(args, 0);
    alertService.sendAlert(message);
  }
}
