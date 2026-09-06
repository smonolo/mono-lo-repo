#include "config.h"
#include <ctype.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static void trim_trailing(char *s) {
    if (!s) return;
    size_t len = strlen(s);
    while (len > 0 && isspace((unsigned char)s[len - 1])) {
        s[len - 1] = '\0';
        len--;
    }
}

static bool parse_env_file(const char *path) {
    if (!path || access(path, R_OK) != 0) {
        return false;
    }

    FILE *f = fopen(path, "r");
    if (!f) return false;

    char line[2048];
    bool loaded_any = false;

    while (fgets(line, sizeof(line), f)) {
        char *p = line;

        // Skip UTF-8 BOM if present
        if ((unsigned char)p[0] == 0xEF && (unsigned char)p[1] == 0xBB && (unsigned char)p[2] == 0xBF) {
            p += 3;
        }

        while (*p && isspace((unsigned char)*p)) p++;
        if (*p == '\0' || *p == '#') continue;

        // Strip leading "export "
        if (strncmp(p, "export", 6) == 0 && isspace((unsigned char)p[6])) {
            p += 6;
            while (*p && isspace((unsigned char)*p)) p++;
        }

        char *eq = strchr(p, '=');
        if (!eq) continue;

        *eq = '\0';
        char *key = p;
        char *val = eq + 1;

        // Trim key
        trim_trailing(key);
        if (*key == '\0') continue;

        // Skip whitespace before value
        while (*val && isspace((unsigned char)*val)) val++;

        char parsed_val[2048];
        size_t out_idx = 0;

        if (*val == '"') {
            val++; // Skip opening quote
            bool escaped = false;
            while (*val) {
                if (escaped) {
                    if (*val == 'n') parsed_val[out_idx++] = '\n';
                    else if (*val == 'r') parsed_val[out_idx++] = '\r';
                    else if (*val == 't') parsed_val[out_idx++] = '\t';
                    else parsed_val[out_idx++] = *val;
                    escaped = false;
                } else if (*val == '\\') {
                    escaped = true;
                } else if (*val == '"') {
                    break; // Closing quote
                } else {
                    parsed_val[out_idx++] = *val;
                }
                val++;
            }
        } else if (*val == '\'') {
            val++; // Skip opening quote
            while (*val && *val != '\'') {
                parsed_val[out_idx++] = *val;
                val++;
            }
        } else {
            // Unquoted value: read until newline or unquoted '#' comment preceded by whitespace
            while (*val && *val != '\r' && *val != '\n') {
                if (*val == '#' && out_idx > 0 && isspace((unsigned char)parsed_val[out_idx - 1])) {
                    out_idx--;
                    break;
                }
                parsed_val[out_idx++] = *val;
                val++;
            }
            while (out_idx > 0 && isspace((unsigned char)parsed_val[out_idx - 1])) {
                out_idx--;
            }
        }

        parsed_val[out_idx] = '\0';

        // Only set if not already set in environment
        const char *existing = getenv(key);
        if (!existing || *existing == '\0') {
            setenv(key, parsed_val, 1);
            loaded_any = true;
        }
    }

    fclose(f);
    return loaded_any;
}

static void try_candidate(const char *path, bool *loaded_primary) {
    if (access(path, R_OK) == 0) {
        if (parse_env_file(path)) {
            *loaded_primary = true;
        }
    }
}

