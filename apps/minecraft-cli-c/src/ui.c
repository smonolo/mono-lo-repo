#include "ui.h"
#include <ctype.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

static bool s_color_enabled = false;

void ui_init(void) {
    if (getenv("NO_COLOR") != NULL) {
        s_color_enabled = false;
        return;
    }
    s_color_enabled = isatty(STDOUT_FILENO) && isatty(STDERR_FILENO);
}

bool ui_is_color_enabled(void) {
    return s_color_enabled;
}

#define ANSI_RESET        "\033[0m"
#define ANSI_BOLD         "\033[1m"
#define ANSI_DIM          "\033[2m"
#define ANSI_RED          "\033[1;31m"
#define ANSI_GREEN        "\033[1;32m"
#define ANSI_YELLOW       "\033[1;33m"
#define ANSI_CYAN         "\033[1;36m"
#define ANSI_WHITE        "\033[1;37m"

static const char *get_rank_ansi(const char *mcColor) {
    if (!s_color_enabled || !mcColor) return "";

    char norm[64];
    size_t len = strlen(mcColor);
    if (len >= sizeof(norm)) len = sizeof(norm) - 1;
    for (size_t i = 0; i < len; i++) {
        char c = (char)tolower((unsigned char)mcColor[i]);
        if (c == '-') c = '_';
        norm[i] = c;
    }
    norm[len] = '\0';

    if (strcmp(norm, "black") == 0) return "\033[1;30m";
    if (strcmp(norm, "dark_blue") == 0) return "\033[34m";
    if (strcmp(norm, "dark_green") == 0) return "\033[32m";
    if (strcmp(norm, "dark_aqua") == 0) return "\033[36m";
    if (strcmp(norm, "dark_red") == 0) return "\033[31m";
    if (strcmp(norm, "dark_purple") == 0) return "\033[35m";
    if (strcmp(norm, "gold") == 0) return "\033[1;33m";
    if (strcmp(norm, "gray") == 0) return "\033[37m";
    if (strcmp(norm, "dark_gray") == 0) return "\033[90m";
    if (strcmp(norm, "blue") == 0) return "\033[1;94m";
    if (strcmp(norm, "green") == 0) return "\033[1;92m";
    if (strcmp(norm, "aqua") == 0) return "\033[1;96m";
    if (strcmp(norm, "red") == 0) return "\033[1;91m";
    if (strcmp(norm, "light_purple") == 0) return "\033[1;95m";
    if (strcmp(norm, "yellow") == 0) return "\033[1;93m";
    if (strcmp(norm, "white") == 0) return "\033[1;97m";

    return "\033[1m";
}

static void format_time(int64_t millis, char *buf, size_t buf_size) {
    if (millis <= 0) {
        snprintf(buf, buf_size, "Never");
        return;
    }
    time_t sec = (time_t)(millis / 1000);
    struct tm tm_val;
    localtime_r(&sec, &tm_val);
    strftime(buf, buf_size, "%Y-%m-%d %H:%M:%S %Z", &tm_val);
}

void ui_print_banner(void) {
    if (s_color_enabled) {
        printf("%sMinecraft CLI%s\n", ANSI_CYAN, ANSI_RESET);
        printf("%s──────────────────────────────────────────────────%s\n", ANSI_DIM, ANSI_RESET);
    } else {
        printf("Minecraft CLI\n");
        printf("──────────────────────────────────────────────────\n");
    }
    fflush(stdout);
}

