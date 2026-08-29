package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.model.Rank;
import dev.smnl.smessential.permission.RankPermissible;
import dev.smnl.smessential.util.MessageFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankService implements Listener {

  private final DatabaseManager databaseManager;
  private UserService userService;
  private final Map<String, Rank> ranks = new ConcurrentHashMap<>();
  private final Map<UUID, String> playerPrimaryRanks = new ConcurrentHashMap<>();
  private final Map<UUID, Set<String>> playerSecondaryRanks = new ConcurrentHashMap<>();
  private final Map<UUID, String> playerCustomDisplayRanks = new ConcurrentHashMap<>();
  private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

  // High-performance zero-GC in-memory caches
  private final Map<UUID, Rank> playerDisplayRankCache = new ConcurrentHashMap<>();
  private final Map<UUID, Set<String>> playerEffectivePermissionsCache = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> rankEffectivePermissionsCache = new ConcurrentHashMap<>();

  private final List<Consumer<Player>> playerUpdateListeners = new CopyOnWriteArrayList<>();
  private final List<Runnable> globalUpdateListeners = new CopyOnWriteArrayList<>();

  private JavaPlugin plugin;

  public RankService(@NotNull DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  public void setup(@NotNull JavaPlugin plugin, @Nullable UserService userService) {
    this.plugin = plugin;
    this.userService = userService;

    loadFromDatabase();

    Bukkit.getPluginManager().registerEvents(this, plugin);

    // Apply permissions for all current online players
    for (Player player : Bukkit.getOnlinePlayers()) {
      applyPermissions(player);
    }
  }

  public void setup(@NotNull JavaPlugin plugin) {
    setup(plugin, null);
  }

  public void setUserService(@NotNull UserService userService) {
    this.userService = userService;
  }

  public void reload() {
    invalidateCaches();
    loadFromDatabase();
    for (Player player : Bukkit.getOnlinePlayers()) {
      applyPermissions(player);
      notifyPlayerUpdate(player);
    }
    notifyGlobalUpdate();
  }

  private void loadFromDatabase() {
    // Load ranks from database
    Map<String, Rank> loadedRanks = databaseManager.loadAllRanks();

    // Load direct permissions
    Map<String, Set<String>> loadedPerms = databaseManager.loadAllRankPermissions();
    for (Map.Entry<String, Set<String>> entry : loadedPerms.entrySet()) {
      Rank rank = loadedRanks.get(entry.getKey());
      if (rank != null) {
        rank.setPermissions(entry.getValue());
      }
    }

    // Load inheritance (Primary ranks only)
    Map<String, Set<String>> loadedInheritance = databaseManager.loadAllRankInheritance();
    for (Map.Entry<String, Set<String>> entry : loadedInheritance.entrySet()) {
      Rank rank = loadedRanks.get(entry.getKey());
      if (rank != null && rank.isPrimary()) {
        Set<String> validParents = new HashSet<>();
        for (String parentId : entry.getValue()) {
          Rank parent = loadedRanks.get(parentId.toLowerCase());
          if (parent != null && parent.isPrimary()) {
            validParents.add(parentId.toLowerCase());
          }
        }
        rank.setParents(validParents);
      }
    }

    this.ranks.clear();
    this.ranks.putAll(loadedRanks);

    // Initial default ranks if none in DB
    if (this.ranks.isEmpty()) {
      createInitialDefaultRanks();
    }

    // Load player assigned ranks from unified user ranks table
    this.playerPrimaryRanks.clear();
    this.playerSecondaryRanks.clear();
    Map<UUID, Set<String>> allUserRanks = databaseManager.loadAllUserRanks();
    for (Map.Entry<UUID, Set<String>> entry : allUserRanks.entrySet()) {
      UUID uuid = entry.getKey();
      for (String rankId : entry.getValue()) {
        Rank r = ranks.get(rankId.toLowerCase());
        if (r != null) {
          if (r.isPrimary()) {
            playerPrimaryRanks.put(uuid, r.getId());
          } else {
            playerSecondaryRanks
                .computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet())
                .add(r.getId());
          }
        }
      }
    }

    // Load custom player display ranks
    this.playerCustomDisplayRanks.clear();
    Map<UUID, String> allUserDisplayRanks = databaseManager.loadAllUserDisplayRanks();
    for (Map.Entry<UUID, String> entry : allUserDisplayRanks.entrySet()) {
      Rank r = ranks.get(entry.getValue().toLowerCase());
      if (r != null) {
        this.playerCustomDisplayRanks.put(entry.getKey(), r.getId());
      }
    }
  }

  private void createInitialDefaultRanks() {
    Rank defaultRank =
        new Rank(
            "default",
            "Default",
            "gray",
            "",
            0,
            true,
            true,
            Set.of(
                "smessential.command.help",
                "smessential.command.msg",
                "smessential.command.ping",
                "smessential.command.support",
                "smessential.command.team",
                "smessential.command.list"));
    ranks.put(defaultRank.getId(), defaultRank);
    databaseManager.saveRank(defaultRank);
    for (String perm : defaultRank.getPermissions()) {
      databaseManager.addRankPermission(defaultRank.getId(), perm);
    }

    Rank staffRank =
        new Rank(
            "staff",
            "Staff",
            "blue",
            "[Staff]",
            50,
            false,
            true,
            Set.of("smessential.command.moderation", "smessential.staffchat"));
    ranks.put(staffRank.getId(), staffRank);
    databaseManager.saveRank(staffRank);
    for (String perm : staffRank.getPermissions()) {
      databaseManager.addRankPermission(staffRank.getId(), perm);
    }

    Rank adminRank =
        new Rank(
            "admin",
            "Admin",
            "red",
            "[Admin]",
            100,
            false,
            false, // Secondary rank
            Set.of("smessential.command.administration", "*"));
    ranks.put(adminRank.getId(), adminRank);
    databaseManager.saveRank(adminRank);
    for (String perm : adminRank.getPermissions()) {
      databaseManager.addRankPermission(adminRank.getId(), perm);
    }
  }

  public @NotNull Collection<Rank> getAllRanks() {
    List<Rank> sorted = new ArrayList<>(ranks.values());
    Collections.sort(sorted);
    return Collections.unmodifiableList(sorted);
  }

  public @NotNull List<Rank> getPrimaryRanks() {
    List<Rank> primary = new ArrayList<>();
    for (Rank r : ranks.values()) {
      if (r.isPrimary()) {
        primary.add(r);
      }
    }
    Collections.sort(primary);
    return Collections.unmodifiableList(primary);
  }

  public @NotNull List<Rank> getSecondaryRanks() {
    List<Rank> secondary = new ArrayList<>();
    for (Rank r : ranks.values()) {
      if (r.isSecondary()) {
        secondary.add(r);
      }
    }
    Collections.sort(secondary);
    return Collections.unmodifiableList(secondary);
  }

  public @Nullable Rank getRank(@NotNull String id) {
    return ranks.get(id.toLowerCase());
  }

  public @NotNull Rank getDefaultRank() {
    for (Rank rank : ranks.values()) {
      if (rank.isDefault() && rank.isPrimary()) {
        return rank;
      }
    }
    for (Rank rank : ranks.values()) {
      if (rank.isDefault()) {
        return rank;
      }
    }
    // Fallback: lowest weight primary rank
    return ranks.values().stream()
        .filter(Rank::isPrimary)
        .min((r1, r2) -> Integer.compare(r1.getWeight(), r2.getWeight()))
        .orElseGet(
            () ->
                new Rank(
                    "default",
                    "Default",
                    "gray",
                    "",
                    0,
                    true,
                    true,
                    Set.of("smessential.command.help", "smessential.command.list")));
  }

  public void setDefaultRank(@NotNull String rankId) {
    String lowerId = rankId.toLowerCase();
    for (Rank rank : ranks.values()) {
      boolean isDef = rank.getId().equals(lowerId);
      if (rank.isDefault() != isDef) {
        rank.setDefault(isDef);
        if (plugin != null) {
          Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> databaseManager.saveRank(rank));
        } else {
          databaseManager.saveRank(rank);
        }
      }
    }
    recalculateAllOnlinePlayers();
  }

  public @NotNull Rank getPrimaryRank(@NotNull UUID uuid) {
    String rankId = playerPrimaryRanks.get(uuid);
    if (rankId != null) {
      Rank rank = ranks.get(rankId);
      if (rank != null && rank.isPrimary()) {
        return rank;
      }
    }
    return getDefaultRank();
  }

  public @NotNull Set<Rank> getSecondaryRanks(@NotNull UUID uuid) {
    Set<String> secIds = playerSecondaryRanks.get(uuid);
    if (secIds == null || secIds.isEmpty()) {
      return Collections.emptySet();
    }
    Set<Rank> set = new HashSet<>();
    for (String id : secIds) {
      Rank r = ranks.get(id.toLowerCase());
      if (r != null && r.isSecondary()) {
        set.add(r);
      }
    }
    return Collections.unmodifiableSet(set);
  }

  public @NotNull List<Rank> getAllRanksForPlayer(@NotNull UUID uuid) {
    List<Rank> list = new ArrayList<>();
    list.add(getPrimaryRank(uuid));
    list.addAll(getSecondaryRanks(uuid));
    Collections.sort(list);
    return list;
  }

  public boolean hasRankAssigned(@NotNull UUID uuid, @NotNull String rankId) {
    String lowerId = rankId.toLowerCase();
    String primary = playerPrimaryRanks.get(uuid);
    if (primary != null && primary.equalsIgnoreCase(lowerId)) {
      return true;
    }
    Set<String> secondaries = playerSecondaryRanks.get(uuid);
    return secondaries != null && secondaries.contains(lowerId);
  }

  public @NotNull Rank getDisplayRank(@NotNull UUID uuid) {
    Rank cached = playerDisplayRankCache.get(uuid);
    if (cached != null) {
      return cached;
    }

    String customId = playerCustomDisplayRanks.get(uuid);
    if (customId != null) {
      Rank customRank = ranks.get(customId.toLowerCase());
      if (customRank != null && hasRankAssigned(uuid, customRank.getId())) {
        playerDisplayRankCache.put(uuid, customRank);
        return customRank;
      } else {
        // Player no longer has this rank or rank was deleted
        playerCustomDisplayRanks.remove(uuid);
        if (plugin != null) {
          Bukkit.getScheduler()
              .runTaskAsynchronously(plugin, () -> databaseManager.removePlayerDisplayRank(uuid));
        }
      }
    }

    Rank primary = getPrimaryRank(uuid);
    playerDisplayRankCache.put(uuid, primary);
    return primary;
  }

  public @Nullable String getCustomDisplayRankId(@NotNull UUID uuid) {
    String customId = playerCustomDisplayRanks.get(uuid);
    if (customId == null) return null;
    Rank custom = ranks.get(customId.toLowerCase());
    if (custom != null && hasRankAssigned(uuid, custom.getId())) {
      return custom.getId();
    }
    return null;
  }

  public boolean setPlayerDisplayRank(@NotNull UUID uuid, @NotNull String rankId) {
    String lowerId = rankId.toLowerCase();
    Rank targetRank = ranks.get(lowerId);
    if (targetRank == null) {
      return false;
    }
    if (!hasRankAssigned(uuid, lowerId)) {
      return false;
    }

    playerCustomDisplayRanks.put(uuid, lowerId);
    invalidatePlayerCache(uuid);

    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin, () -> databaseManager.savePlayerDisplayRank(uuid, lowerId));
    }

    Player online = Bukkit.getPlayer(uuid);
    if (online != null && online.isOnline()) {
      notifyPlayerUpdate(online);
    }
    notifyGlobalUpdate();
    return true;
  }

  public boolean resetPlayerDisplayRank(@NotNull UUID uuid) {
    String prev = playerCustomDisplayRanks.remove(uuid);
    invalidatePlayerCache(uuid);

    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.removePlayerDisplayRank(uuid));
    }

    Player online = Bukkit.getPlayer(uuid);
    if (online != null && online.isOnline()) {
      notifyPlayerUpdate(online);
    }
    notifyGlobalUpdate();
    return prev != null;
  }

  public @NotNull Rank getPlayerRank(@NotNull UUID uuid) {
    return getDisplayRank(uuid);
  }

  public @NotNull Rank getPlayerRank(@NotNull Player player) {
    return getDisplayRank(player.getUniqueId());
  }

  public @Nullable JavaPlugin getPlugin() {
    return plugin;
  }

  public boolean setPlayerRank(@NotNull UUID uuid, @NotNull Rank rank) {
    if (rank.isPrimary()) {
      return setPlayerPrimaryRank(uuid, rank.getId());
    } else {
      return addPlayerSecondaryRank(uuid, rank.getId());
    }
  }

  public boolean setPlayerRank(@NotNull UUID uuid, @NotNull String rankId) {
    Rank rank = getRank(rankId);
    if (rank == null) return false;
    return setPlayerRank(uuid, rank);
  }

  public boolean setPlayerPrimaryRank(@NotNull UUID uuid, @NotNull String rankId) {
    return setPlayerPrimaryRank(uuid, rankId, false);
  }

  public boolean setPlayerPrimaryRank(
      @NotNull UUID uuid, @NotNull String rankId, boolean clearSubRanks) {
    String lowerId = rankId.toLowerCase();
    Rank targetRank = ranks.get(lowerId);
    if (targetRank == null || !targetRank.isPrimary()) {
      return false;
    }

    String previousRankId = playerPrimaryRanks.get(uuid);
    boolean rankChanged = previousRankId == null || !previousRankId.equalsIgnoreCase(lowerId);

    playerPrimaryRanks.put(uuid, lowerId);

    Set<String> clearedSecs = null;
    if (clearSubRanks) {
      Set<String> oldSecs = playerSecondaryRanks.remove(uuid);
      if (oldSecs != null && !oldSecs.isEmpty()) {
        clearedSecs = new HashSet<>(oldSecs);
      }
    }

    // Validate custom display rank
    String customDisplay = playerCustomDisplayRanks.get(uuid);
    if (customDisplay != null && !hasRankAssigned(uuid, customDisplay)) {
      playerCustomDisplayRanks.remove(uuid);
      if (plugin != null) {
        Bukkit.getScheduler()
            .runTaskAsynchronously(plugin, () -> databaseManager.removePlayerDisplayRank(uuid));
      }
    }

    invalidatePlayerCache(uuid);

    final Set<String> finalCleared = clearedSecs;
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin,
              () -> {
                if (previousRankId != null && !previousRankId.equalsIgnoreCase(lowerId)) {
                  databaseManager.removePlayerRank(uuid, previousRankId);
                }
                databaseManager.addPlayerRank(uuid, lowerId);
                if (finalCleared != null) {
                  for (String secId : finalCleared) {
                    databaseManager.removePlayerRank(uuid, secId);
                  }
                }
              });
    }

    Player online = Bukkit.getPlayer(uuid);
    if (online != null && online.isOnline()) {
      applyPermissions(online);
      if (rankChanged || (finalCleared != null && !finalCleared.isEmpty())) {
        online.sendMessage(
            MessageFormatter.formatInfo("Administration", "Your permissions have been updated."));
      }
      notifyPlayerUpdate(online);
    }
    notifyGlobalUpdate();
    return true;
  }

  public boolean addPlayerSecondaryRank(@NotNull UUID uuid, @NotNull String rankId) {
    String lowerId = rankId.toLowerCase();
    Rank targetRank = ranks.get(lowerId);
    if (targetRank == null || !targetRank.isSecondary()) {
      return false;
    }

    Set<String> secSet =
        playerSecondaryRanks.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
    boolean added = secSet.add(lowerId);
    if (added) {
      invalidatePlayerCache(uuid);
      if (plugin != null) {
        Bukkit.getScheduler()
            .runTaskAsynchronously(plugin, () -> databaseManager.addPlayerRank(uuid, lowerId));
      }
      Player online = Bukkit.getPlayer(uuid);
      if (online != null && online.isOnline()) {
        applyPermissions(online);
        online.sendMessage(
            MessageFormatter.formatInfo("Administration", "Your permissions have been updated."));
        notifyPlayerUpdate(online);
      }
      notifyGlobalUpdate();
    }
    return added;
  }

  public boolean removePlayerSecondaryRank(@NotNull UUID uuid, @NotNull String rankId) {
    String lowerId = rankId.toLowerCase();
    Set<String> secSet = playerSecondaryRanks.get(uuid);
    if (secSet == null || !secSet.remove(lowerId)) {
      return false;
    }

    String customDisplay = playerCustomDisplayRanks.get(uuid);
    if (customDisplay != null && customDisplay.equalsIgnoreCase(lowerId)) {
      playerCustomDisplayRanks.remove(uuid);
      if (plugin != null) {
        Bukkit.getScheduler()
            .runTaskAsynchronously(plugin, () -> databaseManager.removePlayerDisplayRank(uuid));
      }
    }

    invalidatePlayerCache(uuid);
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.removePlayerRank(uuid, lowerId));
    }

    Player online = Bukkit.getPlayer(uuid);
    if (online != null && online.isOnline()) {
      applyPermissions(online);
      online.sendMessage(
          MessageFormatter.formatInfo("Administration", "Your permissions have been updated."));
      notifyPlayerUpdate(online);
    }
    notifyGlobalUpdate();
    return true;
  }

  public boolean clearPlayerSecondaryRanks(@NotNull UUID uuid) {
    Set<String> oldSecs = playerSecondaryRanks.remove(uuid);
    if (oldSecs == null || oldSecs.isEmpty()) {
      return false;
    }

    Set<String> toRemove = new HashSet<>(oldSecs);

    String customDisplay = playerCustomDisplayRanks.get(uuid);
    if (customDisplay != null && !hasRankAssigned(uuid, customDisplay)) {
      playerCustomDisplayRanks.remove(uuid);
      if (plugin != null) {
        Bukkit.getScheduler()
            .runTaskAsynchronously(plugin, () -> databaseManager.removePlayerDisplayRank(uuid));
      }
    }

    invalidatePlayerCache(uuid);

    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin,
              () -> {
                for (String secId : toRemove) {
                  databaseManager.removePlayerRank(uuid, secId);
                }
              });
    }

    Player online = Bukkit.getPlayer(uuid);
    if (online != null && online.isOnline()) {
      applyPermissions(online);
      online.sendMessage(
          MessageFormatter.formatInfo("Administration", "Your permissions have been updated."));
      notifyPlayerUpdate(online);
    }
    notifyGlobalUpdate();
    return true;
  }

  public void createRank(@NotNull Rank rank) {
    if (rank.isSecondary()) {
      rank.setParents(Collections.emptySet());
    }
    ranks.put(rank.getId(), rank);
    if (rank.isDefault()) {
      for (Rank r : ranks.values()) {
        if (!r.getId().equals(rank.getId()) && r.isDefault()) {
          r.setDefault(false);
          databaseManager.saveRank(r);
        }
      }
    }
    if (plugin != null) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> databaseManager.saveRank(rank));
    }
    notifyGlobalUpdate();
  }

  public boolean deleteRank(@NotNull String rankId) {
    String lowerId = rankId.toLowerCase();
    Rank removed = ranks.remove(lowerId);
    if (removed == null) {
      return false;
    }

    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.deleteRank(lowerId));
    }

    // Remove from custom display ranks
    playerCustomDisplayRanks.entrySet().removeIf(e -> e.getValue().equalsIgnoreCase(lowerId));

    // Re-evaluate affected primary players
    Rank defaultRank = getDefaultRank();
    for (Map.Entry<UUID, String> entry : playerPrimaryRanks.entrySet()) {
      if (entry.getValue().equalsIgnoreCase(lowerId)) {
        entry.setValue(defaultRank.getId());
        if (plugin != null) {
          Bukkit.getScheduler()
              .runTaskAsynchronously(
                  plugin,
                  () -> {
                    databaseManager.removePlayerRank(entry.getKey(), lowerId);
                    databaseManager.addPlayerRank(entry.getKey(), defaultRank.getId());
                  });
        }
      }
    }

    // Remove from all secondary sets
    for (Set<String> secSet : playerSecondaryRanks.values()) {
      secSet.remove(lowerId);
    }

    // Recalculate all players
    recalculateAllOnlinePlayers();
    return true;
  }

  public void saveRankChanges(@NotNull Rank rank) {
    if (rank.isSecondary()) {
      rank.setParents(Collections.emptySet());
    }
    ranks.put(rank.getId(), rank);
    if (plugin != null) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> databaseManager.saveRank(rank));
    }
    recalculateOnlinePlayersWithRank(rank.getId());
    notifyGlobalUpdate();
  }

  public void addPermissionToRank(@NotNull String rankId, @NotNull String permission) {
    Rank rank = ranks.get(rankId.toLowerCase());
    if (rank == null) return;
    rank.addPermission(permission);
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin, () -> databaseManager.addRankPermission(rank.getId(), permission));
    }
    recalculateOnlinePlayersWithRank(rank.getId());
  }

  public void removePermissionFromRank(@NotNull String rankId, @NotNull String permission) {
    Rank rank = ranks.get(rankId.toLowerCase());
    if (rank == null) return;
    rank.removePermission(permission);
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin, () -> databaseManager.removeRankPermission(rank.getId(), permission));
    }
    recalculateOnlinePlayersWithRank(rank.getId());
  }

  public boolean addParentToRank(@NotNull String rankId, @NotNull String parentId) {
    Rank rank = ranks.get(rankId.toLowerCase());
    Rank parent = ranks.get(parentId.toLowerCase());
    if (rank == null || parent == null) {
      return false;
    }
    // Only primary ranks can participate in inheritance
    if (!rank.isPrimary() || !parent.isPrimary()) {
      return false;
    }
    if (rank.getId().equalsIgnoreCase(parent.getId())) {
      return false;
    }
    if (wouldCauseCycle(rank.getId(), parent.getId())) {
      return false;
    }
    rank.addParent(parent.getId());
    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin, () -> databaseManager.addRankParent(rank.getId(), parent.getId()));
    }
    recalculateAllOnlinePlayers();
    return true;
  }

  public boolean removeParentFromRank(@NotNull String rankId, @NotNull String parentId) {
    Rank rank = ranks.get(rankId.toLowerCase());
    if (rank == null) {
      return false;
    }
    boolean removed = rank.removeParent(parentId.toLowerCase());
    if (removed && plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              plugin, () -> databaseManager.removeRankParent(rank.getId(), parentId.toLowerCase()));
    }
    recalculateAllOnlinePlayers();
    return removed;
  }

  public boolean wouldCauseCycle(@NotNull String rankId, @NotNull String targetParentId) {
    Set<String> visited = new HashSet<>();
    return isAncestorOf(targetParentId, rankId, visited);
  }

  private boolean isAncestorOf(
      @NotNull String currentId, @NotNull String targetId, @NotNull Set<String> visited) {
    if (currentId.equalsIgnoreCase(targetId)) return true;
    if (!visited.add(currentId.toLowerCase())) return false;
    Rank current = getRank(currentId);
    if (current == null || !current.isPrimary()) return false;
    for (String parentId : current.getParents()) {
      if (isAncestorOf(parentId, targetId, visited)) return true;
    }
    return false;
  }

  public void invalidateCaches() {
    playerDisplayRankCache.clear();
    playerEffectivePermissionsCache.clear();
    rankEffectivePermissionsCache.clear();
  }

  public void invalidatePlayerCache(@NotNull UUID uuid) {
    playerDisplayRankCache.remove(uuid);
    playerEffectivePermissionsCache.remove(uuid);
  }

  public @NotNull Set<String> getEffectivePermissions(@NotNull Rank rank) {
    if (rank.isSecondary()) {
      return rank.getPermissions();
    }
    Set<String> cached = rankEffectivePermissionsCache.get(rank.getId());
    if (cached != null) {
      return cached;
    }
    Set<String> effective = new HashSet<>();
    Set<String> visited = new HashSet<>();
    collectPermissionsRecursive(rank, effective, visited);
    Set<String> unmod = Collections.unmodifiableSet(effective);
    rankEffectivePermissionsCache.put(rank.getId(), unmod);
    return unmod;
  }

  private void collectPermissionsRecursive(
      @NotNull Rank current, @NotNull Set<String> accumulator, @NotNull Set<String> visited) {
    if (!visited.add(current.getId())) {
      return;
    }
    for (String parentId : current.getParents()) {
      Rank parentRank = getRank(parentId);
      if (parentRank != null && parentRank.isPrimary()) {
        collectPermissionsRecursive(parentRank, accumulator, visited);
      }
    }
    accumulator.addAll(current.getPermissions());
  }

  public @NotNull Set<String> getEffectivePermissionsForPlayer(@NotNull UUID uuid) {
    Set<String> cached = playerEffectivePermissionsCache.get(uuid);
    if (cached != null) {
      return cached;
    }
    Set<String> effective = new HashSet<>();
    Rank primary = getPrimaryRank(uuid);
    effective.addAll(getEffectivePermissions(primary));

    Set<String> secondaries = playerSecondaryRanks.get(uuid);
    if (secondaries != null && !secondaries.isEmpty()) {
      for (String secId : secondaries) {
        Rank secRank = ranks.get(secId);
        if (secRank != null) {
          effective.addAll(secRank.getPermissions());
        }
      }
    }
    Set<String> unmod = Collections.unmodifiableSet(effective);
    playerEffectivePermissionsCache.put(uuid, unmod);
    return unmod;
  }

  public boolean hasPermission(@NotNull Rank rank, @NotNull String permission) {
    if (permission == null || permission.isBlank()) return false;
    String lower = permission.trim().toLowerCase(Locale.ROOT);
    Set<String> perms = getEffectivePermissions(rank);
    return evaluatePermissionSet(perms, lower);
  }

  public boolean hasPermission(@NotNull Player player, @NotNull String permission) {
    if (permission == null || permission.isBlank()) return false;
    String lower = permission.trim().toLowerCase(Locale.ROOT);
    Set<String> perms = getEffectivePermissionsForPlayer(player.getUniqueId());
    return evaluatePermissionSet(perms, lower);
  }

  private boolean evaluatePermissionSet(@NotNull Set<String> perms, @NotNull String lower) {
    // 1. Explicit direct negation
    if (perms.contains("-" + lower)) {
      return false;
    }

    // 2. Wildcard negation check (e.g. -plugman.* or -smessential.command.*)
    for (String perm : perms) {
      String p = perm.trim().toLowerCase(Locale.ROOT);
      if (p.startsWith("-")) {
        String neg = p.substring(1).trim();
        if (neg.equals("*") || neg.equals("'*'") || neg.equals("\"*\"")) {
          return false;
        }
        if (neg.endsWith(".*")) {
          String base = neg.substring(0, neg.length() - 2);
          if (lower.startsWith(base + ".") || lower.equalsIgnoreCase(base)) {
            return false;
          }
        } else if (neg.endsWith("*")) {
          String base = neg.substring(0, neg.length() - 1);
          if (lower.startsWith(base)) {
            return false;
          }
        }
      }
    }

    // 3. Superuser wildcard
    if (perms.contains("*") || perms.contains("'*'") || perms.contains("\"*\"")) {
      return true;
    }

    // 4. Exact match
    if (perms.contains(lower)) {
      return true;
    }

    // 5. Wildcard match (e.g. plugman.*, smessential.*, smessential.command.*)
    for (String perm : perms) {
      String p = perm.trim().toLowerCase(Locale.ROOT);
      if (p.startsWith("-")) continue;
      if (p.endsWith(".*")) {
        String base = p.substring(0, p.length() - 2);
        if (lower.startsWith(base + ".") || lower.equalsIgnoreCase(base)) {
          return true;
        }
      } else if (p.endsWith("*")) {
        String base = p.substring(0, p.length() - 1);
        if (lower.startsWith(base)) {
          return true;
        }
      }
    }
    return false;
  }

  public void recalculateAllOnlinePlayers() {
    invalidateCaches();
    for (Player online : Bukkit.getOnlinePlayers()) {
      applyPermissions(online);
      notifyPlayerUpdate(online);
    }
    notifyGlobalUpdate();
  }

  public void recalculateOnlinePlayersWithRank(@NotNull String rankId) {
    recalculateAllOnlinePlayers();
  }

  public void applyPermissions(@NotNull Player player) {
    if (plugin == null) return;
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, () -> applyPermissions(player));
      return;
    }

    RankPermissible.inject(player, this);

    PermissionAttachment oldAttachment = attachments.remove(player.getUniqueId());
    if (oldAttachment != null) {
      try {
        player.removeAttachment(oldAttachment);
      } catch (Exception ignored) {
      }
    }

    PermissionAttachment attachment = player.addAttachment(plugin);
    attachments.put(player.getUniqueId(), attachment);

    Set<String> perms = getEffectivePermissionsForPlayer(player.getUniqueId());

    boolean isSuperuser = perms.contains("*") || perms.contains("'*'") || perms.contains("\"*\"");

    if (isSuperuser) {
      if (!player.isOp()) {
        try {
          player.setOp(true);
        } catch (Throwable ignored) {
        }
      }
      for (Permission permission : Bukkit.getPluginManager().getPermissions()) {
        attachment.setPermission(permission.getName(), true);
      }
      attachment.setPermission("*", true);
      try {
        player.sendOpLevel((byte) 4);
      } catch (Throwable ignored) {
      }
    } else {
      if (player.isOp()) {
        try {
          player.setOp(false);
        } catch (Throwable ignored) {
        }
      }
      try {
        player.sendOpLevel((byte) 0);
      } catch (Throwable ignored) {
      }

      // Gather all registered permissions and command permissions for wildcard matching
      Set<String> registered = new HashSet<>();
      for (Permission p : Bukkit.getPluginManager().getPermissions()) {
        registered.add(p.getName());
      }
      for (org.bukkit.plugin.Plugin p : Bukkit.getPluginManager().getPlugins()) {
        if (p.getDescription().getPermissions() != null) {
          for (Permission perm : p.getDescription().getPermissions()) {
            registered.add(perm.getName());
          }
        }
        Map<String, Map<String, Object>> cmds = p.getDescription().getCommands();
        if (cmds != null) {
          for (Map.Entry<String, Map<String, Object>> entry : cmds.entrySet()) {
            Object permObj = entry.getValue().get("permission");
            if (permObj instanceof String permStr && !permStr.isBlank()) {
              registered.add(permStr.trim());
            }
          }
        }
      }

      // Add common / well-known plugin permission nodes for PlugMan and vanilla commands
      registered.addAll(
          List.of(
              "plugman.admin",
              "plugman.help",
              "plugman.list",
              "plugman.info",
              "plugman.status",
              "plugman.usage",
              "plugman.lookup",
              "plugman.reload",
              "plugman.reload.all",
              "plugman.load",
              "plugman.unload",
              "plugman.restart",
              "plugman.restart.all",
              "plugman.check",
              "plugman.dump",
              "plugman.enable",
              "plugman.enable.all",
              "plugman.disable",
              "plugman.disable.all",
              "plugman.update",
              "plugman.update.all"));

      for (String cmd : VANILLA_MINECRAFT_COMMANDS) {
        registered.add("minecraft.command." + cmd);
      }

      for (String perm : perms) {
        String trimmed = perm.trim().toLowerCase(Locale.ROOT);
        boolean negate = trimmed.startsWith("-");
        String actualPerm = negate ? trimmed.substring(1).trim() : trimmed;

        // Set the permission itself in attachment
        attachment.setPermission(actualPerm, !negate);

        if (actualPerm.endsWith(".*")) {
          String base = actualPerm.substring(0, actualPerm.length() - 2);
          for (String regPerm : registered) {
            String regLower = regPerm.toLowerCase(Locale.ROOT);
            if (regLower.startsWith(base + ".") || regLower.equalsIgnoreCase(base)) {
              attachment.setPermission(regPerm, !negate);
            }
          }
        } else if (actualPerm.endsWith("*")) {
          String base = actualPerm.substring(0, actualPerm.length() - 1);
          for (String regPerm : registered) {
            String regLower = regPerm.toLowerCase(Locale.ROOT);
            if (regLower.startsWith(base)) {
              attachment.setPermission(regPerm, !negate);
            }
          }
        }
      }
    }

    player.recalculatePermissions();
    try {
      player.updateCommands();
    } catch (Throwable ignored) {
    }
  }

  public void addPlayerUpdateListener(@NotNull Consumer<Player> listener) {
    playerUpdateListeners.add(listener);
  }

  public void addGlobalUpdateListener(@NotNull Runnable listener) {
    globalUpdateListeners.add(listener);
  }

  private void notifyPlayerUpdate(@NotNull Player player) {
    if (plugin != null && !Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, () -> notifyPlayerUpdate(player));
      return;
    }
    for (Consumer<Player> listener : playerUpdateListeners) {
      try {
        listener.accept(player);
      } catch (Throwable ignored) {
      }
    }
  }

  private void notifyGlobalUpdate() {
    if (plugin != null && !Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, this::notifyGlobalUpdate);
      return;
    }
    for (Runnable listener : globalUpdateListeners) {
      try {
        listener.run();
      } catch (Throwable ignored) {
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (!playerPrimaryRanks.containsKey(player.getUniqueId())) {
      playerPrimaryRanks.put(player.getUniqueId(), getDefaultRank().getId());
      if (plugin != null) {
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                plugin,
                () ->
                    databaseManager.addPlayerRank(player.getUniqueId(), getDefaultRank().getId()));
      }
    }
    RankPermissible.inject(player, this);
    Set<String> perms = getEffectivePermissionsForPlayer(player.getUniqueId());
    boolean isSuperuser = perms.contains("*") || perms.contains("'*'") || perms.contains("\"*\"");
    if (!isSuperuser && player.isOp()) {
      try {
        player.setOp(false);
      } catch (Throwable ignored) {
      }
    }
    applyPermissions(player);
    notifyPlayerUpdate(player);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    invalidatePlayerCache(event.getPlayer().getUniqueId());
    RankPermissible.uninject(event.getPlayer());
    PermissionAttachment attachment = attachments.remove(event.getPlayer().getUniqueId());
    if (attachment != null) {
      try {
        event.getPlayer().removeAttachment(attachment);
      } catch (Exception ignored) {
      }
    }
  }

  private static final Set<String> VANILLA_MINECRAFT_COMMANDS =
      Set.of(
          "gamemode",
          "kill",
          "summon",
          "effect",
          "enchant",
          "clear",
          "experience",
          "xp",
          "difficulty",
          "gamerule",
          "op",
          "deop",
          "stop",
          "save-all",
          "save-on",
          "save-off",
          "worldborder",
          "datapack",
          "scoreboard",
          "tag",
          "title",
          "tellraw",
          "spectate",
          "spreadplayers",
          "setblock",
          "fill",
          "clone",
          "advancement",
          "bossbar",
          "attribute",
          "defaultgamemode",
          "seed",
          "trigger",
          "locate",
          "place",
          "damage",
          "ride",
          "return",
          "forceload",
          "particle",
          "playsound",
          "stopsound",
          "kick",
          "ban",
          "pardon");

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerCommandPreprocess(
      org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
    String msg = event.getMessage();
    if (!msg.startsWith("/")) return;

    String clean = msg.substring(1).trim();
    if (clean.isEmpty()) return;

    String[] parts = clean.split("\\s+");
    String root = parts[0].toLowerCase(Locale.ROOT);

    Player player = event.getPlayer();

    // Check namespace minecraft:
    if (root.startsWith("minecraft:")) {
      String subCmd = root.substring("minecraft:".length());
      String perm = "minecraft.command." + subCmd;
      if (!hasPermission(player, perm)) {
        event.setCancelled(true);
        player.sendMessage(
            net.kyori.adventure.text.Component.text(
                "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.",
                net.kyori.adventure.text.format.NamedTextColor.RED));
        return;
      }
    }

    // Check namespace bukkit:
    if (root.startsWith("bukkit:")) {
      String subCmd = root.substring("bukkit:".length());
      String perm = "bukkit.command." + subCmd;
      if (!hasPermission(player, perm)) {
        event.setCancelled(true);
        player.sendMessage(
            net.kyori.adventure.text.Component.text(
                "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.",
                net.kyori.adventure.text.format.NamedTextColor.RED));
        return;
      }
    }

    // Check vanilla command root
    if (VANILLA_MINECRAFT_COMMANDS.contains(root)) {
      String perm = "minecraft.command." + root;
      if (!hasPermission(player, perm)) {
        event.setCancelled(true);
        player.sendMessage(
            net.kyori.adventure.text.Component.text(
                "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.",
                net.kyori.adventure.text.format.NamedTextColor.RED));
      }
    }
  }
}