bool config_load_env(const char *custom_path) {
    if (custom_path && *custom_path) {
        if (access(custom_path, R_OK) != 0) {
            return false;
        }
        return parse_env_file(custom_path);
    }

    bool loaded_any = false;

    // 1. Check current working directory
    try_candidate(".env", &loaded_any);

    // 2. Discover location relative to binary executable (/proc/self/exe)
    char exe_buf[PATH_MAX];
    ssize_t len = readlink("/proc/self/exe", exe_buf, sizeof(exe_buf) - 1);
    if (len > 0) {
        exe_buf[len] = '\0';
        // Get exe dir
        char *last_slash = strrchr(exe_buf, '/');
        if (last_slash) {
            *last_slash = '\0'; // exe_dir (e.g. .../apps/minecraft-cli-c/bin)

            char path[PATH_MAX];
            // <exe_dir>/.env
            snprintf(path, sizeof(path), "%s/.env", exe_buf);
            try_candidate(path, &loaded_any);

            // <exe_dir>/../.env (e.g. .../apps/minecraft-cli-c/.env)
            snprintf(path, sizeof(path), "%s/../.env", exe_buf);
            try_candidate(path, &loaded_any);

            // <exe_dir>/../../apps/minecraft-cli-c/.env
            snprintf(path, sizeof(path), "%s/../../apps/minecraft-cli-c/.env", exe_buf);
            try_candidate(path, &loaded_any);

            // <exe_dir>/../minecraft-api/.env
            snprintf(path, sizeof(path), "%s/../minecraft-api/.env", exe_buf);
            try_candidate(path, &loaded_any);

            // <exe_dir>/../../apps/minecraft-api/.env
            snprintf(path, sizeof(path), "%s/../../apps/minecraft-api/.env", exe_buf);
            try_candidate(path, &loaded_any);

            // <exe_dir>/../../.env (repo root)
            snprintf(path, sizeof(path), "%s/../../.env", exe_buf);
            try_candidate(path, &loaded_any);
        }
    }

    // 3. Known relative paths from cwd
    const char *rel_candidates[] = {
        "apps/minecraft-cli-c/.env",
        "apps/minecraft-cli/.env",
        "apps/minecraft-api/.env",
        "../minecraft-api/.env",
        "../../apps/minecraft-api/.env",
        "../.env",
        "../../.env",
        "../../../.env",
        NULL
    };

    for (size_t i = 0; rel_candidates[i]; i++) {
        // If we still don't have ADMIN_API_KEY, continue looking in fallbacks
        const char *key = getenv("ADMIN_API_KEY");
        if (loaded_any && key && *key) {
            break;
        }
        try_candidate(rel_candidates[i], &loaded_any);
    }

    return true;
}

static char *normalize_url(arena_t *arena, const char *url) {
    if (!url || !*url) {
        return arena_strdup(arena, "http://localhost:3002/v1");
    }

    size_t len = strlen(url);
    while (len > 0 && url[len - 1] == '/') {
        len--;
    }

    const char *v1_suffix = "/v1";
    size_t v1_len = strlen(v1_suffix);

    if (len >= v1_len && memcmp(url + len - v1_len, v1_suffix, v1_len) == 0) {
        return arena_strndup(arena, url, len);
    }

    char *buf = (char *)arena_alloc(arena, len + v1_len + 1);
    if (!buf) return NULL;
    memcpy(buf, url, len);
    memcpy(buf + len, v1_suffix, v1_len);
    buf[len + v1_len] = '\0';
    return buf;
}

config_t *config_init(arena_t *arena, const char *custom_env_file) {
    config_t *cfg = (config_t *)arena_alloc_zero(arena, sizeof(config_t));
    if (!cfg) return NULL;

    if (!config_load_env(custom_env_file)) {
        return NULL;
    }

    const char *env_url = getenv("MINECRAFT_API_URL");
    if (!env_url || !*env_url) {
        env_url = "http://localhost:3002";
    }
    cfg->api_url = normalize_url(arena, env_url);

    const char *env_key = getenv("ADMIN_API_KEY");
    cfg->api_key = env_key ? arena_strdup(arena, env_key) : "";

    return cfg;
}

void config_set_api_url(arena_t *arena, config_t *cfg, const char *url) {
    if (cfg && url) {
        cfg->api_url = normalize_url(arena, url);
    }
}

void config_set_api_key(arena_t *arena, config_t *cfg, const char *key) {
    if (cfg) {
        cfg->api_key = arena_strdup(arena, key ? key : "");
    }
}
