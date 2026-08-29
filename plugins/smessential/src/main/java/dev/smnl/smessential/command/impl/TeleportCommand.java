package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeleportCommand extends EssentialCommand {

  public TeleportCommand() {
    super(
        "Teleport",
        "Moderation",
        "Teleports to a player or coordinates",
        "smessential.command.moderation",
        false,
        new String[] {"tp"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      sendUsage(
          sender,
          "/teleport <player> | /teleport <player1> <player2> | /teleport <x> <y> <z> | /teleport <player> <x> <y> <z>");
      return;
    }

    if (args.length == 1) {
      if (!(sender instanceof Player player)) {
        sendError(sender, "Usage: /teleport <player1> <player2>");
        return;
      }
      Player target = PlayerUtils.findOnlinePlayer(args[0]);
      if (target == null) {
        sendError(sender, "'" + args[0] + "' is not online.");
        return;
      }
      player.teleport(target.getLocation());
      Component msg =
          Component.text("Teleported to ", NamedTextColor.GRAY)
              .append(PlayerUtils.getGeneralDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      player.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      return;
    }

    if (args.length == 2) {
      Player target1 = PlayerUtils.findOnlinePlayer(args[0]);
      if (target1 == null) {
        sendError(sender, "'" + args[0] + "' is not online.");
        return;
      }
      Player target2 = PlayerUtils.findOnlinePlayer(args[1]);
      if (target2 == null) {
        sendError(sender, "'" + args[1] + "' is not online.");
        return;
      }
      target1.teleport(target2.getLocation());
      Component msgSender =
          Component.text("Teleported ", NamedTextColor.GRAY)
              .append(PlayerUtils.getGeneralDisplayName(target1))
              .append(Component.text(" to ", NamedTextColor.GRAY))
              .append(PlayerUtils.getGeneralDisplayName(target2))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msgSender));
      if (target1 != sender) {
        Component msgTarget =
            Component.text("You were teleported to ", NamedTextColor.GRAY)
                .append(PlayerUtils.getGeneralDisplayName(target2))
                .append(Component.text(".", NamedTextColor.GRAY));
        target1.sendMessage(MessageFormatter.formatInfo(getToolName(), msgTarget));
      }
      return;
    }

    if (args.length == 3) {
      if (!(sender instanceof Player player)) {
        sendError(sender, "Usage: /teleport <player> <x> <y> <z>");
        return;
      }
      Location current = player.getLocation();
      Double x = parseCoord(args[0], current.getX());
      Double y = parseCoord(args[1], current.getY());
      Double z = parseCoord(args[2], current.getZ());
      if (x == null || y == null || z == null) {
        sendError(sender, "Invalid coordinates provided.");
        return;
      }
      Location dest =
          new Location(current.getWorld(), x, y, z, current.getYaw(), current.getPitch());
      player.teleport(dest);
      Component msgCoords =
          Component.text("Teleported to ", NamedTextColor.GRAY)
              .append(
                  Component.text(String.format("%.1f, %.1f, %.1f", x, y, z), NamedTextColor.WHITE))
              .append(Component.text(".", NamedTextColor.GRAY));
      player.sendMessage(MessageFormatter.formatInfo(getToolName(), msgCoords));
      return;
    }

    if (args.length == 4) {
      Player target = PlayerUtils.findOnlinePlayer(args[0]);
      if (target == null) {
        sendError(sender, "'" + args[0] + "' is not online.");
        return;
      }
      Location current = target.getLocation();
      Double x = parseCoord(args[1], current.getX());
      Double y = parseCoord(args[2], current.getY());
      Double z = parseCoord(args[3], current.getZ());
      if (x == null || y == null || z == null) {
        sendError(sender, "Invalid coordinates provided.");
        return;
      }
      Location dest =
          new Location(current.getWorld(), x, y, z, current.getYaw(), current.getPitch());
      target.teleport(dest);
      Component msgSender =
          Component.text("Teleported ", NamedTextColor.GRAY)
              .append(PlayerUtils.getGeneralDisplayName(target))
              .append(Component.text(" to ", NamedTextColor.GRAY))
              .append(
                  Component.text(String.format("%.1f, %.1f, %.1f", x, y, z), NamedTextColor.WHITE))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msgSender));
      if (target != sender) {
        Component msgTarget =
            Component.text("You were teleported to ", NamedTextColor.GRAY)
                .append(
                    Component.text(
                        String.format("%.1f, %.1f, %.1f", x, y, z), NamedTextColor.WHITE))
                .append(Component.text(".", NamedTextColor.GRAY));
        target.sendMessage(MessageFormatter.formatInfo(getToolName(), msgTarget));
      }
      return;
    }

    sendUsage(
        sender,
        "/teleport <player> | /teleport <player1> <player2> | /teleport <x> <y> <z> | /teleport <player> <x> <y> <z>");
  }

  private @Nullable Double parseCoord(String input, double current) {
    if (input.startsWith("~")) {
      if (input.length() == 1) {
        return current;
      }
      try {
        return current + Double.parseDouble(input.substring(1));
      } catch (NumberFormatException e) {
        return null;
      }
    }
    try {
      return Double.parseDouble(input);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0] : "";
      return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), prefix);
    }
    if (args.length == 2 && !args[0].startsWith("~")) {
      return PlayerUtils.getSuggestedPlayerNames(commandSourceStack.getSender(), args[1]);
    }
    return Collections.emptyList();
  }
}
