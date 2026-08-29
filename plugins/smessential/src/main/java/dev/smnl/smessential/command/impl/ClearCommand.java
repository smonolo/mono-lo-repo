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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClearCommand extends EssentialCommand {

  public ClearCommand() {
    super(
        "Clear",
        "Administration",
        "Clears items from player inventory",
        "smessential.command.administration",
        false,
        new String[] {"ci", "clearinventory"});
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
      if (sender instanceof Player player) {
        target = player;
      } else {
        sendUsage(sender, "/clear <player>");
        return;
      }
    }

    int removedCount = 0;
    for (ItemStack item : target.getInventory().getContents()) {
      if (item != null && !item.isEmpty() && !item.getType().isAir()) {
        removedCount += item.getAmount();
      }
    }

    target.getInventory().clear();

    target.sendMessage(
        MessageFormatter.formatInfo(getToolName(), "Your inventory has been cleared."));

    if (target != sender && sender instanceof Player) {
      Component senderMsg =
          Component.text("Cleared inventory of ", NamedTextColor.GRAY)
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(" (", NamedTextColor.GRAY))
              .append(
                  Component.text(
                      removedCount + " item" + (removedCount == 1 ? "" : "s"),
                      NamedTextColor.WHITE))
              .append(Component.text(" removed).", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), senderMsg));
    }

    Component broadcastMsg =
        MessageFormatter.formatInfo(
            getToolName(),
            Component.empty()
                .append(PlayerUtils.getGeneralDisplayName(target))
                .append(
                    Component.text(
                        "'s inventory was cleared by a staff member.", NamedTextColor.GRAY)));

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
