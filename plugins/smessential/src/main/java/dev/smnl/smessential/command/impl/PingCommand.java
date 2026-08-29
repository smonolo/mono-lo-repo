package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends EssentialCommand {

  public PingCommand() {
    super(
        "Ping",
        "World",
        "Checks your latency to the server",
        "smessential.command.ping",
        false,
        new String[0]);
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      if (sender instanceof Player player) {
        int ping = player.getPing();
        Component msg =
            Component.text("Your ping is ", NamedTextColor.GRAY)
                .append(Component.text(ping + "ms", NamedTextColor.WHITE))
                .append(Component.text(".", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      } else {
        sendUsage(sender, "/ping <player>");
      }
      return;
    }

    Player target = PlayerUtils.findOnlinePlayer(args[0], sender);
    if (target == null) {
      sendError(sender, "'" + args[0] + "' is not online.");
      return;
    }

    int ping = target.getPing();
    Component msg =
        Component.text("Ping of ", NamedTextColor.GRAY)
            .append(PlayerUtils.getStaffVisibleDisplayName(target))
            .append(Component.text(" is ", NamedTextColor.GRAY))
            .append(Component.text(ping + "ms", NamedTextColor.WHITE))
            .append(Component.text(".", NamedTextColor.GRAY));

    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
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
