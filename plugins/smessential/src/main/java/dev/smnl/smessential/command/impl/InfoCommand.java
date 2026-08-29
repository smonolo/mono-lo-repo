package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.gui.InfoGUIManager;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InfoCommand extends EssentialCommand {

  private final InfoGUIManager infoGUIManager;

  public InfoCommand(@NotNull InfoGUIManager infoGUIManager) {
    super(
        "Info",
        "Moderation",
        "Opens detailed player information and moderation menu",
        "smessential.command.moderation",
        true,
        new String[0]);
    this.infoGUIManager = infoGUIManager;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length < 1) {
      sendUsage(sender, "/info <player>");
      return;
    }

    Player staff = (Player) sender;
    String targetName = args[0];
    Player target = PlayerUtils.findOnlinePlayer(targetName);

    if (target == null) {
      sendError(staff, "'" + targetName + "' is not online.");
      return;
    }

    infoGUIManager.openInfoGUI(staff, target);
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
