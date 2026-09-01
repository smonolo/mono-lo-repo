package dev.smnl.smessential.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.smnl.smessential.SMEssential;
import dev.smnl.smessential.database.DatabaseManager.UserData;
import dev.smnl.smessential.model.Rank;
import dev.smnl.smessential.model.StatisticType;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HttpServerService {

  private static final Gson GSON = new Gson();

  private final SMEssential plugin;
  private final RankService rankService;
  private final AfkService afkService;
  private final UserService userService;
  private final StatisticService statisticService;
  private HttpServer server;
  private ExecutorService executor;

  private boolean enabled;
  private String host;
  private int port;
  private String secret;

  private final Map<String, RateLimitTracker> rateLimitMap = new ConcurrentHashMap<>();
  private final Map<String, CachedPayload> payloadCache = new ConcurrentHashMap<>();

  private record RateLimitTracker(long windowStart, int count) {}

  private record CachedPayload(long timestamp, byte[] data) {}

  public HttpServerService(
      @NotNull SMEssential plugin,
      @NotNull RankService rankService,
      @NotNull AfkService afkService,
      @NotNull UserService userService,
      @NotNull StatisticService statisticService) {
    this.plugin = plugin;
    this.rankService = rankService;
    this.afkService = afkService;
    this.userService = userService;
    this.statisticService = statisticService;
  }

  public void setup() {
    loadConfig();
    if (!enabled) {
      plugin.getLogger().info("SMEssential HTTP API is disabled in configuration.");
      return;
    }

    try {
      executor = Executors.newVirtualThreadPerTaskExecutor();
      server = HttpServer.create(new InetSocketAddress(host, port), 0);
      server.setExecutor(executor);

      server.createContext("/v1/status", new StatusHandler());
      server.createContext("/v1/players", new PlayersListHandler());
      server.createContext("/v1/player", new SinglePlayerHandler());
      server.createContext("/v1/leaderboards", new LeaderboardsHandler());
      server.createContext("/v1/leaderboard", new LeaderboardsHandler());
      server.createContext("/v1/world", new WorldHandler());

      server.createContext("/api/status", new StatusHandler());
      server.createContext("/api/players", new PlayersListHandler());
      server.createContext("/api/player", new SinglePlayerHandler());
      server.createContext("/api/leaderboards", new LeaderboardsHandler());
      server.createContext("/api/leaderboard", new LeaderboardsHandler());
      server.createContext("/api/world", new WorldHandler());

      server.start();
      plugin.getLogger().info("SMEssential HTTP API listening on " + host + ":" + port);
    } catch (IOException e) {
      plugin.getLogger().severe("Failed to start SMEssential HTTP API: " + e.getMessage());
    }
  }

  public void shutdown() {
    if (server != null) {
      try {
        server.stop(1);
        plugin.getLogger().info("SMEssential HTTP API stopped.");
      } catch (Exception ignored) {
      }
    }
    if (executor != null) {
      executor.shutdownNow();
    }
    payloadCache.clear();
    rateLimitMap.clear();
  }

  public void reload() {
    shutdown();
    setup();
  }

  private void loadConfig() {
    this.enabled = plugin.getConfig().getBoolean("api.enabled", true);
    String rawHost = plugin.getConfig().getString("api.host", "0.0.0.0");
    this.host = resolveEnv(rawHost);
    if (this.host.isBlank()) {
      this.host = "0.0.0.0";
    }

    String rawPort = plugin.getConfig().getString("api.port", "25580");
    String resolvedPort = resolveEnv(rawPort);
    try {
      this.port = Integer.parseInt(resolvedPort);
    } catch (NumberFormatException e) {
      this.port = 25580;
    }

    String rawSecret = plugin.getConfig().getString("api.secret", "smessential-secret-key");
    this.secret = resolveEnv(rawSecret);
  }

  private String resolveEnv(String val) {
    if (val == null) return "";
    val = val.trim();
    if (val.startsWith("${") && val.endsWith("}")) {
      String inner = val.substring(2, val.length() - 1);
      String envName = inner;
      String defaultVal = "";
      if (inner.contains(":")) {
        String[] parts = inner.split(":", 2);
        envName = parts[0];
        defaultVal = parts[1];
      }
      String env = System.getenv(envName);
      return (env != null && !env.isBlank()) ? env : defaultVal;
    }
    return val;
  }

  private boolean isRateLimited(@NotNull HttpExchange exchange) {
    InetSocketAddress remote = exchange.getRemoteAddress();
    if (remote == null || remote.getAddress() == null) {
      return false;
    }
    String ip = remote.getAddress().getHostAddress();
    if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)) {
      return false;
    }

    long now = System.currentTimeMillis();
    RateLimitTracker tracker =
        rateLimitMap.compute(
            ip,
            (k, v) -> {
              if (v == null || (now - v.windowStart()) > 60000L) {
                return new RateLimitTracker(now, 1);
              }
              return new RateLimitTracker(v.windowStart(), v.count() + 1);
            });

    return tracker.count() > 120;
  }

  private boolean authenticate(@NotNull HttpExchange exchange) {
    if (secret == null || secret.isBlank()) {
      return true;
    }
    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return false;
    }
    String token = authHeader.substring("Bearer ".length()).trim();
    return MessageDigest.isEqual(
        secret.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
  }

  private void sendJsonResponse(
      @NotNull HttpExchange exchange, int statusCode, @NotNull JsonObject json) throws IOException {
    byte[] bytes = GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
    sendRawBytesResponse(exchange, statusCode, bytes);
  }

  private void sendRawBytesResponse(@NotNull HttpExchange exchange, int statusCode, byte[] bytes)
      throws IOException {
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
    exchange
        .getResponseHeaders()
        .set("Access-Control-Allow-Headers", "Content-Type, Authorization");

    if (statusCode == 204) {
      exchange.sendResponseHeaders(204, -1);
      exchange.close();
      return;
    }

    exchange.sendResponseHeaders(statusCode, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  private long getSafeStat(OfflinePlayer player, Statistic statistic) {
    try {
      return player.getStatistic(statistic);
    } catch (Exception e) {
      return 0L;
    }
  }

  private class StatusHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        sendJsonResponse(exchange, 204, new JsonObject());
        return;
      }

      if (isRateLimited(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Too Many Requests");
        sendJsonResponse(exchange, 429, err);
        return;
      }

      if (!authenticate(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Unauthorized");
        sendJsonResponse(exchange, 401, err);
        return;
      }

      long now = System.currentTimeMillis();
      CachedPayload cached = payloadCache.get("status");
      if (cached != null && (now - cached.timestamp()) < 3000L) {
        sendRawBytesResponse(exchange, 200, cached.data());
        return;
      }

      JsonObject root = new JsonObject();
      root.addProperty("online", true);
      root.addProperty("serverName", Bukkit.getServer().getName());
      root.addProperty("version", Bukkit.getVersion());
      root.addProperty("bukkitVersion", Bukkit.getBukkitVersion());
      root.addProperty("motd", Bukkit.getMotd());
      root.addProperty("maxPlayers", Bukkit.getMaxPlayers());
      root.addProperty("onlinePlayers", Bukkit.getOnlinePlayers().size());

      double[] tps = Bukkit.getTPS();
      JsonArray tpsArray = new JsonArray();
      for (double t : tps) {
        tpsArray.add(Math.round(t * 100.0) / 100.0);
      }
      root.add("tps", tpsArray);

      Runtime rt = Runtime.getRuntime();
      long usedMem = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
      long allocatedMem = rt.totalMemory() / (1024 * 1024);
      long maxMem = rt.maxMemory() / (1024 * 1024);

      JsonObject memory = new JsonObject();
      memory.addProperty("usedMb", usedMem);
      memory.addProperty("allocatedMb", allocatedMem);
      memory.addProperty("maxMb", maxMem);
      root.add("memory", memory);

      long uptimeSeconds =
          java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
      root.addProperty("uptimeSeconds", uptimeSeconds);

      byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
      payloadCache.put("status", new CachedPayload(now, bytes));
      sendRawBytesResponse(exchange, 200, bytes);
    }
  }

  private class PlayersListHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        sendJsonResponse(exchange, 204, new JsonObject());
        return;
      }

      if (isRateLimited(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Too Many Requests");
        sendJsonResponse(exchange, 429, err);
        return;
      }

      if (!authenticate(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Unauthorized");
        sendJsonResponse(exchange, 401, err);
        return;
      }

      long now = System.currentTimeMillis();
      CachedPayload cached = payloadCache.get("players_list");
      if (cached != null && (now - cached.timestamp()) < 3000L) {
        sendRawBytesResponse(exchange, 200, cached.data());
        return;
      }

      JsonObject root = new JsonObject();
      root.addProperty("online", true);

      Map<UUID, UserData> allUsers = userService.getAllUsers();
      Set<UUID> allUuids = new HashSet<>(allUsers.keySet());
      for (Player p : Bukkit.getOnlinePlayers()) {
        allUuids.add(p.getUniqueId());
      }

      List<JsonObject> playerObjects = new ArrayList<>();
      int onlineCount = 0;

      for (UUID uuid : allUuids) {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        boolean isOnline = (onlinePlayer != null && onlinePlayer.isOnline());
        if (isOnline) {
          onlineCount++;
        }

        UserData userData = allUsers.get(uuid);
        OfflinePlayer offlinePlayer =
            (onlinePlayer != null) ? onlinePlayer : Bukkit.getOfflinePlayer(uuid);

        String username =
            (onlinePlayer != null)
                ? onlinePlayer.getName()
                : (userData != null ? userData.username() : offlinePlayer.getName());
        if (username == null || username.isBlank()) {
          username = uuid.toString().substring(0, 8);
        }

        long lastLogin =
            (onlinePlayer != null)
                ? (onlinePlayer.getLastLogin() > 0
                    ? onlinePlayer.getLastLogin()
                    : onlinePlayer.getLastPlayed())
                : (userData != null && userData.lastJoin() > 0
                    ? userData.lastJoin()
                    : offlinePlayer.getLastPlayed());

        JsonObject p = new JsonObject();
        p.addProperty("uuid", uuid.toString());
        p.addProperty("username", username);
        p.addProperty("online", isOnline);
        p.addProperty("lastLogin", lastLogin);

        Rank displayRank = rankService.getDisplayRank(uuid);
        JsonObject r = new JsonObject();
        r.addProperty("id", displayRank.getId());
        r.addProperty("name", displayRank.getName());
        r.addProperty("color", displayRank.getColor());
        r.addProperty("prefix", displayRank.getPrefix());
        p.add("rank", r);

        if (isOnline) {
          p.addProperty("ping", onlinePlayer.getPing());
          p.addProperty("afk", afkService.isAfk(onlinePlayer));
          p.addProperty("world", onlinePlayer.getWorld().getName());
          try {
            p.addProperty("biome", onlinePlayer.getLocation().getBlock().getBiome().name());
          } catch (Exception ignored) {
          }
        } else {
          p.addProperty("ping", 0);
          p.addProperty("afk", false);
          p.addProperty("world", "Offline");
        }

        playerObjects.add(p);
      }

      playerObjects.sort(
          (a, b) -> {
            boolean aOnline = a.get("online").getAsBoolean();
            boolean bOnline = b.get("online").getAsBoolean();
            if (aOnline != bOnline) {
              return aOnline ? -1 : 1;
            }
            long aLast = a.has("lastLogin") ? a.get("lastLogin").getAsLong() : 0;
            long bLast = b.has("lastLogin") ? b.get("lastLogin").getAsLong() : 0;
            return Long.compare(bLast, aLast);
          });

      JsonArray playersArray = new JsonArray();
      for (JsonObject po : playerObjects) {
        playersArray.add(po);
      }

      root.add("players", playersArray);
      root.addProperty("count", playersArray.size());
      root.addProperty("onlineCount", onlineCount);

      byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
      payloadCache.put("players_list", new CachedPayload(now, bytes));
      sendRawBytesResponse(exchange, 200, bytes);
    }
  }

  private class SinglePlayerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        sendJsonResponse(exchange, 204, new JsonObject());
        return;
      }

      if (isRateLimited(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Too Many Requests");
        sendJsonResponse(exchange, 429, err);
        return;
      }

      if (!authenticate(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Unauthorized");
        sendJsonResponse(exchange, 401, err);
        return;
      }

      String query = exchange.getRequestURI().getQuery();
      String path = exchange.getRequestURI().getPath();
      String targetId = null;

      if (query != null) {
        for (String param : query.split("&")) {
          String[] pair = param.split("=", 2);
          if (pair.length == 2
              && ("uuid".equalsIgnoreCase(pair[0]) || "name".equalsIgnoreCase(pair[0]))) {
            targetId = pair[1].trim();
            break;
          }
        }
      }

      if (targetId == null && path.length() > "/api/player/".length()) {
        targetId = path.substring("/api/player/".length()).trim();
      }

      if (targetId == null || targetId.isBlank()) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Missing uuid or name parameter");
        sendJsonResponse(exchange, 400, err);
        return;
      }

      long now = System.currentTimeMillis();
      String cacheKey = "player_" + targetId.toLowerCase();
      CachedPayload cached = payloadCache.get(cacheKey);
      if (cached != null && (now - cached.timestamp()) < 15000L) {
        sendRawBytesResponse(exchange, 200, cached.data());
        return;
      }

      UUID uuid = null;
      try {
        uuid = UUID.fromString(targetId);
      } catch (IllegalArgumentException ignored) {
        UserData byName = userService.getUserByUsername(targetId);
        if (byName != null) {
          uuid = byName.uuid();
        }
      }

      if (uuid == null) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Player not found");
        sendJsonResponse(exchange, 404, err);
        return;
      }

      Player onlinePlayer = Bukkit.getPlayer(uuid);
      boolean isOnline = (onlinePlayer != null && onlinePlayer.isOnline());
      UserData userData = userService.getUser(uuid);
      OfflinePlayer offlinePlayer =
          (onlinePlayer != null) ? onlinePlayer : Bukkit.getOfflinePlayer(uuid);

      String username =
          (onlinePlayer != null)
              ? onlinePlayer.getName()
              : (userData != null ? userData.username() : offlinePlayer.getName());
      if (username == null || username.isBlank()) {
        username = uuid.toString().substring(0, 8);
      }

      long firstLogin =
          (userData != null && userData.firstJoin() > 0)
              ? userData.firstJoin()
              : offlinePlayer.getFirstPlayed();
      long lastLogin =
          (onlinePlayer != null)
              ? (onlinePlayer.getLastLogin() > 0
                  ? onlinePlayer.getLastLogin()
                  : onlinePlayer.getLastPlayed())
              : (userData != null && userData.lastJoin() > 0
                  ? userData.lastJoin()
                  : offlinePlayer.getLastPlayed());

      JsonObject p = new JsonObject();
      p.addProperty("uuid", uuid.toString());
      p.addProperty("username", username);
      p.addProperty("displayName", username);
      p.addProperty("online", isOnline);
      p.addProperty("firstLogin", firstLogin);
      p.addProperty("lastLogin", lastLogin);

      Rank displayRank = rankService.getDisplayRank(uuid);
      JsonObject r = new JsonObject();
      r.addProperty("id", displayRank.getId());
      r.addProperty("name", displayRank.getName());
      r.addProperty("color", displayRank.getColor());
      r.addProperty("prefix", displayRank.getPrefix());
      p.add("rank", r);

      Rank primaryRank = rankService.getPrimaryRank(uuid);
      JsonObject pr = new JsonObject();
      pr.addProperty("id", primaryRank.getId());
      pr.addProperty("name", primaryRank.getName());
      pr.addProperty("color", primaryRank.getColor());
      pr.addProperty("prefix", primaryRank.getPrefix());
      p.add("primaryRank", pr);

      JsonArray ranksArray = new JsonArray();
      for (Rank assigned : rankService.getAllRanksForPlayer(uuid)) {
        JsonObject ar = new JsonObject();
        ar.addProperty("id", assigned.getId());
        ar.addProperty("name", assigned.getName());
        ar.addProperty("color", assigned.getColor());
        ar.addProperty("prefix", assigned.getPrefix());
        ar.addProperty("primary", assigned.isPrimary());
        ranksArray.add(ar);
      }
      p.add("ranks", ranksArray);

      if (isOnline) {
        p.addProperty("ping", onlinePlayer.getPing());
        p.addProperty("afk", afkService.isAfk(onlinePlayer));
        p.addProperty("world", onlinePlayer.getWorld().getName());
        try {
          p.addProperty("biome", onlinePlayer.getLocation().getBlock().getBiome().name());
        } catch (Exception ignored) {
        }
        p.addProperty("gamemode", onlinePlayer.getGameMode().name());
        p.addProperty("health", Math.round(onlinePlayer.getHealth() * 10.0) / 10.0);
        p.addProperty("food", onlinePlayer.getFoodLevel());
        p.addProperty("level", onlinePlayer.getLevel());
      } else {
        p.addProperty("ping", 0);
        p.addProperty("afk", false);
        p.addProperty("world", "Offline");
      }

      JsonObject stats = new JsonObject();
      stats.addProperty(
          "playTimeSeconds", getSafeStat(offlinePlayer, Statistic.PLAY_ONE_MINUTE) / 20);
      stats.addProperty("deaths", getSafeStat(offlinePlayer, Statistic.DEATHS));
      stats.addProperty("mobKills", getSafeStat(offlinePlayer, Statistic.MOB_KILLS));
      stats.addProperty("playerKills", getSafeStat(offlinePlayer, Statistic.PLAYER_KILLS));
      stats.addProperty("damageDealt", getSafeStat(offlinePlayer, Statistic.DAMAGE_DEALT));
      stats.addProperty("damageTaken", getSafeStat(offlinePlayer, Statistic.DAMAGE_TAKEN));
      stats.addProperty(
          "damageBlocked", getSafeStat(offlinePlayer, Statistic.DAMAGE_BLOCKED_BY_SHIELD));
      stats.addProperty("damageResisted", getSafeStat(offlinePlayer, Statistic.DAMAGE_RESISTED));
      stats.addProperty("damageAbsorbed", getSafeStat(offlinePlayer, Statistic.DAMAGE_ABSORBED));
      stats.addProperty("jumps", getSafeStat(offlinePlayer, Statistic.JUMP));
      stats.addProperty(
          "walkDistanceMeters", getSafeStat(offlinePlayer, Statistic.WALK_ONE_CM) / 100);
      stats.addProperty(
          "sprintDistanceMeters", getSafeStat(offlinePlayer, Statistic.SPRINT_ONE_CM) / 100);
      stats.addProperty(
          "flyDistanceMeters", getSafeStat(offlinePlayer, Statistic.FLY_ONE_CM) / 100);
      stats.addProperty(
          "elytraDistanceMeters", getSafeStat(offlinePlayer, Statistic.AVIATE_ONE_CM) / 100);
      stats.addProperty(
          "boatDistanceMeters", getSafeStat(offlinePlayer, Statistic.BOAT_ONE_CM) / 100);
      stats.addProperty(
          "minecartDistanceMeters", getSafeStat(offlinePlayer, Statistic.MINECART_ONE_CM) / 100);
      stats.addProperty(
          "horseDistanceMeters", getSafeStat(offlinePlayer, Statistic.HORSE_ONE_CM) / 100);
      stats.addProperty(
          "swimDistanceMeters", getSafeStat(offlinePlayer, Statistic.SWIM_ONE_CM) / 100);
      stats.addProperty(
          "climbDistanceMeters", getSafeStat(offlinePlayer, Statistic.CLIMB_ONE_CM) / 100);
      stats.addProperty("sneakTimeSeconds", getSafeStat(offlinePlayer, Statistic.SNEAK_TIME) / 20);
      stats.addProperty(
          "timeSinceRestSeconds", getSafeStat(offlinePlayer, Statistic.TIME_SINCE_REST) / 20);
      stats.addProperty("sleeps", getSafeStat(offlinePlayer, Statistic.SLEEP_IN_BED));
      stats.addProperty("chestsOpened", getSafeStat(offlinePlayer, Statistic.CHEST_OPENED));
      stats.addProperty("itemsEnchanted", getSafeStat(offlinePlayer, Statistic.ITEM_ENCHANTED));
      stats.addProperty("fishCaught", getSafeStat(offlinePlayer, Statistic.FISH_CAUGHT));
      stats.addProperty("animalsBred", getSafeStat(offlinePlayer, Statistic.ANIMALS_BRED));
      stats.addProperty("raidsWon", getSafeStat(offlinePlayer, Statistic.RAID_WIN));
      stats.addProperty("raidsTriggered", getSafeStat(offlinePlayer, Statistic.RAID_TRIGGER));
      stats.addProperty("trades", getSafeStat(offlinePlayer, Statistic.TRADED_WITH_VILLAGER));
      stats.addProperty("toolsBroken", getSafeStat(offlinePlayer, Statistic.BREAK_ITEM));
      stats.addProperty("bellRings", getSafeStat(offlinePlayer, Statistic.BELL_RING));
      stats.addProperty("musicDiscsPlayed", getSafeStat(offlinePlayer, Statistic.RECORD_PLAYED));
      p.add("stats", stats);

      JsonObject root = new JsonObject();
      root.addProperty("online", true);
      root.add("player", p);

      byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
      payloadCache.put(cacheKey, new CachedPayload(now, bytes));
      sendRawBytesResponse(exchange, 200, bytes);
    }
  }

  private class LeaderboardsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        sendJsonResponse(exchange, 204, new JsonObject());
        return;
      }

      if (isRateLimited(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Too Many Requests");
        sendJsonResponse(exchange, 429, err);
        return;
      }

      if (!authenticate(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Unauthorized");
        sendJsonResponse(exchange, 401, err);
        return;
      }

      String query = exchange.getRequestURI().getQuery();
      String requestedStat = null;
      if (query != null) {
        for (String param : query.split("&")) {
          String[] pair = param.split("=", 2);
          if (pair.length == 2
              && ("stat".equalsIgnoreCase(pair[0]) || "type".equalsIgnoreCase(pair[0]))) {
            requestedStat = pair[1].trim();
            break;
          }
        }
      }

      long now = System.currentTimeMillis();
      String cacheKey =
          "leaderboards_" + (requestedStat != null ? requestedStat.toLowerCase() : "all");
      CachedPayload cached = payloadCache.get(cacheKey);
      if (cached != null && (now - cached.timestamp()) < 15000L) {
        sendRawBytesResponse(exchange, 200, cached.data());
        return;
      }

      JsonObject root = new JsonObject();
      root.addProperty("online", true);

      if (requestedStat != null && !requestedStat.isBlank()) {
        StatisticType type = StatisticType.fromKey(requestedStat);
        if (type == null) {
          JsonObject err = new JsonObject();
          err.addProperty("error", "Invalid statistic type: " + requestedStat);
          sendJsonResponse(exchange, 400, err);
          return;
        }
        root.add("leaderboard", buildLeaderboardJson(type, 10));
        byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
        payloadCache.put(cacheKey, new CachedPayload(now, bytes));
        sendRawBytesResponse(exchange, 200, bytes);
        return;
      }

      JsonArray list = new JsonArray();
      for (StatisticType type : StatisticType.values()) {
        list.add(buildLeaderboardJson(type, 10));
      }
      root.add("leaderboards", list);

      byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
      payloadCache.put(cacheKey, new CachedPayload(now, bytes));
      sendRawBytesResponse(exchange, 200, bytes);
    }
  }

  private JsonObject buildLeaderboardJson(StatisticType type, int limit) {
    JsonObject obj = new JsonObject();
    obj.addProperty("key", type.getKey());
    obj.addProperty("name", type.getDisplayName());
    obj.addProperty("description", type.getDescription());

    JsonArray topArray = new JsonArray();
    if (statisticService != null) {
      List<Map.Entry<UUID, Long>> top = statisticService.getTopPlayers(type, limit);
      int rankNum = 1;
      for (Map.Entry<UUID, Long> entry : top) {
        UUID uuid = entry.getKey();
        long score = entry.getValue();

        UserData uData = userService != null ? userService.getUser(uuid) : null;
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        String username =
            onlinePlayer != null
                ? onlinePlayer.getName()
                : (uData != null ? uData.username() : Bukkit.getOfflinePlayer(uuid).getName());
        if (username == null || username.isBlank()) {
          username = uuid.toString().substring(0, 8);
        }

        JsonObject item = new JsonObject();
        item.addProperty("position", rankNum);
        item.addProperty("rank", rankNum);
        rankNum++;
        item.addProperty("uuid", uuid.toString());
        item.addProperty("username", username);
        item.addProperty("score", score);
        item.addProperty("formattedValue", type.formatValue(score));

        if (rankService != null) {
          Rank rank = rankService.getDisplayRank(uuid);
          if (rank != null) {
            JsonObject r = new JsonObject();
            r.addProperty("id", rank.getId());
            r.addProperty("name", rank.getName());
            r.addProperty("color", rank.getColor());
            item.add("rankData", r);
            item.add("playerRank", r);
          }
        }

        topArray.add(item);
      }
    }

    obj.add("top", topArray);
    return obj;
  }

  private record WorldDimensionSnapshot(
      String name,
      String environment,
      String difficulty,
      boolean hardcore,
      boolean pvp,
      int seaLevel,
      int minHeight,
      int maxHeight,
      int chunkCount,
      int entityCount,
      int livingEntitiesCount,
      int playerCount,
      int spawnX,
      int spawnY,
      int spawnZ,
      double borderSize,
      double borderCenterX,
      double borderCenterZ,
      double borderBuffer,
      double borderDamage,
      int borderWarning) {}

  private record WorldEnvironmentSnapshot(
      long gameTime,
      long fullTime,
      long timeOfDay,
      boolean hasStorm,
      boolean isThundering,
      int weatherDurationTicks,
      int thunderDurationTicks,
      int clearWeatherDurationTicks,
      List<WorldDimensionSnapshot> dimensions) {}

  private WorldEnvironmentSnapshot captureWorldSnapshotSync() {
    List<World> worlds = Bukkit.getWorlds();
    World mainWorld = null;
    for (World w : worlds) {
      if (w.getEnvironment() == World.Environment.NORMAL) {
        mainWorld = w;
        break;
      }
    }
    if (mainWorld == null && !worlds.isEmpty()) {
      mainWorld = worlds.get(0);
    }

    long gameTime = mainWorld != null ? mainWorld.getGameTime() : 0L;
    long fullTime = mainWorld != null ? mainWorld.getFullTime() : 0L;
    long timeOfDay = mainWorld != null ? mainWorld.getTime() : 0L;
    boolean hasStorm = mainWorld != null && mainWorld.hasStorm();
    boolean isThundering = mainWorld != null && mainWorld.isThundering();
    int weatherDuration = mainWorld != null ? mainWorld.getWeatherDuration() : 0;
    int thunderDuration = mainWorld != null ? mainWorld.getThunderDuration() : 0;
    int clearDuration = mainWorld != null ? mainWorld.getClearWeatherDuration() : 0;

    List<WorldDimensionSnapshot> dims = new ArrayList<>(worlds.size());
    for (World w : worlds) {
      int chunkCount;
      try {
        chunkCount = w.getChunkCount();
      } catch (Throwable ignored) {
        chunkCount = w.getLoadedChunks().length;
      }

      int entityCount;
      try {
        entityCount = w.getEntityCount();
      } catch (Throwable ignored) {
        entityCount = w.getEntities().size();
      }

      int livingCount = 0;
      try {
        livingCount = w.getLivingEntities().size();
      } catch (Throwable ignored) {
      }

      int playerCount = w.getPlayers().size();
      int spawnX = w.getSpawnLocation().getBlockX();
      int spawnY = w.getSpawnLocation().getBlockY();
      int spawnZ = w.getSpawnLocation().getBlockZ();

      WorldBorder b = w.getWorldBorder();
      double bSize = b != null ? b.getSize() : 0.0;
      double bCenterX = b != null && b.getCenter() != null ? b.getCenter().getX() : 0.0;
      double bCenterZ = b != null && b.getCenter() != null ? b.getCenter().getZ() : 0.0;
      double bBuffer = b != null ? b.getDamageBuffer() : 0.0;
      double bDamage = b != null ? b.getDamageAmount() : 0.0;
      int bWarning = b != null ? b.getWarningDistance() : 0;

      dims.add(
          new WorldDimensionSnapshot(
              w.getName(),
              w.getEnvironment().name(),
              w.getDifficulty().name(),
              w.isHardcore(),
              w.getPVP(),
              w.getSeaLevel(),
              w.getMinHeight(),
              w.getMaxHeight(),
              chunkCount,
              entityCount,
              livingCount,
              playerCount,
              spawnX,
              spawnY,
              spawnZ,
              bSize,
              bCenterX,
              bCenterZ,
              bBuffer,
              bDamage,
              bWarning));
    }

    return new WorldEnvironmentSnapshot(
        gameTime,
        fullTime,
        timeOfDay,
        hasStorm,
        isThundering,
        weatherDuration,
        thunderDuration,
        clearDuration,
        dims);
  }

  private class WorldHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        sendJsonResponse(exchange, 204, new JsonObject());
        return;
      }

      if (isRateLimited(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Too Many Requests");
        sendJsonResponse(exchange, 429, err);
        return;
      }

      if (!authenticate(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Unauthorized");
        sendJsonResponse(exchange, 401, err);
        return;
      }

      long now = System.currentTimeMillis();
      CachedPayload cached = payloadCache.get("world");
      if (cached != null && (now - cached.timestamp()) < 5000L) {
        sendRawBytesResponse(exchange, 200, cached.data());
        return;
      }

      WorldEnvironmentSnapshot snapshot = null;
      try {
        Future<WorldEnvironmentSnapshot> future =
            Bukkit.getScheduler()
                .callSyncMethod(plugin, HttpServerService.this::captureWorldSnapshotSync);
        snapshot = future.get(2, TimeUnit.SECONDS);
      } catch (Exception e) {
        plugin.getLogger().warning("Failed to capture sync world snapshot: " + e.getMessage());
      }

      if (snapshot == null) {
        if (cached != null) {
          sendRawBytesResponse(exchange, 200, cached.data());
          return;
        }
        JsonObject err = new JsonObject();
        err.addProperty("online", false);
        sendJsonResponse(exchange, 503, err);
        return;
      }

      JsonObject root = new JsonObject();
      root.addProperty("online", true);

      long gameTime = snapshot.gameTime();
      long fullTime = snapshot.fullTime();
      long timeOfDay = snapshot.timeOfDay();
      long day = gameTime / 24000L;

      JsonObject worldAge = new JsonObject();
      worldAge.addProperty("ticks", gameTime);
      worldAge.addProperty("fullTimeTicks", fullTime);
      worldAge.addProperty("days", day);
      worldAge.addProperty("formatted", "Day " + (day + 1));
      root.add("worldAge", worldAge);

      JsonObject time = new JsonObject();
      time.addProperty("ticks", timeOfDay);
      time.addProperty("timeOfDay", formatClockTime(timeOfDay));
      time.addProperty("phase", getTimePhaseName(timeOfDay));
      time.addProperty("isDay", timeOfDay >= 0 && timeOfDay < 12000);
      root.add("time", time);

      int moonPhaseNum = (int) ((fullTime / 24000) % 8);
      JsonObject moon = new JsonObject();
      moon.addProperty("phase", moonPhaseNum);
      moon.addProperty("name", getMoonPhaseName(fullTime));
      root.add("moonPhase", moon);

      JsonObject weather = new JsonObject();
      boolean hasStorm = snapshot.hasStorm();
      boolean isThundering = snapshot.isThundering();
      weather.addProperty("isRaining", hasStorm);
      weather.addProperty("isThundering", isThundering);
      weather.addProperty("status", isThundering ? "Thunderstorm" : (hasStorm ? "Rain" : "Clear"));
      weather.addProperty("weatherDurationSeconds", snapshot.weatherDurationTicks() / 20);
      weather.addProperty("thunderDurationSeconds", snapshot.thunderDurationTicks() / 20);
      weather.addProperty("clearWeatherDurationSeconds", snapshot.clearWeatherDurationTicks() / 20);
      root.add("weather", weather);

      JsonArray dimensions = new JsonArray();
      for (WorldDimensionSnapshot dimSnap : snapshot.dimensions()) {
        JsonObject dim = new JsonObject();
        dim.addProperty("name", dimSnap.name());
        dim.addProperty("environment", dimSnap.environment());
        dim.addProperty("difficulty", dimSnap.difficulty());
        dim.addProperty("hardcore", dimSnap.hardcore());
        dim.addProperty("pvp", dimSnap.pvp());
        dim.addProperty("seaLevel", dimSnap.seaLevel());
        dim.addProperty("minHeight", dimSnap.minHeight());
        dim.addProperty("maxHeight", dimSnap.maxHeight());
        dim.addProperty("loadedChunks", dimSnap.chunkCount());
        dim.addProperty("entitiesCount", dimSnap.entityCount());
        dim.addProperty("livingEntitiesCount", dimSnap.livingEntitiesCount());
        dim.addProperty("playersCount", dimSnap.playerCount());

        JsonObject spawn = new JsonObject();
        spawn.addProperty("x", dimSnap.spawnX());
        spawn.addProperty("y", dimSnap.spawnY());
        spawn.addProperty("z", dimSnap.spawnZ());
        dim.add("spawn", spawn);

        if (dimSnap.borderSize() > 0) {
          JsonObject wb = new JsonObject();
          wb.addProperty("size", dimSnap.borderSize());
          wb.addProperty("centerX", dimSnap.borderCenterX());
          wb.addProperty("centerZ", dimSnap.borderCenterZ());
          wb.addProperty("damageBuffer", dimSnap.borderBuffer());
          wb.addProperty("damageAmount", dimSnap.borderDamage());
          wb.addProperty("warningDistance", dimSnap.borderWarning());
          dim.add("worldBorder", wb);
        }

        dimensions.add(dim);
      }
      root.add("dimensions", dimensions);

      if (statisticService != null) {
        Map<StatisticType, Long> totals = statisticService.getGlobalAggregates();
        JsonObject aggregates = new JsonObject();
        for (Map.Entry<StatisticType, Long> e : totals.entrySet()) {
          aggregates.addProperty(e.getKey().getKey(), e.getValue());
        }
        root.add("aggregates", aggregates);
      }

      byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
      payloadCache.put("world", new CachedPayload(now, bytes));
      sendRawBytesResponse(exchange, 200, bytes);
    }
  }

  private static String getMoonPhaseName(long fullTime) {
    int phase = (int) ((fullTime / 24000) % 8);
    return switch (phase) {
      case 0 -> "Full Moon";
      case 1 -> "Waning Gibbous";
      case 2 -> "Third Quarter";
      case 3 -> "Waning Crescent";
      case 4 -> "New Moon";
      case 5 -> "Waxing Crescent";
      case 6 -> "First Quarter";
      case 7 -> "Waxing Gibbous";
      default -> "Full Moon";
    };
  }

  private static String getTimePhaseName(long timeOfDay) {
    long t = (timeOfDay % 24000 + 24000) % 24000;
    if (t >= 0 && t < 1000) return "Sunrise / Dawn";
    if (t >= 1000 && t < 6000) return "Morning";
    if (t >= 6000 && t < 11000) return "Afternoon";
    if (t >= 11000 && t < 13000) return "Sunset / Dusk";
    if (t >= 13000 && t < 18000) return "Night";
    if (t >= 18000 && t < 22000) return "Midnight";
    return "Late Night";
  }

  private static String formatClockTime(long timeOfDay) {
    long t = (timeOfDay + 6000) % 24000;
    if (t < 0) t += 24000;
    long hours = t / 1000;
    long minutes = (t % 1000) * 60 / 1000;
    return String.format(Locale.US, "%02d:%02d", hours, minutes);
  }
}
