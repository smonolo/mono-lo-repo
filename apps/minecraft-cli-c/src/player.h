#ifndef PLAYER_H
#define PLAYER_H

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include "arena.h"
#include "json.h"

typedef struct {
    const char *uuid;
    const char *username;
    int64_t first_join;
    int64_t last_join;
    bool in_users;
} player_info_t;

typedef struct {
    const char *id;
    const char *type;
    const char *reason;
    const char *issuer;
    int64_t created_at;
    int64_t expires_at;
} punishment_summary_t;

typedef struct {
    const char *name;
    const char *added_by;
    int64_t added_at;
} whitelist_summary_t;

typedef struct {
    const char *id;
    const char *name;
    const char *color;
    const char *prefix;
} rank_detail_t;

typedef struct {
    player_info_t player;
    rank_detail_t *ranks;
    size_t ranks_count;
    rank_detail_t *display_rank;
    punishment_summary_t *target_punishments;
    size_t punishments_count;
    int64_t issuer_punishments_count;
    whitelist_summary_t *whitelist;
    int total_records_to_delete;
} player_summary_t;

typedef struct {
    player_info_t player;
    int64_t deleted_users;
    int64_t deleted_user_ranks;
    int64_t deleted_user_display_ranks;
    int64_t deleted_punishments;
    int64_t deleted_whitelist;
    int64_t preserved_issuer_punishments;
    int64_t total_deleted;
} delete_result_t;

typedef struct {
    const char *uuid;
    const char *username;
    bool online;
    int64_t last_login;
    rank_detail_t rank;
    int ping;
    bool afk;
    const char *world;
} player_list_item_t;

typedef struct {
    bool online;
    player_list_item_t *players;
    size_t count;
    size_t online_count;
} player_list_t;

bool parse_player_summary(arena_t *arena, const json_value_t *root, player_summary_t *out, char *err_buf, size_t err_size);
bool parse_delete_result(arena_t *arena, const json_value_t *root, delete_result_t *out, char *err_buf, size_t err_size);
bool parse_player_list(arena_t *arena, const json_value_t *root, player_list_t *out, char *err_buf, size_t err_size);
void parse_api_error_message(const json_value_t *root, int status_code, char *out_buf, size_t out_size);

#endif /* PLAYER_H */
