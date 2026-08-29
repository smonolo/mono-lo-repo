package dev.smnl.smessential.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record LeaderboardData(
    @NotNull String id,
    @NotNull StatisticType statType,
    @NotNull String worldName,
    double x,
    double y,
    double z,
    float yaw,
    float pitch,
    int limit,
    int width,
    int height) {

  public LeaderboardData(
      @NotNull String id,
      @NotNull StatisticType statType,
      @NotNull String worldName,
      double x,
      double y,
      double z,
      float yaw,
      float pitch,
      int limit) {
    this(id, statType, worldName, x, y, z, yaw, pitch, limit, 1, 1);
  }

  public @Nullable Location toLocation() {
    World world = Bukkit.getWorld(worldName);
    if (world == null) return null;
    return new Location(world, x, y, z, yaw, pitch);
  }
}
