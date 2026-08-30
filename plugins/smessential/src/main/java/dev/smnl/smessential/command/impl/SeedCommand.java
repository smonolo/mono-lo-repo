package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.util.MessageFormatter;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SeedCommand extends EssentialCommand {

  public SeedCommand() {
    super(
        "Seed",
        "World",
        "Displays the world seed",
        "smessential.command.seed",
        false,
        new String[0]);
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    World world = resolveWorld(sender, args.length >= 1 ? args[0] : null);
    if (world == null) {
      if (args.length >= 1) {
        sendError(sender, "World '" + args[0] + "' not found.");
      } else {
        sendError(sender, "Could not determine world.");
      }
      return;
    }

    long seed = world.getSeed();
    String seedStr = String.valueOf(seed);

    Component seedValueComp =
        Component.text("[" + seedStr + "]", NamedTextColor.WHITE)
            .clickEvent(ClickEvent.copyToClipboard(seedStr))
            .hoverEvent(
                HoverEvent.showText(
                    Component.text("Click to copy seed to clipboard", NamedTextColor.GRAY)));

    Component msg =
        Component.text("Seed for world '", NamedTextColor.GRAY)
            .append(Component.text(world.getName(), NamedTextColor.WHITE))
            .append(Component.text("': ", NamedTextColor.GRAY))
            .append(seedValueComp);

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

  @Override
  public @NotNull Collection<String> suggest(
      @NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
    if (args.length <= 1) {
      String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
      return Bukkit.getWorlds().stream()
          .map(World::getName)
          .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
          .toList();
    }
    return Collections.emptyList();
  }
}
