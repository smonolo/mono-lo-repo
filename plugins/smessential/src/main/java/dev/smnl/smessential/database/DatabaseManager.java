package dev.smnl.smessential.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.smnl.smessential.model.LeaderboardData;
import dev.smnl.smessential.model.Rank;
import dev.smnl.smessential.model.StatisticType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseManager {

  public record PunishmentData(
      String id,
      UUID uuid,
      String type,
      String username,
      String reason,
      String issuer,
      long createdAt,
      long expiresAt,
      long unpunishedAt,
      @Nullable String unpunishedBy) {
    public boolean isActive() {
      return unpunishedAt <= 0;
    }
  }

  public record MuteData(
      String username,
      String reason,
      String issuer,
      long createdAt,
      long expiresAt,
      long unpunishedAt,
      @Nullable String unpunishedBy) {
    public MuteData(String username, String reason, String issuer, long createdAt) {
      this(username, reason, issuer, createdAt, 0L, 0L, null);
    }

    public boolean isActive() {
      return unpunishedAt <= 0;
    }
  }

  public record BanData(
      String username,
      String reason,
      String issuer,
      long createdAt,
      long expiresAt,
      long unpunishedAt,
      @Nullable String unpunishedBy) {
    public BanData(String username, String reason, String issuer, long createdAt) {
      this(username, reason, issuer, createdAt, 0L, 0L, null);
    }

    public boolean isActive() {
      return unpunishedAt <= 0;
    }
  }

  public record WhitelistData(
      @Nullable UUID uuid, @Nullable String username, @NotNull String addedBy, long addedAt) {
    public @NotNull String getDisplayName() {
      if (username != null && !username.isBlank()) {
        return username;
      }
      if (uuid != null) {
        return uuid.toString();
      }
      return "Unknown";
    }
  }

  public record UserData(UUID uuid, String username, long firstJoin, long lastJoin) {}

  public record DatabaseInfo(
      boolean connected,
      String host,
      int port,
      String database,
      String schema,
      String username,
      boolean ssl,
      int activeConnections,
      int idleConnections,
      int totalConnections,
      int maxConnections,
      int threadsAwaitingConnection,
      long pingMs) {}

  private final JavaPlugin plugin;
  private HikariDataSource dataSource;

  private String host = "127.0.0.1";
  private int port = 5432;
  private String database = "postgres";
  private String schema = "minecraft";
  private String username = "postgres";
  private boolean ssl = false;

  public DatabaseManager(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void init() {
    try {
      this.host = resolveConfigString("database.host", "127.0.0.1");
      this.port = resolveConfigInt("database.port", 5432);
      this.database = resolveConfigString("database.database", "postgres");
      this.schema = resolveConfigString("database.schema", "minecraft");
      this.username = resolveConfigString("database.username", "postgres");
      String password = resolveConfigString("database.password", "password");
      this.ssl = resolveConfigBoolean("database.ssl", false);

      int maxPoolSize = resolveConfigInt("database.pool.maximum-pool-size", 3);
      int minIdle = resolveConfigInt("database.pool.minimum-idle", 1);
      long connTimeout = resolveConfigLong("database.pool.connection-timeout-ms", 3000L);
      long idleTimeout = resolveConfigLong("database.pool.idle-timeout-ms", 600000L);
      long maxLifetime = resolveConfigLong("database.pool.max-lifetime-ms", 1800000L);

      HikariConfig config = new HikariConfig();
      config.setDriverClassName("org.postgresql.Driver");
      config.setJdbcUrl(
          "jdbc:postgresql://"
              + host
              + ":"
              + port
              + "/"
              + database
              + "?currentSchema="
              + schema
              + "&ssl="
              + ssl);
      config.setUsername(username);
      config.setPassword(password);
      config.setSchema(schema);
      config.addDataSourceProperty("currentSchema", schema);

      config.setMaximumPoolSize(maxPoolSize);
      config.setMinimumIdle(minIdle);
      config.setConnectionTimeout(connTimeout);
      config.setIdleTimeout(idleTimeout);
      config.setMaxLifetime(maxLifetime);
      config.setInitializationFailTimeout(2000L);
      config.setPoolName("SMEssential-PostgresPool");

      this.dataSource = new HikariDataSource(config);

      applySchema(schema);

      plugin
          .getLogger()
          .info(
              "PostgreSQL connected successfully: "
                  + host
                  + ":"
                  + port
                  + "/"
                  + database
                  + " (schema: "
                  + schema
                  + ")");

    } catch (Exception e) {
      plugin.getLogger().severe("Failed to initialize PostgreSQL database: " + e.getMessage());
    }
  }

  private void applySchema(String schema) throws SQLException, IOException {
    String sanitizedSchema = schema.replace("\"", "\"\"");
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement()) {
      try {
        stmt.execute("CREATE SCHEMA IF NOT EXISTS \"" + sanitizedSchema + "\";");
      } catch (SQLException e) {
        plugin
            .getLogger()
            .info(
                "Notice: Could not run CREATE SCHEMA for '"
                    + schema
                    + "' (user might lack DB create permission; proceeding with existing schema)");
      }
      try {
        stmt.execute("SET search_path TO \"" + sanitizedSchema + "\";");
      } catch (SQLException e) {
        plugin
            .getLogger()
            .warning("Could not set search_path to '" + schema + "': " + e.getMessage());
      }

      try (InputStream in = plugin.getResource("schema.sql")) {
        if (in == null) {
          plugin
              .getLogger()
              .warning("schema.sql resource not found, skipping schema initialization.");
          return;
        }
        String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        for (String statement : sql.split(";")) {
          String trimmed = statement.trim();
          if (!trimmed.isEmpty()) {
            stmt.execute(trimmed);
          }
        }
      }
    }
  }

  private String resolveConfigString(String path, String defaultValue) {
    Object raw = plugin.getConfig().get(path);
    if (raw == null) return defaultValue;
    return resolveEnv(raw.toString().trim());
  }

  private int resolveConfigInt(String path, int defaultValue) {
    Object raw = plugin.getConfig().get(path);
    if (raw == null) return defaultValue;
    if (raw instanceof Number number) {
      return number.intValue();
    }
    String resolved = resolveEnv(raw.toString().trim());
    try {
      return Integer.parseInt(resolved);
    } catch (NumberFormatException e) {
      plugin
          .getLogger()
          .warning(
              "Invalid integer config for "
                  + path
                  + ": '"
                  + raw
                  + "', falling back to "
                  + defaultValue);
      return defaultValue;
    }
  }

  private long resolveConfigLong(String path, long defaultValue) {
    Object raw = plugin.getConfig().get(path);
    if (raw == null) return defaultValue;
    if (raw instanceof Number number) {
      return number.longValue();
    }
    String resolved = resolveEnv(raw.toString().trim());
    try {
      return Long.parseLong(resolved);
    } catch (NumberFormatException e) {
      plugin
          .getLogger()
          .warning(
              "Invalid long config for "
                  + path
                  + ": '"
                  + raw
                  + "', falling back to "
                  + defaultValue);
      return defaultValue;
    }
  }

  private boolean resolveConfigBoolean(String path, boolean defaultValue) {
    Object raw = plugin.getConfig().get(path);
    if (raw == null) return defaultValue;
    if (raw instanceof Boolean bool) {
      return bool;
    }
    String resolved = resolveEnv(raw.toString().trim());
    return Boolean.parseBoolean(resolved);
  }

  private String resolveEnv(String val) {
    if (val.startsWith("${") && val.endsWith("}")) {
      String inner = val.substring(2, val.length() - 1);
      int colonIdx = inner.indexOf(':');
      if (colonIdx != -1) {
        String envName = inner.substring(0, colonIdx);
        String fallback = inner.substring(colonIdx + 1);
        String envVal = System.getenv(envName);
        return (envVal != null && !envVal.isEmpty()) ? envVal : fallback;
      } else {
        String envVal = System.getenv(inner);
        return envVal != null ? envVal : "";
      }
    }
    return val;
  }

  public @NotNull DatabaseInfo getDatabaseInfo() {
    boolean connected = dataSource != null && !dataSource.isClosed();
    int active = 0;
    int idle = 0;
    int total = 0;
    int max = 0;
    int awaiting = 0;

    if (connected) {
      max = dataSource.getMaximumPoolSize();
      var poolMx = dataSource.getHikariPoolMXBean();
      if (poolMx != null) {
        active = poolMx.getActiveConnections();
        idle = poolMx.getIdleConnections();
        total = poolMx.getTotalConnections();
        awaiting = poolMx.getThreadsAwaitingConnection();
      }
    }

    long pingMs = -1;
    if (connected) {
      long start = System.nanoTime();
      try (Connection connection = dataSource.getConnection();
          Statement stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery("SELECT 1;")) {
        pingMs = (System.nanoTime() - start) / 1_000_000;
      } catch (SQLException e) {
        connected = false;
      }
    }

    return new DatabaseInfo(
        connected,
        this.host,
        this.port,
        this.database,
        this.schema,
        this.username,
        this.ssl,
        active,
        idle,
        total,
        max,
        awaiting,
        pingMs);
  }

  private void executeUpdate(String sql, Object... params) {
    if (dataSource == null || dataSource.isClosed()) return;
    try (Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
      for (int i = 0; i < params.length; i++) {
        pstmt.setObject(i + 1, params[i]);
      }
      pstmt.executeUpdate();
    } catch (SQLException e) {
      plugin.getLogger().severe("Database update error: " + e.getMessage());
    }
  }

  public void saveAlert(@NotNull String target, @Nullable String message) {
    if (message == null || message.isBlank()) {
      deleteAlert(target);
      return;
    }
    String sql =
        "INSERT INTO smessential_alerts (target, message) VALUES (?, ?) "
            + "ON CONFLICT(target) DO UPDATE SET message = EXCLUDED.message;";
    executeUpdate(sql, target.toUpperCase(), message);
  }

  public void deleteAlert(@NotNull String target) {
    executeUpdate("DELETE FROM smessential_alerts WHERE UPPER(target) = UPPER(?);", target);
  }

  public @Nullable String getAlert(@NotNull String target) {
    if (dataSource == null || dataSource.isClosed()) return null;
    String sql = "SELECT message FROM smessential_alerts WHERE UPPER(target) = UPPER(?);";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, target);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("message");
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database query error: " + e.getMessage());
    }
    return null;
  }

  public void savePunishment(
      @NotNull String uuid,
      @NotNull String type,
      @NotNull String username,
      @NotNull String reason,
      @NotNull String issuer,
      long createdAt,
      long expiresAt) {
    String id = UUID.randomUUID().toString();
    String sql =
        "INSERT INTO smessential_punishments (id, uuid, type, username, reason, issuer, created_at, expires_at, unpunished_at, unpunished_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, NULL);";
    executeUpdate(
        sql,
        id,
        uuid,
        type.toUpperCase(Locale.ROOT),
        username,
        reason,
        issuer,
        createdAt,
        expiresAt);
  }

  public void savePunishment(
      @NotNull String uuid,
      @NotNull String type,
      @NotNull String username,
      @NotNull String reason,
      @NotNull String issuer,
      long createdAt) {
    savePunishment(uuid, type, username, reason, issuer, createdAt, 0L);
  }

  public void unpunish(
      @NotNull String uuid,
      @NotNull String type,
      @Nullable String unpunishedBy,
      long unpunishedAt) {
    String sql =
        "UPDATE smessential_punishments SET unpunished_at = ?, unpunished_by = ? "
            + "WHERE uuid = ? AND UPPER(type) = UPPER(?) AND (unpunished_at IS NULL OR unpunished_at <= 0);";
    executeUpdate(sql, unpunishedAt, unpunishedBy, uuid, type);
  }

  public void unpunishByUsername(
      @NotNull String username,
      @NotNull String type,
      @Nullable String unpunishedBy,
      long unpunishedAt) {
    String sql =
        "UPDATE smessential_punishments SET unpunished_at = ?, unpunished_by = ? "
            + "WHERE LOWER(username) = LOWER(?) AND UPPER(type) = UPPER(?) AND (unpunished_at IS NULL OR unpunished_at <= 0);";
    executeUpdate(sql, unpunishedAt, unpunishedBy, username, type);
  }

  public void deletePunishment(@NotNull String uuid, @NotNull String type) {
    unpunish(uuid, type, "CONSOLE", System.currentTimeMillis());
  }

  public void deletePunishmentByUsername(@NotNull String username, @NotNull String type) {
    unpunishByUsername(username, type, "CONSOLE", System.currentTimeMillis());
  }

  public void saveMute(
      @NotNull String uuid,
      @NotNull String username,
      @NotNull String reason,
      @NotNull String issuer,
      long createdAt,
      long expiresAt) {
    savePunishment(uuid, "MUTE", username, reason, issuer, createdAt, expiresAt);
  }

  public void saveMute(
      @NotNull String uuid,
      @NotNull String username,
      @NotNull String reason,
      @NotNull String issuer,
      long createdAt) {
    saveMute(uuid, username, reason, issuer, createdAt, 0L);
  }

  public void unmutePlayer(@NotNull String uuid, @Nullable String unmutedBy, long unmutedAt) {
    unpunish(uuid, "MUTE", unmutedBy, unmutedAt);
  }

  public void deleteMute(@NotNull String uuid) {
    unmutePlayer(uuid, "CONSOLE", System.currentTimeMillis());
  }

  public void deleteMuteByUsername(@NotNull String username) {
    unpunishByUsername(username, "MUTE", "CONSOLE", System.currentTimeMillis());
  }

  public void saveBan(
      @NotNull String uuid,
      @NotNull String username,
      @NotNull String reason,
      @NotNull String issuer,
      long createdAt,
      long expiresAt) {
    savePunishment(uuid, "BAN", username, reason, issuer, createdAt, expiresAt);
  }

  public void saveBan(
      @NotNull String uuid,
      @NotNull String username,
      @NotNull String reason,
      @NotNull String issuer,
      long createdAt) {
    saveBan(uuid, username, reason, issuer, createdAt, 0L);
  }

  public void unbanPlayer(@NotNull String uuid, @Nullable String unbannedBy, long unbannedAt) {
    unpunish(uuid, "BAN", unbannedBy, unbannedAt);
  }

  public void deleteBan(@NotNull String uuid) {
    unbanPlayer(uuid, "CONSOLE", System.currentTimeMillis());
  }

  public void deleteBanByUsername(@NotNull String username) {
    unpunishByUsername(username, "BAN", "CONSOLE", System.currentTimeMillis());
  }

  @FunctionalInterface
  private interface PunishmentRecordFactory<T> {
    T create(
        String username,
        String reason,
        String issuer,
        long createdAt,
        long expiresAt,
        long unpunishedAt,
        String unpunishedBy);
  }

  private <T> Map<UUID, T> loadPunishmentsByType(String type, PunishmentRecordFactory<T> factory) {
    Map<UUID, T> map = new HashMap<>();
    if (dataSource == null || dataSource.isClosed()) return map;
    String sql =
        "SELECT uuid, username, reason, issuer, created_at, expires_at, unpunished_at, unpunished_by FROM smessential_punishments WHERE"
            + " UPPER(type) = UPPER(?) AND (unpunished_at IS NULL OR unpunished_at <= 0) "
            + "ORDER BY created_at DESC;";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, type);
      try (ResultSet rs = pstmt.executeQuery()) {
        long now = System.currentTimeMillis();
        while (rs.next()) {
          try {
            String uuidStr = rs.getString("uuid");
            if (uuidStr == null || uuidStr.isBlank()) continue;
            UUID uuid = UUID.fromString(uuidStr);
            if (map.containsKey(uuid)) continue;

            String username = rs.getString("username");
            String reason = rs.getString("reason");
            String issuer = rs.getString("issuer");
            if (issuer == null || issuer.isBlank()) issuer = "CONSOLE";
            long createdAt = rs.getLong("created_at");
            if (createdAt <= 0) createdAt = now;
            long expiresAt = rs.getLong("expires_at");
            long unpunishedAt = rs.getLong("unpunished_at");
            String unpunishedBy = rs.getString("unpunished_by");

            if (unpunishedAt > 0) continue;
            if (expiresAt > 0 && now >= expiresAt) continue;

            map.put(
                uuid,
                factory.create(
                    username, reason, issuer, createdAt, expiresAt, unpunishedAt, unpunishedBy));
          } catch (IllegalArgumentException ignored) {
          }
        }
      }
    } catch (SQLException e) {
      plugin
          .getLogger()
          .severe("Database load error (punishments: " + type + "): " + e.getMessage());
    }
    return map;
  }

  public @NotNull Map<UUID, MuteData> loadAllMuteEntries() {
    return loadPunishmentsByType("MUTE", MuteData::new);
  }

  public @NotNull Map<UUID, BanData> loadAllBanEntries() {
    return loadPunishmentsByType("BAN", BanData::new);
  }

  public @NotNull List<PunishmentData> loadPlayerPunishmentHistory(@NotNull UUID uuid) {
    List<PunishmentData> list = new ArrayList<>();
    if (dataSource == null || dataSource.isClosed()) return list;
    String sql =
        "SELECT id, uuid, type, username, reason, issuer, created_at, expires_at, unpunished_at, unpunished_by "
            + "FROM smessential_punishments WHERE uuid = ? ORDER BY created_at DESC;";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, uuid.toString());
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String id = rs.getString("id");
          String type = rs.getString("type");
          String username = rs.getString("username");
          String reason = rs.getString("reason");
          String issuer = rs.getString("issuer");
          long createdAt = rs.getLong("created_at");
          long expiresAt = rs.getLong("expires_at");
          long unpunishedAt = rs.getLong("unpunished_at");
          String unpunishedBy = rs.getString("unpunished_by");
          list.add(
              new PunishmentData(
                  id,
                  uuid,
                  type,
                  username,
                  reason,
                  issuer,
                  createdAt,
                  expiresAt,
                  unpunishedAt,
                  unpunishedBy));
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (history): " + e.getMessage());
    }
    return list;
  }

  public void saveSetting(@NotNull String key, @Nullable String value) {
    if (value == null) {
      deleteSetting(key);
      return;
    }
    String sql =
        "INSERT INTO smessential_settings (key, value) VALUES (?, ?) "
            + "ON CONFLICT(key) DO UPDATE SET value = EXCLUDED.value;";
    executeUpdate(sql, key.toLowerCase(Locale.ROOT), value);
  }

  public void deleteSetting(@NotNull String key) {
    executeUpdate("DELETE FROM smessential_settings WHERE LOWER(key) = LOWER(?);", key);
  }

  public @Nullable String getSetting(@NotNull String key) {
    if (dataSource == null || dataSource.isClosed()) return null;
    String sql = "SELECT value FROM smessential_settings WHERE LOWER(key) = LOWER(?);";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, key.toLowerCase(Locale.ROOT));
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("value");
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database query error (settings): " + e.getMessage());
    }
    return null;
  }

  public boolean getBooleanSetting(@NotNull String key, boolean defaultValue) {
    String val = getSetting(key);
    if (val == null) return defaultValue;
    return Boolean.parseBoolean(val);
  }

  public void saveBooleanSetting(@NotNull String key, boolean value) {
    saveSetting(key, String.valueOf(value));
  }

  public boolean loadWhitelistState() {
    return getBooleanSetting("whitelist_enabled", false);
  }

  public void saveWhitelistState(boolean enabled) {
    saveBooleanSetting("whitelist_enabled", enabled);
  }

  public boolean loadGlobalMuteState() {
    return getBooleanSetting("global_mute_enabled", false);
  }

  public void saveGlobalMuteState(boolean enabled) {
    saveBooleanSetting("global_mute_enabled", enabled);
  }

  public void saveWhitelistPlayer(
      @Nullable UUID uuid, @Nullable String name, @NotNull String addedBy, long addedAt) {
    String uuidStr = uuid != null ? uuid.toString() : null;
    String nameStr = name != null ? name : (uuidStr != null ? uuidStr : "");
    if (uuidStr != null && name != null) {
      executeUpdate(
          "DELETE FROM smessential_whitelist WHERE uuid = ? OR LOWER(name) = LOWER(?);",
          uuidStr,
          name);
    } else if (uuidStr != null) {
      executeUpdate(
          "DELETE FROM smessential_whitelist WHERE uuid = ? OR LOWER(name) = LOWER(?);",
          uuidStr,
          uuidStr);
    } else {
      executeUpdate("DELETE FROM smessential_whitelist WHERE LOWER(name) = LOWER(?);", nameStr);
    }

    String sql =
        "INSERT INTO smessential_whitelist (uuid, name, added_by, added_at) VALUES (?, ?, ?, ?);";
    executeUpdate(sql, uuidStr, nameStr, addedBy, addedAt);
  }

  public void addWhitelistPlayer(@NotNull String name, @NotNull String addedBy, long addedAt) {
    saveWhitelistPlayer(null, name, addedBy, addedAt);
  }

  public void removeWhitelistPlayer(@Nullable UUID uuid, @Nullable String name) {
    String uuidStr = uuid != null ? uuid.toString() : null;
    if (uuidStr != null && name != null) {
      executeUpdate(
          "DELETE FROM smessential_whitelist WHERE uuid = ? OR LOWER(name) = LOWER(?);",
          uuidStr,
          name);
    } else if (uuidStr != null) {
      executeUpdate(
          "DELETE FROM smessential_whitelist WHERE uuid = ? OR LOWER(name) = LOWER(?);",
          uuidStr,
          uuidStr);
    } else if (name != null) {
      executeUpdate("DELETE FROM smessential_whitelist WHERE LOWER(name) = LOWER(?);", name);
    }
  }

  public void removeWhitelistPlayer(@NotNull String name) {
    removeWhitelistPlayer(null, name);
  }

  public void clearWhitelistPlayers() {
    executeUpdate("DELETE FROM smessential_whitelist;");
  }

  public @NotNull List<WhitelistData> loadAllWhitelistPlayers() {
    List<WhitelistData> list = new ArrayList<>();
    if (dataSource == null || dataSource.isClosed()) return list;
    String sql = "SELECT uuid, name, added_by, added_at FROM smessential_whitelist;";
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        String uuidStr = rs.getString("uuid");
        String name = rs.getString("name");
        String addedBy = rs.getString("added_by");
        if (addedBy == null || addedBy.isBlank()) addedBy = "Console";
        long addedAt = rs.getLong("added_at");
        if (addedAt <= 0) addedAt = System.currentTimeMillis();

        UUID uuid = null;
        if (uuidStr != null && !uuidStr.isBlank()) {
          try {
            uuid = UUID.fromString(uuidStr);
          } catch (IllegalArgumentException ignored) {
          }
        }
        if (uuid == null && name != null && !name.isBlank()) {
          try {
            uuid = UUID.fromString(name);
            name = null;
          } catch (IllegalArgumentException ignored) {
          }
        }

        if (uuid != null || (name != null && !name.isBlank())) {
          list.add(new WhitelistData(uuid, name, addedBy, addedAt));
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (whitelist): " + e.getMessage());
    }
    return list;
  }

  public void saveUser(@NotNull UUID uuid, @NotNull String username, long joinTime) {
    String sql =
        "INSERT INTO smessential_users (uuid, username, first_join, last_join) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT(uuid) DO UPDATE SET username = EXCLUDED.username, last_join = EXCLUDED.last_join;";
    executeUpdate(sql, uuid.toString(), username, joinTime, joinTime);
  }

  public @NotNull Map<UUID, UserData> loadAllUsers() {
    Map<UUID, UserData> map = new HashMap<>();
    if (dataSource == null || dataSource.isClosed()) return map;
    String sql = "SELECT uuid, username, first_join, last_join FROM smessential_users;";
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        try {
          String uuidStr = rs.getString("uuid");
          if (uuidStr == null || uuidStr.isBlank()) continue;
          UUID uuid = UUID.fromString(uuidStr);
          String username = rs.getString("username");
          long firstJoin = rs.getLong("first_join");
          long lastJoin = rs.getLong("last_join");
          if (firstJoin <= 0) firstJoin = System.currentTimeMillis();
          if (lastJoin <= 0) lastJoin = firstJoin;
          if (username != null && !username.isBlank()) {
            map.put(uuid, new UserData(uuid, username, firstJoin, lastJoin));
          }
        } catch (IllegalArgumentException ignored) {
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (users): " + e.getMessage());
    }
    return map;
  }

  public void saveLeaderboard(@NotNull LeaderboardData data) {
    String sql =
        "INSERT INTO smessential_leaderboards (id, stat_key, world, x, y, z, yaw, pitch, display_limit, width, height) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT(id) DO UPDATE SET "
            + "stat_key = EXCLUDED.stat_key, world = EXCLUDED.world, x = EXCLUDED.x, y = EXCLUDED.y, "
            + "z = EXCLUDED.z, yaw = EXCLUDED.yaw, pitch = EXCLUDED.pitch, display_limit = EXCLUDED.display_limit, "
            + "width = EXCLUDED.width, height = EXCLUDED.height;";
    executeUpdate(
        sql,
        data.id().toLowerCase(Locale.ROOT),
        data.statType().getKey(),
        data.worldName(),
        data.x(),
        data.y(),
        data.z(),
        data.yaw(),
        data.pitch(),
        data.limit(),
        data.width(),
        data.height());
  }

  public void deleteLeaderboard(@NotNull String id) {
    executeUpdate("DELETE FROM smessential_leaderboards WHERE LOWER(id) = LOWER(?);", id);
  }

  public @NotNull Map<String, LeaderboardData> loadAllLeaderboards() {
    Map<String, LeaderboardData> map = new HashMap<>();
    if (dataSource == null || dataSource.isClosed()) return map;
    String sql =
        "SELECT id, stat_key, world, x, y, z, yaw, pitch, display_limit, width, height FROM smessential_leaderboards;";
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        try {
          String id = rs.getString("id");
          String statKey = rs.getString("stat_key");
          StatisticType statType = StatisticType.fromKey(statKey);
          if (id == null || id.isBlank() || statType == null) continue;
          String world = rs.getString("world");
          double x = rs.getDouble("x");
          double y = rs.getDouble("y");
          double z = rs.getDouble("z");
          float yaw = rs.getFloat("yaw");
          float pitch = rs.getFloat("pitch");
          int limit = rs.getInt("display_limit");
          if (limit <= 0) limit = 10;
          int width = rs.getInt("width");
          if (width <= 0) width = 1;
          int height = rs.getInt("height");
          if (height <= 0) height = 1;
          map.put(
              id.toLowerCase(Locale.ROOT),
              new LeaderboardData(
                  id.toLowerCase(Locale.ROOT),
                  statType,
                  world,
                  x,
                  y,
                  z,
                  yaw,
                  pitch,
                  limit,
                  width,
                  height));
        } catch (Exception ignored) {
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (leaderboards): " + e.getMessage());
    }
    return map;
  }

  public void saveRank(@NotNull Rank rank) {
    String sql =
        "INSERT INTO smessential_ranks (id, name, color, prefix, weight, is_default, is_primary) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT(id) DO UPDATE SET "
            + "name = EXCLUDED.name, color = EXCLUDED.color, prefix = EXCLUDED.prefix, "
            + "weight = EXCLUDED.weight, is_default = EXCLUDED.is_default, is_primary = EXCLUDED.is_primary;";
    executeUpdate(
        sql,
        rank.getId().toLowerCase(),
        rank.getName(),
        rank.getColor(),
        rank.getPrefix(),
        rank.getWeight(),
        rank.isDefault(),
        rank.isPrimary());
  }

  public void deleteRank(@NotNull String rankId) {
    executeUpdate(
        "DELETE FROM smessential_rank_permissions WHERE LOWER(rank_id) = LOWER(?);", rankId);
    executeUpdate(
        "DELETE FROM smessential_rank_inheritance WHERE LOWER(rank_id) = LOWER(?) OR LOWER(parent_id) = LOWER(?);",
        rankId,
        rankId);
    executeUpdate("DELETE FROM smessential_user_ranks WHERE LOWER(rank_id) = LOWER(?);", rankId);
    executeUpdate(
        "DELETE FROM smessential_user_display_ranks WHERE LOWER(rank_id) = LOWER(?);", rankId);
    executeUpdate("DELETE FROM smessential_ranks WHERE LOWER(id) = LOWER(?);", rankId);
  }

  public @NotNull Map<String, Rank> loadAllRanks() {
    Map<String, Rank> map = new HashMap<>();
    if (dataSource == null || dataSource.isClosed()) return map;
    String sql =
        "SELECT id, name, color, prefix, weight, is_default, is_primary FROM smessential_ranks;";
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        try {
          String id = rs.getString("id");
          String name = rs.getString("name");
          String color = rs.getString("color");
          String prefix = rs.getString("prefix");
          int weight = rs.getInt("weight");
          boolean isDefault = rs.getBoolean("is_default");
          boolean isPrimary = true;
          try {
            Object obj = rs.getObject("is_primary");
            if (obj != null) {
              isPrimary = rs.getBoolean("is_primary");
            }
          } catch (Throwable ignored) {
          }
          if (id != null && !id.isBlank()) {
            map.put(
                id.toLowerCase(),
                new Rank(
                    id.toLowerCase(),
                    name != null ? name : id,
                    color != null ? color : "white",
                    prefix != null ? prefix : "",
                    weight,
                    isDefault,
                    isPrimary,
                    new HashSet<>()));
          }
        } catch (Exception ignored) {
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (ranks): " + e.getMessage());
    }
    return map;
  }

  public void addPlayerRank(@NotNull UUID uuid, @NotNull String rankId) {
    String sql =
        "INSERT INTO smessential_user_ranks (uuid, rank_id) VALUES (?, ?) "
            + "ON CONFLICT(uuid, rank_id) DO NOTHING;";
    executeUpdate(sql, uuid.toString(), rankId.toLowerCase());
  }

  public void removePlayerRank(@NotNull UUID uuid, @NotNull String rankId) {
    String sql = "DELETE FROM smessential_user_ranks WHERE uuid = ? AND LOWER(rank_id) = LOWER(?);";
    executeUpdate(sql, uuid.toString(), rankId.toLowerCase());
  }

  public @NotNull Map<UUID, Set<String>> loadAllUserRanks() {
    Map<UUID, Set<String>> map = new HashMap<>();
    if (dataSource == null || dataSource.isClosed()) return map;
    String sql = "SELECT uuid, rank_id FROM smessential_user_ranks;";
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        try {
          String uuidStr = rs.getString("uuid");
          String rankId = rs.getString("rank_id");
          if (uuidStr != null && rankId != null && !rankId.isBlank()) {
            UUID uuid = UUID.fromString(uuidStr);
            map.computeIfAbsent(uuid, k -> new HashSet<>()).add(rankId.toLowerCase());
          }
        } catch (Exception ignored) {
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (user ranks): " + e.getMessage());
    }
    return map;
  }

  public void savePlayerDisplayRank(@NotNull UUID uuid, @NotNull String rankId) {
    String sql =
        "INSERT INTO smessential_user_display_ranks (uuid, rank_id) VALUES (?, ?) "
            + "ON CONFLICT(uuid) DO UPDATE SET rank_id = EXCLUDED.rank_id;";
    executeUpdate(sql, uuid.toString(), rankId.toLowerCase());
  }

  public void removePlayerDisplayRank(@NotNull UUID uuid) {
    String sql = "DELETE FROM smessential_user_display_ranks WHERE uuid = ?;";
    executeUpdate(sql, uuid.toString());
  }

  public @NotNull Map<UUID, String> loadAllUserDisplayRanks() {
    Map<UUID, String> map = new HashMap<>();
    if (dataSource == null || dataSource.isClosed()) return map;
    String sql = "SELECT uuid, rank_id FROM smessential_user_display_ranks;";
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        try {
          String uuidStr = rs.getString("uuid");
          String rankId = rs.getString("rank_id");
          if (uuidStr != null && rankId != null && !rankId.isBlank()) {
            map.put(UUID.fromString(uuidStr), rankId.toLowerCase());
          }
        } catch (Exception ignored) {
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (user display ranks): " + e.getMessage());
    }
    return map;
  }

  public void addRankPermission(@NotNull String rankId, @NotNull String permission) {
    String sql =
        "INSERT INTO smessential_rank_permissions (rank_id, permission) VALUES (?, ?) "
            + "ON CONFLICT(rank_id, permission) DO NOTHING;";
    executeUpdate(sql, rankId.toLowerCase(), permission.toLowerCase());
  }

  public void removeRankPermission(@NotNull String rankId, @NotNull String permission) {
    String sql =
        "DELETE FROM smessential_rank_permissions WHERE LOWER(rank_id) = LOWER(?) AND LOWER(permission) = LOWER(?);";
    executeUpdate(sql, rankId.toLowerCase(), permission.toLowerCase());
  }

  public @NotNull Map<String, Set<String>> loadAllRankPermissions() {
    Map<String, Set<String>> map = new HashMap<>();
    if (dataSource == null || dataSource.isClosed()) return map;
    String sql = "SELECT rank_id, permission FROM smessential_rank_permissions;";
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        try {
          String rankId = rs.getString("rank_id");
          String permission = rs.getString("permission");
          if (rankId != null && !rankId.isBlank() && permission != null && !permission.isBlank()) {
            map.computeIfAbsent(rankId.toLowerCase(), k -> new HashSet<>())
                .add(permission.toLowerCase());
          }
        } catch (Exception ignored) {
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (rank permissions): " + e.getMessage());
    }
    return map;
  }

  public void addRankParent(@NotNull String rankId, @NotNull String parentId) {

    String sql =
        "INSERT INTO smessential_rank_inheritance (rank_id, parent_id) VALUES (?, ?) "
            + "ON CONFLICT(rank_id, parent_id) DO NOTHING;";
    executeUpdate(sql, rankId.toLowerCase(), parentId.toLowerCase());
  }

  public void removeRankParent(@NotNull String rankId, @NotNull String parentId) {
    String sql =
        "DELETE FROM smessential_rank_inheritance WHERE LOWER(rank_id) = LOWER(?) AND LOWER(parent_id) = LOWER(?);";
    executeUpdate(sql, rankId.toLowerCase(), parentId.toLowerCase());
  }

  public @NotNull Map<String, Set<String>> loadAllRankInheritance() {
    Map<String, Set<String>> map = new HashMap<>();
    if (dataSource == null || dataSource.isClosed()) return map;
    String sql = "SELECT rank_id, parent_id FROM smessential_rank_inheritance;";
    try (Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        try {
          String rankId = rs.getString("rank_id");
          String parentId = rs.getString("parent_id");
          if (rankId != null && !rankId.isBlank() && parentId != null && !parentId.isBlank()) {
            map.computeIfAbsent(rankId.toLowerCase(), k -> new HashSet<>())
                .add(parentId.toLowerCase());
          }
        } catch (Exception ignored) {
        }
      }
    } catch (SQLException e) {
      plugin.getLogger().severe("Database load error (rank inheritance): " + e.getMessage());
    }
    return map;
  }

  public void close() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      dataSource = null;
    }
  }
}
