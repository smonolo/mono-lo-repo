package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.gui.InvseeGUIManager;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InvseeCommand extends EssentialCommand {

  private final InvseeGUIManager invseeGUIManager;

  public InvseeCommand(@NotNull InvseeGUIManager invseeGUIManager) {
    super(
        "Invsee",
        "Moderation",
        "Views a player's inventory silently",
        "smessential.command.moderation",
        true,
        new String[] {"viewinv", "seeinv", "openinv", "inv"});
    this.invseeGUIManager = invseeGUIManager;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (!(sender instanceof Player staff)) {
      sendError(sender, "This command can only be executed by players.");
      return;
    }

    if (args.length == 0) {
      sendUsage(sender, "/invsee <player> [enderchest|ec]");
      return;
    }

    Player target = PlayerUtils.findOnlinePlayer(args[0], sender);
    if (target == null) {
      sendError(sender, "'" + args[0] + "' is not online.");
      return;
    }

    boolean enderChest = false;
    if (args.length >= 2) {
      String mode = args[1].toLowerCase(Locale.ROOT);
      if (mode.equals("enderchest") || mode.equals("ec") || mode.equals("ender")) {
        enderChest = true;
      }
    }

    if (enderChest) {
      invseeGUIManager.openEnderChestGUI(staff, target);
      Component msg =
          Component.text("Viewing ender chest of ", NamedTextColor.GRAY)
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
    } else {
      invseeGUIManager.openInventoryGUI(staff, target);
      Component msg =
          Component.text("Viewing inventory of ", NamedTextColor.GRAY)
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
    }
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0] : "";
      return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), prefix);
    }
    if (args.length == 2) {
      String prefix = args[1].toLowerCase(Locale.ROOT);
      return List.of("enderchest", "ec").stream().filter(s -> s.startsWith(prefix)).toList();
    }
    return Collections.emptyList();
  }
}
