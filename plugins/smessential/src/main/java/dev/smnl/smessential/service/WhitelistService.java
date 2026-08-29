package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.database.DatabaseManager.UserData;
import dev.smnl.smessential.database.DatabaseManager.WhitelistData;
import dev.smnl.smessential.model.Rank;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WhitelistService implements Listener {

  private final DatabaseManager databaseManager;
  private final RankService rankService;
  private final UserService userService;
  private final Map<String, WhitelistData> whitelistEntries = new ConcurrentHashMap<>();
  private volatile boolean enabled = false;
  private JavaPlugin plugin;

  public WhitelistService(
      @NotNull DatabaseManager databaseManager,
      @Nullable RankService rankService,
      @Nullable UserService userService) {
    this.databaseManager = databaseManager;
    this.rankService = rankService;
    this.userService = userService;
  }

  public WhitelistService(
      @NotNull DatabaseManager databaseManager, @Nullable RankService rankService) {
    this(databaseManager, rankService, null);
  }

  public WhitelistService(@NotNull DatabaseManager databaseManager) {
    this(databaseManager, null, null);
  }

  public void setup(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
    this.enabled = databaseManager.loadWhitelistState();
    loadEntriesFromDatabase();
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public @Nullable JavaPlugin getPlugin() {
    return plugin;
  }

  public void reload() {
    this.enabled = databaseManager.loadWhitelistState();
    this.whitelistEntries.clear();
    loadEntriesFromDatabase();
  }

  private void loadEntriesFromDatabase() {
    List<WhitelistData> loaded = databaseManager.loadAllWhitelistPlayers();
    for (WhitelistData data : loaded) {
      WhitelistData enriched = enrichEntry(data);
      whitelistEntries.put(getEntryKey(enriched), enriched);
    }
  }

  private @NotNull String getEntryKey(@NotNull WhitelistData data) {
    if (data.uuid() != null) {
      return data.uuid().toString().toLowerCase(Locale.ROOT);
    }
    if (data.username() != null && !data.username().isBlank()) {
      return data.username().toLowerCase(Locale.ROOT);
    }
    return UUID.randomUUID().toString();
  }

  private @NotNull WhitelistData enrichEntry(@NotNull WhitelistData data) {
    UUID uuid = data.uuid();
    String username = data.username();

    if (uuid != null && (username == null || username.isBlank())) {
      if (userService != null) {
        UserData ud = userService.getUser(uuid);
        if (ud != null && ud.username() != null && !ud.username().isBlank()) {
          username = ud.username();
        }
      }
      if (username == null || username.isBlank()) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
          username = online.getName();
        }
      }
    } else if (uuid == null && username != null && !username.isBlank()) {
      if (userService != null) {
        UserData ud = userService.getUserByUsername(username);
        if (ud != null && ud.uuid() != null) {
          uuid = ud.uuid();
          username = ud.username();
        }
      }
      if (uuid == null) {
        Player online = Bukkit.getPlayer(username);
        if (online != null) {
          uuid = online.getUniqueId();
          username = online.getName();
        }
      }
    }

    return new WhitelistData(uuid, username, data.addedBy(), data.addedAt());
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.saveWhitelistState(enabled));
    } else {
      databaseManager.saveWhitelistState(enabled);
    }
  }

  public @Nullable WhitelistData findWhitelistEntry(@NotNull UUID uuid, @NotNull String username) {
    for (WhitelistData entry : whitelistEntries.values()) {
      if (entry.uuid() != null && entry.uuid().equals(uuid)) {
        return entry;
      }
      if (entry.username() != null && entry.username().equalsIgnoreCase(username)) {
        return entry;
      }
    }

    if (userService != null) {
      UserData ud = userService.getUser(uuid);
      if (ud != null && ud.username() != null) {
        for (WhitelistData entry : whitelistEntries.values()) {
          if (entry.username() != null && entry.username().equalsIgnoreCase(ud.username())) {
            return entry;
          }
        }
      }
    }

    return null;
  }

  public @Nullable WhitelistData findWhitelistEntry(@NotNull String query) {
    String clean = query.trim();
    if (clean.isEmpty()) return null;

    try {
      UUID uuid = UUID.fromString(clean);
      return findWhitelistEntry(uuid, "");
    } catch (IllegalArgumentException ignored) {
    }

    for (WhitelistData entry : whitelistEntries.values()) {
      if (entry.username() != null && entry.username().equalsIgnoreCase(clean)) {
        return entry;
      }
    }

    if (userService != null) {
      UserData ud = userService.getUserByUsername(clean);
      if (ud != null && ud.uuid() != null) {
        return findWhitelistEntry(ud.uuid(), clean);
      }
    }

    OfflinePlayer offline = Bukkit.getOfflinePlayer(clean);
    if (offline.hasPlayedBefore()) {
      return findWhitelistEntry(offline.getUniqueId(), clean);
    }

    return null;
  }

  public boolean isWhitelisted(@NotNull UUID uuid, @NotNull String username) {
    return findWhitelistEntry(uuid, username) != null;
  }

  public boolean isWhitelisted(@NotNull UUID uuid) {
    return findWhitelistEntry(uuid, "") != null;
  }

  public boolean isWhitelisted(@NotNull String query) {
    return findWhitelistEntry(query) != null;
  }

  public @NotNull WhitelistData addPlayer(@NotNull String target, @NotNull String addedBy) {
    UUID targetUuid = null;
    String targetName = null;
    String clean = target.trim();

    try {
      targetUuid = UUID.fromString(clean);
      if (userService != null) {
        UserData ud = userService.getUser(targetUuid);
        if (ud != null) {
          targetName = ud.username();
        }
      }
      if (targetName == null) {
        Player online = Bukkit.getPlayer(targetUuid);
        if (online != null) {
          targetName = online.getName();
        }
      }
      if (targetName == null) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetUuid);
        if (offline.getName() != null && !offline.getName().isBlank()) {
          targetName = offline.getName();
        }
      }
    } catch (IllegalArgumentException notUuid) {
      targetName = clean;
      Player online = Bukkit.getPlayer(targetName);
      if (online != null) {
        targetUuid = online.getUniqueId();
        targetName = online.getName();
      } else if (userService != null) {
        UserData ud = userService.getUserByUsername(targetName);
        if (ud != null) {
          targetUuid = ud.uuid();
          targetName = ud.username();
        }
      }
      if (targetUuid == null) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
        if (offline.hasPlayedBefore()) {
          targetUuid = offline.getUniqueId();
        }
      }
    }

    String normalizedAddedBy = addedBy;
    if (!addedBy.equalsIgnoreCase("CONSOLE")) {
      try {
        UUID.fromString(addedBy);
      } catch (IllegalArgumentException notUuid) {
        if (userService != null) {
          UUID u = userService.findUuidByUsername(addedBy);
          if (u != null) {
            normalizedAddedBy = u.toString();
          }
        }
        if (normalizedAddedBy.equals(addedBy)) {
          Player p = Bukkit.getPlayer(addedBy);
          if (p != null) {
            normalizedAddedBy = p.getUniqueId().toString();
          }
        }
      }
    }

    long now = System.currentTimeMillis();
    WhitelistData existing =
        targetUuid != null
            ? findWhitelistEntry(targetUuid, targetName != null ? targetName : "")
            : findWhitelistEntry(targetName != null ? targetName : clean);

    if (existing != null) {
      whitelistEntries.remove(getEntryKey(existing));
      if (targetUuid == null) targetUuid = existing.uuid();
      if (targetName == null) targetName = existing.username();
    }

    WhitelistData newEntry = new WhitelistData(targetUuid, targetName, normalizedAddedBy, now);
    whitelistEntries.put(getEntryKey(newEntry), newEntry);

    UUID finalUuid = targetUuid;
    String finalName = targetName;
    String finalAddedBy = normalizedAddedBy;
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin,
              () -> databaseManager.saveWhitelistPlayer(finalUuid, finalName, finalAddedBy, now));
    } else {
      databaseManager.saveWhitelistPlayer(finalUuid, finalName, finalAddedBy, now);
    }

    return newEntry;
  }

  public @Nullable WhitelistData removePlayer(@NotNull String target) {
    WhitelistData entry = findWhitelistEntry(target);
    if (entry == null) {
      return null;
    }

    whitelistEntries.remove(getEntryKey(entry));

    UUID uuid = entry.uuid();
    String name = entry.username();
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.removeWhitelistPlayer(uuid, name));
    } else {
      databaseManager.removeWhitelistPlayer(uuid, name);
    }

    return entry;
  }

  public void clearWhitelist() {
    whitelistEntries.clear();
    if (plugin != null) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, databaseManager::clearWhitelistPlayers);
    } else {
      databaseManager.clearWhitelistPlayers();
    }
  }

  public @NotNull Collection<WhitelistData> getWhitelistEntries() {
    return Collections.unmodifiableCollection(whitelistEntries.values());
  }

  public @NotNull Set<String> getWhitelistedPlayers() {
    Set<String> set = new HashSet<>();
    for (WhitelistData data : whitelistEntries.values()) {
      if (data.username() != null && !data.username().isBlank()) {
        set.add(data.username());
      } else if (data.uuid() != null) {
        set.add(data.uuid().toString());
      }
    }
    return Collections.unmodifiableSet(set);
  }

  public int getWhitelistCount() {
    return whitelistEntries.size();
  }

  public @NotNull Component createWhitelistScreen() {
    return Component.empty()
        .append(Component.text("Headquarters", NamedTextColor.BLUE, TextDecoration.BOLD))
        .append(Component.newline())
        .append(Component.newline())
        .append(
            Component.text("You are not whitelisted on this server.", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, false));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    if (!enabled) {
      return;
    }

    UUID uuid = event.getUniqueId();
    String username = event.getName();

    WhitelistData entry = findWhitelistEntry(uuid, username);
    if (entry != null) {
      // Sync missing uuid or username if it wasn't populated yet or changed
      boolean needsSync =
          entry.uuid() == null
              || entry.username() == null
              || !entry.username().equalsIgnoreCase(username);
      if (needsSync) {
        whitelistEntries.remove(getEntryKey(entry));
        WhitelistData updated = new WhitelistData(uuid, username, entry.addedBy(), entry.addedAt());
        whitelistEntries.put(getEntryKey(updated), updated);
        if (plugin != null) {
          Bukkit.getScheduler()
              .runTaskAsynchronously(
                  plugin,
                  () ->
                      databaseManager.saveWhitelistPlayer(
                          uuid, username, entry.addedBy(), entry.addedAt()));
        }
      }
      return;
    }

    // Check staff / bypass permissions via RankService
    if (rankService != null) {
      Rank rank = rankService.getPlayerRank(uuid);
      if (rankService.hasPermission(rank, "smessential.whitelist.bypass")) {
        return;
      }
    }

    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, createWhitelistScreen());
  }
}
