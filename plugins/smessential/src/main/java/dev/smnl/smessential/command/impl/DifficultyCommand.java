package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.FontUtils;
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
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DifficultyCommand extends EssentialCommand {

  public DifficultyCommand() {
    super(
        "Difficulty",
        "Administration",
        "Sets or queries the world difficulty",
        "smessential.command.administration",
        false,
        new String[] {"diff"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      World world = resolveWorld(sender, null);
      if (world == null) {
        sendUsage(sender, "/difficulty <peaceful|easy|normal|hard> [world]");
        return;
      }
      String diffName = FontUtils.formatEnumTitleCase(world.getDifficulty().name());
      Component msg =
          Component.text("Difficulty for world '", NamedTextColor.GRAY)
              .append(Component.text(world.getName(), NamedTextColor.WHITE))
              .append(Component.text("' is ", NamedTextColor.GRAY))
              .append(Component.text(diffName, NamedTextColor.WHITE))
              .append(Component.text(".", NamedTextColor.GRAY));
      sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
      return;
    }

    Difficulty difficulty = parseDifficulty(args[0]);
    if (difficulty == null) {
      sendError(
          sender, "Invalid difficulty: '" + args[0] + "'. Use peaceful, easy, normal, or hard.");
      return;
    }

    World world = resolveWorld(sender, args.length >= 2 ? args[1] : null);
    if (world == null) {
      sendError(sender, "World not found.");
      return;
    }

    world.setDifficulty(difficulty);
    String diffName = FontUtils.formatEnumTitleCase(difficulty.name());

    Component broadcastMsg =
        MessageFormatter.formatInfo(
            getToolName(),
            Component.text("World difficulty was set to ", NamedTextColor.GRAY)
                .append(Component.text(diffName, NamedTextColor.WHITE))
                .append(Component.text(" in world '", NamedTextColor.GRAY))
                .append(Component.text(world.getName(), NamedTextColor.WHITE))
                .append(Component.text("' by a staff member.", NamedTextColor.GRAY)));

    PlayerUtils.broadcastMessage(broadcastMsg);
  }

  private @Nullable Difficulty parseDifficulty(@NotNull String input) {
    String lower = input.toLowerCase(Locale.ROOT);
    return switch (lower) {
      case "peaceful", "p", "0" -> Difficulty.PEACEFUL;
      case "easy", "e", "1" -> Difficulty.EASY;
      case "normal", "n", "2" -> Difficulty.NORMAL;
      case "hard", "h", "3" -> Difficulty.HARD;
      default -> null;
    };
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

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
      List<String> options = List.of("peaceful", "easy", "normal", "hard");
      return options.stream().filter(s -> s.startsWith(prefix)).toList();
    }
    if (args.length == 2) {
      String prefix = args[1].toLowerCase(Locale.ROOT);
      return Bukkit.getWorlds().stream()
          .map(World::getName)
          .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
          .toList();
    }
    return Collections.emptyList();
  }
}
