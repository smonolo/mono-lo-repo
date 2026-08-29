package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SupportCommand extends EssentialCommand {

  public SupportCommand() {
    super(
        "Support",
        "Support",
        "Sends a support message to all online staff members",
        "smessential.command.support",
        true,
        new String[] {"s"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      sendUsage(sender, "/support <message>");
      return;
    }

    String message = joinArgs(args, 0);

    Component header =
        Component.text("[HQ] ", NamedTextColor.BLUE)
            .append(Component.text("(Support) ", NamedTextColor.GOLD));

    Component nameComp = PlayerUtils.getStaffVisibleDisplayName(sender);
    Component messageComp = Component.text(": " + message, NamedTextColor.GRAY);
    Component fullSupportMessage = header.append(nameComp).append(messageComp);

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      if (onlinePlayer.hasPermission("smessential.command.moderation")) {
        onlinePlayer.sendMessage(fullSupportMessage);
      }
    }

    Bukkit.getConsoleSender().sendMessage(fullSupportMessage);

    sendInfo(sender, "Your support message has been sent to online staff.");
  }
}
