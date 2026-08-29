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

public class StaffChatCommand extends EssentialCommand {

  public StaffChatCommand() {
    super(
        "StaffChat",
        "Moderation",
        "Sends a message to online staff members",
        "smessential.command.moderation",
        false,
        new String[] {"sc"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      sendUsage(sender, "/staffchat <message>");
      return;
    }

    String message = joinArgs(args, 0);

    Component header =
        Component.text("[HQ] ", NamedTextColor.BLUE)
            .append(Component.text("(Staff) ", NamedTextColor.GOLD));

    Component nameComp = PlayerUtils.getStaffVisibleDisplayName(sender);
    Component messageComp = Component.text(": " + message, NamedTextColor.GRAY);
    Component fullStaffMessage = header.append(nameComp).append(messageComp);

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      if (permission() == null || onlinePlayer.hasPermission(permission())) {
        onlinePlayer.sendMessage(fullStaffMessage);
      }
    }

    if (!(sender instanceof Player)) {
      sender.sendMessage(fullStaffMessage);
    }
  }
}
