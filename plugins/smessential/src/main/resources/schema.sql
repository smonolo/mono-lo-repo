-- Alerts table
CREATE TABLE IF NOT EXISTS smessential_alerts (
    target VARCHAR(64) PRIMARY KEY,
    message TEXT NOT NULL
);

-- Punishments table (unified history for mutes, bans, etc.)
CREATE TABLE IF NOT EXISTS smessential_punishments (
    id VARCHAR(36) PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    type VARCHAR(16) NOT NULL,
    username VARCHAR(64) NOT NULL,
    reason TEXT NOT NULL,
    issuer VARCHAR(64) NOT NULL,
    created_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL DEFAULT 0,
    unpunished_at BIGINT NOT NULL DEFAULT 0,
    unpunished_by VARCHAR(64)
);

-- Global server settings / toggles table
CREATE TABLE IF NOT EXISTS smessential_settings (
    key VARCHAR(64) PRIMARY KEY,
    value TEXT NOT NULL
);

-- Whitelist entries table
CREATE TABLE IF NOT EXISTS smessential_whitelist (
    uuid VARCHAR(36),
    name VARCHAR(64),
    added_by VARCHAR(64) NOT NULL,
    added_at BIGINT NOT NULL
);

-- Users table (profile & join history)
CREATE TABLE IF NOT EXISTS smessential_users (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    first_join BIGINT NOT NULL,
    last_join BIGINT NOT NULL
);

-- Drop statistics table if previously created
DROP TABLE IF EXISTS smessential_statistics;

-- Leaderboards table
CREATE TABLE IF NOT EXISTS smessential_leaderboards (
    id VARCHAR(64) PRIMARY KEY,
    stat_key VARCHAR(64) NOT NULL,
    world VARCHAR(64) NOT NULL,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    z DOUBLE PRECISION NOT NULL,
    yaw REAL NOT NULL DEFAULT 0.0,
    pitch REAL NOT NULL DEFAULT 0.0,
    display_limit INT NOT NULL DEFAULT 10,
    width INT NOT NULL DEFAULT 1,
    height INT NOT NULL DEFAULT 1
);

-- Ranks table (definitions)
CREATE TABLE IF NOT EXISTS smessential_ranks (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    color VARCHAR(64) NOT NULL DEFAULT 'white',
    prefix VARCHAR(128) NOT NULL DEFAULT '',
    weight INT NOT NULL DEFAULT 0,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE
);

-- User Ranks table (assigned ranks for players)
CREATE TABLE IF NOT EXISTS smessential_user_ranks (
    uuid VARCHAR(36) NOT NULL,
    rank_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (uuid, rank_id)
);

-- Rank Permissions table
CREATE TABLE IF NOT EXISTS smessential_rank_permissions (
    rank_id VARCHAR(64) NOT NULL,
    permission VARCHAR(128) NOT NULL,
    PRIMARY KEY (rank_id, permission)
);

-- Rank Inheritance table
CREATE TABLE IF NOT EXISTS smessential_rank_inheritance (
    rank_id VARCHAR(64) NOT NULL,
    parent_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (rank_id, parent_id)
);

-- User Display Rank table (custom display appearance per player)
CREATE TABLE IF NOT EXISTS smessential_user_display_ranks (
    uuid VARCHAR(36) PRIMARY KEY,
    rank_id VARCHAR(64) NOT NULL
);

