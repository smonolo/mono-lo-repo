package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.database.DatabaseManager.BanData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BanService implements Listener {

  private final DatabaseManager databaseManager;
  private final Map<UUID, BanData> bannedPlayers = new ConcurrentHashMap<>();

  public BanService(DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public void setup(JavaPlugin plugin) {
    bannedPlayers.putAll(databaseManager.loadAllBanEntries());
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public boolean isBanned(UUID uuid) {
    BanData data = bannedPlayers.get(uuid);
    if (data == null) {
      return false;
    }
    if (!data.isActive()) {
      bannedPlayers.remove(uuid);
      return false;
    }
    return true;
  }

  public @Nullable BanData getBanData(UUID uuid) {
    BanData data = bannedPlayers.get(uuid);
    if (data != null && !data.isActive()) {
      bannedPlayers.remove(uuid);
      return null;
    }
    return data;
  }

  public @Nullable String getBanReason(UUID uuid) {
    BanData data = getBanData(uuid);
    return data != null ? data.reason() : null;
  }

  public @Nullable UUID findBannedUuid(@NotNull String username) {
    for (Map.Entry<UUID, BanData> entry : bannedPlayers.entrySet()) {
      if (entry.getValue().isActive() && entry.getValue().username().equalsIgnoreCase(username)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public boolean banPlayer(
      UUID uuid, String username, String reason, String issuer, long createdAt, long expiresAt) {
    if (isBanned(uuid)) {
      return false;
    }
    BanData data = new BanData(username, reason, issuer, createdAt, expiresAt, 0L, null);
    bannedPlayers.put(uuid, data);
    databaseManager.saveBan(uuid.toString(), username, reason, issuer, createdAt, expiresAt);
    return true;
  }

  public boolean banPlayer(
      UUID uuid, String username, String reason, String issuer, long createdAt) {
    return banPlayer(uuid, username, reason, issuer, createdAt, 0L);
  }

  public boolean unbanPlayer(@NotNull UUID uuid, @NotNull String unbannedBy) {
    BanData removed = bannedPlayers.remove(uuid);
    long now = System.currentTimeMillis();
    databaseManager.unbanPlayer(uuid.toString(), unbannedBy, now);
    return removed != null;
  }

  public boolean unbanPlayer(@NotNull String username, @NotNull String unbannedBy) {
    UUID uuid = findBannedUuid(username);
    if (uuid != null) {
      return unbanPlayer(uuid, unbannedBy);
    }
    long now = System.currentTimeMillis();
    databaseManager.unpunishByUsername(username, "BAN", unbannedBy, now);
    return true;
  }

  public boolean unbanPlayer(@NotNull String username) {
    return unbanPlayer(username, "CONSOLE");
  }

  public static Component createBanScreen(String reason) {
    return Component.empty()
        .append(Component.text("Headquarters", NamedTextColor.BLUE, TextDecoration.BOLD))
        .append(Component.newline())
        .append(Component.newline())
        .append(
            Component.text("You have been banned for " + reason + ".", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, false));
  }

  public static Component createKickScreen(String reason) {
    return Component.empty()
        .append(Component.text("Headquarters", NamedTextColor.BLUE, TextDecoration.BOLD))
        .append(Component.newline())
        .append(Component.newline())
        .append(
            Component.text("You have been kicked for " + reason + ".", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, false));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    UUID uuid = event.getUniqueId();
    if (isBanned(uuid)) {
      String reason = getBanReason(uuid);
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
          createBanScreen(reason != null ? reason : "violating server rules"));
    }
  }
}