void ui_print_player_profile(FILE *out, const player_summary_t *s) {
    char first_buf[64], last_buf[64];
    format_time(s->player.first_join, first_buf, sizeof(first_buf));
    format_time(s->player.last_join, last_buf, sizeof(last_buf));

    if (s_color_enabled) {
        fprintf(out, "\n%sPlayer Profile:%s\n", ANSI_BOLD, ANSI_RESET);
        fprintf(out, "  • %-16s %s%s%s\n", "Username:", ANSI_GREEN, s->player.username, ANSI_RESET);
        fprintf(out, "  • %-16s %s%s%s\n", "UUID:", ANSI_CYAN, s->player.uuid, ANSI_RESET);
        fprintf(out, "  • %-16s %s%s%s\n", "First Login:", ANSI_DIM, first_buf, ANSI_RESET);
        fprintf(out, "  • %-16s %s%s%s\n", "Last Login:", ANSI_DIM, last_buf, ANSI_RESET);

        fprintf(out, "  • %-16s ", "Ranks:");
        if (s->ranks_count == 0) {
            fprintf(out, "%sDefault%s\n", ANSI_DIM, ANSI_RESET);
        } else {
            for (size_t i = 0; i < s->ranks_count; i++) {
                const char *col = get_rank_ansi(s->ranks[i].color);
                fprintf(out, "%s%s%s%s", col, s->ranks[i].name, ANSI_RESET, (i + 1 < s->ranks_count ? ", " : ""));
            }
            fprintf(out, "\n");
        }

        if (s->display_rank) {
            const char *col = get_rank_ansi(s->display_rank->color);
            fprintf(out, "  • %-16s %s%s%s\n", "Display Rank:", col, s->display_rank->name, ANSI_RESET);
        }
        fprintf(out, "\n");
    } else {
        fprintf(out, "\nPlayer Profile:\n");
        fprintf(out, "  • %-16s %s\n", "Username:", s->player.username);
        fprintf(out, "  • %-16s %s\n", "UUID:", s->player.uuid);
        fprintf(out, "  • %-16s %s\n", "First Login:", first_buf);
        fprintf(out, "  • %-16s %s\n", "Last Login:", last_buf);

        fprintf(out, "  • %-16s ", "Ranks:");
        if (s->ranks_count == 0) {
            fprintf(out, "Default\n");
        } else {
            for (size_t i = 0; i < s->ranks_count; i++) {
                fprintf(out, "%s%s", s->ranks[i].name, (i + 1 < s->ranks_count ? ", " : ""));
            }
            fprintf(out, "\n");
        }

        if (s->display_rank) {
            fprintf(out, "  • %-16s %s\n", "Display Rank:", s->display_rank->name);
        }
        fprintf(out, "\n");
    }
    fflush(out);
}

