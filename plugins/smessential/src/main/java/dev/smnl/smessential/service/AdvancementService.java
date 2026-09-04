package dev.smnl.smessential.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.smnl.smessential.SMEssential;
import dev.smnl.smessential.util.ComponentUtils;
import io.papermc.paper.advancement.AdvancementDisplay;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementService {

  private static final Gson GSON = new Gson();
  private static final DateTimeFormatter FORMATTER_SPACE =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
  private static final DateTimeFormatter FORMATTER_ISO = DateTimeFormatter.ISO_DATE_TIME;

  private final SMEssential plugin;
  private final UserService userService;

  private final List<AdvancementRecord> registeredAdvancements = new ArrayList<>();
  private final Map<String, AdvancementRecord> advancementsById = new LinkedHashMap<>();
  private final Map<String, AdvancementRecord> advancementsByNormalizedKey = new HashMap<>();
  private final List<String> categories = new ArrayList<>();

  // Live in-memory completion tracking per player (UUID -> (normalizedAdvancementId -> timestampMillis))
  private final Map<UUID, Map<String, Long>> liveCompletions = new ConcurrentHashMap<>();

  // Per-player cached achievements JSON
  private final Map<UUID, CachedPlayerPayload> playerCache = new ConcurrentHashMap<>();

  // Global cached achievements JSON
  private volatile CachedGlobalPayload globalCache;

  // 5-minute cache for advancement folders found on disk
  private volatile CachedFolders cachedAdvancementFolders;

  // File parse cache to prevent re-reading and re-parsing unchanged JSON files on disk
  private final Map<String, CachedAdvancementFile> advancementFileCache = new ConcurrentHashMap<>();

  private record CachedFolders(long timestamp, Set<File> folders) {}

  private record CachedAdvancementFile(long lastModified, Map<String, Long> completedMap) {}

  private record CachedPlayerPayload(long timestamp, JsonObject payload) {}

  private record CachedGlobalPayload(long timestamp, JsonObject payload) {}

  public record AdvancementRecord(
      String id,
      String title,
      String description,
      String frame,
      String icon,
      String category,
      String categoryName,
      String parent,
      int criteriaCount,
      boolean announceToChat,
      boolean hidden,
      int sortOrder) {}

  public AdvancementService(@NotNull SMEssential plugin, @Nullable UserService userService) {
    this.plugin = plugin;
    this.userService = userService;
    // Pre-populate with standard Minecraft catalog so data is NEVER empty
    loadBuiltInCatalog();
  }

  public void setup() {
    // Schedule load on main thread after server starts up
    Bukkit.getScheduler().runTask(plugin, () -> {
      loadAdvancementsSync();
      syncAllOnlinePlayers();
    });

    Set<File> folders = getAllAdvancementFolders();
    plugin.getLogger().info(
        "AdvancementService initialized with "
            + registeredAdvancements.size()
            + " achievements catalog and "
            + folders.size()
            + " advancement folder(s) found: "
            + folders);
  }

  public void syncAllOnlinePlayers() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      syncPlayerAdvancements(player);
    }
  }

  /**
   * Loads or enriches registered advancements from Bukkit's live iterator on the main thread.
   */
  public synchronized void loadAdvancementsSync() {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, this::loadAdvancementsSync);
      return;
    }

    try {
      Iterator<Advancement> iterator = Bukkit.advancementIterator();
      if (!iterator.hasNext()) {
        return;
      }

      while (iterator.hasNext()) {
        try {
          Advancement adv = iterator.next();
          AdvancementDisplay display = adv.getDisplay();
          if (display == null) {
            continue;
          }

          String id = adv.getKey().toString();
          String title = ComponentUtils.toPlainText(display.title());
          String description = ComponentUtils.toPlainText(display.description());

          // If Adventure serialized raw translatable key, resolve from our built-in catalog
          AdvancementRecord existing = findRecord(id);
          if (title.startsWith("advancements.") || title.isBlank()) {
            if (existing != null) {
              title = existing.title();
            } else {
              title = formatFallbackTitle(adv.getKey().getKey());
            }
          }
          if (description.startsWith("advancements.") || description.isBlank()) {
            if (existing != null) {
              description = existing.description();
            } else {
              description = "";
            }
          }

          String frame = display.frame() != null ? display.frame().name() : (existing != null ? existing.frame() : "TASK");

          String icon = existing != null ? existing.icon() : "stone";
          try {
            if (display.icon() != null && display.icon().getType() != null) {
              icon = display.icon().getType().getKey().getKey().toLowerCase(Locale.ROOT);
            }
          } catch (Throwable ignored) {
          }

          String keyPath = adv.getKey().getKey();
          String category = keyPath.contains("/") ? keyPath.split("/")[0] : adv.getKey().getNamespace();
          category = category.toLowerCase(Locale.ROOT);

          String categoryName = formatCategoryName(category);
          String parent = adv.getParent() != null ? adv.getParent().getKey().toString() : (existing != null ? existing.parent() : null);
          int criteriaCount = adv.getCriteria() != null ? adv.getCriteria().size() : (existing != null ? existing.criteriaCount() : 1);
          boolean announce = display.doesAnnounceToChat();
          boolean hidden = display.isHidden();
          int sortOrder = calculateCategoryWeight(category);

          AdvancementRecord record =
              new AdvancementRecord(
                  id,
                  title,
                  description,
                  frame,
                  icon,
                  category,
                  categoryName,
                  parent,
                  criteriaCount,
                  announce,
                  hidden,
                  sortOrder);

          registerRecord(record);
        } catch (Throwable t) {
          plugin.getLogger().warning("Error parsing Bukkit advancement: " + t.getMessage());
        }
      }

      rebuildCategoriesAndSorting();
      playerCache.clear();
      advancementFileCache.clear();
      cachedAdvancementFolders = null;
      globalCache = null;
      plugin.getLogger().info("AdvancementService refreshed with " + registeredAdvancements.size() + " total achievements.");
    } catch (Throwable e) {
      plugin.getLogger().warning("Failed iterating Bukkit advancements: " + e.getMessage());
    }
  }

  private void registerRecord(AdvancementRecord record) {
    advancementsById.put(record.id(), record);
    advancementsByNormalizedKey.put(normalizeKey(record.id()), record);
    advancementsByNormalizedKey.put(stripNamespace(record.id()), record);
  }

  private synchronized void rebuildCategoriesAndSorting() {
    List<AdvancementRecord> list = new ArrayList<>(advancementsById.values());
    list.sort(
        Comparator.comparingInt(AdvancementRecord::sortOrder)
            .thenComparing(r -> r.parent() == null ? 0 : 1)
            .thenComparing(AdvancementRecord::category)
            .thenComparing(AdvancementRecord::title));

    registeredAdvancements.clear();
    registeredAdvancements.addAll(list);

    Set<String> catSet = new HashSet<>();
    for (AdvancementRecord r : registeredAdvancements) {
      catSet.add(r.category());
    }
    List<String> sortedCategories = new ArrayList<>(catSet);
    sortedCategories.sort(Comparator.comparingInt(this::calculateCategoryWeight));
    categories.clear();
    categories.addAll(sortedCategories);
  }

  public @Nullable AdvancementRecord findRecord(@Nullable String key) {
    if (key == null || key.isBlank()) return null;
    AdvancementRecord rec = advancementsById.get(key);
    if (rec != null) return rec;
    rec = advancementsByNormalizedKey.get(normalizeKey(key));
    if (rec != null) return rec;
    return advancementsByNormalizedKey.get(stripNamespace(key));
  }

  public static String normalizeKey(String key) {
    if (key == null) return "";
    String normalized = key.trim().toLowerCase(Locale.ROOT);
    if (!normalized.contains(":")) {
      normalized = "minecraft:" + normalized;
    }
    return normalized;
  }

  public static String stripNamespace(String key) {
    if (key == null) return "";
    int colonIndex = key.indexOf(':');
    return (colonIndex != -1 ? key.substring(colonIndex + 1) : key).trim().toLowerCase(Locale.ROOT);
  }

  private int calculateCategoryWeight(String category) {
    return switch (category.toLowerCase(Locale.ROOT)) {
      case "story" -> 1;
      case "nether" -> 2;
      case "end" -> 3;
      case "adventure" -> 4;
      case "husbandry" -> 5;
      default -> 10;
    };
  }

  private String formatCategoryName(String category) {
    return switch (category.toLowerCase(Locale.ROOT)) {
      case "story" -> "Story";
      case "nether" -> "Nether";
      case "end" -> "The End";
      case "adventure" -> "Adventure";
      case "husbandry" -> "Husbandry";
      default -> {
        if (category.isEmpty()) yield "Other";
        yield Character.toUpperCase(category.charAt(0))
            + category.substring(1).replace('_', ' ');
      }
    };
  }

  private String formatFallbackTitle(String path) {
    String name = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
    String[] parts = name.split("_");
    StringBuilder sb = new StringBuilder();
    for (String p : parts) {
      if (!p.isEmpty()) {
        sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
      }
    }
    return sb.toString().trim();
  }

  public List<AdvancementRecord> getRegisteredAdvancements() {
    return Collections.unmodifiableList(registeredAdvancements);
  }

  public List<String> getCategories() {
    return Collections.unmodifiableList(categories);
  }

  public void recordCompletion(@NotNull UUID uuid, @NotNull String advancementId, long timestamp) {
    Map<String, Long> map = liveCompletions.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
    map.put(advancementId, timestamp);
    map.put(normalizeKey(advancementId), timestamp);
    map.put(stripNamespace(advancementId), timestamp);
    playerCache.remove(uuid);
    globalCache = null;
  }

  public void syncPlayerAdvancements(@NotNull Player player) {
    if (player == null || !player.isOnline()) return;
    UUID uuid = player.getUniqueId();
    Map<String, Long> map = liveCompletions.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());

    for (AdvancementRecord rec : getRegisteredAdvancements()) {
      try {
        NamespacedKey namespacedKey = NamespacedKey.fromString(rec.id());
        if (namespacedKey == null) continue;
        Advancement adv = Bukkit.getAdvancement(namespacedKey);
        if (adv == null) continue;

        AdvancementProgress progress = player.getAdvancementProgress(adv);
        if (progress.isDone()) {
          long dateAwarded = 0L;
          for (String crit : progress.getAwardedCriteria()) {
            java.util.Date d = progress.getDateAwarded(crit);
            if (d != null && d.getTime() > dateAwarded) {
              dateAwarded = d.getTime();
            }
          }
          if (dateAwarded <= 0L) {
            dateAwarded = System.currentTimeMillis();
          }
          map.put(rec.id(), dateAwarded);
          map.put(normalizeKey(rec.id()), dateAwarded);
          map.put(stripNamespace(rec.id()), dateAwarded);
        }
      } catch (Throwable ignored) {
      }
    }
    playerCache.remove(uuid);
    globalCache = null;
  }

  /**
   * Recursively finds all directories named 'advancements' across the entire server.
   * Result is cached for 5 minutes to prevent continuous disk scanning.
   */
  public Set<File> getAllAdvancementFolders() {
    long now = System.currentTimeMillis();
    CachedFolders cached = cachedAdvancementFolders;
    if (cached != null && (now - cached.timestamp()) < 300_000L) {
      return cached.folders();
    }

    Set<File> result = new HashSet<>();
    Set<String> visitedPaths = new HashSet<>();
    List<File> baseRoots = new ArrayList<>();

    // 1. All loaded worlds
    for (World world : Bukkit.getWorlds()) {
      if (world.getWorldFolder() != null) {
        baseRoots.add(world.getWorldFolder());
        baseRoots.add(world.getWorldFolder().getAbsoluteFile());
      }
    }

    // 2. World container
    try {
      File container = Bukkit.getWorldContainer();
      if (container != null) {
        baseRoots.add(container);
        baseRoots.add(container.getAbsoluteFile());
      }
    } catch (Throwable ignored) {
    }

    // 3. Server root
    try {
      File pluginDir = plugin.getDataFolder().getAbsoluteFile();
      File pluginsDir = pluginDir.getParentFile();
      if (pluginsDir != null) {
        File serverRoot = pluginsDir.getParentFile();
        if (serverRoot != null) {
          baseRoots.add(serverRoot);
        }
      }
    } catch (Throwable ignored) {
    }

    baseRoots.add(new File("."));
    String[] commonDirs = {"world", "world_nether", "world_the_end", "survival", "main"};
    for (String cd : commonDirs) {
      baseRoots.add(new File(cd));
      baseRoots.add(new File(".", cd));
    }

    for (File root : baseRoots) {
      if (root == null || !root.exists()) continue;
      scanForAdvancementFolders(root, 0, 3, result, visitedPaths);
    }

    Set<File> unmodifiable = Collections.unmodifiableSet(result);
    cachedAdvancementFolders = new CachedFolders(now, unmodifiable);
    return unmodifiable;
  }

  private void scanForAdvancementFolders(File dir, int currentDepth, int maxDepth, Set<File> result, Set<String> visitedPaths) {
    if (dir == null || !dir.isDirectory()) return;

    if ("advancements".equalsIgnoreCase(dir.getName())) {
      try {
        String canon = dir.getCanonicalPath();
        if (visitedPaths.add(canon)) {
          result.add(dir.getCanonicalFile());
        }
      } catch (Exception e) {
        if (visitedPaths.add(dir.getAbsolutePath())) {
          result.add(dir.getAbsoluteFile());
        }
      }
      return;
    }

    File advFolder = new File(dir, "advancements");
    if (advFolder.exists() && advFolder.isDirectory()) {
      try {
        String canon = advFolder.getCanonicalPath();
        if (visitedPaths.add(canon)) {
          result.add(advFolder.getCanonicalFile());
        }
      } catch (Exception e) {
        if (visitedPaths.add(advFolder.getAbsolutePath())) {
          result.add(advFolder.getAbsoluteFile());
        }
      }
    }

    if (currentDepth < maxDepth) {
      File[] subdirs = dir.listFiles(File::isDirectory);
      if (subdirs != null) {
        for (File sub : subdirs) {
          String name = sub.getName();
          if ("plugins".equalsIgnoreCase(name) || "libraries".equalsIgnoreCase(name) || "cache".equalsIgnoreCase(name) || "logs".equalsIgnoreCase(name)) {
            continue;
          }
          scanForAdvancementFolders(sub, currentDepth + 1, maxDepth, result, visitedPaths);
        }
      }
    }
  }

  public List<File> findAllAdvancementFilesForPlayer(@NotNull UUID uuid) {
    List<File> matches = new ArrayList<>();
    String uuidWithHyphens = uuid.toString().toLowerCase(Locale.ROOT);
    String uuidNoHyphens = uuidWithHyphens.replace("-", "");

    for (File folder : getAllAdvancementFolders()) {
      File direct1 = new File(folder, uuidWithHyphens + ".json");
      if (direct1.isFile()) {
        matches.add(direct1);
        continue;
      }
      File direct2 = new File(folder, uuidNoHyphens + ".json");
      if (direct2.isFile()) {
        matches.add(direct2);
        continue;
      }
      File direct3 = new File(folder, uuidWithHyphens.toUpperCase(Locale.ROOT) + ".json");
      if (direct3.isFile()) {
        matches.add(direct3);
        continue;
      }
      File direct4 = new File(folder, uuidNoHyphens.toUpperCase(Locale.ROOT) + ".json");
      if (direct4.isFile()) {
        matches.add(direct4);
        continue;
      }

      File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
      if (files != null) {
        for (File f : files) {
          String baseName = f.getName().substring(0, f.getName().length() - 5);
          String clean = baseName.replace("-", "").toLowerCase(Locale.ROOT);
          if (clean.equals(uuidNoHyphens)) {
            matches.add(f);
          }
        }
      }
    }

    return matches;
  }

  public @NotNull Map<String, Long> loadPlayerCompletedMap(@NotNull UUID uuid) {
    Map<String, Long> completed = new HashMap<>();

    // 1. Read from saved JSON file(s) on disk using file cache
    List<File> files = findAllAdvancementFilesForPlayer(uuid);
    for (File file : files) {
      completed.putAll(parseAdvancementFileWithCache(file));
    }

    // 2. Overlay live in-memory completions
    Map<String, Long> live = liveCompletions.get(uuid);
    if (live != null) {
      completed.putAll(live);
    }

    // 3. If player is online, sync directly if on main thread or schedule sync
    Player online = Bukkit.getPlayer(uuid);
    if (online != null && online.isOnline()) {
      if (Bukkit.isPrimaryThread()) {
        syncPlayerAdvancements(online);
        Map<String, Long> reloadedLive = liveCompletions.get(uuid);
        if (reloadedLive != null) {
          completed.putAll(reloadedLive);
        }
      } else {
        Bukkit.getScheduler().runTask(plugin, () -> syncPlayerAdvancements(online));
      }
    }

    return completed;
  }

  private Map<String, Long> parseAdvancementFileWithCache(File file) {
    if (file == null || !file.isFile() || !file.canRead()) {
      return Collections.emptyMap();
    }
    String path = file.getAbsolutePath();
    long fileMod = file.lastModified();

    CachedAdvancementFile cached = advancementFileCache.get(path);
    if (cached != null && cached.lastModified() == fileMod) {
      return cached.completedMap();
    }

    Map<String, Long> completed = new HashMap<>();
    try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

      for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
        String rawKey = entry.getKey();
        if ("dataversion".equalsIgnoreCase(rawKey)) {
          continue;
        }
        if (!entry.getValue().isJsonObject()) {
          continue;
        }

        JsonObject advObj = entry.getValue().getAsJsonObject();
        if (isAdvancementDone(advObj, rawKey)) {
          long completedTime = fileMod;
          if (advObj.has("criteria") && advObj.get("criteria").isJsonObject()) {
            JsonObject criteria = advObj.getAsJsonObject("criteria");
            long maxCriteriaTime = 0;
            for (Map.Entry<String, JsonElement> critEntry : criteria.entrySet()) {
              long parsed = parseCriteriaDate(critEntry.getValue());
              if (parsed > maxCriteriaTime) {
                maxCriteriaTime = parsed;
              }
            }
            if (maxCriteriaTime > 0) {
              completedTime = maxCriteriaTime;
            }
          }

          completed.put(rawKey, completedTime);
          completed.put(normalizeKey(rawKey), completedTime);
          completed.put(stripNamespace(rawKey), completedTime);
        }
      }

      if (advancementFileCache.size() > 1000) {
        advancementFileCache.clear();
      }
      advancementFileCache.put(
          path, new CachedAdvancementFile(fileMod, Collections.unmodifiableMap(completed)));
    } catch (Throwable e) {
      plugin.getLogger().warning("Error reading advancement file " + path + ": " + e.getMessage());
    }

    return completed;
  }

  private boolean isAdvancementDone(JsonObject advObj, String advKey) {
    if (advObj == null) return false;

    if (advObj.has("done")) {
      JsonElement elem = advObj.get("done");
      if (elem.isJsonPrimitive()) {
        try {
          if (elem.getAsBoolean()) return true;
        } catch (Throwable ignored) {
        }
        String val = elem.getAsString().trim();
        if ("true".equalsIgnoreCase(val) || "1".equals(val)) return true;
        if ("false".equalsIgnoreCase(val) || "0".equals(val)) return false;
      }
    }

    if (advObj.has("criteria") && advObj.get("criteria").isJsonObject()) {
      JsonObject crit = advObj.getAsJsonObject("criteria");
      if (crit.size() > 0) {
        AdvancementRecord rec = findRecord(advKey);
        if (rec != null && rec.criteriaCount() > 0 && crit.size() >= rec.criteriaCount()) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isRecordCompleted(AdvancementRecord rec, Map<String, Long> completedMap) {
    if (completedMap.containsKey(rec.id())) return true;
    if (completedMap.containsKey(normalizeKey(rec.id()))) return true;
    if (completedMap.containsKey(stripNamespace(rec.id()))) return true;
    return false;
  }

  private Long getRecordCompletedTime(AdvancementRecord rec, Map<String, Long> completedMap) {
    Long t = completedMap.get(rec.id());
    if (t != null && t > 0) return t;
    t = completedMap.get(normalizeKey(rec.id()));
    if (t != null && t > 0) return t;
    t = completedMap.get(stripNamespace(rec.id()));
    if (t != null && t > 0) return t;
    return null;
  }

  private long parseCriteriaDate(JsonElement elem) {
    if (elem == null || elem.isJsonNull()) {
      return 0L;
    }
    if (elem.isJsonPrimitive()) {
      try {
        if (elem.getAsJsonPrimitive().isNumber()) {
          return elem.getAsLong();
        }
      } catch (Throwable ignored) {
      }
      String dateStr = elem.getAsString();
      if (dateStr == null || dateStr.isBlank()) {
        return 0L;
      }
      try {
        return Long.parseLong(dateStr.trim());
      } catch (Throwable ignored) {
      }
      try {
        return ZonedDateTime.parse(dateStr.trim(), FORMATTER_SPACE).toInstant().toEpochMilli();
      } catch (Throwable ignored) {
      }
      try {
        return Instant.parse(dateStr.trim()).toEpochMilli();
      } catch (Throwable ignored) {
      }
      try {
        return ZonedDateTime.parse(dateStr.trim(), FORMATTER_ISO).toInstant().toEpochMilli();
      } catch (Throwable ignored) {
      }
      try {
        return OffsetDateTime.parse(dateStr.trim()).toInstant().toEpochMilli();
      } catch (Throwable ignored) {
      }
    }
    return 0L;
  }

  public @NotNull JsonObject getPlayerAchievementsJson(@NotNull UUID uuid) {
    long now = System.currentTimeMillis();
    CachedPlayerPayload cached = playerCache.get(uuid);
    if (cached != null && (now - cached.timestamp()) < 15000L) {
      return cached.payload();
    }

    List<AdvancementRecord> all = getRegisteredAdvancements();
    Map<String, Long> completedMap = loadPlayerCompletedMap(uuid);

    JsonObject root = new JsonObject();
    JsonArray list = new JsonArray();
    int completedCount = 0;

    for (AdvancementRecord rec : all) {
      boolean isDone = isRecordCompleted(rec, completedMap);
      if (isDone) {
        completedCount++;
      }

      JsonObject item = new JsonObject();
      item.addProperty("id", rec.id());
      item.addProperty("title", rec.title());
      item.addProperty("description", rec.description());
      item.addProperty("frame", rec.frame());
      item.addProperty("icon", rec.icon());
      item.addProperty("category", rec.category());
      item.addProperty("categoryName", rec.categoryName());
      if (rec.parent() != null) {
        item.addProperty("parent", rec.parent());
      }
      item.addProperty("completed", isDone);
      if (isDone) {
        Long date = getRecordCompletedTime(rec, completedMap);
        if (date != null && date > 0) {
          item.addProperty("completedAt", date);
        }
      }
      list.add(item);
    }

    int totalCount = all.size();
    double percentage = totalCount > 0
        ? Math.round((completedCount * 1000.0) / totalCount) / 10.0
        : 0.0;

    root.addProperty("completedCount", completedCount);
    root.addProperty("totalCount", totalCount);
    root.addProperty("percentage", percentage);
    root.add("list", list);

    if (playerCache.size() > 500) {
      playerCache.entrySet().removeIf(e -> (now - e.getValue().timestamp()) > 15000L);
    }
    playerCache.put(uuid, new CachedPlayerPayload(now, root));
    return root;
  }

  public @NotNull JsonObject getGlobalAchievementsJson() {
    long now = System.currentTimeMillis();
    CachedGlobalPayload cached = globalCache;
    if (cached != null && (now - cached.timestamp()) < 30000L) {
      return cached.payload();
    }

    List<AdvancementRecord> all = getRegisteredAdvancements();
    Map<String, Integer> counts = new HashMap<>();
    for (AdvancementRecord rec : all) {
      counts.put(normalizeKey(rec.id()), 0);
    }

    Set<File> folders = getAllAdvancementFolders();
    Set<String> scannedUuids = new HashSet<>();
    int totalCompletions = 0;

    for (File folder : folders) {
      File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
      if (files == null) continue;

      for (File f : files) {
        String name = f.getName().substring(0, f.getName().length() - 5);
        String cleanUuid = name.replace("-", "").toLowerCase(Locale.ROOT);
        if (cleanUuid.length() < 16) {
          continue;
        }
        if (!scannedUuids.add(cleanUuid)) {
          continue;
        }

        Map<String, Long> playerCompleted = parseAdvancementFileWithCache(f);
        Set<String> playerCompletedNormKeys = new HashSet<>();
        for (String rawKey : playerCompleted.keySet()) {
          playerCompletedNormKeys.add(normalizeKey(rawKey));
        }

        for (String normKey : playerCompletedNormKeys) {
          if (counts.containsKey(normKey)) {
            counts.put(normKey, counts.get(normKey) + 1);
            totalCompletions++;
          }
        }
      }
    }

    // Include any online players or live completions not already counted
    for (Map.Entry<UUID, Map<String, Long>> liveEntry : liveCompletions.entrySet()) {
      UUID liveUuid = liveEntry.getKey();
      String cleanUuid = liveUuid.toString().replace("-", "").toLowerCase(Locale.ROOT);
      if (scannedUuids.add(cleanUuid)) {
        Set<String> playerCompletedNormKeys = new HashSet<>();
        for (String rawKey : liveEntry.getValue().keySet()) {
          playerCompletedNormKeys.add(normalizeKey(rawKey));
        }
        for (String normKey : playerCompletedNormKeys) {
          if (counts.containsKey(normKey)) {
            counts.put(normKey, counts.get(normKey) + 1);
            totalCompletions++;
          }
        }
      }
    }

    if (userService != null) {
      for (UUID u : userService.getAllUsers().keySet()) {
        scannedUuids.add(u.toString().replace("-", "").toLowerCase(Locale.ROOT));
      }
    }

    int totalTrackedPlayers = Math.max(scannedUuids.size(), 1);

    JsonObject root = new JsonObject();
    root.addProperty("online", true);
    root.addProperty("total", all.size());

    JsonArray cats = new JsonArray();
    for (String c : getCategories()) {
      cats.add(c);
    }
    root.add("categories", cats);

    JsonArray achievementsArray = new JsonArray();
    for (AdvancementRecord rec : all) {
      String normId = normalizeKey(rec.id());
      int count = counts.getOrDefault(normId, 0);
      double percentage = Math.round((count * 1000.0) / totalTrackedPlayers) / 10.0;

      JsonObject item = new JsonObject();
      item.addProperty("id", rec.id());
      item.addProperty("title", rec.title());
      item.addProperty("description", rec.description());
      item.addProperty("frame", rec.frame());
      item.addProperty("icon", rec.icon());
      item.addProperty("category", rec.category());
      item.addProperty("categoryName", rec.categoryName());
      if (rec.parent() != null) {
        item.addProperty("parent", rec.parent());
      }
      item.addProperty("criteriaCount", rec.criteriaCount());
      item.addProperty("completedCount", count);
      item.addProperty("completedPercentage", percentage);

      achievementsArray.add(item);
    }
    root.add("achievements", achievementsArray);

    JsonObject stats = new JsonObject();
    stats.addProperty("totalAchievements", all.size());
    stats.addProperty("totalCompletions", totalCompletions);
    stats.addProperty("trackedPlayers", totalTrackedPlayers);
    root.add("globalStats", stats);

    globalCache = new CachedGlobalPayload(now, root);
    return root;
  }

  /**
   * Pre-populates the complete vanilla Minecraft 1.21 advancements catalog.
   */
  private void loadBuiltInCatalog() {
    addCatalogItem("minecraft:story/root", "Minecraft", "The heart and story of the game", "TASK", "grass_block", "story", null, 1);
    addCatalogItem("minecraft:story/mine_stone", "Stone Age", "Mine stone with your new pickaxe", "TASK", "wooden_pickaxe", "story", "minecraft:story/root", 1);
    addCatalogItem("minecraft:story/upgrade_tools", "Getting an Upgrade", "Construct a better pickaxe", "TASK", "stone_pickaxe", "story", "minecraft:story/mine_stone", 1);
    addCatalogItem("minecraft:story/smelt_iron", "Acquire Hardware", "Smelt an iron ingot", "TASK", "iron_ingot", "story", "minecraft:story/upgrade_tools", 1);
    addCatalogItem("minecraft:story/obtain_armor", "Suit Up", "Protect yourself with a piece of iron armor", "TASK", "iron_chestplate", "story", "minecraft:story/smelt_iron", 1);
    addCatalogItem("minecraft:story/lava_bucket", "Hot Stuff", "Fill a bucket with lava", "TASK", "lava_bucket", "story", "minecraft:story/smelt_iron", 1);
    addCatalogItem("minecraft:story/iron_tools", "Isn't It Iron Pick", "Upgrade your pickaxe", "TASK", "iron_pickaxe", "story", "minecraft:story/smelt_iron", 1);
    addCatalogItem("minecraft:story/deflect_arrow", "Not Today, Thank You", "Deflect a projectile with a shield", "TASK", "shield", "story", "minecraft:story/obtain_armor", 1);
    addCatalogItem("minecraft:story/form_obsidian", "Ice Bucket Challenge", "Obtain a block of obsidian", "TASK", "obsidian", "story", "minecraft:story/lava_bucket", 1);
    addCatalogItem("minecraft:story/mine_diamond", "Diamonds!", "Acquire diamonds", "TASK", "diamond", "story", "minecraft:story/iron_tools", 1);
    addCatalogItem("minecraft:story/enter_the_nether", "We Need to Go Deeper", "Build, light and enter a Nether Portal", "TASK", "flint_and_steel", "story", "minecraft:story/form_obsidian", 1);
    addCatalogItem("minecraft:story/shiny_gear", "Cover Me with Diamonds", "Diamond armor saves lives", "TASK", "diamond_chestplate", "story", "minecraft:story/mine_diamond", 1);
    addCatalogItem("minecraft:story/enchant_item", "Enchanter", "Enchant an item at an Enchanting Table", "TASK", "enchanting_table", "story", "minecraft:story/mine_diamond", 1);
    addCatalogItem("minecraft:story/cure_zombie_villager", "Zombie Doctor", "Weaken and then cure a zombie villager", "GOAL", "golden_apple", "story", "minecraft:story/smelt_iron", 1);
    addCatalogItem("minecraft:story/follow_ender_eye", "Eye Spy", "Follow an Ender Eye", "TASK", "ender_eye", "story", "minecraft:story/enter_the_nether", 1);
    addCatalogItem("minecraft:story/enter_the_end", "The End?", "Enter the End Portal", "TASK", "end_stone", "story", "minecraft:story/follow_ender_eye", 1);
    addCatalogItem("minecraft:nether/root", "Nether", "Bring summer clothes", "TASK", "netherrack", "nether", null, 1);
    addCatalogItem("minecraft:nether/return_to_sender", "Return to Sender", "Destroy a Ghast with a fireball", "CHALLENGE", "fire_charge", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/find_bastion", "Those Were the Days", "Enter a Bastion Remnant", "TASK", "polished_blackstone_bricks", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/obtain_ancient_debris", "Hidden in the Depths", "Obtain Ancient Debris", "TASK", "ancient_debris", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/fast_travel", "Subspace Bubble", "Use the Nether to travel 7 km in the Overworld", "CHALLENGE", "map", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/find_fortress", "A Terrible Fortress", "Break your way into a Nether Fortress", "TASK", "nether_bricks", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/obtain_crying_obsidian", "Who is Cutting Onions?", "Obtain Crying Obsidian", "TASK", "crying_obsidian", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/distract_piglin", "Oh Shiny", "Distract Piglins with Gold", "TASK", "gold_ingot", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/ride_strider", "This Boat Has Legs", "Ride a Strider with a Warped Fungus on a Stick", "TASK", "warped_fungus_on_a_stick", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/uneasy_alliance", "Uneasy Alliance", "Rescue a Ghast from the Nether, bring it safely home to the Overworld... and then kill it", "CHALLENGE", "ghast_tear", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/loot_bastion", "War Pigs", "Loot a chest in a Bastion Remnant", "TASK", "chest", "nether", "minecraft:nether/find_bastion", 1);
    addCatalogItem("minecraft:nether/use_lodestone", "Country Lode, Take Me Home", "Use a Compass on a Lodestone", "TASK", "lodestone", "nether", "minecraft:nether/root", 1);
    addCatalogItem("minecraft:nether/netherite_armor", "Cover Me in Debris", "Get a full suit of Netherite armor", "CHALLENGE", "netherite_chestplate", "nether", "minecraft:nether/obtain_ancient_debris", 1);
    addCatalogItem("minecraft:nether/get_wither_skull", "Spooky Scary Skeleton", "Obtain a Wither Skeleton's skull", "TASK", "wither_skeleton_skull", "nether", "minecraft:nether/find_fortress", 1);
    addCatalogItem("minecraft:nether/obtain_blaze_rod", "Into Fire", "Relieve a Blaze of its rod", "TASK", "blaze_rod", "nether", "minecraft:nether/find_fortress", 1);
    addCatalogItem("minecraft:nether/charge_respawn_anchor", "Not Quite \"Nine\" Lives", "Charge a Respawn Anchor to the maximum", "TASK", "respawn_anchor", "nether", "minecraft:nether/obtain_crying_obsidian", 1);
    addCatalogItem("minecraft:nether/ride_strider_in_overworld_lava", "Feels Like Home", "Take a Strider for a loooong ride on a lava lake in the Overworld", "TASK", "warped_fungus_on_a_stick", "nether", "minecraft:nether/ride_strider", 1);
    addCatalogItem("minecraft:nether/explore_nether", "Hot Tourist Destinations", "Explore all Nether biomes", "CHALLENGE", "netherite_boots", "nether", "minecraft:nether/root", 5);
    addCatalogItem("minecraft:nether/summon_wither", "Withering Heights", "Summon the Wither", "TASK", "nether_star", "nether", "minecraft:nether/get_wither_skull", 1);
    addCatalogItem("minecraft:nether/brew_potion", "Local Brewery", "Brew a potion", "TASK", "potion", "nether", "minecraft:nether/obtain_blaze_rod", 1);
    addCatalogItem("minecraft:nether/create_beacon", "Bring Home the Beacon", "Construct and place a Beacon", "TASK", "beacon", "nether", "minecraft:nether/summon_wither", 1);
    addCatalogItem("minecraft:nether/all_potions", "A Furious Cocktail", "Have every potion effect applied at the same time", "CHALLENGE", "milk_bucket", "nether", "minecraft:nether/brew_potion", 1);
    addCatalogItem("minecraft:nether/create_full_beacon", "Beaconator", "Bring a beacon to full power", "GOAL", "beacon", "nether", "minecraft:nether/create_beacon", 1);
    addCatalogItem("minecraft:nether/all_effects", "How Did We Get Here?", "Have every effect applied at the same time", "CHALLENGE", "bucket", "nether", "minecraft:nether/all_potions", 1);
    addCatalogItem("minecraft:end/root", "The End", "Or the beginning?", "TASK", "end_stone", "end", null, 1);
    addCatalogItem("minecraft:end/kill_dragon", "Free the End", "Good luck", "TASK", "dragon_head", "end", "minecraft:end/root", 1);
    addCatalogItem("minecraft:end/dragon_egg", "The Next Generation", "Hold the Dragon Egg", "GOAL", "dragon_egg", "end", "minecraft:end/kill_dragon", 1);
    addCatalogItem("minecraft:end/enter_end_gateway", "Remote Getaway", "Escape the island", "TASK", "ender_pearl", "end", "minecraft:end/kill_dragon", 1);
    addCatalogItem("minecraft:end/respawn_dragon", "The End... Again...", "Respawn the Ender Dragon", "GOAL", "end_crystal", "end", "minecraft:end/kill_dragon", 1);
    addCatalogItem("minecraft:end/dragon_breath", "You Need a Mint", "Collect Dragon's Breath in a Glass Bottle", "GOAL", "dragon_breath", "end", "minecraft:end/kill_dragon", 1);
    addCatalogItem("minecraft:end/find_end_city", "The City at the End of the Game", "Go on in, what could happen?", "TASK", "purpur_block", "end", "minecraft:end/enter_end_gateway", 1);
    addCatalogItem("minecraft:end/elytra", "Sky's the Limit", "Find Elytra", "GOAL", "elytra", "end", "minecraft:end/find_end_city", 1);
    addCatalogItem("minecraft:end/levitate", "Great View From Up Here", "Levitate up 50 blocks from the attacks of a Shulker", "CHALLENGE", "shulker_shell", "end", "minecraft:end/find_end_city", 1);
    addCatalogItem("minecraft:adventure/root", "Adventure", "Adventure, exploration and combat", "TASK", "map", "adventure", null, 1);
    addCatalogItem("minecraft:adventure/voluntary_exile", "Voluntary Exile", "Kill a raid captain", "TASK", "ominous_banner", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/kill_a_mob", "Monster Hunter", "Kill any hostile monster", "TASK", "iron_sword", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/trade", "What a Deal!", "Successfully trade with a Villager", "TASK", "emerald", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/sleep_in_bed", "Sweet Dreams", "Sleep in a bed to change your respawn point", "TASK", "red_bed", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/shoot_arrow", "Take Aim", "Shoot something with an arrow", "TASK", "bow", "adventure", "minecraft:adventure/kill_a_mob", 1);
    addCatalogItem("minecraft:adventure/throw_trident", "A Throwaway Joke", "Throw a Trident at something", "TASK", "trident", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/summon_iron_golem", "Hired Help", "Summon an Iron Golem to help defend a village", "GOAL", "iron_block", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/ol_betsy", "Ol' Betsy", "Shoot a Crossbow", "TASK", "crossbow", "adventure", "minecraft:adventure/kill_a_mob", 1);
    addCatalogItem("minecraft:adventure/hero_of_the_village", "Hero of the Village", "Successfully defend a village from a raid", "CHALLENGE", "emerald", "adventure", "minecraft:adventure/voluntary_exile", 1);
    addCatalogItem("minecraft:adventure/two_birds_one_arrow", "Two Birds, One Arrow", "Kill two Phantoms with a piercing arrow", "CHALLENGE", "crossbow", "adventure", "minecraft:adventure/ol_betsy", 1);
    addCatalogItem("minecraft:adventure/whos_the_pillager_now", "Who's the Pillager Now?", "Give a Pillager a taste of their own medicine", "TASK", "crossbow", "adventure", "minecraft:adventure/ol_betsy", 1);
    addCatalogItem("minecraft:adventure/arbalistic", "Arbalistic", "Kill five unique mobs with one crossbow shot", "CHALLENGE", "crossbow", "adventure", "minecraft:adventure/ol_betsy", 1);
    addCatalogItem("minecraft:adventure/adventuring_time", "Adventuring Time", "Discover every biome", "CHALLENGE", "diamond_boots", "adventure", "minecraft:adventure/root", 52);
    addCatalogItem("minecraft:adventure/very_very_frightening", "Very Very Frightening", "Strike a Villager with lightning", "CHALLENGE", "trident", "adventure", "minecraft:adventure/throw_trident", 1);
    addCatalogItem("minecraft:adventure/sniper_duel", "Sniper Duel", "Kill a Skeleton with an arrow from more than 50 meters away", "CHALLENGE", "arrow", "adventure", "minecraft:adventure/shoot_arrow", 1);
    addCatalogItem("minecraft:adventure/bullseye", "Bullseye", "Hit the bullseye of a Target block from at least 30 meters away", "CHALLENGE", "target", "adventure", "minecraft:adventure/shoot_arrow", 1);
    addCatalogItem("minecraft:adventure/kill_all_mobs", "Monsters Hunted", "Kill one of every hostile monster", "CHALLENGE", "diamond_sword", "adventure", "minecraft:adventure/kill_a_mob", 34);
    addCatalogItem("minecraft:adventure/totem_of_undying", "Postmortal", "Use a Totem of Undying to cheat death", "GOAL", "totem_of_undying", "adventure", "minecraft:adventure/kill_a_mob", 1);
    addCatalogItem("minecraft:adventure/play_jukebox_in_meadows", "Sound of Music", "Make the Meadows come alive with the sound of music from a Jukebox", "TASK", "jukebox", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/walk_on_powder_snow_with_leather_boots", "Light as a Rabbit", "Walk on powder snow... without sinking into it", "TASK", "leather_boots", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/spyglass_at_parrot", "Is It a Bird?", "Look at a Parrot through a Spyglass", "TASK", "spyglass", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/spyglass_at_ghast", "Is It a Balloon?", "Look at a Ghast through a Spyglass", "TASK", "spyglass", "adventure", "minecraft:adventure/spyglass_at_parrot", 1);
    addCatalogItem("minecraft:adventure/spyglass_at_dragon", "Is It a Plane?", "Look at the Ender Dragon through a Spyglass", "TASK", "spyglass", "adventure", "minecraft:adventure/spyglass_at_ghast", 1);
    addCatalogItem("minecraft:adventure/fall_from_world_height", "Caves & Cliffs", "Free fall from the top of the world to the bottom", "TASK", "water_bucket", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/avoid_vibration", "Sneak 100", "Sneak near a Sculk Sensor or Warden to prevent it detecting you", "TASK", "sculk_sensor", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/kill_mob_near_sculk_catalyst", "It Spreads", "Kill a mob near a Sculk Catalyst", "TASK", "sculk_catalyst", "adventure", "minecraft:adventure/avoid_vibration", 1);
    addCatalogItem("minecraft:adventure/trim_with_any_armor_pattern", "Crafting a New Look", "Craft a trimmed armor at a Smithing Table", "TASK", "smithing_table", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/trim_with_all_exclusive_armor_patterns", "Smithing with Style", "Apply these smithing templates at least once", "CHALLENGE", "smithing_table", "adventure", "minecraft:adventure/trim_with_any_armor_pattern", 8);
    addCatalogItem("minecraft:adventure/salvage_sherd", "Respecting the Remnants", "Brush a Suspicious block to obtain a Pottery Sherd", "TASK", "brush", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/craft_decorated_pot_using_only_sherds", "Careful Restoration", "Make a Decorated Pot out of 4 Pottery Sherds", "TASK", "decorated_pot", "adventure", "minecraft:adventure/salvage_sherd", 1);
    addCatalogItem("minecraft:adventure/read_power_of_chiseled_bookshelf", "The Power of Books", "Read the power signal of a Chiseled Bookshelf using a Comparator", "TASK", "chiseled_bookshelf", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/brush_armadillo", "Isn't It Scute?", "Brush an Armadillo to obtain an Armadillo Scute", "TASK", "armadillo_scute", "adventure", "minecraft:adventure/salvage_sherd", 1);
    addCatalogItem("minecraft:adventure/who_needs_rockets", "Who Needs Rockets?", "Launch yourself up at least 7 blocks using a Wind Charge", "TASK", "wind_charge", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/crafters_crafting_crafters", "Crafters Crafting Crafters", "Be near a Crafter when it crafts a Crafter", "CHALLENGE", "crafter", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/lighten_up", "Lighten Up", "Un-oxidize a Copper Bulb using an Axe", "TASK", "copper_bulb", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/under_lock_and_key", "Under Lock and Key", "Unlock a Trial Spawner or Vault using a Trial Key", "TASK", "trial_key", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/revaulting", "Revaulting", "Unlock an Ominous Vault using an Ominous Trial Key", "GOAL", "ominous_trial_key", "adventure", "minecraft:adventure/under_lock_and_key", 1);
    addCatalogItem("minecraft:adventure/blowback", "Blowback", "Defeat a Breeze with a deflected Wind Charge", "CHALLENGE", "wind_charge", "adventure", "minecraft:adventure/who_needs_rockets", 1);
    addCatalogItem("minecraft:adventure/over_overkill", "Over-Overkill", "Deal 50 hearts of damage in a single hit using a Mace", "CHALLENGE", "mace", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/trade_at_world_height", "Star Trader", "Trade with a Villager at the build height limit", "GOAL", "emerald", "adventure", "minecraft:adventure/trade", 1);
    addCatalogItem("minecraft:adventure/honey_block_slide", "Sticky Situation", "Jump into a Honey Block to break your fall", "TASK", "honey_block", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/lightning_rod_with_villager_no_fire", "Surge Protector", "Protect a Villager from an undesired shock without starting a fire", "TASK", "lightning_rod", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/walk_on_lava_with_boots", "Walk the Walk", "Walk safely over lava with Netherite boots", "TASK", "netherite_boots", "adventure", "minecraft:adventure/root", 1);
    addCatalogItem("minecraft:adventure/fall_with_mace", "Mace to Face", "Hit a target while falling using a Mace", "TASK", "mace", "adventure", "minecraft:adventure/over_overkill", 1);
    addCatalogItem("minecraft:adventure/use_vault", "Unlock the Vault", "Unlock a Vault with a Trial Key", "TASK", "vault", "adventure", "minecraft:adventure/under_lock_and_key", 1);
    addCatalogItem("minecraft:adventure/ominous_trial_spawner", "A Real Challenge", "Trigger an Ominous Trial Spawner", "TASK", "trial_spawner", "adventure", "minecraft:adventure/under_lock_and_key", 1);
    addCatalogItem("minecraft:husbandry/root", "Husbandry", "The world is full of friends and food", "TASK", "wheat", "husbandry", null, 1);
    addCatalogItem("minecraft:husbandry/breed_an_animal", "The Parrots and the Bats", "Breed two animals together", "TASK", "wheat", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/tame_an_animal", "Best Friends Forever", "Tame an animal", "TASK", "lead", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/plant_seed", "A Seedy Place", "Plant a seed and watch it grow", "TASK", "wheat_seeds", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/bred_all_animals", "Two by Two", "Breed all the animals!", "CHALLENGE", "golden_carrot", "husbandry", "minecraft:husbandry/breed_an_animal", 23);
    addCatalogItem("minecraft:husbandry/balanced_diet", "A Balanced Diet", "Eat everything that is edible, even if it is not good for you", "CHALLENGE", "apple", "husbandry", "minecraft:husbandry/plant_seed", 40);
    addCatalogItem("minecraft:husbandry/break_diamond_hoe", "Serious Dedication", "Completely use up a diamond hoe, and then reevaluate your life choices", "CHALLENGE", "diamond_hoe", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/tactical_fishing", "Tactical Fishing", "Catch a fish... without a fishing rod!", "TASK", "water_bucket", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/silk_touch_nest", "Total Beelocation", "Move a Bee Nest with 3 Bees inside using Silk Touch", "GOAL", "bee_nest", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/safely_harvest_honey", "Bee Our Guest", "Use a Campfire to collect Honey without aggravating the bees", "TASK", "honey_bottle", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/wax_on", "Wax On", "Apply Honeycomb to a Copper block!", "TASK", "honeycomb", "husbandry", "minecraft:husbandry/safely_harvest_honey", 1);
    addCatalogItem("minecraft:husbandry/wax_off", "Wax Off", "Scrape wax off a Copper block!", "TASK", "stone_axe", "husbandry", "minecraft:husbandry/wax_on", 1);
    addCatalogItem("minecraft:husbandry/axolotl_in_a_bucket", "The Cutest Predator", "Catch an Axolotl in a bucket", "TASK", "axolotl_bucket", "husbandry", "minecraft:husbandry/tactical_fishing", 1);
    addCatalogItem("minecraft:husbandry/kill_axolotl_target", "The Healing Power of Friendship!", "Team up with an Axolotl and win a fight", "TASK", "tropical_fish_bucket", "husbandry", "minecraft:husbandry/axolotl_in_a_bucket", 1);
    addCatalogItem("minecraft:husbandry/froglights", "With Our Powers Combined!", "Have all Froglights in your inventory", "CHALLENGE", "ochre_froglight", "husbandry", "minecraft:husbandry/root", 3);
    addCatalogItem("minecraft:husbandry/leash_all_frog_variants", "When the Squad Hops into Town", "Get each Frog variant on a Lead", "TASK", "lead", "husbandry", "minecraft:husbandry/root", 3);
    addCatalogItem("minecraft:husbandry/tadpole_in_a_bucket", "Bukkit Bukkit", "Catch a Tadpole in a Bucket", "TASK", "tadpole_bucket", "husbandry", "minecraft:husbandry/tactical_fishing", 1);
    addCatalogItem("minecraft:husbandry/allay_drop_item_to_dump", "You've Got a Friend in Me", "Have an Allay deliver items to you", "TASK", "cookie", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/allay_deliver_cake_to_note_block", "Birthday Song", "Have an Allay drop a Cake at a Note Block", "CHALLENGE", "cake", "husbandry", "minecraft:husbandry/allay_drop_item_to_dump", 1);
    addCatalogItem("minecraft:husbandry/sniff_sniffer", "Smells Interesting", "Obtain a Sniffer Egg", "TASK", "sniffer_egg", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/feed_snifflet", "Little Sniffs", "Feed a Snifflet", "TASK", "torchflower_seeds", "husbandry", "minecraft:husbandry/sniff_sniffer", 1);
    addCatalogItem("minecraft:husbandry/obtain_sniffer_egg", "Planting the Past", "Plant any Sniffer seed", "TASK", "pitcher_pod", "husbandry", "minecraft:husbandry/feed_snifflet", 1);
    addCatalogItem("minecraft:husbandry/complete_catalogue", "A Complete Catalogue", "Tame all cat variants!", "CHALLENGE", "cod", "husbandry", "minecraft:husbandry/tame_an_animal", 11);
    addCatalogItem("minecraft:husbandry/fishy_business", "Fishy Business", "Catch a fish using a fishing rod", "TASK", "fishing_rod", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/ride_a_boat_with_a_goat", "Whatever Floats Your Goat!", "Get in a Boat and float with a Goat", "TASK", "oak_boat", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/make_a_sign_glow", "Glow and Behold!", "Make the text of a Sign glow with a Glow Ink Sac", "TASK", "glow_ink_sac", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/obtain_netherite_hoe", "Serious Dedication", "Upgrade a Hoe to Netherite", "CHALLENGE", "netherite_hoe", "husbandry", "minecraft:husbandry/break_diamond_hoe", 1);
    addCatalogItem("minecraft:husbandry/repair_wolf_armor", "Good as New", "Repair damaged Wolf Armor using Armadillo Scutes", "TASK", "wolf_armor", "husbandry", "minecraft:husbandry/tame_an_animal", 1);
    addCatalogItem("minecraft:husbandry/remove_wolf_armor", "Shear Brilliance", "Remove Wolf Armor from a Wolf using Shears", "TASK", "shears", "husbandry", "minecraft:husbandry/repair_wolf_armor", 1);
    addCatalogItem("minecraft:husbandry/whole_pack", "The Whole Pack", "Tame one of each Wolf variant", "CHALLENGE", "bone", "husbandry", "minecraft:husbandry/tame_an_animal", 9);
    addCatalogItem("minecraft:husbandry/allay_deliver_item_to_player", "Delivery Service", "Have an Allay deliver items to a player", "TASK", "cookie", "husbandry", "minecraft:husbandry/allay_drop_item_to_dump", 1);
    addCatalogItem("minecraft:husbandry/leash_all_horse_variants", "The Whole Herd", "Leash each horse variant", "TASK", "lead", "husbandry", "minecraft:husbandry/tame_an_animal", 5);
    addCatalogItem("minecraft:husbandry/brush_armadillo", "Isn't It Scute?", "Brush an Armadillo to get Armadillo Scutes", "TASK", "armadillo_scute", "husbandry", "minecraft:husbandry/root", 1);
    addCatalogItem("minecraft:husbandry/feed_armadillo", "Armadillo Armor", "Feed an Armadillo with Spider Eyes", "TASK", "spider_eye", "husbandry", "minecraft:husbandry/brush_armadillo", 1);
    addCatalogItem("minecraft:husbandry/obtain_wolf_armor", "Suit Up Your Pup", "Equip Wolf Armor onto a tamed Wolf", "TASK", "wolf_armor", "husbandry", "minecraft:husbandry/brush_armadillo", 1);
    addCatalogItem("minecraft:husbandry/place_hanging_sign", "Hang in There", "Place a Hanging Sign", "TASK", "oak_hanging_sign", "husbandry", "minecraft:husbandry/make_a_sign_glow", 1);
    addCatalogItem("minecraft:husbandry/feed_allay", "Sweet Treat", "Give an Allay something sweet", "TASK", "sugar", "husbandry", "minecraft:husbandry/allay_drop_item_to_dump", 1);

    rebuildCategoriesAndSorting();
  }

  private void addCatalogItem(
      String id,
      String title,
      String description,
      String frame,
      String icon,
      String category,
      String parent,
      int criteriaCount) {
    AdvancementRecord rec =
        new AdvancementRecord(
            id,
            title,
            description,
            frame,
            icon,
            category,
            formatCategoryName(category),
            parent,
            criteriaCount,
            true,
            false,
            calculateCategoryWeight(category));
    registerRecord(rec);
  }
}
