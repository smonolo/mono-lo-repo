package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.gui.PunishGUIManager;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PunishCommand extends EssentialCommand {

  private final PunishGUIManager guiManager;

  public PunishCommand(@NotNull PunishGUIManager guiManager) {
    super(
        "Punish",
        "Moderation",
        "Executes moderation actions",
        "smessential.command.moderation",
        true,
        new String[] {"p"});
    this.guiManager = guiManager;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 1) {
      sendUsage(sender, "/punish <player> [reason]");
      return;
    }

    Player staff = (Player) sender;
    String targetName = args[0];
    String reason = args.length >= 2 ? joinArgs(args, 1) : "Other";

    guiManager.openPunishGUI(staff, targetName, reason);
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