void ui_print_player_summary(FILE *out, const player_summary_t *s) {
    if (s_color_enabled) {
        fprintf(out, "%s\nTarget Player Details:%s\n", ANSI_BOLD, ANSI_RESET);
        fprintf(out, "  • %s%-16s%s %s%s%s\n", ANSI_BOLD, "Username:", ANSI_RESET, ANSI_GREEN, s->player.username, ANSI_RESET);
        fprintf(out, "  • %s%-16s%s %s%s%s\n", ANSI_BOLD, "UUID:", ANSI_RESET, ANSI_CYAN, s->player.uuid, ANSI_RESET);

        if (s->player.first_join > 0) {
            char tbuf[64];
            format_time(s->player.first_join, tbuf, sizeof(tbuf));
            fprintf(out, "  • %s%-16s%s %s%s%s\n", ANSI_BOLD, "First Join:", ANSI_RESET, ANSI_DIM, tbuf, ANSI_RESET);
        }
        if (s->player.last_join > 0) {
            char tbuf[64];
            format_time(s->player.last_join, tbuf, sizeof(tbuf));
            fprintf(out, "  • %s%-16s%s %s%s%s\n", ANSI_BOLD, "Last Join:", ANSI_RESET, ANSI_DIM, tbuf, ANSI_RESET);
        }

        fprintf(out, "%s\nDatabase Records to Delete:%s\n", ANSI_BOLD, ANSI_RESET);

        // 1. Users table
        if (s->player.in_users) {
            fprintf(out, "  %s[-]%s smessential_users (profile)\n", ANSI_RED, ANSI_RESET);
        } else {
            fprintf(out, "  %s[ ]%s smessential_users (not found)\n", ANSI_DIM, ANSI_RESET);
        }

        // 2. Ranks table
        if (s->ranks_count > 0) {
            fprintf(out, "  %s[-]%s smessential_user_ranks (%zu assigned: ", ANSI_RED, ANSI_RESET, s->ranks_count);
            for (size_t i = 0; i < s->ranks_count; i++) {
                const char *col = get_rank_ansi(s->ranks[i].color);
                fprintf(out, "%s%s%s%s", col, s->ranks[i].name, ANSI_RESET, (i + 1 < s->ranks_count ? ", " : ""));
            }
            fprintf(out, ")\n");
        } else {
            fprintf(out, "  %s[ ]%s smessential_user_ranks (no ranks assigned)\n", ANSI_DIM, ANSI_RESET);
        }

        // 3. Display rank
        if (s->display_rank) {
            const char *col = get_rank_ansi(s->display_rank->color);
            fprintf(out, "  %s[-]%s smessential_user_display_ranks (custom display: %s%s%s)\n",
                    ANSI_RED, ANSI_RESET, col, s->display_rank->name, ANSI_RESET);
        } else {
            fprintf(out, "  %s[ ]%s smessential_user_display_ranks (no custom display rank)\n", ANSI_DIM, ANSI_RESET);
        }

        // 4. Target Punishments
        if (s->punishments_count > 0) {
            fprintf(out, "  %s[-]%s smessential_punishments (%zu target records):\n",
                    ANSI_RED, ANSI_RESET, s->punishments_count);
            for (size_t i = 0; i < s->punishments_count; i++) {
                fprintf(out, "      - [%s%s%s] Reason: \"%s\" (by %s%s%s)\n",
                        ANSI_YELLOW, s->target_punishments[i].type, ANSI_RESET,
                        s->target_punishments[i].reason,
                        ANSI_DIM, s->target_punishments[i].issuer, ANSI_RESET);
            }
        } else {
            fprintf(out, "  %s[ ]%s smessential_punishments (0 target punishments)\n", ANSI_DIM, ANSI_RESET);
        }

        // 5. Whitelist
        if (s->whitelist) {
            fprintf(out, "  %s[-]%s smessential_whitelist (whitelisted as \"%s\", added by %s%s%s)\n",
                    ANSI_RED, ANSI_RESET, s->whitelist->name, ANSI_DIM, s->whitelist->added_by, ANSI_RESET);
        } else {
            fprintf(out, "  %s[ ]%s smessential_whitelist (not whitelisted)\n", ANSI_DIM, ANSI_RESET);
        }

        // Preserved records
        fprintf(out, "%s\nPreserved Records (Integrity Protection):%s\n", ANSI_BOLD, ANSI_RESET);
        if (s->issuer_punishments_count > 0) {
            fprintf(out, "  %s[OK]%s %ld punishment(s) issued as staff will be %sPRESERVED%s (audit history retained)\n",
                    ANSI_GREEN, ANSI_RESET, (long)s->issuer_punishments_count, ANSI_BOLD, ANSI_RESET);
        } else {
            fprintf(out, "  %s[OK]%s 0 staff punishments issued by this player\n", ANSI_DIM, ANSI_RESET);
        }

        fprintf(out, "\nTotal records slated for deletion: %s%d%s\n",
                ANSI_RED, s->total_records_to_delete, ANSI_RESET);
    } else {
        fprintf(out, "\nTarget Player Details:\n");
        fprintf(out, "  • %-16s %s\n", "Username:", s->player.username);
        fprintf(out, "  • %-16s %s\n", "UUID:", s->player.uuid);

        if (s->player.first_join > 0) {
            char tbuf[64];
            format_time(s->player.first_join, tbuf, sizeof(tbuf));
            fprintf(out, "  • %-16s %s\n", "First Join:", tbuf);
        }
        if (s->player.last_join > 0) {
            char tbuf[64];
            format_time(s->player.last_join, tbuf, sizeof(tbuf));
            fprintf(out, "  • %-16s %s\n", "Last Join:", tbuf);
        }

        fprintf(out, "\nDatabase Records to Delete:\n");
        fprintf(out, "  %s smessential_users (profile)\n", s->player.in_users ? "[-]" : "[ ]");
        if (s->ranks_count > 0) {
            fprintf(out, "  [-] smessential_user_ranks (%zu assigned: ", s->ranks_count);
            for (size_t i = 0; i < s->ranks_count; i++) {
                fprintf(out, "%s%s", s->ranks[i].name, (i + 1 < s->ranks_count ? ", " : ""));
            }
            fprintf(out, ")\n");
        } else {
            fprintf(out, "  [ ] smessential_user_ranks (no ranks assigned)\n");
        }

        if (s->display_rank) {
            fprintf(out, "  [-] smessential_user_display_ranks (custom display: %s)\n", s->display_rank->name);
        } else {
            fprintf(out, "  [ ] smessential_user_display_ranks (no custom display rank)\n");
        }

        if (s->punishments_count > 0) {
            fprintf(out, "  [-] smessential_punishments (%zu target records):\n", s->punishments_count);
            for (size_t i = 0; i < s->punishments_count; i++) {
                fprintf(out, "      - [%s] Reason: \"%s\" (by %s)\n",
                        s->target_punishments[i].type,
                        s->target_punishments[i].reason,
                        s->target_punishments[i].issuer);
            }
        } else {
            fprintf(out, "  [ ] smessential_punishments (0 target punishments)\n");
        }

        if (s->whitelist) {
            fprintf(out, "  [-] smessential_whitelist (whitelisted as \"%s\", added by %s)\n",
                    s->whitelist->name, s->whitelist->added_by);
        } else {
            fprintf(out, "  [ ] smessential_whitelist (not whitelisted)\n");
        }

        fprintf(out, "\nPreserved Records (Integrity Protection):\n");
        if (s->issuer_punishments_count > 0) {
            fprintf(out, "  [OK] %ld punishment(s) issued as staff will be PRESERVED (audit history retained)\n",
                    (long)s->issuer_punishments_count);
        } else {
            fprintf(out, "  [OK] 0 staff punishments issued by this player\n");
        }

        fprintf(out, "\nTotal records slated for deletion: %d\n", s->total_records_to_delete);
    }
    fflush(out);
}

