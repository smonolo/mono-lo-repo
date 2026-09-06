#ifndef CONFIG_H
#define CONFIG_H

#include <stdbool.h>
#include "arena.h"

typedef struct {
    const char *api_url;
    const char *api_key;
    const char *env_file;
    bool json_output;
    bool quiet;
    bool yes;
    bool dry_run;
} config_t;

bool config_load_env(const char *custom_path);
config_t *config_init(arena_t *arena, const char *custom_env_file);
void config_set_api_url(arena_t *arena, config_t *cfg, const char *url);
void config_set_api_key(arena_t *arena, config_t *cfg, const char *key);

#endif /* CONFIG_H */
