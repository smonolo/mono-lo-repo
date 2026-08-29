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
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HttpServerService {

  private static final Gson GSON = new Gson();

  private final SMEssential plugin;
  private final RankService rankService;
  private final AfkService afkService;
  private final UserService userService;
  private HttpServer server;
  private ExecutorService executor;

  private boolean enabled;
  private String host;
  private int port;
  private String secret;

  public HttpServerService(
      @NotNull SMEssential plugin,
      @NotNull RankService rankService,
      @NotNull AfkService afkService,
      @NotNull UserService userService) {
    this.plugin = plugin;
    this.rankService = rankService;
    this.afkService = afkService;
    this.userService = userService;
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

      server.createContext("/api/status", new StatusHandler());
      server.createContext("/api/players", new PlayersListHandler());
      server.createContext("/api/player", new SinglePlayerHandler());

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
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
    exchange
        .getResponseHeaders()
        .set("Access-Control-Allow-Headers", "Authorization, Content-Type");
    exchange.sendResponseHeaders(statusCode, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  private int getSafeStat(OfflinePlayer player, Statistic stat) {
    try {
      return player.getStatistic(stat);
    } catch (Throwable ignored) {
      return 0;
    }
  }

  private class StatusHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        sendJsonResponse(exchange, 204, new JsonObject());
        return;
      }

      if (!authenticate(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Unauthorized");
        sendJsonResponse(exchange, 401, err);
        return;
      }

      JsonObject root = new JsonObject();
      root.addProperty("online", true);
      root.addProperty("onlinePlayers", Bukkit.getOnlinePlayers().size());
      root.addProperty("maxPlayers", Bukkit.getMaxPlayers());
      root.addProperty("version", plugin.getPluginMeta().getVersion());
      root.addProperty("minecraftVersion", Bukkit.getMinecraftVersion());

      double[] tps = Bukkit.getTPS();
      root.addProperty("tps", Math.min(20.0, Math.round(tps[0] * 100.0) / 100.0));
      root.addProperty("mspt", Math.round(Bukkit.getAverageTickTime() * 10.0) / 10.0);

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

      sendJsonResponse(exchange, 200, root);
    }
  }

  private class PlayersListHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        sendJsonResponse(exchange, 204, new JsonObject());
        return;
      }

      if (!authenticate(exchange)) {
        JsonObject err = new JsonObject();
        err.addProperty("error", "Unauthorized");
        sendJsonResponse(exchange, 401, err);
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
      sendJsonResponse(exchange, 200, root);
    }
  }

  private class SinglePlayerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        sendJsonResponse(exchange, 204, new JsonObject());
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
      stats.addProperty("jumps", getSafeStat(offlinePlayer, Statistic.JUMP));
      stats.addProperty(
          "walkDistanceMeters", getSafeStat(offlinePlayer, Statistic.WALK_ONE_CM) / 100);
      stats.addProperty(
          "flyDistanceMeters", getSafeStat(offlinePlayer, Statistic.FLY_ONE_CM) / 100);
      stats.addProperty(
          "timeSinceRestSeconds", getSafeStat(offlinePlayer, Statistic.TIME_SINCE_REST) / 20);
      p.add("stats", stats);

      JsonObject root = new JsonObject();
      root.addProperty("online", true);
      root.add("player", p);
      sendJsonResponse(exchange, 200, root);
    }
  }
}