bool ui_ask_confirmation(const char *prompt) {
    if (s_color_enabled) {
        printf("\n%s[?]%s %s%s%s %s[y/N]:%s ",
               ANSI_YELLOW, ANSI_RESET, ANSI_BOLD, prompt, ANSI_RESET, ANSI_DIM, ANSI_RESET);
    } else {
        printf("\n[?] %s [y/N]: ", prompt);
    }
    fflush(stdout);

    char line[128];
    if (!fgets(line, sizeof(line), stdin)) {
        return false;
    }

    char *p = line;
    while (*p && isspace((unsigned char)*p)) p++;
    size_t len = strlen(p);
    while (len > 0 && isspace((unsigned char)p[len - 1])) {
        p[len - 1] = '\0';
        len--;
    }

    return (strcasecmp(p, "y") == 0 || strcasecmp(p, "yes") == 0);
}

void ui_print_delete_success(FILE *out, const delete_result_t *res) {
    if (s_color_enabled) {
        fprintf(out, "%s\n[OK] Player successfully deleted%s\n", ANSI_GREEN, ANSI_RESET);
        fprintf(out, "%s──────────────────────────────────────────────────%s\n", ANSI_DIM, ANSI_RESET);
        fprintf(out, "  • Target:                  %s%s%s (%s%s%s)\n",
                ANSI_GREEN, res->player.username, ANSI_RESET,
                ANSI_CYAN, res->player.uuid, ANSI_RESET);
        fprintf(out, "  • Users Table:             %ld deleted\n", (long)res->deleted_users);
        fprintf(out, "  • User Ranks:              %ld deleted\n", (long)res->deleted_user_ranks);
        fprintf(out, "  • User Display Ranks:      %ld deleted\n", (long)res->deleted_user_display_ranks);
        fprintf(out, "  • Target Punishments:      %ld deleted\n", (long)res->deleted_punishments);
        fprintf(out, "  • Whitelist Entries:       %ld deleted\n", (long)res->deleted_whitelist);
        fprintf(out, "  • Staff Issuer Punishments: %ld preserved\n", (long)res->preserved_issuer_punishments);
        fprintf(out, "%s──────────────────────────────────────────────────%s\n", ANSI_DIM, ANSI_RESET);
        fprintf(out, "  %sTotal Records Deleted:%s %s%ld%s\n",
                ANSI_BOLD, ANSI_RESET, ANSI_GREEN, (long)res->total_deleted, ANSI_RESET);
    } else {
        fprintf(out, "\n[OK] Player successfully deleted\n");
        fprintf(out, "──────────────────────────────────────────────────\n");
        fprintf(out, "  • Target:                  %s (%s)\n", res->player.username, res->player.uuid);
        fprintf(out, "  • Users Table:             %ld deleted\n", (long)res->deleted_users);
        fprintf(out, "  • User Ranks:              %ld deleted\n", (long)res->deleted_user_ranks);
        fprintf(out, "  • User Display Ranks:      %ld deleted\n", (long)res->deleted_user_display_ranks);
        fprintf(out, "  • Target Punishments:      %ld deleted\n", (long)res->deleted_punishments);
        fprintf(out, "  • Whitelist Entries:       %ld deleted\n", (long)res->deleted_whitelist);
        fprintf(out, "  • Staff Issuer Punishments: %ld preserved\n", (long)res->preserved_issuer_punishments);
        fprintf(out, "──────────────────────────────────────────────────\n");
        fprintf(out, "  Total Records Deleted: %ld\n", (long)res->total_deleted);
    }
    fflush(out);
}

