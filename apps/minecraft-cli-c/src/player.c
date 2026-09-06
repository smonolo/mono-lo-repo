#include "player.h"
#include <stdio.h>
#include <string.h>

static void parse_player_info(const json_value_t *obj, player_info_t *info) {
    if (!obj) return;
    info->uuid = json_get_string(obj, "uuid", "");
    info->username = json_get_string(obj, "username", "");
    info->first_join = json_get_int64(obj, "first_join", 0);
    info->last_join = json_get_int64(obj, "last_join", 0);
    info->in_users = json_get_bool(obj, "in_users", false);
}

static void parse_rank_detail(const json_value_t *obj, rank_detail_t *rank) {
    if (!obj) return;
    rank->id = json_get_string(obj, "id", "");
    rank->name = json_get_string(obj, "name", "");
    rank->color = json_get_string(obj, "color", "");
    rank->prefix = json_get_string(obj, "prefix", "");
}

bool parse_player_summary(arena_t *arena, const json_value_t *root, player_summary_t *out, char *err_buf, size_t err_size) {
    if (!root) {
        if (err_buf && err_size > 0) snprintf(err_buf, err_size, "Empty response");
        return false;
    }

    const json_value_t *sum = json_get_object(root, "summary");
    if (!sum) {
        if (root->type == JSON_OBJECT && json_get(root, "player")) {
            sum = root;
        } else {
            if (err_buf && err_size > 0) snprintf(err_buf, err_size, "Missing summary in API response");
            return false;
        }
    }

    memset(out, 0, sizeof(player_summary_t));

    const json_value_t *p_obj = json_get_object(sum, "player");
    parse_player_info(p_obj, &out->player);

    const json_value_t *ranks_arr = json_get_array(sum, "ranks");
    if (ranks_arr && ranks_arr->array.count > 0) {
        out->ranks_count = ranks_arr->array.count;
        out->ranks = (rank_detail_t *)arena_alloc(arena, out->ranks_count * sizeof(rank_detail_t));
        for (size_t i = 0; i < out->ranks_count; i++) {
            parse_rank_detail(ranks_arr->array.items[i], &out->ranks[i]);
        }
    }

    const json_value_t *disp_obj = json_get_object(sum, "display_rank");
    if (disp_obj && disp_obj->type == JSON_OBJECT) {
        out->display_rank = (rank_detail_t *)arena_alloc(arena, sizeof(rank_detail_t));
        parse_rank_detail(disp_obj, out->display_rank);
    }

    const json_value_t *punish_arr = json_get_array(sum, "target_punishments");
    if (punish_arr && punish_arr->array.count > 0) {
        out->punishments_count = punish_arr->array.count;
        out->target_punishments = (punishment_summary_t *)arena_alloc(arena, out->punishments_count * sizeof(punishment_summary_t));
        for (size_t i = 0; i < out->punishments_count; i++) {
            const json_value_t *item = punish_arr->array.items[i];
            out->target_punishments[i].id = json_get_string(item, "id", "");
            out->target_punishments[i].type = json_get_string(item, "type", "");
            out->target_punishments[i].reason = json_get_string(item, "reason", "");
            out->target_punishments[i].issuer = json_get_string(item, "issuer", "");
            out->target_punishments[i].created_at = json_get_int64(item, "created_at", 0);
            out->target_punishments[i].expires_at = json_get_int64(item, "expires_at", 0);
        }
    }

    out->issuer_punishments_count = json_get_int64(sum, "issuer_punishments_count", 0);

    const json_value_t *wl_obj = json_get_object(sum, "whitelist");
    if (wl_obj && wl_obj->type == JSON_OBJECT) {
        out->whitelist = (whitelist_summary_t *)arena_alloc(arena, sizeof(whitelist_summary_t));
        out->whitelist->name = json_get_string(wl_obj, "name", "");
        out->whitelist->added_by = json_get_string(wl_obj, "added_by", "");
        out->whitelist->added_at = json_get_int64(wl_obj, "added_at", 0);
    }

    out->total_records_to_delete = (int)json_get_int64(sum, "total_records_to_delete", 0);

    return true;
}

