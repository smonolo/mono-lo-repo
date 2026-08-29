package dev.smnl.smessential.service;

import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpectateService implements Listener {

  public record SpectateState(
      @NotNull Location previousLocation, @NotNull GameMode previousGameMode) {}

  private final JavaPlugin plugin;
  private final Map<UUID, SpectateState> spectatingPlayers = new ConcurrentHashMap<>();

  public SpectateService(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public boolean isSpectating(@NotNull UUID uuid) {
    return spectatingPlayers.containsKey(uuid);
  }

  public void startSpectating(@NotNull Player staff, @Nullable Player target) {
    if (!spectatingPlayers.containsKey(staff.getUniqueId())) {
      spectatingPlayers.put(
          staff.getUniqueId(), new SpectateState(staff.getLocation().clone(), staff.getGameMode()));
    }

    staff.setGameMode(GameMode.SPECTATOR);

    if (target != null) {
      staff.teleport(target.getLocation());
      Component msg =
          Component.text("Spectating ", NamedTextColor.GRAY)
              .append(PlayerUtils.getGeneralDisplayName(target))
              .append(Component.text(".", NamedTextColor.GRAY));
      staff.sendMessage(MessageFormatter.formatInfo("Moderation", msg));
    } else {
      staff.sendMessage(MessageFormatter.formatInfo("Moderation", "Spectate mode enabled."));
    }
  }

  public void stopSpectating(@NotNull Player staff) {
    SpectateState state = spectatingPlayers.remove(staff.getUniqueId());
    if (state != null) {
      staff.teleport(state.previousLocation());
      staff.setGameMode(state.previousGameMode());
    } else {
      staff.setGameMode(GameMode.SURVIVAL);
    }
    staff.sendMessage(MessageFormatter.formatInfo("Moderation", "Spectate mode disabled."));
  }

  public void toggleSpectating(@NotNull Player staff, @Nullable Player target) {
    if (staff.getGameMode() == GameMode.SPECTATOR || isSpectating(staff.getUniqueId())) {
      if (target != null) {

        staff.teleport(target.getLocation());
        Component msg =
            Component.text("Spectating ", NamedTextColor.GRAY)
                .append(PlayerUtils.getGeneralDisplayName(target))
                .append(Component.text(".", NamedTextColor.GRAY));
        staff.sendMessage(MessageFormatter.formatInfo("Moderation", msg));
      } else {
        stopSpectating(staff);
      }
    } else {
      startSpectating(staff, target);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    SpectateState state = spectatingPlayers.remove(player.getUniqueId());
    if (state != null) {
      player.teleport(state.previousLocation());
      player.setGameMode(state.previousGameMode());
    }
  }
}
