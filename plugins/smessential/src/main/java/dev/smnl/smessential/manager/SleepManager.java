package dev.smnl.smessential.manager;

import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SleepManager implements Listener {

  private final JavaPlugin plugin;
  private final Map<UUID, Long> lastNightSkipAnnounced = new ConcurrentHashMap<>();

  public SleepManager(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerBedEnter(PlayerBedEnterEvent event) {
    if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
      return;
    }

    Player player = event.getPlayer();
    World world = player.getWorld();

    // Delayed by 1 tick so Bukkit updates sleeping state
    Bukkit.getScheduler()
        .runTask(
            plugin,
            () -> {
              if (!player.isOnline() || !player.isSleeping()) {
                return;
              }

              Component playerComp = PlayerUtils.getGeneralDisplayName(player);
              Component fullMessage =
                  MessageFormatter.formatInfo(
                      "World",
                      Component.empty()
                          .append(playerComp)
                          .append(Component.text(" is sleeping.", NamedTextColor.GRAY)));

              for (Player recipient : world.getPlayers()) {
                if (recipient != null && recipient.isOnline()) {
                  recipient.sendMessage(fullMessage);
                }
              }
            });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTimeSkip(TimeSkipEvent event) {
    if (event == null || event.getWorld() == null || !isNightSkip(event)) {
      return;
    }

    World world = event.getWorld();
    long now = System.currentTimeMillis();
    Long last = lastNightSkipAnnounced.get(world.getUID());
    if (last != null && now - last < 5000L) {
      return;
    }
    lastNightSkipAnnounced.put(world.getUID(), now);

    Component message =
        MessageFormatter.formatInfo("World", "The night has been skipped. Good morning!");
    for (Player player : new ArrayList<>(world.getPlayers())) {
      if (player != null && player.isOnline()) {
        player.sendMessage(message);
      }
    }
  }

  private boolean isNightSkip(@NotNull TimeSkipEvent event) {
    try {
      java.lang.reflect.Method method = event.getClass().getMethod("getSkipReason");
      Object reason = method.invoke(event);
      if (reason != null) {
        return "NIGHT_SKIP".equalsIgnoreCase(reason.toString());
      }
    } catch (Throwable ignored) {
    }

    try {
      java.lang.reflect.Method method = event.getClass().getMethod("getReason");
      Object reason = method.invoke(event);
      if (reason != null) {
        return "NIGHT_SKIP".equalsIgnoreCase(reason.toString());
      }
    } catch (Throwable ignored) {
    }

    try {
      return event.getSkipAmount() >= 1000L;
    } catch (Throwable ignored) {
    }

    return false;
  }
}
