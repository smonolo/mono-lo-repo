package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.FreezeService;
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
import org.jetbrains.annotations.Nullable;

public class FlyCommand extends EssentialCommand {

  private final FreezeService freezeService;

  public FlyCommand(@Nullable FreezeService freezeService) {
    super(
        "Fly",
        "Moderation",
        "Toggles flight mode for a player",
        "smessential.command.moderation",
        true,
        new String[] {"flight"});
    this.freezeService = freezeService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    Player target;
    if (args.length > 0) {
      target = PlayerUtils.findOnlinePlayer(args[0], sender);
      if (target == null) {
        sendError(sender, "'" + args[0] + "' is not online.");
        return;
      }
    } else {
      target = (Player) sender;
    }

    if (freezeService != null && freezeService.isFrozen(target.getUniqueId())) {
      sendError(sender, "You cannot toggle flight for a frozen player.");
      return;
    }

    boolean newFlyState = !target.getAllowFlight();
    target.setAllowFlight(newFlyState);
    if (!newFlyState) {
      target.setFlying(false);
    }

    String stateMsg = newFlyState ? "enabled." : "disabled.";

    if (target == sender) {
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), "Flight " + stateMsg));
    } else {
      target.sendMessage(MessageFormatter.formatInfo(getToolName(), "Flight " + stateMsg));
      Component msg =
          Component.text("Flight " + stateMsg + " for ", NamedTextColor.GRAY)
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
    }

    String actionWord = newFlyState ? "enabled" : "disabled";
    Component broadcastMsg =
        MessageFormatter.formatInfo(
            getToolName(),
            Component.empty()
                .append(Component.text("Flight was " + actionWord + " for ", NamedTextColor.GRAY))
                .append(PlayerUtils.getGeneralDisplayName(target))
                .append(Component.text(" by a staff member.", NamedTextColor.GRAY)));

    PlayerUtils.broadcastMessage(broadcastMsg);
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
