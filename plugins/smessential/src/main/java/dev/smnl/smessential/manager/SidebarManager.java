package dev.smnl.smessential.manager;

import dev.smnl.smessential.model.Team;
import dev.smnl.smessential.service.TeamService;
import dev.smnl.smessential.util.FontUtils;
import dev.smnl.smessential.util.PlayerUtils;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SidebarManager implements Listener {

  private final JavaPlugin plugin;
  private TeamService teamService;
  private final Map<UUID, Scoreboard> playerBoards = new ConcurrentHashMap<>();
  private final Map<UUID, List<Component>> lastRenderedLines = new ConcurrentHashMap<>();
  private final Map<UUID, Integer> lastRenderedCount = new ConcurrentHashMap<>();

  private static final int MIN_SIDEBAR_PIXEL_WIDTH =
      FontUtils.getPixelWidth("XYZ: 99999, 99999, 9999") + 8;

  public SidebarManager(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void setTeamService(@Nullable TeamService teamService) {
    this.teamService = teamService;
  }

  public void setup() {
    Bukkit.getPluginManager().registerEvents(this, plugin);

    Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20L, 20L);

    updateAll();
  }

  public @NotNull Scoreboard getScoreboard(@NotNull Player player) {
    return playerBoards.computeIfAbsent(
        player.getUniqueId(), uuid -> Bukkit.getScoreboardManager().getNewScoreboard());
  }

  public @Nullable Scoreboard getScoreboard() {
    if (Bukkit.getOnlinePlayers().isEmpty()) {
      return null;
    }
    Player first = Bukkit.getOnlinePlayers().iterator().next();
    return getScoreboard(first);
  }

  public void updateAll() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      updateSidebar(player);
    }
  }

  public void updateSidebar(@NotNull Player player) {
    if (!player.isOnline()) {
      return;
    }

    Scoreboard board = getScoreboard(player);

    Component title = Component.text("Headquarters", NamedTextColor.BLUE, TextDecoration.BOLD);
    Objective objective = board.getObjective("hq_sidebar");
    if (objective == null) {
      objective = board.registerNewObjective("hq_sidebar", Criteria.DUMMY, title);
      objective.setDisplaySlot(DisplaySlot.SIDEBAR);
      objective.numberFormat(NumberFormat.blank());
    }

    String titleText = "Headquarters";
    int titleWidth = FontUtils.getPixelWidth(titleText, true);

    long day = (player.getWorld().getFullTime() / 24000L) + 1;
    long ticks = player.getWorld().getTime();
    long hours = ((ticks / 1000) + 6) % 24;
    long rawMinutes = (ticks % 1000) * 60 / 1000;
    long minutes = rawMinutes < 30 ? 0 : 30;
    String formattedTime = String.format("%02d:%02d", hours, minutes);

    boolean isMonsterSpawnTime =
        (ticks >= 13000 && ticks < 23000) || player.getWorld().isThundering();
    NamedTextColor timeColor = isMonsterSpawnTime ? NamedTextColor.RED : NamedTextColor.WHITE;

    org.bukkit.Location loc = player.getLocation();
    String xyz = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    String biomeKey = resolveBiomeKey(player);
    String biomeName = FontUtils.formatEnumTitleCase(biomeKey);
    NamedTextColor biomeColor = resolveBiomeColor(biomeKey);

    WeatherDisplay weather = resolveWeather(player);

    String rawFooter = "mc.smnl.dev";
    String timeLine = "Time: " + formattedTime + " (Day " + day + ")";
    String xyzLine = "XYZ: " + xyz;
    String biomeLine = "Biome: " + biomeName;
    String weatherLine = "Weather: " + weather.name();

    Team team = teamService != null ? teamService.getTeam(player.getUniqueId()) : null;
    List<String> dynamicStrings = new ArrayList<>();
    dynamicStrings.add(titleText);
    dynamicStrings.add(timeLine);
    dynamicStrings.add(xyzLine);
    dynamicStrings.add(biomeLine);
    dynamicStrings.add(weatherLine);

    List<Component> teamComponents = new ArrayList<>();
    if (team != null) {
      dynamicStrings.add("Team:");
      List<UUID> memberUuids = new ArrayList<>(team.getMembers());
      memberUuids.sort(
          (u1, u2) -> {
            if (u1.equals(u2)) return 0;
            if (u1.equals(player.getUniqueId())) return -1;
            if (u2.equals(player.getUniqueId())) return 1;
            Player p1 = Bukkit.getPlayer(u1);
            Player p2 = Bukkit.getPlayer(u2);
            String n1 = p1 != null ? p1.getName() : "";
            String n2 = p2 != null ? p2.getName() : "";
            int comp = n1.compareToIgnoreCase(n2);
            if (comp != 0) return comp;
            return u1.compareTo(u2);
          });

      for (UUID memberUuid : memberUuids) {
        Player member = Bukkit.getPlayer(memberUuid);
        if (member != null && member.isOnline()) {
          int hp = Math.max(0, (int) Math.ceil(member.getHealth()));
          String memberName = member.getName();
          dynamicStrings.add(memberName + " (" + hp + "HP)");
          Component coloredName = PlayerUtils.getPlayerGroupColoredName(member, memberName);
          teamComponents.add(
              coloredName
                  .append(Component.text(" (", NamedTextColor.GRAY))
                  .append(Component.text(hp + "HP", NamedTextColor.RED))
                  .append(Component.text(")", NamedTextColor.GRAY)));
        }
      }
    }

    dynamicStrings.add(rawFooter);

    int dynamicWidth =
        FontUtils.getMaxPixelWidth(titleWidth, dynamicStrings.toArray(new String[0]));
    int maxPixelWidth = Math.max(MIN_SIDEBAR_PIXEL_WIDTH, dynamicWidth);
    String centeredFooter = FontUtils.centerAndPadLine(rawFooter, maxPixelWidth);

    List<Component> lineList = new ArrayList<>();
    lineList.add(Component.empty());

    lineList.add(
        Component.text("Time: ", NamedTextColor.GRAY)
            .append(Component.text(formattedTime, timeColor))
            .append(Component.text(" (Day ", NamedTextColor.GRAY))
            .append(Component.text(String.valueOf(day), NamedTextColor.WHITE))
            .append(Component.text(")", NamedTextColor.GRAY)));
    lineList.add(
        Component.text("XYZ: ", NamedTextColor.GRAY)
            .append(Component.text(xyz, NamedTextColor.WHITE)));
    lineList.add(
        Component.text("Biome: ", NamedTextColor.GRAY)
            .append(Component.text(biomeName, biomeColor)));
    lineList.add(
        Component.text("Weather: ", NamedTextColor.GRAY)
            .append(Component.text(weather.name(), weather.color())));

    if (!teamComponents.isEmpty()) {
      lineList.add(Component.empty());
      lineList.add(Component.text("Team:", NamedTextColor.GRAY));
      lineList.addAll(teamComponents);
    }

    lineList.add(Component.empty());

    lineList.add(
        Component.text(centeredFooter, NamedTextColor.GOLD).decoration(TextDecoration.BOLD, false));

    List<Component> previousLines = lastRenderedLines.get(player.getUniqueId());
    int prevCount = lastRenderedCount.getOrDefault(player.getUniqueId(), 0);

    if (previousLines != null && previousLines.equals(lineList)) {
      if (player.getScoreboard() != board) {
        player.setScoreboard(board);
      }
      return;
    }

    int scoreValue = lineList.size();
    for (int i = 0; i < lineList.size(); i++) {
      Component line = lineList.get(i);
      String entryKey = "\u00a7" + Integer.toHexString(i);
      Score score = objective.getScore(entryKey);

      if (!score.isScoreSet() || score.getScore() != scoreValue) {
        score.setScore(scoreValue);
      }
      if (previousLines == null
          || i >= previousLines.size()
          || !line.equals(previousLines.get(i))) {
        score.customName(line);
      }
      scoreValue--;
    }

    for (int i = lineList.size(); i < prevCount; i++) {
      String entryKey = "\u00a7" + Integer.toHexString(i);
      board.resetScores(entryKey);
    }

    lastRenderedLines.put(player.getUniqueId(), lineList);
    lastRenderedCount.put(player.getUniqueId(), lineList.size());

    if (player.getScoreboard() != board) {
      player.setScoreboard(board);
    }
  }

  public static @NotNull String resolveBiomeKey(@NotNull Player player) {
    try {
      Object biome = player.getLocation().getBlock().getBiome();
      if (biome instanceof org.bukkit.Keyed keyed) {
        return keyed.getKey().getKey().toLowerCase();
      }
      if (biome instanceof Enum<?> enumBiome) {
        return enumBiome.name().toLowerCase();
      }
      try {
        java.lang.reflect.Method getKeyMethod = biome.getClass().getMethod("getKey");
        Object key = getKeyMethod.invoke(biome);
        if (key instanceof org.bukkit.NamespacedKey namespacedKey) {
          return namespacedKey.getKey().toLowerCase();
        }
      } catch (Throwable ignored) {
      }
      try {
        java.lang.reflect.Method nameMethod = biome.getClass().getMethod("name");
        Object name = nameMethod.invoke(biome);
        if (name instanceof String str) {
          return str.toLowerCase();
        }
      } catch (Throwable ignored) {
      }
      return biome.toString().toLowerCase();
    } catch (Throwable t) {
      return "plains";
    }
  }

  public static @NotNull NamedTextColor resolveBiomeColor(@NotNull String key) {
    return switch (key) {
      case "nether_wastes" -> NamedTextColor.RED;
      case "crimson_forest" -> NamedTextColor.DARK_RED;
      case "warped_forest" -> NamedTextColor.DARK_AQUA;
      case "soul_sand_valley" -> NamedTextColor.AQUA;
      case "basalt_deltas" -> NamedTextColor.DARK_GRAY;

      case "the_end", "end_highlands", "end_midlands", "small_end_islands", "end_barrens" ->
          NamedTextColor.DARK_PURPLE;

      case "snowy_plains",
              "ice_spikes",
              "snowy_taiga",
              "snowy_beach",
              "grove",
              "snowy_slopes",
              "jagged_peaks",
              "frozen_peaks",
              "frozen_river",
              "frozen_ocean",
              "deep_frozen_ocean" ->
          NamedTextColor.WHITE;

      case "ocean",
              "deep_ocean",
              "warm_ocean",
              "lukewarm_ocean",
              "deep_lukewarm_ocean",
              "cold_ocean",
              "deep_cold_ocean",
              "river" ->
          NamedTextColor.AQUA;

      case "cherry_grove", "mushroom_fields" -> NamedTextColor.LIGHT_PURPLE;

      case "deep_dark" -> NamedTextColor.DARK_AQUA;
      case "dripstone_caves" -> NamedTextColor.GRAY;
      case "lush_caves" -> NamedTextColor.GREEN;

      case "forest",
              "dark_forest",
              "birch_forest",
              "old_growth_birch_forest",
              "taiga",
              "old_growth_pine_taiga",
              "old_growth_spruce_taiga",
              "swamp",
              "mangrove_swamp" ->
          NamedTextColor.DARK_GREEN;

      case "jungle", "sparse_jungle", "bamboo_jungle", "flower_forest", "meadow", "plains" ->
          NamedTextColor.GREEN;

      case "desert", "beach", "sunflower_plains" -> NamedTextColor.YELLOW;

      case "savanna", "savanna_plateau", "windswept_savanna" -> NamedTextColor.GOLD;
      case "badlands", "eroded_badlands", "wooded_badlands" -> NamedTextColor.RED;

      case "windswept_hills",
              "windswept_gravelly_hills",
              "windswept_forest",
              "stony_peaks",
              "stony_shore" ->
          NamedTextColor.GRAY;

      default -> {
        if (key.contains("end")) yield NamedTextColor.DARK_PURPLE;
        if (key.contains("nether") || key.contains("crimson")) yield NamedTextColor.DARK_RED;
        if (key.contains("ocean") || key.contains("river") || key.contains("water"))
          yield NamedTextColor.AQUA;
        if (key.contains("snow") || key.contains("ice") || key.contains("frozen"))
          yield NamedTextColor.WHITE;
        if (key.contains("cherry")) yield NamedTextColor.LIGHT_PURPLE;
        if (key.contains("jungle") || key.contains("forest") || key.contains("swamp"))
          yield NamedTextColor.DARK_GREEN;
        if (key.contains("desert") || key.contains("sand") || key.contains("beach"))
          yield NamedTextColor.YELLOW;
        if (key.contains("savanna") || key.contains("badlands") || key.contains("mesa"))
          yield NamedTextColor.GOLD;
        if (key.contains("mountain") || key.contains("peak") || key.contains("hill"))
          yield NamedTextColor.GRAY;
        yield NamedTextColor.GOLD;
      }
    };
  }

  public record WeatherDisplay(@NotNull String name, @NotNull NamedTextColor color) {}

  public static @NotNull WeatherDisplay resolveWeather(@NotNull Player player) {
    World world = player.getWorld();
    if (world.getEnvironment() != World.Environment.NORMAL) {
      return new WeatherDisplay("Sunny", NamedTextColor.YELLOW);
    }

    boolean isThundering = world.isThundering();
    boolean hasStorm = world.hasStorm();

    if (!hasStorm && !isThundering) {
      return new WeatherDisplay("Sunny", NamedTextColor.YELLOW);
    }

    String biomeKey = resolveBiomeKey(player);
    if (biomeKey.contains("desert")
        || biomeKey.contains("savanna")
        || biomeKey.contains("badlands")
        || biomeKey.contains("mesa")) {
      return new WeatherDisplay("Sunny", NamedTextColor.YELLOW);
    }

    boolean isSnow =
        biomeKey.contains("snow")
            || biomeKey.contains("ice")
            || biomeKey.contains("frozen")
            || biomeKey.contains("grove")
            || biomeKey.contains("jagged_peaks")
            || biomeKey.contains("snowy_slopes");

    try {
      if (player.getLocation().getBlock().getTemperature() < 0.15) {
        isSnow = true;
      }
    } catch (Throwable ignored) {
    }

    if (isSnow) {
      if (isThundering) {
        return new WeatherDisplay("Blizzard", NamedTextColor.AQUA);
      }
      return new WeatherDisplay("Snow", NamedTextColor.WHITE);
    }

    if (isThundering) {
      return new WeatherDisplay("Thunder", NamedTextColor.RED);
    }
    return new WeatherDisplay("Rain", NamedTextColor.AQUA);
  }

  public static @NotNull String formatEnvironment(@NotNull World.Environment env) {
    return switch (env) {
      case NORMAL -> "Overworld";
      case NETHER -> "Nether";
      case THE_END -> "The End";
      case CUSTOM -> "Custom";
    };
  }

  public void applySidebar(Player player) {
    updateSidebar(player);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Bukkit.getScheduler().runTask(plugin, this::updateAll);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    playerBoards.remove(uuid);
    lastRenderedLines.remove(uuid);
    lastRenderedCount.remove(uuid);
    Bukkit.getScheduler().runTask(plugin, this::updateAll);
  }
}
