package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.CommandManager;
import dev.smnl.smessential.command.EssentialCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HelpCommand extends EssentialCommand {

  private static final int PAGE_SIZE = 10;
  private final CommandManager commandManager;

  public HelpCommand(CommandManager commandManager) {
    super(
        "Help",
        "Help",
        "Lists available commands",
        "smessential.command.help",
        false,
        new String[] {"?", "h"});
    this.commandManager = commandManager;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    List<EssentialCommand> available =
        commandManager.getRegisteredCommands().stream()
            .filter(cmd -> !cmd.isHiddenFromHelp() && cmd.canUse(stack))
            .toList();

    int page = 1;
    if (args.length > 0) {
      try {
        page = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        sendError(sender, "Invalid page number.");
        return;
      }
    }

    List<HelpEntry> entries =
        available.stream()
            .map(cmd -> new HelpEntry("/" + cmd.getName().toLowerCase(), cmd.getDescription()))
            .toList();

    sendPaginatedHelp(sender, "Available commands", "help", entries, page);
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      int totalPages =
          Math.max(
              1,
              (int)
                  Math.ceil(
                      (double)
                              commandManager.getRegisteredCommands().stream()
                                  .filter(
                                      cmd ->
                                          !cmd.isHiddenFromHelp() && cmd.canUse(commandSourceStack))
                                  .count()
                          / PAGE_SIZE));
      List<String> suggestions = new java.util.ArrayList<>();
      for (int i = 1; i <= totalPages; i++) {
        suggestions.add(String.valueOf(i));
      }
      return suggestions;
    }
    return Collections.emptyList();
  }
}
