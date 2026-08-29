package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.SpectateService;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpectateCommand extends EssentialCommand {

  private final SpectateService spectateService;

  public SpectateCommand(@NotNull SpectateService spectateService) {
    super(
        "Spectate",
        "Moderation",
        "Toggles spectator mode to silently inspect players",
        "smessential.command.moderation",
        true,
        new String[] {"spec", "sp"});
    this.spectateService = spectateService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (!(sender instanceof Player staff)) {
      sendError(sender, "Only players can execute this command.");
      return;
    }

    if (args.length == 0) {
      spectateService.toggleSpectating(staff, null);
      return;
    }

    Player target = PlayerUtils.findOnlinePlayer(args[0]);
    if (target == null) {
      sendError(staff, "'" + args[0] + "' is not online.");
      return;
    }

    if (target.getUniqueId().equals(staff.getUniqueId())) {
      spectateService.toggleSpectating(staff, null);
      return;
    }

    spectateService.toggleSpectating(staff, target);
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
