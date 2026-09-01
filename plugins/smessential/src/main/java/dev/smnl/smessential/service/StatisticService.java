package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.model.StatisticType;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatisticService {

  private UserService userService;
  private final Map<StatisticType, CachedTopPlayers> topPlayersCache = new ConcurrentHashMap<>();

  private record CachedTopPlayers(long timestamp, List<Map.Entry<UUID, Long>> list) {}

  public StatisticService() {}

  public StatisticService(@Nullable DatabaseManager databaseManager) {}

  public StatisticService(
      @Nullable DatabaseManager databaseManager, @Nullable UserService userService) {
    this.userService = userService;
  }

  public void setup(@NotNull JavaPlugin plugin) {}

  public void setup(@NotNull JavaPlugin plugin, @NotNull UserService userService) {
    this.userService = userService;
  }

  public void setUserService(@NotNull UserService userService) {
    this.userService = userService;
  }

  public long getStatistic(@NotNull UUID uuid, @NotNull StatisticType type) {
    if (type.getBukkitStatistic() == null) {
      return 0L;
    }
    Player online = Bukkit.getPlayer(uuid);
    if (online != null) {
      return getStatistic(online, type);
    }
    OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
    return getStatistic(offline, type);
  }

  public long getStatistic(@NotNull OfflinePlayer player, @NotNull StatisticType type) {
    if (type.getBukkitStatistic() == null) {
      return 0L;
    }
    try {
      return player.getStatistic(type.getBukkitStatistic());
    } catch (IllegalArgumentException | NullPointerException e) {
      return 0L;
    }
  }

  public @NotNull Map<StatisticType, Long> getAllStatistics(@NotNull UUID uuid) {
    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
    return getAllStatistics(player);
  }

  public @NotNull Map<StatisticType, Long> getAllStatistics(@NotNull OfflinePlayer player) {
    Map<StatisticType, Long> map = new EnumMap<>(StatisticType.class);
    for (StatisticType type : StatisticType.values()) {
      map.put(type, getStatistic(player, type));
    }
    return Collections.unmodifiableMap(map);
  }

  public @NotNull List<Map.Entry<UUID, Long>> getTopPlayers(
      @NotNull StatisticType type, int limit) {
    if (type.getBukkitStatistic() == null) {
      return Collections.emptyList();
    }

    long now = System.currentTimeMillis();
    CachedTopPlayers cached = topPlayersCache.get(type);
    if (cached != null && (now - cached.timestamp()) < 15000L && cached.list().size() >= limit) {
      return cached.list().stream().limit(limit).toList();
    }

    Set<UUID> allUuids = new HashSet<>();
    if (userService != null) {
      allUuids.addAll(userService.getAllUsers().keySet());
    }
    for (Player online : Bukkit.getOnlinePlayers()) {
      allUuids.add(online.getUniqueId());
    }

    List<Map.Entry<UUID, Long>> fresh =
        allUuids.stream()
            .map(uuid -> Map.entry(uuid, getStatistic(uuid, type)))
            .filter(e -> e.getValue() > 0)
            .sorted(Map.Entry.<UUID, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(Math.max(limit, 25))
            .toList();

    topPlayersCache.put(type, new CachedTopPlayers(now, fresh));
    return fresh.stream().limit(limit).toList();
  }

  private record CachedAggregates(long timestamp, Map<StatisticType, Long> totals) {}

  private volatile CachedAggregates cachedAggregates;

  public @NotNull Map<StatisticType, Long> getGlobalAggregates() {
    long now = System.currentTimeMillis();
    CachedAggregates cached = cachedAggregates;
    if (cached != null && (now - cached.timestamp()) < 60000L) {
      return cached.totals();
    }

    Set<UUID> allUuids = new HashSet<>();
    if (userService != null) {
      allUuids.addAll(userService.getAllUsers().keySet());
    }
    for (Player online : Bukkit.getOnlinePlayers()) {
      allUuids.add(online.getUniqueId());
    }

    Map<StatisticType, Long> totals = new EnumMap<>(StatisticType.class);
    for (StatisticType type : StatisticType.values()) {
      totals.put(type, 0L);
    }

    for (UUID uuid : allUuids) {
      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      for (StatisticType type : StatisticType.values()) {
        long val = getStatistic(player, type);
        if (val > 0) {
          totals.put(type, totals.get(type) + val);
        }
      }
    }

    Map<StatisticType, Long> unmodifiable = Collections.unmodifiableMap(totals);
    cachedAggregates = new CachedAggregates(now, unmodifiable);
    return unmodifiable;
  }
}
