package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.MessageService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReplyCommand extends EssentialCommand {

  private final MessageService messageService;

  public ReplyCommand(MessageService messageService) {
    super(
        "Reply",
        "Message",
        "Replies to the last direct message",
        "smessential.command.msg",
        true,
        new String[] {"r"});
    this.messageService = messageService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 1) {
      sendUsage(sender, "/reply <message>");
      return;
    }

    Player player = (Player) sender;
    String message = joinArgs(args, 0);
    messageService.sendReply(player, message);
  }
}