bool parse_delete_result(arena_t *arena, const json_value_t *root, delete_result_t *out, char *err_buf, size_t err_size) {
    (void)arena;
    if (!root) {
        if (err_buf && err_size > 0) snprintf(err_buf, err_size, "Empty response");
        return false;
    }

    const json_value_t *res = json_get_object(root, "result");
    if (!res) {
        if (root->type == JSON_OBJECT && json_get(root, "player")) {
            res = root;
        } else {
            if (err_buf && err_size > 0) snprintf(err_buf, err_size, "Missing result in API response");
            return false;
        }
    }

    memset(out, 0, sizeof(delete_result_t));

    const json_value_t *p_obj = json_get_object(res, "player");
    parse_player_info(p_obj, &out->player);

    out->deleted_users = json_get_int64(res, "deleted_users", 0);
    out->deleted_user_ranks = json_get_int64(res, "deleted_user_ranks", 0);
    out->deleted_user_display_ranks = json_get_int64(res, "deleted_user_display_ranks", 0);
    out->deleted_punishments = json_get_int64(res, "deleted_punishments", 0);
    out->deleted_whitelist = json_get_int64(res, "deleted_whitelist", 0);
    out->preserved_issuer_punishments = json_get_int64(res, "preserved_issuer_punishments", 0);
    out->total_deleted = json_get_int64(res, "total_deleted", 0);

    return true;
}

void parse_api_error_message(const json_value_t *root, int status_code, char *out_buf, size_t out_size) {
    if (!out_buf || out_size == 0) return;

    if (root && root->type == JSON_OBJECT) {
        json_value_t *msg_val = json_get(root, "message");
        if (msg_val) {
            if (msg_val->type == JSON_STRING) {
                snprintf(out_buf, out_size, "API error (HTTP %d): %s", status_code, msg_val->str_val);
                return;
            }
            if (msg_val->type == JSON_ARRAY && msg_val->array.count > 0) {
                size_t offset = snprintf(out_buf, out_size, "API error (HTTP %d): ", status_code);
                for (size_t i = 0; i < msg_val->array.count && offset < out_size - 1; i++) {
                    const char *s = "";
                    if (msg_val->array.items[i]->type == JSON_STRING) {
                        s = msg_val->array.items[i]->str_val;
                    }
                    offset += snprintf(out_buf + offset, out_size - offset, "%s%s", (i > 0 ? ", " : ""), s);
                }
                return;
            }
        }
        const char *err_str = json_get_string(root, "error", NULL);
        if (err_str) {
            snprintf(out_buf, out_size, "API error (HTTP %d): %s", status_code, err_str);
            return;
        }
    }

    snprintf(out_buf, out_size, "API error (HTTP %d)", status_code);
}

bool parse_player_list(arena_t *arena, const json_value_t *root, player_list_t *out, char *err_buf, size_t err_size) {
    if (!root || root->type != JSON_OBJECT) {
        if (err_buf && err_size > 0) snprintf(err_buf, err_size, "Invalid players response");
        return false;
    }

    memset(out, 0, sizeof(player_list_t));
    out->online = json_get_bool(root, "online", true);
    out->online_count = (size_t)json_get_int64(root, "onlineCount", 0);

    const json_value_t *p_arr = json_get_array(root, "players");
    if (p_arr && p_arr->array.count > 0) {
        out->count = p_arr->array.count;
        out->players = (player_list_item_t *)arena_alloc_zero(arena, out->count * sizeof(player_list_item_t));
        for (size_t i = 0; i < out->count; i++) {
            const json_value_t *item = p_arr->array.items[i];
            out->players[i].uuid = json_get_string(item, "uuid", "");
            out->players[i].username = json_get_string(item, "username", "");
            out->players[i].online = json_get_bool(item, "online", false);
            out->players[i].last_login = json_get_int64(item, "lastLogin", 0);
            out->players[i].ping = (int)json_get_int64(item, "ping", 0);
            out->players[i].afk = json_get_bool(item, "afk", false);
            out->players[i].world = json_get_string(item, "world", "Offline");

            const json_value_t *r_obj = json_get_object(item, "rank");
            if (r_obj) {
                parse_rank_detail(r_obj, &out->players[i].rank);
            }
        }
    }

    return true;
}

