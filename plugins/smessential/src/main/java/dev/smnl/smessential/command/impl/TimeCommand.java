package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimeCommand extends EssentialCommand {

  public TimeCommand() {
    super(
        "Time",
        "Administration",
        "Sets or queries the time of day in the world",
        "smessential.command.administration",
        false,
        new String[0]);
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      World world = resolveWorld(sender, null);
      if (world == null) {
        sendError(
            sender, "Could not determine world. Usage: /time <set|add|query> <value> [world]");
        return;
      }
      displayTimeStatus(sender, world);
      return;
    }

    String sub = args[0].toLowerCase(Locale.ROOT);

    switch (sub) {
      case "set" -> {
        if (args.length < 2) {
          sendUsage(
              sender, "/time set <day|night|noon|midnight|sunrise|sunset|<ticks>|<HH:mm>> [world]");
          return;
        }
        World world = resolveWorld(sender, args.length >= 3 ? args[2] : null);
        if (world == null) {
          sendError(sender, "World not found.");
          return;
        }
        Long targetTicks = parseTimeValue(args[1]);
        if (targetTicks == null) {
          sendError(sender, "Invalid time value: '" + args[1] + "'.");
          return;
        }
        world.setTime(targetTicks);
        sendSetSuccess(sender, args[1], targetTicks, world);
      }
      case "add" -> {
        if (args.length < 2) {
          sendUsage(sender, "/time add <ticks> [world]");
          return;
        }
        World world = resolveWorld(sender, args.length >= 3 ? args[2] : null);
        if (world == null) {
          sendError(sender, "World not found.");
          return;
        }
        Long addAmount = parseTicks(args[1]);
        if (addAmount == null || addAmount < 0) {
          sendError(sender, "Invalid amount of ticks: '" + args[1] + "'.");
          return;
        }
        world.setFullTime(world.getFullTime() + addAmount);
        long day = (world.getFullTime() / 24000L) + 1;
        String formatted = formatTicksToTime(world.getTime());
        Component msg =
            Component.text("Added ", NamedTextColor.GRAY)
                .append(Component.text(addAmount + " ticks", NamedTextColor.WHITE))
                .append(Component.text(" to time (now ", NamedTextColor.GRAY))
                .append(Component.text(formatted, NamedTextColor.WHITE))
                .append(Component.text(", Day ", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(day), NamedTextColor.WHITE))
                .append(Component.text(") in world '", NamedTextColor.GRAY))
                .append(Component.text(world.getName(), NamedTextColor.WHITE))
                .append(Component.text("' by a staff member.", NamedTextColor.GRAY));
        Component broadcastMsg = MessageFormatter.formatInfo(getToolName(), msg);
        PlayerUtils.broadcastMessage(broadcastMsg);
      }
      case "query" -> {
        String queryType = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "daytime";
        World world = resolveWorld(sender, args.length >= 3 ? args[2] : null);
        if (world == null) {
          sendError(sender, "World not found.");
          return;
        }
        handleQuery(sender, queryType, world);
      }
      case "day", "night", "noon", "midday", "midnight", "sunrise", "sunset", "dawn", "dusk" -> {
        World world = resolveWorld(sender, args.length >= 2 ? args[1] : null);
        if (world == null) {
          sendError(sender, "World not found.");
          return;
        }
        Long targetTicks = parseTimeValue(sub);
        if (targetTicks != null) {
          world.setTime(targetTicks);
          sendSetSuccess(sender, sub, targetTicks, world);
        }
      }
      default -> {
        Long targetTicks = parseTimeValue(sub);
        if (targetTicks != null) {
          World world = resolveWorld(sender, args.length >= 2 ? args[1] : null);
          if (world == null) {
            sendError(sender, "World not found.");
            return;
          }
          world.setTime(targetTicks);
          sendSetSuccess(sender, sub, targetTicks, world);
        } else {
          sendUsage(sender, "/time <set|add|query> <value> [world]");
        }
      }
    }
  }

  private void sendSetSuccess(
      @NotNull CommandSender sender,
      @NotNull String inputName,
      long targetTicks,
      @NotNull World world) {
    String formattedTime = formatTicksToTime(targetTicks);
    Component msg =
        Component.text("Time was set to ", NamedTextColor.GRAY)
            .append(Component.text(inputName, NamedTextColor.WHITE))
            .append(Component.text(" (", NamedTextColor.GRAY))
            .append(Component.text(formattedTime, NamedTextColor.WHITE))
            .append(Component.text(") in world '", NamedTextColor.GRAY))
            .append(Component.text(world.getName(), NamedTextColor.WHITE))
            .append(Component.text("' by a staff member.", NamedTextColor.GRAY));

    Component broadcastMsg = MessageFormatter.formatInfo(getToolName(), msg);
    PlayerUtils.broadcastMessage(broadcastMsg);
  }

  private void handleQuery(
      @NotNull CommandSender sender, @NotNull String queryType, @NotNull World world) {
    switch (queryType) {
      case "daytime" -> {
        long ticks = world.getTime();
        String formatted = formatTicksToTime(ticks);
        Component msg =
            Component.text("Daytime is ", NamedTextColor.GRAY)
                .append(Component.text(ticks + " ticks", NamedTextColor.WHITE))
                .append(Component.text(" (", NamedTextColor.GRAY))
                .append(Component.text(formatted, NamedTextColor.WHITE))
                .append(Component.text(") in world '", NamedTextColor.GRAY))
                .append(Component.text(world.getName(), NamedTextColor.WHITE))
                .append(Component.text("'.", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      }
      case "gametime" -> {
        long fullTime = world.getFullTime();
        Component msg =
            Component.text("Game time is ", NamedTextColor.GRAY)
                .append(Component.text(fullTime + " ticks", NamedTextColor.WHITE))
                .append(Component.text(" in world '", NamedTextColor.GRAY))
                .append(Component.text(world.getName(), NamedTextColor.WHITE))
                .append(Component.text("'.", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      }
      case "day" -> {
        long day = (world.getFullTime() / 24000L) + 1;
        Component msg =
            Component.text("Current day is Day ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(day), NamedTextColor.WHITE))
                .append(Component.text(" in world '", NamedTextColor.GRAY))
                .append(Component.text(world.getName(), NamedTextColor.WHITE))
                .append(Component.text("'.", NamedTextColor.GRAY));
        sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      }
      default -> sendUsage(sender, "/time query [daytime|gametime|day] [world]");
    }
  }

  private void displayTimeStatus(@NotNull CommandSender sender, @NotNull World world) {
    long ticks = world.getTime();
    long day = (world.getFullTime() / 24000L) + 1;
    String formattedTime = formatTicksToTime(ticks);

    Component msg =
        Component.text("Time in '", NamedTextColor.GRAY)
            .append(Component.text(world.getName(), NamedTextColor.WHITE))
            .append(Component.text("' is ", NamedTextColor.GRAY))
            .append(Component.text(formattedTime, NamedTextColor.WHITE))
            .append(Component.text(" (Day ", NamedTextColor.GRAY))
            .append(Component.text(String.valueOf(day), NamedTextColor.WHITE))
            .append(Component.text(", ", NamedTextColor.GRAY))
            .append(Component.text(ticks + " ticks", NamedTextColor.WHITE))
            .append(Component.text(").", NamedTextColor.GRAY));

    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
  }

  private @Nullable World resolveWorld(@NotNull CommandSender sender, @Nullable String worldName) {
    if (worldName != null) {
      return Bukkit.getWorld(worldName);
    }
    if (sender instanceof Player player) {
      return player.getWorld();
    }
    return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
  }

  private @Nullable Long parseTimeValue(@NotNull String input) {
    String lower = input.toLowerCase(Locale.ROOT);
    return switch (lower) {
      case "day" -> 1000L;
      case "noon", "midday" -> 6000L;
      case "sunset", "dusk" -> 12000L;
      case "night" -> 13000L;
      case "midnight" -> 18000L;
      case "sunrise", "dawn" -> 23000L;
      default -> {
        if (lower.contains(":")) {
          yield parseTimeString(lower);
        }
        yield parseTicks(lower);
      }
    };
  }

  private @Nullable Long parseTimeString(@NotNull String timeStr) {
    String[] parts = timeStr.split(":");
    if (parts.length != 2) {
      return null;
    }
    try {
      int hours = Integer.parseInt(parts[0]);
      int minutes = Integer.parseInt(parts[1]);
      if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
        return null;
      }
      return (((hours - 6 + 24) % 24) * 1000L) + (minutes * 1000L / 60L);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private @Nullable Long parseTicks(@NotNull String input) {
    String cleaned = input.toLowerCase(Locale.ROOT);
    if (cleaned.endsWith("t")) {
      cleaned = cleaned.substring(0, cleaned.length() - 1);
    }
    if (cleaned.endsWith("d")) {
      try {
        return Long.parseLong(cleaned.substring(0, cleaned.length() - 1)) * 24000L;
      } catch (NumberFormatException e) {
        return null;
      }
    }
    try {
      long val = Long.parseLong(cleaned);
      return val >= 0 ? val : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private @NotNull String formatTicksToTime(long ticks) {
    long normalized = ticks % 24000L;
    long hours = ((normalized / 1000L) + 6) % 24;
    long rawMinutes = (normalized % 1000L) * 60 / 1000;
    return String.format(Locale.ROOT, "%02d:%02d", hours, rawMinutes);
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase() : "";
      List<String> options =
          List.of("set", "add", "query", "day", "night", "noon", "midnight", "sunrise", "sunset");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }
    if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
      String prefix = args[1].toLowerCase();
      List<String> options =
          List.of(
              "day",
              "night",
              "noon",
              "midnight",
              "sunrise",
              "sunset",
              "0",
              "6000",
              "12000",
              "18000");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }
    if (args.length == 2 && args[0].equalsIgnoreCase("query")) {
      String prefix = args[1].toLowerCase();
      List<String> options = List.of("daytime", "gametime", "day");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }
    if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
      String prefix = args[1].toLowerCase();
      List<String> options = List.of("1000", "6000", "12000", "24000");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }
    return Collections.emptyList();
  }
}
