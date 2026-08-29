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

public class FeedCommand extends EssentialCommand {

  private final FreezeService freezeService;

  public FeedCommand(@Nullable FreezeService freezeService) {
    super(
        "Feed",
        "Moderation",
        "Restores a player's hunger",
        "smessential.command.moderation",
        false,
        new String[] {"eat"});
    this.freezeService = freezeService;
  }

  public FeedCommand() {
    this(null);
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
        sendUsage(sender, "/feed <player>");
        return;
      }
    }

    if (freezeService != null && freezeService.isFrozen(target.getUniqueId())) {
      sendError(sender, "You cannot feed a frozen player.");
      return;
    }

    target.setFoodLevel(20);
    target.setSaturation(20.0f);

    target.sendMessage(MessageFormatter.formatInfo(getToolName(), "Your appetite was sated."));

    if (target != sender && sender instanceof Player) {
      Component msg =
          Component.text("Fed ", NamedTextColor.GRAY)
              .append(PlayerUtils.getStaffVisibleDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
    }

    Component broadcastMsg =
        MessageFormatter.formatInfo(
            getToolName(),
            Component.empty()
                .append(PlayerUtils.getGeneralDisplayName(target))
                .append(Component.text(" was fed by a staff member.", NamedTextColor.GRAY)));

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
