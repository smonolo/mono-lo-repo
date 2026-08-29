package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.AfkService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AfkCommand extends EssentialCommand {

  private final AfkService afkService;

  public AfkCommand(@NotNull AfkService afkService) {
    super(
        "Afk",
        "World",
        "Toggles your AFK status",
        "smessential.command.afk",
        true,
        new String[] {"away"});
    this.afkService = afkService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (sender instanceof Player player) {
      afkService.toggleAfk(player);
    }
  }
}
