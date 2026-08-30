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

public class FreezeCommand extends EssentialCommand {

  private final FreezeService freezeService;

  public FreezeCommand(@NotNull FreezeService freezeService) {
    super(
        "Freeze",
        "Moderation",
        "Freezes or unfreezes a player",
        "smessential.command.moderation",
        false,
        new String[] {"f"});
    this.freezeService = freezeService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 1) {
      sendUsage(sender, "/freeze <player> [reason]");
      return;
    }

    Player target = requireOnlinePlayer(sender, args[0]);
    if (target == null) {
      return;
    }

    if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
      sendError(sender, "You cannot freeze yourself.");
      return;
    }

    String staffUuid = (sender instanceof Player p) ? p.getUniqueId().toString() : "CONSOLE";
    String reason = args.length >= 2 ? joinArgs(args, 1) : "Staff Investigation";

    boolean isNowFrozen = freezeService.toggleFreeze(target, staffUuid, reason);
    String stateMsg = isNowFrozen ? "frozen." : "unfrozen.";

    Component msg =
        Component.empty()
            .append(PlayerUtils.getStaffVisibleDisplayName(target))
            .append(Component.text(" has been " + stateMsg, NamedTextColor.GRAY));

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
