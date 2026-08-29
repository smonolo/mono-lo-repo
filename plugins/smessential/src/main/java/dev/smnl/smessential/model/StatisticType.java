package dev.smnl.smessential.model;

import java.text.NumberFormat;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum StatisticType {
  PLAY_TIME(
      "play_time",
      "Play Time",
      "Total time spent on server",
      Statistic.PLAY_ONE_MINUTE,
      Material.CLOCK),
  DEATHS("deaths", "Deaths", "Total player deaths", Statistic.DEATHS, Material.SKELETON_SKULL),
  PLAYER_KILLS(
      "player_kills",
      "Player Kills",
      "Total players defeated in combat",
      Statistic.PLAYER_KILLS,
      Material.NETHERITE_SWORD),
  MOB_KILLS(
      "mob_kills",
      "Mob Kills",
      "Total monsters and mobs defeated",
      Statistic.MOB_KILLS,
      Material.DIAMOND_SWORD),
  DAMAGE_DEALT(
      "damage_dealt",
      "Damage Dealt",
      "Total damage inflicted",
      Statistic.DAMAGE_DEALT,
      Material.TARGET),
  DAMAGE_TAKEN(
      "damage_taken",
      "Damage Taken",
      "Total damage received",
      Statistic.DAMAGE_TAKEN,
      Material.SHIELD),
  TIME_SINCE_DEATH(
      "time_since_death",
      "Time Since Death",
      "Time survived since last death",
      Statistic.TIME_SINCE_DEATH,
      Material.TOTEM_OF_UNDYING),
  WALK_DISTANCE(
      "walk_distance",
      "Distance Walked",
      "Total distance walked",
      Statistic.WALK_ONE_CM,
      Material.LEATHER_BOOTS),
  SPRINT_DISTANCE(
      "sprint_distance",
      "Distance Sprinted",
      "Total distance sprinted",
      Statistic.SPRINT_ONE_CM,
      Material.DIAMOND_BOOTS),
  FLY_DISTANCE(
      "fly_distance",
      "Distance Flown",
      "Total distance flown",
      Statistic.FLY_ONE_CM,
      Material.ELYTRA),
  JUMPS("jumps", "Jumps", "Total jumps made", Statistic.JUMP, Material.RABBIT_FOOT),
  SLEEPS(
      "sleeps",
      "Times Slept",
      "Total times slept in a bed",
      Statistic.SLEEP_IN_BED,
      Material.RED_BED),
  CHESTS_OPENED(
      "chests_opened",
      "Chests Opened",
      "Total chests opened",
      Statistic.CHEST_OPENED,
      Material.CHEST),
  ITEMS_ENCHANTED(
      "items_enchanted",
      "Items Enchanted",
      "Total items enchanted",
      Statistic.ITEM_ENCHANTED,
      Material.ENCHANTING_TABLE),
  FISH_CAUGHT(
      "fish_caught",
      "Fish Caught",
      "Total fish and treasure caught",
      Statistic.FISH_CAUGHT,
      Material.FISHING_ROD),
  ANIMALS_BRED(
      "animals_bred", "Animals Bred", "Total animals bred", Statistic.ANIMALS_BRED, Material.LEAD),
  RAIDS_WON(
      "raids_won",
      "Raids Won",
      "Total raids successfully defended",
      Statistic.RAID_WIN,
      Material.OMINOUS_BOTTLE),
  TRADES(
      "trades",
      "Villager Trades",
      "Total trades completed with villagers",
      Statistic.TRADED_WITH_VILLAGER,
      Material.EMERALD);

  private final String key;
  private final String displayName;
  private final String description;
  private final Statistic bukkitStatistic;
  private final Material icon;

  StatisticType(
      @NotNull String key,
      @NotNull String displayName,
      @NotNull String description,
      @NotNull Statistic bukkitStatistic,
      @NotNull Material icon) {
    this.key = key;
    this.displayName = displayName;
    this.description = description;
    this.bukkitStatistic = bukkitStatistic;
    this.icon = icon;
  }

  public @NotNull String getKey() {
    return key;
  }

  public @NotNull String getDisplayName() {
    return displayName;
  }

  public @NotNull String getDescription() {
    return description;
  }

  public @NotNull Statistic getBukkitStatistic() {
    return bukkitStatistic;
  }

  public @NotNull Material getIcon() {
    return icon;
  }

  public @NotNull String formatValue(long value) {
    return switch (this) {
      case PLAY_TIME, TIME_SINCE_DEATH -> formatTime(value);
      case WALK_DISTANCE, SPRINT_DISTANCE, FLY_DISTANCE -> formatDistance(value);
      case DAMAGE_DEALT, DAMAGE_TAKEN -> formatDamage(value);
      default -> NumberFormat.getNumberInstance(Locale.US).format(value);
    };
  }

  private static String formatTime(long ticks) {
    long totalSecs = ticks / 20;
    long days = totalSecs / 86400;
    long hours = (totalSecs % 86400) / 3600;
    long mins = (totalSecs % 3600) / 60;
    long secs = totalSecs % 60;

    if (days > 0) {
      return String.format(Locale.US, "%dd %dh", days, hours);
    }
    if (hours > 0) {
      return String.format(Locale.US, "%dh %dm", hours, mins);
    }
    if (mins > 0) {
      return String.format(Locale.US, "%dm %ds", mins, secs);
    }
    return secs + "s";
  }

  private static String formatDistance(long cm) {
    double meters = cm / 100.0;
    if (meters >= 1000.0) {
      double km = meters / 1000.0;
      return String.format(Locale.US, "%.1f km", km);
    }
    return String.format(Locale.US, "%.0f m", meters);
  }

  private static String formatDamage(long tenthsHp) {
    double hp = tenthsHp / 10.0;
    return NumberFormat.getNumberInstance(Locale.US).format((long) hp) + " HP";
  }

  public static @Nullable StatisticType fromKey(@Nullable String key) {
    if (key == null || key.isBlank()) return null;
    String normalized = key.trim().toLowerCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
    for (StatisticType type : values()) {
      if (type.key.equalsIgnoreCase(normalized) || type.name().equalsIgnoreCase(normalized)) {
        return type;
      }
    }

    return switch (normalized) {
      case "playtime", "time_played", "played" -> PLAY_TIME;
      case "death" -> DEATHS;
      case "kills", "pkills", "pvp_kills" -> PLAYER_KILLS;
      case "mobkills", "pve_kills", "mobs" -> MOB_KILLS;
      case "damage", "dealt" -> DAMAGE_DEALT;
      case "taken" -> DAMAGE_TAKEN;
      case "walk", "walked" -> WALK_DISTANCE;
      case "sprint", "sprinted" -> SPRINT_DISTANCE;
      case "fly", "flown", "flight" -> FLY_DISTANCE;
      case "jump" -> JUMPS;
      case "sleep", "beds" -> SLEEPS;
      case "chests", "chest" -> CHESTS_OPENED;
      case "enchants", "enchanted" -> ITEMS_ENCHANTED;
      case "fish", "fished" -> FISH_CAUGHT;
      case "breed", "breeding" -> ANIMALS_BRED;
      case "raid", "raids" -> RAIDS_WON;
      case "trade", "villager_trades" -> TRADES;
      default -> null;
    };
  }
}