void ui_print_json_profile(FILE *out, const player_summary_t *s) {
    arena_t *a = arena_create(4096);
    json_value_t *obj = json_create_object(a);
    json_object_set(a, obj, "uuid", json_create_string(a, s->player.uuid));
    json_object_set(a, obj, "username", json_create_string(a, s->player.username));
    json_object_set(a, obj, "first_login", json_create_int(a, s->player.first_join));
    json_object_set(a, obj, "last_login", json_create_int(a, s->player.last_join));

    json_value_t *ranks = json_create_array(a);
    for (size_t i = 0; i < s->ranks_count; i++) {
        json_value_t *ro = json_create_object(a);
        json_object_set(a, ro, "id", json_create_string(a, s->ranks[i].id));
        json_object_set(a, ro, "name", json_create_string(a, s->ranks[i].name));
        json_object_set(a, ro, "color", json_create_string(a, s->ranks[i].color));
        json_object_set(a, ro, "prefix", json_create_string(a, s->ranks[i].prefix));
        json_array_append(a, ranks, ro);
    }
    json_object_set(a, obj, "ranks", ranks);

    if (s->display_rank) {
        json_value_t *ro = json_create_object(a);
        json_object_set(a, ro, "id", json_create_string(a, s->display_rank->id));
        json_object_set(a, ro, "name", json_create_string(a, s->display_rank->name));
        json_object_set(a, ro, "color", json_create_string(a, s->display_rank->color));
        json_object_set(a, ro, "prefix", json_create_string(a, s->display_rank->prefix));
        json_object_set(a, obj, "display_rank", ro);
    }

    json_print(obj, out, true);
    arena_destroy(a);
}

void ui_print_json_dry_run(FILE *out, const player_summary_t *s) {
    arena_t *a = arena_create(8192);
    json_value_t *root = json_create_object(a);
    json_object_set(a, root, "dry_run", json_create_bool(a, true));

    json_value_t *sum = json_create_object(a);
    json_value_t *p_obj = json_create_object(a);
    json_object_set(a, p_obj, "uuid", json_create_string(a, s->player.uuid));
    json_object_set(a, p_obj, "username", json_create_string(a, s->player.username));
    json_object_set(a, p_obj, "first_join", json_create_int(a, s->player.first_join));
    json_object_set(a, p_obj, "last_join", json_create_int(a, s->player.last_join));
    json_object_set(a, p_obj, "in_users", json_create_bool(a, s->player.in_users));
    json_object_set(a, sum, "player", p_obj);

    json_value_t *ranks = json_create_array(a);
    for (size_t i = 0; i < s->ranks_count; i++) {
        json_value_t *ro = json_create_object(a);
        json_object_set(a, ro, "id", json_create_string(a, s->ranks[i].id));
        json_object_set(a, ro, "name", json_create_string(a, s->ranks[i].name));
        json_object_set(a, ro, "color", json_create_string(a, s->ranks[i].color));
        json_object_set(a, ro, "prefix", json_create_string(a, s->ranks[i].prefix));
        json_array_append(a, ranks, ro);
    }
    json_object_set(a, sum, "ranks", ranks);

    if (s->display_rank) {
        json_value_t *ro = json_create_object(a);
        json_object_set(a, ro, "id", json_create_string(a, s->display_rank->id));
        json_object_set(a, ro, "name", json_create_string(a, s->display_rank->name));
        json_object_set(a, ro, "color", json_create_string(a, s->display_rank->color));
        json_object_set(a, ro, "prefix", json_create_string(a, s->display_rank->prefix));
        json_object_set(a, sum, "display_rank", ro);
    } else {
        json_object_set(a, sum, "display_rank", json_create_null(a));
    }

    json_value_t *target_punish = json_create_array(a);
    for (size_t i = 0; i < s->punishments_count; i++) {
        json_value_t *po = json_create_object(a);
        json_object_set(a, po, "id", json_create_string(a, s->target_punishments[i].id));
        json_object_set(a, po, "type", json_create_string(a, s->target_punishments[i].type));
        json_object_set(a, po, "reason", json_create_string(a, s->target_punishments[i].reason));
        json_object_set(a, po, "issuer", json_create_string(a, s->target_punishments[i].issuer));
        json_object_set(a, po, "created_at", json_create_int(a, s->target_punishments[i].created_at));
        json_object_set(a, po, "expires_at", json_create_int(a, s->target_punishments[i].expires_at));
        json_array_append(a, target_punish, po);
    }
    json_object_set(a, sum, "target_punishments", target_punish);

    json_object_set(a, sum, "issuer_punishments_count", json_create_int(a, s->issuer_punishments_count));

    if (s->whitelist) {
        json_value_t *wo = json_create_object(a);
        json_object_set(a, wo, "name", json_create_string(a, s->whitelist->name));
        json_object_set(a, wo, "added_by", json_create_string(a, s->whitelist->added_by));
        json_object_set(a, wo, "added_at", json_create_int(a, s->whitelist->added_at));
        json_object_set(a, sum, "whitelist", wo);
    } else {
        json_object_set(a, sum, "whitelist", json_create_null(a));
    }

    json_object_set(a, sum, "total_records_to_delete", json_create_int(a, s->total_records_to_delete));
    json_object_set(a, root, "summary", sum);

    json_print(root, out, true);
    arena_destroy(a);
}

