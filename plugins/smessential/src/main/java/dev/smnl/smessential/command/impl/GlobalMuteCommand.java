package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.MuteService;
import dev.smnl.smessential.util.MessageFormatter;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GlobalMuteCommand extends EssentialCommand {

  private final MuteService muteService;

  public GlobalMuteCommand(MuteService muteService) {
    super(
        "GlobalMute",
        "Moderation",
        "Toggles global chat mute",
        "smessential.command.moderation",
        false,
        new String[] {"gm"});
    this.muteService = muteService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    boolean newState = muteService.toggleGlobalMute();
    String stateMsg = newState ? "enabled." : "disabled.";

    Bukkit.getServer()
        .sendMessage(MessageFormatter.formatInfo(getToolName(), "Global mute is now " + stateMsg));
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    return Collections.emptyList();
  }
}
