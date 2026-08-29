package dev.smnl.smessential.service;

import dev.smnl.smessential.database.DatabaseManager;
import dev.smnl.smessential.model.LeaderboardData;
import dev.smnl.smessential.model.StatisticType;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeaderboardService implements Listener {

  private final DatabaseManager databaseManager;
  private final StatisticService statisticService;
  private final UserService userService;
  private final RankService rankService;

  private final Map<String, LeaderboardData> leaderboards = new ConcurrentHashMap<>();
  private final Map<String, UUID> activeDisplays = new ConcurrentHashMap<>();
  private final Map<String, LeaderboardMapRenderer> activeRenderers = new ConcurrentHashMap<>();
  private final Map<String, MapView> activeMapViews = new ConcurrentHashMap<>();
  private final Map<String, BufferedImage> cachedTileImages = new ConcurrentHashMap<>();
  private final Map<String, List<Map.Entry<UUID, Long>>> lastStatsSnapshot =
      new ConcurrentHashMap<>();

  private JavaPlugin plugin;
  private NamespacedKey leaderboardKey;
  private NamespacedKey tileKey;
  private BukkitTask refreshTask;

  public LeaderboardService(
      @NotNull DatabaseManager databaseManager,
      @NotNull StatisticService statisticService,
      @NotNull UserService userService,
      @Nullable RankService rankService) {
    this.databaseManager = databaseManager;
    this.statisticService = statisticService;
    this.userService = userService;
    this.rankService = rankService;
  }

  public LeaderboardService(
      @NotNull DatabaseManager databaseManager,
      @NotNull StatisticService statisticService,
      @NotNull UserService userService) {
    this(databaseManager, statisticService, userService, null);
  }

  public void setup(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
    this.leaderboardKey = new NamespacedKey(plugin, "leaderboard_id");
    this.tileKey = new NamespacedKey(plugin, "leaderboard_tile");

    Map<String, LeaderboardData> loaded = databaseManager.loadAllLeaderboards();
    this.leaderboards.putAll(loaded);

    Bukkit.getPluginManager().registerEvents(this, plugin);

    if (rankService != null) {
      rankService.addGlobalUpdateListener(this::refreshAllDisplays);
    }

    // Clean up any legacy TextDisplays from previous versions in all loaded worlds
    for (World world : Bukkit.getWorlds()) {
      for (TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
        if (display.getPersistentDataContainer().has(leaderboardKey, PersistentDataType.STRING)) {
          display.remove();
        }
      }
    }

    // Initial spawn / find in loaded worlds
    Bukkit.getScheduler().runTask(plugin, this::refreshAllDisplays);

    // Periodic content check (every 10 seconds / 200 ticks) - only re-renders if stats actually
    // changed
    this.refreshTask =
        Bukkit.getScheduler().runTaskTimer(plugin, this::periodicContentCheck, 200L, 200L);
  }

  public void shutdown() {
    if (refreshTask != null) {
      refreshTask.cancel();
      refreshTask = null;
    }
  }

  public @NotNull Collection<LeaderboardData> getAllLeaderboards() {
    return Collections.unmodifiableCollection(leaderboards.values());
  }

  public @Nullable LeaderboardData getLeaderboard(@NotNull String id) {
    return leaderboards.get(id.toLowerCase(Locale.ROOT));
  }

  public void createOrUpdateLeaderboard(
      @NotNull String id,
      @NotNull StatisticType statType,
      @NotNull Location location,
      int limit,
      int width,
      int height) {
    String lowerId = id.toLowerCase(Locale.ROOT);
    String worldName = location.getWorld() != null ? location.getWorld().getName() : "world";
    int validW = Math.max(1, Math.min(5, width));
    int validH = Math.max(1, Math.min(5, height));

    LeaderboardData oldData = leaderboards.get(lowerId);

    LeaderboardData data =
        new LeaderboardData(
            lowerId,
            statType,
            worldName,
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch(),
            limit,
            validW,
            validH);

    leaderboards.put(lowerId, data);
    lastStatsSnapshot.remove(lowerId);

    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.saveLeaderboard(data));
      Bukkit.getScheduler()
          .runTask(
              plugin,
              () -> {
                if (oldData != null) {
                  removeDisplayEntity(lowerId, oldData);
                }
                spawnOrUpdateDisplay(data);
              });
    }
  }

  public void createOrUpdateLeaderboard(
      @NotNull String id, @NotNull StatisticType statType, @NotNull Location location, int limit) {
    createOrUpdateLeaderboard(id, statType, location, limit, 1, 1);
  }

  public boolean removeLeaderboard(@NotNull String id) {
    String lowerId = id.toLowerCase(Locale.ROOT);
    LeaderboardData removed = leaderboards.remove(lowerId);
    if (removed == null) {
      return false;
    }

    lastStatsSnapshot.remove(lowerId);
    cachedTileImages.keySet().removeIf(k -> k.startsWith(lowerId));

    if (plugin != null) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> databaseManager.deleteLeaderboard(lowerId));
      Bukkit.getScheduler().runTask(plugin, () -> removeDisplayEntity(lowerId, removed));
    }
    return true;
  }

  public void reload() {
    Map<String, LeaderboardData> loaded = databaseManager.loadAllLeaderboards();
    leaderboards.clear();
    leaderboards.putAll(loaded);
    lastStatsSnapshot.clear();
    cachedTileImages.clear();
    refreshAllDisplays();
  }

  public void refreshAllDisplays() {
    if (plugin != null && !Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, this::refreshAllDisplays);
      return;
    }
    for (LeaderboardData data : leaderboards.values()) {
      spawnOrUpdateDisplay(data);
    }
  }

  private void periodicContentCheck() {
    for (LeaderboardData data : leaderboards.values()) {
      updateContentIfChanged(data);
    }
  }

  public boolean updateContentIfChanged(@NotNull LeaderboardData data) {
    List<Map.Entry<UUID, Long>> currentStats =
        statisticService.getTopPlayers(data.statType(), data.limit());
    List<Map.Entry<UUID, Long>> previousStats = lastStatsSnapshot.get(data.id());

    if (previousStats != null && Objects.equals(currentStats, previousStats)) {
      return false;
    }

    lastStatsSnapshot.put(data.id(), currentStats);
    reRenderAndBroadcastTiles(data);
    return true;
  }

  private void reRenderAndBroadcastTiles(@NotNull LeaderboardData data) {
    int w = Math.max(1, data.width());
    int h = Math.max(1, data.height());
    BufferedImage fullImage = renderLeaderboardImage(data.id());

    for (int row = 0; row < h; row++) {
      for (int col = 0; col < w; col++) {
        String tilePos = col + "_" + row;
        String tileId = data.id() + "_" + tilePos;
        BufferedImage subImage = fullImage.getSubimage(col * 128, row * 128, 128, 128);
        cachedTileImages.put(tileId, subImage);

        LeaderboardMapRenderer renderer = activeRenderers.get(tileId);
        if (renderer != null) {
          renderer.updateImage(subImage);
        }
      }
    }
  }

  public @NotNull BufferedImage getOrRenderTileImage(@NotNull String tileId) {
    BufferedImage cached = cachedTileImages.get(tileId);
    if (cached != null) {
      return cached;
    }

    int firstUnderscore = tileId.indexOf('_');
    String leaderboardId = firstUnderscore > 0 ? tileId.substring(0, firstUnderscore) : tileId;
    LeaderboardData data = getLeaderboard(leaderboardId);
    if (data != null) {
      reRenderAndBroadcastTiles(data);
      BufferedImage freshlyRendered = cachedTileImages.get(tileId);
      if (freshlyRendered != null) {
        return freshlyRendered;
      }
    }

    BufferedImage fallback = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = fallback.createGraphics();
    g.setColor(new java.awt.Color(20, 22, 28));
    g.fillRect(0, 0, 128, 128);
    g.dispose();
    return fallback;
  }

  private void spawnOrUpdateDisplay(@NotNull LeaderboardData data) {
    if (plugin != null && !Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, () -> spawnOrUpdateDisplay(data));
      return;
    }

    Location loc = data.toLocation();
    if (loc == null || loc.getWorld() == null) {
      return;
    }

    World world = loc.getWorld();
    int chunkX = loc.getBlockX() >> 4;
    int chunkZ = loc.getBlockZ() >> 4;

    if (!world.isChunkLoaded(chunkX, chunkZ)) {
      return;
    }

    BlockFace facing = blockFaceFromYawPitch(data.yaw(), data.pitch());
    BlockFace rightFace = getViewerRightFace(facing);

    int w = Math.max(1, data.width());
    int h = Math.max(1, data.height());

    reRenderAndBroadcastTiles(data);

    for (int row = 0; row < h; row++) {
      for (int col = 0; col < w; col++) {
        String tilePos = col + "_" + row;
        String tileId = data.id() + "_" + tilePos;

        Location tileLoc =
            loc.clone().add(col * rightFace.getModX(), -row, col * rightFace.getModZ());
        tileLoc.setYaw(data.yaw());
        tileLoc.setPitch(data.pitch());

        int tileChunkX = tileLoc.getBlockX() >> 4;
        int tileChunkZ = tileLoc.getBlockZ() >> 4;
        if (!world.isChunkLoaded(tileChunkX, tileChunkZ)) {
          continue;
        }

        MapView mapView = getOrCreateMapView(world, data.id(), col, row);
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        if (meta != null) {
          meta.setMapView(mapView);
          meta.displayName(null);
          mapItem.setItemMeta(meta);
        }

        ItemFrame frame = findExistingFrame(world, data.id(), col, row, tileLoc);

        if (frame != null && frame.isValid()) {
          activeDisplays.put(tileId, frame.getUniqueId());
          frame.setFacingDirection(facing, true);
          frame.setVisible(false);
          frame.setFixed(true);
          frame.setInvulnerable(true);
          frame.setCustomNameVisible(false);
          frame.customName(null);
          frame.setItem(mapItem, false);
        } else {
          try {
            GlowItemFrame spawned =
                world.spawn(
                    tileLoc,
                    GlowItemFrame.class,
                    entity -> {
                      if (leaderboardKey != null) {
                        entity
                            .getPersistentDataContainer()
                            .set(leaderboardKey, PersistentDataType.STRING, data.id());
                      }
                      if (tileKey != null) {
                        entity
                            .getPersistentDataContainer()
                            .set(tileKey, PersistentDataType.STRING, tilePos);
                      }
                      entity.setFacingDirection(facing, true);
                      entity.setVisible(false);
                      entity.setFixed(true);
                      entity.setInvulnerable(true);
                      entity.setCustomNameVisible(false);
                      entity.customName(null);
                      entity.setItem(mapItem, false);
                    });
            activeDisplays.put(tileId, spawned.getUniqueId());
          } catch (Exception e) {
            ItemFrame spawned =
                world.spawn(
                    tileLoc,
                    ItemFrame.class,
                    entity -> {
                      if (leaderboardKey != null) {
                        entity
                            .getPersistentDataContainer()
                            .set(leaderboardKey, PersistentDataType.STRING, data.id());
                      }
                      if (tileKey != null) {
                        entity
                            .getPersistentDataContainer()
                            .set(tileKey, PersistentDataType.STRING, tilePos);
                      }
                      entity.setFacingDirection(facing, true);
                      entity.setVisible(false);
                      entity.setFixed(true);
                      entity.setInvulnerable(true);
                      entity.setCustomNameVisible(false);
                      entity.customName(null);
                      entity.setItem(mapItem, false);
                    });
            activeDisplays.put(tileId, spawned.getUniqueId());
          }
        }
      }
    }
  }

  private @NotNull MapView getOrCreateMapView(
      @NotNull World world, @NotNull String leaderboardId, int col, int row) {
    String tileId = leaderboardId + "_" + col + "_" + row;
    return activeMapViews.computeIfAbsent(
        tileId,
        id -> {
          MapView view = Bukkit.createMap(world);
          view.setTrackingPosition(false);
          view.setUnlimitedTracking(false);
          view.setLocked(true);
          for (MapRenderer r : view.getRenderers()) {
            view.removeRenderer(r);
          }
          LeaderboardMapRenderer renderer = new LeaderboardMapRenderer(id, this);
          view.addRenderer(renderer);
          activeRenderers.put(id, renderer);
          return view;
        });
  }

  private @Nullable ItemFrame findExistingFrame(
      @NotNull World world, @NotNull String id, int col, int row, @NotNull Location tileLoc) {
    String tilePos = col + "_" + row;
    String tileId = id + "_" + tilePos;
    UUID trackedUuid = activeDisplays.get(tileId);
    if (trackedUuid != null) {
      Entity entity = Bukkit.getEntity(trackedUuid);
      if (entity instanceof ItemFrame itemFrame && itemFrame.isValid()) {
        return itemFrame;
      }
    }

    if (leaderboardKey == null) return null;

    Location targetBlockLoc = tileLoc.getBlock().getLocation();

    for (ItemFrame entity : world.getEntitiesByClass(ItemFrame.class)) {
      String tag =
          entity.getPersistentDataContainer().get(leaderboardKey, PersistentDataType.STRING);
      if (!id.equalsIgnoreCase(tag)) {
        continue;
      }
      String tile =
          tileKey != null
              ? entity.getPersistentDataContainer().get(tileKey, PersistentDataType.STRING)
              : null;
      if (tilePos.equalsIgnoreCase(tile)
          || (tile == null
              && entity.getLocation().getBlock().getLocation().equals(targetBlockLoc))) {
        return entity;
      }
    }

    return null;
  }

  private void removeDisplayEntity(@NotNull String id, @Nullable LeaderboardData data) {
    if (plugin != null && !Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, () -> removeDisplayEntity(id, data));
      return;
    }

    activeDisplays
        .keySet()
        .removeIf(
            k -> {
              if (k.startsWith(id)) {
                UUID uuid = activeDisplays.get(k);
                if (uuid != null) {
                  Entity entity = Bukkit.getEntity(uuid);
                  if (entity != null) {
                    entity.remove();
                  }
                }
                return true;
              }
              return false;
            });

    activeRenderers.keySet().removeIf(k -> k.startsWith(id));
    activeMapViews.keySet().removeIf(k -> k.startsWith(id));
    cachedTileImages.keySet().removeIf(k -> k.startsWith(id));
    lastStatsSnapshot.remove(id);

    if (data != null && leaderboardKey != null) {
      World world = Bukkit.getWorld(data.worldName());
      if (world != null) {
        for (ItemFrame entity : world.getEntitiesByClass(ItemFrame.class)) {
          String tag =
              entity.getPersistentDataContainer().get(leaderboardKey, PersistentDataType.STRING);
          if (id.equalsIgnoreCase(tag)) {
            entity.remove();
          }
        }
      }
    }
  }

  public @NotNull BufferedImage renderLeaderboardImage(@NotNull String id) {
    LeaderboardData data = getLeaderboard(id);
    int width = data != null ? Math.max(1, data.width()) : 1;
    int height = data != null ? Math.max(1, data.height()) : 1;
    int totalW = width * 128;
    int totalH = height * 128;

    BufferedImage image = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();

    try {
      g.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Background - Dark obsidian / slate
      g.setColor(new java.awt.Color(20, 22, 28));
      g.fillRect(0, 0, totalW, totalH);

      // Subtle Slate Border
      g.setColor(new java.awt.Color(35, 38, 48));
      g.drawRect(0, 0, totalW - 1, totalH - 1);

      // Header Banner
      int headerH = (int) Math.min(64, Math.max(26, 24 + ((height - 1) * 14)));
      g.setColor(new java.awt.Color(28, 36, 52));
      g.fillRect(1, 1, totalW - 2, headerH);

      // Header Title: "LEADERBOARD"
      int titleFontSize = Math.min(22, Math.max(9, 8 + (width * 2) + (height * 2)));
      g.setFont(new Font("SansSerif", Font.BOLD, titleFontSize));
      g.setColor(new java.awt.Color(85, 170, 255)); // HQ Blue
      String title = "LEADERBOARD";
      FontMetrics fmTitle = g.getFontMetrics();
      int titleX = (totalW - fmTitle.stringWidth(title)) / 2;
      int titleY = (int) (headerH * 0.45);
      g.drawString(title, titleX, titleY);

      // Subtitle: "Top <limit> • <Stat>"
      String statName = data != null ? data.statType().getDisplayName() : "Stats";
      int limit = data != null ? data.limit() : 10;
      String subtitle = "Top " + limit + " • " + statName;
      int subFontSize = Math.min(15, Math.max(7, 6 + width + height));
      g.setFont(new Font("SansSerif", Font.BOLD, subFontSize));
      g.setColor(new java.awt.Color(255, 180, 50)); // Gold
      FontMetrics fmSub = g.getFontMetrics();
      int subX = (totalW - fmSub.stringWidth(subtitle)) / 2;
      int subY = (int) (headerH * 0.85);
      g.drawString(subtitle, subX, subY);

      // Header Divider Line
      g.setColor(new java.awt.Color(45, 50, 65));
      g.drawLine(1, headerH + 1, totalW - 2, headerH + 1);

      // Entries & Column layout - 1 column per 128px tile width
      List<Map.Entry<UUID, Long>> topPlayers =
          data != null
              ? statisticService.getTopPlayers(data.statType(), limit)
              : Collections.emptyList();

      int bodyY = headerH + 4;
      int bodyH = totalH - bodyY - 3;

      if (topPlayers.isEmpty()) {
        g.setFont(new Font("SansSerif", Font.ITALIC, Math.min(14, 8 + height)));
        g.setColor(new java.awt.Color(130, 135, 145));
        String emptyText = "No records yet";
        FontMetrics fmEmpty = g.getFontMetrics();
        int emptyX = (totalW - fmEmpty.stringWidth(emptyText)) / 2;
        g.drawString(emptyText, emptyX, bodyY + (bodyH / 2));
      } else {
        int count = Math.min(topPlayers.size(), limit);
        int cols = width;
        int colW = totalW / cols;
        int entriesPerCol = (int) Math.ceil((double) count / cols);
        int rowHeight = Math.max(8, Math.min(26, bodyH / Math.max(1, entriesPerCol)));
        int entryFontSize = Math.min(14, Math.max(7, (int) (rowHeight * 0.65)));
        g.setFont(new Font("SansSerif", Font.BOLD, entryFontSize));
        FontMetrics fmRow = g.getFontMetrics();

        for (int c = 0; c < cols; c++) {
          int colX = c * colW;

          for (int r = 0; r < entriesPerCol; r++) {
            int i = (c * entriesPerCol) + r;
            if (i >= count) {
              break;
            }

            Map.Entry<UUID, Long> entry = topPlayers.get(i);
            int rank = i + 1;

            int cellX = colX + 2;
            int cellW = colW - 4;
            int cellY = bodyY + (r * rowHeight);

            // Alternating row background
            java.awt.Color rowBg =
                switch (rank) {
                  case 1 -> new java.awt.Color(38, 36, 26);
                  case 2 -> new java.awt.Color(32, 34, 40);
                  case 3 -> new java.awt.Color(36, 30, 26);
                  default ->
                      (r % 2 == 0)
                          ? new java.awt.Color(22, 24, 30)
                          : new java.awt.Color(25, 27, 34);
                };
            g.setColor(rowBg);
            g.fillRect(cellX, cellY, cellW, rowHeight);

            int textBaseY = cellY + ((rowHeight + fmRow.getAscent() - fmRow.getDescent()) / 2);

            // Rank Badge
            java.awt.Color rankColor =
                switch (rank) {
                  case 1 -> new java.awt.Color(255, 215, 0); // Gold
                  case 2 -> new java.awt.Color(220, 220, 220); // Silver
                  case 3 -> new java.awt.Color(205, 130, 60); // Bronze
                  default -> new java.awt.Color(140, 145, 160); // Slate
                };
            g.setColor(rankColor);
            String rankStr = "#" + rank;
            g.drawString(rankStr, cellX + 3, textBaseY);

            // Value calculation to determine space for player name
            String formattedVal =
                data != null
                    ? data.statType().formatValue(entry.getValue())
                    : String.valueOf(entry.getValue());
            int valWidth = fmRow.stringWidth(formattedVal);

            // Player Name & Color
            PlayerDisplayInfo pInfo = resolvePlayerInfo(entry.getKey());
            g.setColor(pInfo.color());
            String name = pInfo.name();
            int nameStartX = cellX + 3 + fmRow.stringWidth(rankStr) + 6;
            int maxNameWidth = (cellX + cellW - 4 - valWidth) - nameStartX - 4;

            if (fmRow.stringWidth(name) > maxNameWidth && name.length() > 3) {
              while (name.length() > 3 && fmRow.stringWidth(name + "…") > maxNameWidth) {
                name = name.substring(0, name.length() - 1);
              }
              name = name + "…";
            }
            g.drawString(name, nameStartX, textBaseY);

            // Render Value right-aligned
            g.setColor(
                rank == 1 ? new java.awt.Color(255, 220, 100) : new java.awt.Color(255, 255, 255));
            g.drawString(formattedVal, cellX + cellW - 4 - valWidth, textBaseY);
          }

          // Column divider line between tiles
          if (c > 0) {
            g.setColor(new java.awt.Color(45, 50, 65));
            g.drawLine(colX, bodyY, colX, bodyY + bodyH);
          }
        }
      }
    } finally {
      g.dispose();
    }

    return image;
  }

  private record PlayerDisplayInfo(@NotNull String name, @NotNull java.awt.Color color) {}

  private @NotNull PlayerDisplayInfo resolvePlayerInfo(@NotNull UUID uuid) {
    String name = null;
    Player online = Bukkit.getPlayer(uuid);
    if (online != null) {
      name = online.getName();
    } else {
      var userData = userService.getUser(uuid);
      if (userData != null && userData.username() != null && !userData.username().isBlank()) {
        name = userData.username();
      }
    }

    if (name == null) {
      name = uuid.toString().substring(0, 8);
    }

    java.awt.Color color = new java.awt.Color(240, 240, 245);
    if (rankService != null) {
      dev.smnl.smessential.model.Rank rank = rankService.getPlayerRank(uuid);
      if (rank != null) {
        color = parseRankColor(rank.getColor(), color);
      }
    }

    return new PlayerDisplayInfo(name, color);
  }

  private @NotNull java.awt.Color parseRankColor(
      @Nullable String colorStr, @NotNull java.awt.Color fallback) {
    if (colorStr == null || colorStr.isBlank()) return fallback;
    if (colorStr.startsWith("#")) {
      try {
        return java.awt.Color.decode(colorStr);
      } catch (Exception ignored) {
        return fallback;
      }
    }
    return switch (colorStr.toLowerCase(Locale.ROOT)) {
      case "black" -> new java.awt.Color(0, 0, 0);
      case "dark_blue", "darkblue" -> new java.awt.Color(0, 0, 170);
      case "dark_green", "darkgreen" -> new java.awt.Color(0, 170, 0);
      case "dark_aqua", "darkaqua", "cyan" -> new java.awt.Color(0, 170, 170);
      case "dark_red", "darkred" -> new java.awt.Color(170, 0, 0);
      case "dark_purple", "darkpurple", "purple" -> new java.awt.Color(170, 0, 170);
      case "gold", "orange" -> new java.awt.Color(255, 170, 0);
      case "gray", "grey" -> new java.awt.Color(170, 170, 170);
      case "dark_gray", "darkgrey" -> new java.awt.Color(85, 85, 85);
      case "blue" -> new java.awt.Color(85, 85, 255);
      case "green" -> new java.awt.Color(85, 255, 85);
      case "aqua" -> new java.awt.Color(85, 255, 255);
      case "red" -> new java.awt.Color(255, 85, 85);
      case "light_purple", "pink" -> new java.awt.Color(255, 85, 255);
      case "yellow" -> new java.awt.Color(255, 255, 85);
      case "white" -> new java.awt.Color(255, 255, 255);
      default -> fallback;
    };
  }

  public static @NotNull BlockFace blockFaceFromYawPitch(float yaw, float pitch) {
    if (pitch >= 60.0f) {
      return BlockFace.DOWN;
    }
    if (pitch <= -60.0f) {
      return BlockFace.UP;
    }
    float rot = (yaw % 360.0f + 360.0f) % 360.0f;
    if (rot >= 45.0f && rot < 135.0f) {
      return BlockFace.WEST;
    } else if (rot >= 135.0f && rot < 225.0f) {
      return BlockFace.NORTH;
    } else if (rot >= 225.0f && rot < 315.0f) {
      return BlockFace.EAST;
    } else {
      return BlockFace.SOUTH;
    }
  }

  public static @NotNull BlockFace getViewerRightFace(@NotNull BlockFace frameFacing) {
    return switch (frameFacing) {
      case NORTH -> BlockFace.EAST;
      case SOUTH -> BlockFace.WEST;
      case WEST -> BlockFace.SOUTH;
      case EAST -> BlockFace.NORTH;
      default -> BlockFace.WEST;
    };
  }

  public static float yawFromBlockFace(@NotNull BlockFace face) {
    return switch (face) {
      case NORTH -> 180.0f;
      case SOUTH -> 0.0f;
      case WEST -> 90.0f;
      case EAST -> 270.0f;
      default -> 0.0f;
    };
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onHangingBreak(HangingBreakEvent event) {
    if (isLeaderboardEntity(event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    if (isLeaderboardEntity(event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEntityEvent event) {
    if (isLeaderboardEntity(event.getRightClicked())) {
      event.setCancelled(true);
    }
  }

  private boolean isLeaderboardEntity(@Nullable Entity entity) {
    if (entity == null || leaderboardKey == null) return false;
    return entity.getPersistentDataContainer().has(leaderboardKey, PersistentDataType.STRING);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onWorldLoad(WorldLoadEvent event) {
    for (LeaderboardData data : leaderboards.values()) {
      if (data.worldName().equalsIgnoreCase(event.getWorld().getName())) {
        spawnOrUpdateDisplay(data);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onChunkLoad(ChunkLoadEvent event) {
    for (LeaderboardData data : leaderboards.values()) {
      if (data.worldName().equalsIgnoreCase(event.getWorld().getName())) {
        int targetChunkX = (int) Math.floor(data.x()) >> 4;
        int targetChunkZ = (int) Math.floor(data.z()) >> 4;
        if (event.getChunk().getX() == targetChunkX && event.getChunk().getZ() == targetChunkZ) {
          spawnOrUpdateDisplay(data);
        }
      }
    }
  }
}
