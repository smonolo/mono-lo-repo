package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.database.DatabaseManager.MuteData;
import dev.smnl.smessential.util.MessageFormatter;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MuteService implements Listener {

  private final DatabaseManager databaseManager;
  private final Map<UUID, MuteData> mutedPlayers = new ConcurrentHashMap<>();
  private volatile boolean globalMute = false;

  private JavaPlugin plugin;

  public MuteService(DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public void setup(JavaPlugin plugin) {
    this.plugin = plugin;
    this.globalMute = databaseManager.loadGlobalMuteState();
    mutedPlayers.putAll(databaseManager.loadAllMuteEntries());
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public boolean isGlobalMute() {
    return globalMute;
  }

  public void setGlobalMute(boolean globalMute) {
    this.globalMute = globalMute;
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.saveGlobalMuteState(globalMute));
    } else {
      databaseManager.saveGlobalMuteState(globalMute);
    }
  }

  public boolean toggleGlobalMute() {
    setGlobalMute(!this.globalMute);
    return this.globalMute;
  }

  public boolean isMuted(UUID uuid) {
    MuteData data = mutedPlayers.get(uuid);
    if (data == null) {
      return false;
    }
    if (!data.isActive()) {
      mutedPlayers.remove(uuid);
      return false;
    }
    return true;
  }

  public @Nullable MuteData getMuteData(UUID uuid) {
    MuteData data = mutedPlayers.get(uuid);
    if (data != null && !data.isActive()) {
      mutedPlayers.remove(uuid);
      return null;
    }
    return data;
  }

  public @Nullable String getMuteReason(UUID uuid) {
    MuteData data = getMuteData(uuid);
    return data != null ? data.reason() : null;
  }

  public @Nullable UUID findMutedUuid(@NotNull String username) {
    for (Map.Entry<UUID, MuteData> entry : mutedPlayers.entrySet()) {
      if (entry.getValue().isActive() && entry.getValue().username().equalsIgnoreCase(username)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public boolean mutePlayer(
      UUID uuid, String username, String reason, String issuer, long createdAt, long expiresAt) {
    if (isMuted(uuid)) {
      return false;
    }
    MuteData data = new MuteData(username, reason, issuer, createdAt, expiresAt, 0L, null);
    mutedPlayers.put(uuid, data);
    databaseManager.saveMute(uuid.toString(), username, reason, issuer, createdAt, expiresAt);
    return true;
  }

  public boolean mutePlayer(
      UUID uuid, String username, String reason, String issuer, long createdAt) {
    return mutePlayer(uuid, username, reason, issuer, createdAt, 0L);
  }

  public boolean unmutePlayer(UUID uuid, @NotNull String unmutedBy) {
    MuteData removed = mutedPlayers.remove(uuid);
    long now = System.currentTimeMillis();
    databaseManager.unmutePlayer(uuid.toString(), unmutedBy, now);
    return removed != null;
  }

  public boolean unmutePlayer(UUID uuid) {
    return unmutePlayer(uuid, "CONSOLE");
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onAsyncChat(AsyncChatEvent event) {
    Player player = event.getPlayer();
    if (isMuted(player.getUniqueId())) {
      event.setCancelled(true);
      player.sendMessage(MessageFormatter.formatError("Moderation", "You are currently muted."));
      return;
    }

    if (globalMute && !player.hasPermission("smessential.command.moderation")) {
      event.setCancelled(true);
      player.sendMessage(
          MessageFormatter.formatError(
              "Moderation", "You cannot send messages while global mute is enabled."));
    }
  }
}
