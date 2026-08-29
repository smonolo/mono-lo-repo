package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.database.DatabaseManager.UserData;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserService implements Listener {

  private final DatabaseManager databaseManager;
  private final Map<UUID, UserData> users = new ConcurrentHashMap<>();
  private JavaPlugin plugin;

  public UserService(@NotNull DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public void setup(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
    this.users.putAll(databaseManager.loadAllUsers());
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public void reload() {
    this.users.clear();
    this.users.putAll(databaseManager.loadAllUsers());
  }

  public @Nullable UserData getUser(@NotNull UUID uuid) {
    return users.get(uuid);
  }

  public @Nullable UserData getUserByUsername(@NotNull String username) {
    for (UserData data : users.values()) {
      if (data.username().equalsIgnoreCase(username)) {
        return data;
      }
    }
    return null;
  }

  public @Nullable UUID findUuidByUsername(@NotNull String username) {
    UserData data = getUserByUsername(username);
    return data != null ? data.uuid() : null;
  }

  public boolean hasPlayedBefore(@NotNull UUID uuid) {
    return users.containsKey(uuid);
  }

  public boolean hasPlayedBefore(@NotNull String username) {
    return getUserByUsername(username) != null;
  }

  public @NotNull Map<UUID, UserData> getAllUsers() {
    return Collections.unmodifiableMap(users);
  }

  public int getUserCount() {
    return users.size();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();
    String username = player.getName();
    long now = System.currentTimeMillis();

    UserData existing = users.get(uuid);
    long firstJoin = (existing != null) ? existing.firstJoin() : now;
    UserData updated = new UserData(uuid, username, firstJoin, now);
    users.put(uuid, updated);

    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.saveUser(uuid, username, now));
    }
  }
}
