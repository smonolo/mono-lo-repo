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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatisticService {

  private UserService userService;

  public StatisticService() {}

  public StatisticService(@Nullable DatabaseManager databaseManager) {}

  public StatisticService(
      @Nullable DatabaseManager databaseManager, @Nullable UserService userService) {
    this.userService = userService;
  }

  public void setup(@NotNull JavaPlugin plugin) {
    // Native statistics: no database table setup or async DB sync required
  }

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

    Set<UUID> allUuids = new HashSet<>();
    if (userService != null) {
      allUuids.addAll(userService.getAllUsers().keySet());
    }
    for (Player online : Bukkit.getOnlinePlayers()) {
      allUuids.add(online.getUniqueId());
    }

    return allUuids.stream()
        .map(uuid -> Map.entry(uuid, getStatistic(uuid, type)))
        .filter(e -> e.getValue() > 0)
        .sorted(Map.Entry.<UUID, Long>comparingByValue(Comparator.reverseOrder()))
        .limit(limit)
        .toList();
  }
}
