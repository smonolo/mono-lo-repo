package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.service.TeamService;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamCommand extends EssentialCommand {

  private final TeamService teamService;

  public TeamCommand(@NotNull TeamService teamService) {
    super(
        "Team",
        "Team",
        "Manages survival teams",
        "smessential.command.team",
        true,
        new String[] {"party", "t"});
    this.teamService = teamService;
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    Player player = (Player) sender;

    if (args.length == 0) {
      sendHelp(player);
      return;
    }

    String sub = args[0].toLowerCase(Locale.ROOT);

    switch (sub) {
      case "invite" -> {
        if (args.length < 2) {
          sendUsage(player, "/team invite <player>");
          return;
        }
        Player target = PlayerUtils.findOnlinePlayer(args[1], player);
        if (target == null) {
          sendError(player, "'" + args[1] + "' is not online.");
          return;
        }
        teamService.invite(player, target);
      }
      case "accept" -> {
        String inviterName = args.length >= 2 ? args[1] : null;
        teamService.acceptInvite(player, inviterName);
      }
      case "decline", "deny" -> {
        String inviterName = args.length >= 2 ? args[1] : null;
        teamService.declineInvite(player, inviterName);
      }
      case "leave", "disband" -> teamService.leaveTeam(player);
      case "kick" -> {
        if (args.length < 2) {
          sendUsage(player, "/team kick <player>");
          return;
        }
        Player target = PlayerUtils.findOnlinePlayer(args[1], player);
        if (target == null) {
          sendError(player, "'" + args[1] + "' is not online.");
          return;
        }
        teamService.removeMember(player, target);
      }
      default -> sendHelp(player);
    }
  }

  private void sendHelp(@NotNull Player player) {
    Component help =
        Component.text("Survival Team Commands:", NamedTextColor.GOLD)
            .append(Component.newline())
            .append(Component.text("  /team invite <player>", NamedTextColor.WHITE))
            .append(Component.text(" - Invites a player to your team", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("  /team accept [player]", NamedTextColor.WHITE))
            .append(Component.text(" - Accepts a pending team invite", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("  /team decline [player]", NamedTextColor.WHITE))
            .append(Component.text(" - Declines a pending team invite", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("  /team leave", NamedTextColor.WHITE))
            .append(Component.text(" - Leaves your current team", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("  /team kick <player>", NamedTextColor.WHITE))
            .append(Component.text(" - Kicks a player from your team", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("  /team disband", NamedTextColor.WHITE))
            .append(Component.text(" - Disbands your team", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("  /team info", NamedTextColor.WHITE))
            .append(Component.text(" - Shows your team members and health", NamedTextColor.GRAY));
    player.sendMessage(MessageFormatter.formatInfo(getToolName(), help));
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
      List<String> options =
          List.of("invite", "accept", "decline", "leave", "kick", "disband", "info");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }

    if (args.length == 2
        && (args[0].equalsIgnoreCase("invite")
            || args[0].equalsIgnoreCase("accept")
            || args[0].equalsIgnoreCase("decline")
            || args[0].equalsIgnoreCase("kick"))) {
      String prefix = args[1];
      return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), prefix);
    }

    return Collections.emptyList();
  }
}