void ui_print_json_delete_success(FILE *out, const delete_result_t *res) {
    arena_t *a = arena_create(4096);
    json_value_t *root = json_create_object(a);
    json_object_set(a, root, "success", json_create_bool(a, true));

    json_value_t *ro = json_create_object(a);
    json_value_t *po = json_create_object(a);
    json_object_set(a, po, "uuid", json_create_string(a, res->player.uuid));
    json_object_set(a, po, "username", json_create_string(a, res->player.username));
    json_object_set(a, po, "first_join", json_create_int(a, res->player.first_join));
    json_object_set(a, po, "last_join", json_create_int(a, res->player.last_join));
    json_object_set(a, po, "in_users", json_create_bool(a, res->player.in_users));
    json_object_set(a, ro, "player", po);

    json_object_set(a, ro, "deleted_users", json_create_int(a, res->deleted_users));
    json_object_set(a, ro, "deleted_user_ranks", json_create_int(a, res->deleted_user_ranks));
    json_object_set(a, ro, "deleted_user_display_ranks", json_create_int(a, res->deleted_user_display_ranks));
    json_object_set(a, ro, "deleted_punishments", json_create_int(a, res->deleted_punishments));
    json_object_set(a, ro, "deleted_whitelist", json_create_int(a, res->deleted_whitelist));
    json_object_set(a, ro, "preserved_issuer_punishments", json_create_int(a, res->preserved_issuer_punishments));
    json_object_set(a, ro, "total_deleted", json_create_int(a, res->total_deleted));

    json_object_set(a, root, "result", ro);

    json_print(root, out, true);
    arena_destroy(a);
}

void ui_print_json_not_found(FILE *out, const char *identifier) {
    arena_t *a = arena_create(1024);
    json_value_t *root = json_create_object(a);
    json_object_set(a, root, "success", json_create_bool(a, false));
    json_object_set(a, root, "error", json_create_string(a, "player not found in SMEssential database"));
    json_object_set(a, root, "identifier", json_create_string(a, identifier ? identifier : ""));
    json_print(root, out, true);
    arena_destroy(a);
}

