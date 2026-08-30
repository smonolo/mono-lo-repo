package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.MessageService;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MsgCommand extends EssentialCommand {

  private final MessageService messageService;

  public MsgCommand(@NotNull MessageService messageService) {
    super(
        "Msg",
        "Message",
        "Sends a private message to a player",
        "smessential.command.msg",
        false,
        new String[] {"message", "tell", "w", "whisper"});
    this.messageService = messageService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 2) {
      sendUsage(sender, "/msg <player> <message>");
      return;
    }

    Player target = messageService.findTarget(args[0], sender);
    if (target == null) {
      sendError(sender, "'" + args[0] + "' is not online.");
      return;
    }

    String msgContent = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    messageService.sendMessage(sender, target, msgContent);
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
