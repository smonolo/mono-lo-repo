package dev.smnl.smessential.service;

import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class AfkService implements Listener {

  private static final long AFK_TIMEOUT_MS = 300_000L; // 5 minutes

  private final JavaPlugin plugin;
  private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
  private final Set<UUID> afkPlayers = ConcurrentHashMap.newKeySet();

  public AfkService(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);

    long now = System.currentTimeMillis();
    for (Player player : Bukkit.getOnlinePlayers()) {
      lastActivity.put(player.getUniqueId(), now);
    }

    // Periodic check every 5 seconds (100 ticks)
    Bukkit.getScheduler().runTaskTimer(plugin, this::checkAfkStatus, 100L, 100L);
  }

  public boolean isAfk(@NotNull UUID uuid) {
    return afkPlayers.contains(uuid);
  }

  public boolean isAfk(@NotNull Player player) {
    return isAfk(player.getUniqueId());
  }

  public void setAfk(@NotNull Player player, boolean afk, boolean announce) {
    UUID uuid = player.getUniqueId();
    if (afk) {
      if (afkPlayers.add(uuid)) {
        player.setSleepingIgnored(true);
        updatePlayerListName(player, true);
        if (announce) {
          Component message =
              MessageFormatter.formatInfo(
                  "World",
                  Component.empty()
                      .append(PlayerUtils.getPlayerDisplayName(player))
                      .append(Component.text(" is now AFK.", NamedTextColor.GRAY)));
          PlayerUtils.broadcastMessage(message);
        }
      }
    } else {
      if (afkPlayers.remove(uuid)) {
        lastActivity.put(uuid, System.currentTimeMillis());
        player.setSleepingIgnored(false);
        updatePlayerListName(player, false);
        if (announce) {
          Component message =
              MessageFormatter.formatInfo(
                  "World",
                  Component.empty()
                      .append(PlayerUtils.getPlayerDisplayName(player))
                      .append(Component.text(" is no longer AFK.", NamedTextColor.GRAY)));
          PlayerUtils.broadcastMessage(message);
        }
      }
    }
  }

  public void toggleAfk(@NotNull Player player) {
    setAfk(player, !isAfk(player), true);
  }

  public void onActivity(@NotNull Player player) {
    UUID uuid = player.getUniqueId();
    lastActivity.put(uuid, System.currentTimeMillis());
    if (isAfk(uuid)) {
      setAfk(player, false, true);
    }
  }

  private void checkAfkStatus() {
    long now = System.currentTimeMillis();
    for (Player player : Bukkit.getOnlinePlayers()) {
      UUID uuid = player.getUniqueId();
      if (!afkPlayers.contains(uuid)) {
        long last = lastActivity.getOrDefault(uuid, now);
        if (now - last >= AFK_TIMEOUT_MS) {
          setAfk(player, true, true);
        }
      }
    }
  }

  private void updatePlayerListName(@NotNull Player player, boolean afk) {
    Component baseName = PlayerUtils.getPlayerDisplayName(player);
    if (afk) {
      Component afkName = Component.text("[AFK] ", NamedTextColor.GRAY).append(baseName);
      player.playerListName(afkName);
    } else {
      player.playerListName(baseName);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    Location from = event.getFrom();
    Location to = event.getTo();
    if (from.getBlockX() != to.getBlockX()
        || from.getBlockY() != to.getBlockY()
        || from.getBlockZ() != to.getBlockZ()) {
      onActivity(event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    onActivity(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      onActivity(player);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onAsyncChat(AsyncChatEvent event) {
    onActivity(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    String msg = event.getMessage().toLowerCase();
    if (!msg.startsWith("/afk") && !msg.startsWith("/away")) {
      onActivity(event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    afkPlayers.remove(uuid);
    lastActivity.remove(uuid);
  }
}
