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

public class WeatherCommand extends EssentialCommand {

  public WeatherCommand() {
    super(
        "Weather",
        "Administration",
        "Sets the weather in the world",
        "smessential.command.administration",
        false,
        new String[] {"w"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      World world = resolveWorld(sender, null);
      if (world == null) {
        sendUsage(sender, "/weather <clear|rain|storm|thunder> [duration] [world]");
        return;
      }
      displayWeatherStatus(sender, world);
      return;
    }

    String type = args[0].toLowerCase(Locale.ROOT);
    Integer durationTicks = null;
    String worldName = null;

    if (args.length >= 2) {
      durationTicks = parseDurationToTicks(args[1]);
      if (durationTicks == null) {
        worldName = args[1];
      }
    }

    if (args.length >= 3) {
      worldName = args[2];
    }

    World world = resolveWorld(sender, worldName);
    if (world == null) {
      sendError(sender, "World not found.");
      return;
    }

    int duration = durationTicks != null ? durationTicks : 6000; // 5 minutes default

    switch (type) {
      case "clear", "sun", "sunny" -> {
        world.setStorm(false);
        world.setThundering(false);
        world.setClearWeatherDuration(duration);
        world.setWeatherDuration(0);
        world.setThunderDuration(0);
        broadcastWeatherChange(sender, "Clear", duration, world);
      }
      case "rain" -> {
        world.setStorm(true);
        world.setThundering(false);
        world.setWeatherDuration(duration);
        world.setClearWeatherDuration(0);
        world.setThunderDuration(0);
        broadcastWeatherChange(sender, "Rain", duration, world);
      }
      case "storm", "thunder", "thunderstorm" -> {
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(duration);
        world.setThunderDuration(duration);
        world.setClearWeatherDuration(0);
        broadcastWeatherChange(sender, "Thunderstorm", duration, world);
      }
      default -> sendUsage(sender, "/weather <clear|rain|storm|thunder> [duration] [world]");
    }
  }

  private void broadcastWeatherChange(
      @NotNull CommandSender sender,
      @NotNull String typeName,
      int durationTicks,
      @NotNull World world) {
    long seconds = durationTicks / 20L;
    String durationString = formatSeconds(seconds);

    Component msg =
        Component.text("Weather was set to ", NamedTextColor.GRAY)
            .append(Component.text(typeName, NamedTextColor.WHITE))
            .append(Component.text(" for ", NamedTextColor.GRAY))
            .append(Component.text(durationString, NamedTextColor.WHITE))
            .append(Component.text(" in world '", NamedTextColor.GRAY))
            .append(Component.text(world.getName(), NamedTextColor.WHITE))
            .append(Component.text("' by a staff member.", NamedTextColor.GRAY));

    Component broadcastMsg = MessageFormatter.formatInfo(getToolName(), msg);
    PlayerUtils.broadcastMessage(broadcastMsg);
  }

  private void displayWeatherStatus(@NotNull CommandSender sender, @NotNull World world) {
    String state = "Clear";
    if (world.isThundering()) {
      state = "Thunderstorm";
    } else if (world.hasStorm()) {
      state = "Rain";
    }

    Component msg =
        Component.text("Current weather in '", NamedTextColor.GRAY)
            .append(Component.text(world.getName(), NamedTextColor.WHITE))
            .append(Component.text("' is ", NamedTextColor.GRAY))
            .append(Component.text(state, NamedTextColor.WHITE))
            .append(Component.text(".", NamedTextColor.GRAY));

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

  private @Nullable Integer parseDurationToTicks(@NotNull String input) {
    String lower = input.toLowerCase(Locale.ROOT);
    try {
      if (lower.endsWith("s")) {
        int seconds = Integer.parseInt(lower.substring(0, lower.length() - 1));
        return seconds * 20;
      }
      if (lower.endsWith("m")) {
        int minutes = Integer.parseInt(lower.substring(0, lower.length() - 1));
        return minutes * 60 * 20;
      }
      if (lower.endsWith("d")) {
        int days = Integer.parseInt(lower.substring(0, lower.length() - 1));
        return days * 24000;
      }
      if (lower.endsWith("t")) {
        return Integer.parseInt(lower.substring(0, lower.length() - 1));
      }
      int seconds = Integer.parseInt(lower);
      return seconds * 20;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private @NotNull String formatSeconds(long seconds) {
    if (seconds >= 3600) {
      long hours = seconds / 3600;
      long remainingM = (seconds % 3600) / 60;
      return hours + "h" + (remainingM > 0 ? " " + remainingM + "m" : "");
    }
    if (seconds >= 60) {
      long minutes = seconds / 60;
      long remainingS = seconds % 60;
      return minutes + "m" + (remainingS > 0 ? " " + remainingS + "s" : "");
    }
    return seconds + "s";
  }

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase() : "";
      List<String> options = List.of("clear", "rain", "storm", "thunder");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }
    if (args.length == 2) {
      String prefix = args[1].toLowerCase();
      List<String> options = List.of("30s", "1m", "5m", "10m", "30m", "1h");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }
    return Collections.emptyList();
  }
}