void ui_print_player_list(FILE *out, const player_list_t *list) {
    if (!list) return;

    if (s_color_enabled) {
        fprintf(out, "\n%sPlayers (%zu total, %zu online):%s\n",
                ANSI_BOLD, list->count, list->online_count, ANSI_RESET);
        fprintf(out, "%s%-18s %-38s %-12s %-12s %s%s\n",
                ANSI_DIM, "USERNAME", "UUID", "STATUS", "RANK", "LAST LOGIN", ANSI_RESET);
        fprintf(out, "%s──────────────────────────────────────────────────────────────────────────────────────────────%s\n",
                ANSI_DIM, ANSI_RESET);

        for (size_t i = 0; i < list->count; i++) {
            const player_list_item_t *p = &list->players[i];
            char time_buf[64];
            format_time(p->last_login, time_buf, sizeof(time_buf));

            const char *status_str = p->online ? "\033[1;32mOnline \033[0m" : "\033[2mOffline\033[0m";
            const char *rank_col = get_rank_ansi(p->rank.color);

            fprintf(out, "%s%-18s%s %s%-38s%s %s %s%-12s%s %s%s%s\n",
                    ANSI_BOLD, p->username, ANSI_RESET,
                    ANSI_CYAN, p->uuid, ANSI_RESET,
                    status_str,
                    rank_col, (p->rank.name && *p->rank.name ? p->rank.name : "Default"), ANSI_RESET,
                    ANSI_DIM, time_buf, ANSI_RESET);
        }
        fprintf(out, "\n");
    } else {
        fprintf(out, "\nPlayers (%zu total, %zu online):\n", list->count, list->online_count);
        fprintf(out, "%-18s %-38s %-10s %-12s %s\n",
                "USERNAME", "UUID", "STATUS", "RANK", "LAST LOGIN");
        fprintf(out, "──────────────────────────────────────────────────────────────────────────────────────────────\n");

        for (size_t i = 0; i < list->count; i++) {
            const player_list_item_t *p = &list->players[i];
            char time_buf[64];
            format_time(p->last_login, time_buf, sizeof(time_buf));

            fprintf(out, "%-18s %-38s %-10s %-12s %s\n",
                    p->username, p->uuid,
                    p->online ? "Online" : "Offline",
                    (p->rank.name && *p->rank.name ? p->rank.name : "Default"),
                    time_buf);
        }
        fprintf(out, "\n");
    }
    fflush(out);
}

void ui_print_json_player_list(FILE *out, const player_list_t *list) {
    arena_t *a = arena_create(16384);
    json_value_t *root = json_create_object(a);
    json_object_set(a, root, "online", json_create_bool(a, list->online));
    json_object_set(a, root, "count", json_create_int(a, (int64_t)list->count));
    json_object_set(a, root, "online_count", json_create_int(a, (int64_t)list->online_count));

    json_value_t *p_arr = json_create_array(a);
    for (size_t i = 0; i < list->count; i++) {
        const player_list_item_t *p = &list->players[i];
        json_value_t *po = json_create_object(a);
        json_object_set(a, po, "uuid", json_create_string(a, p->uuid));
        json_object_set(a, po, "username", json_create_string(a, p->username));
        json_object_set(a, po, "online", json_create_bool(a, p->online));
        json_object_set(a, po, "last_login", json_create_int(a, p->last_login));

        json_value_t *ro = json_create_object(a);
        json_object_set(a, ro, "id", json_create_string(a, p->rank.id));
        json_object_set(a, ro, "name", json_create_string(a, p->rank.name));
        json_object_set(a, ro, "color", json_create_string(a, p->rank.color));
        json_object_set(a, ro, "prefix", json_create_string(a, p->rank.prefix));
        json_object_set(a, po, "rank", ro);

        json_array_append(a, p_arr, po);
    }
    json_object_set(a, root, "players", p_arr);

    json_print(root, out, true);
    arena_destroy(a);
}

void ui_error(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    if (s_color_enabled) {
        fprintf(stderr, "%s[ERROR]%s ", ANSI_RED, ANSI_RESET);
    } else {
        fprintf(stderr, "[ERROR] ");
    }
    vfprintf(stderr, fmt, args);
    fprintf(stderr, "\n");
    va_end(args);
}

void ui_warn(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    if (s_color_enabled) {
        fprintf(stderr, "%s[WARN]%s ", ANSI_YELLOW, ANSI_RESET);
    } else {
        fprintf(stderr, "[WARN] ");
    }
    vfprintf(stderr, fmt, args);
    fprintf(stderr, "\n");
    va_end(args);
}

void ui_success(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    if (s_color_enabled) {
        printf("%s[OK]%s ", ANSI_GREEN, ANSI_RESET);
    } else {
        printf("[OK] ");
    }
    vprintf(fmt, args);
    printf("\n");
    va_end(args);
}
