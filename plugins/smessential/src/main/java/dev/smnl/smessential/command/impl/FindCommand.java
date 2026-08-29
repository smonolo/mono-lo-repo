package dev.smnl.smessential.command.impl;

import dev.smnl.smessential.command.EssentialCommand;
import dev.smnl.smessential.manager.SidebarManager;
import dev.smnl.smessential.util.FontUtils;
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

public class FindCommand extends EssentialCommand {

  public FindCommand() {
    super(
        "Find",
        "Moderation",
        "Locates an online player's coordinates and world",
        "smessential.command.moderation",
        false,
        new String[] {"where", "whereis", "locateplayer"});
  }

  @Override
  protected void run(
      @NotNull CommandSourceStack stack, @NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) {
      sendUsage(sender, "/find <player>");
      return;
    }

    Player target = PlayerUtils.findOnlinePlayer(args[0], sender);
    if (target == null) {
      sendError(sender, "'" + args[0] + "' is not online.");
      return;
    }

    Location loc = target.getLocation();
    int x = loc.getBlockX();
    int y = loc.getBlockY();
    int z = loc.getBlockZ();
    String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "Unknown";
    String biomeKey = SidebarManager.resolveBiomeKey(target);
    String biomeName = FontUtils.formatEnumTitleCase(biomeKey);
    NamedTextColor biomeColor = SidebarManager.resolveBiomeColor(biomeKey);

    Component targetDisplay = PlayerUtils.getStaffVisibleDisplayName(target);

    Component msg =
        Component.text("Located ", NamedTextColor.GRAY)
            .append(targetDisplay)
            .append(Component.text(" at ", NamedTextColor.GRAY))
            .append(Component.text(x + ", " + y + ", " + z, NamedTextColor.WHITE))
            .append(Component.text(" in world '", NamedTextColor.GRAY))
            .append(Component.text(worldName, NamedTextColor.WHITE))
            .append(Component.text("' (Biome: ", NamedTextColor.GRAY))
            .append(Component.text(biomeName, biomeColor))
            .append(Component.text(").", NamedTextColor.GRAY));

    sender.sendMessage(MessageFormatter.formatInfo(getToolName(), msg));
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
