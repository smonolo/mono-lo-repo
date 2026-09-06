#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "arena.h"
#include "config.h"
#include "http.h"
#include "json.h"
#include "player.h"
#include "ui.h"
#include "uuid.h"

#define VERSION "1.0.0"

typedef enum {
    CMD_ROOT = 0,
    CMD_HELP,
    CMD_VERSION,
    CMD_PLAYER,
    CMD_PLAYER_GET,
    CMD_PLAYER_DELETE
} command_type_t;

typedef struct {
    command_type_t cmd;
    const char *identifier;
    const char *help_arg;
} parsed_args_t;

static void print_root_help(void) {
    printf("A robust C CLI toolkit for administering the Minecraft project via minecraft-api.\n"
           "Provides commands for managing players and administrative tasks.\n\n"
           "Usage:\n"
           "  mc [command]\n\n"
           "Available Commands:\n"
           "  completion  Generate the autocompletion script for the specified shell\n"
           "  help        Help about any command\n"
           "  list        List registered players in SMEssential (alias: ls)\n"
           "  player      Manage players in the SMEssential database\n\n"
           "Flags:\n"
           "      --api-key string    Admin API Key for minecraft-api (overrides ADMIN_API_KEY)\n"
           "      --api-url string    minecraft-api base URL (overrides MINECRAFT_API_URL)\n"
           "      --env-file string   Path to custom .env file\n"
           "  -h, --help              help for mc\n"
           "      --json              Output results in JSON format\n"
           "  -q, --quiet             Suppress banner and non-essential output\n"
           "  -v, --version           version for mc\n\n"
           "Use \"mc [command] --help\" for more information about a command.\n");
}

static void print_player_help(void) {
    printf("Perform operations on players stored in the SMEssential PostgreSQL database.\n\n"
           "Usage:\n"
           "  mc player [command]\n\n"
           "Aliases:\n"
           "  player, players, p\n\n"
           "Available Commands:\n"
           "  delete      Completely delete a player and their target records via minecraft-api\n"
           "  get         Get details of a player (UUID, username, first login, last login, ranks)\n"
           "  list        List all players registered in SMEssential (aliases: list, ls, l)\n\n"
           "Flags:\n"
           "  -h, --help   help for player\n\n"
           "Global Flags:\n"
           "      --api-key string    Admin API Key for minecraft-api (overrides ADMIN_API_KEY)\n"
           "      --api-url string    minecraft-api base URL (overrides MINECRAFT_API_URL)\n"
           "      --env-file string   Path to custom .env file\n"
           "      --json              Output results in JSON format\n"
           "  -q, --quiet             Suppress banner and non-essential output\n\n"
           "Use \"mc player [command] --help\" for more information about a command.\n");
}

static void print_player_list_help(void) {
    printf("Retrieves and displays a summary table of all known players from SMEssential via minecraft-api.\n\n"
           "Usage:\n"
           "  mc player list [flags]\n\n"
           "Aliases:\n"
           "  list, ls, l\n\n"
           "Flags:\n"
           "  -h, --help   help for list\n\n"
           "Global Flags:\n"
           "      --api-key string    Admin API Key for minecraft-api (overrides ADMIN_API_KEY)\n"
           "      --api-url string    minecraft-api base URL (overrides MINECRAFT_API_URL)\n"
           "      --env-file string   Path to custom .env file\n"
           "      --json              Output results in JSON format\n"
           "  -q, --quiet             Suppress banner and non-essential output\n");
}

static void print_player_get_help(void) {
    printf("Retrieves and displays a player's core profile information from SMEssential via minecraft-api.\n"
           "Displays UUID, username, first login timestamp, last login timestamp, and assigned ranks.\n\n"
           "Usage:\n"
           "  mc player get <username|uuid> [flags]\n\n"
           "Aliases:\n"
           "  get, info, show, view\n\n"
           "Flags:\n"
           "  -h, --help   help for get\n\n"
           "Global Flags:\n"
           "      --api-key string    Admin API Key for minecraft-api (overrides ADMIN_API_KEY)\n"
           "      --api-url string    minecraft-api base URL (overrides MINECRAFT_API_URL)\n"
           "      --env-file string   Path to custom .env file\n"
           "      --json              Output results in JSON format\n"
           "  -q, --quiet             Suppress banner and non-essential output\n");
}

static void print_player_delete_help(void) {
    printf("Permanently removes a player and all linked records from the SMEssential database via minecraft-api.\n\n"
           "Affected tables:\n"
           "  • smessential_users (player profile)\n"
           "  • smessential_user_ranks (assigned rank permissions)\n"
           "  • smessential_user_display_ranks (custom player display rank)\n"
           "  • smessential_punishments (ONLY where the player is the target, preserving staff audit logs)\n"
           "  • smessential_whitelist (whitelist entries targeting this player)\n\n"
           "Safe operations:\n"
           "  • Punishments issued BY this player as staff are strictly PRESERVED to maintain audit history.\n"
           "  • Cache invalidation on minecraft-api is automatically triggered.\n"
           "  • All deletions are wrapped in an atomic PostgreSQL transaction on the API.\n\n"
           "Usage:\n"
           "  mc player delete <username|uuid> [flags]\n\n"
           "Aliases:\n"
           "  delete, remove, rm, del\n\n"
           "Flags:\n"
           "      --dry-run   Simulate deletion without modifying the database\n"
           "      --force     Alias for --yes\n"
           "  -h, --help      help for delete\n"
           "  -y, --yes       Skip interactive confirmation prompt\n\n"
           "Global Flags:\n"
           "      --api-key string    Admin API Key for minecraft-api (overrides ADMIN_API_KEY)\n"
           "      --api-url string    minecraft-api base URL (overrides MINECRAFT_API_URL)\n"
           "      --env-file string   Path to custom .env file\n"
           "      --json              Output results in JSON format\n"
           "  -q, --quiet             Suppress banner and non-essential output\n");
}

static bool is_player_cmd(const char *arg) {
    return strcmp(arg, "player") == 0 ||
           strcmp(arg, "players") == 0 ||
           strcmp(arg, "p") == 0;
}

static bool is_get_cmd(const char *arg) {
    return strcmp(arg, "get") == 0 ||
           strcmp(arg, "info") == 0 ||
           strcmp(arg, "show") == 0 ||
           strcmp(arg, "view") == 0;
}

static bool is_delete_cmd(const char *arg) {
    return strcmp(arg, "delete") == 0 ||
           strcmp(arg, "remove") == 0 ||
           strcmp(arg, "rm") == 0 ||
           strcmp(arg, "del") == 0;
}

static bool is_list_cmd(const char *arg) {
    return strcmp(arg, "list") == 0 ||
           strcmp(arg, "ls") == 0 ||
           strcmp(arg, "l") == 0;
}

static int run_player_list(arena_t *arena, const config_t *cfg) {
    char url[1024];
    snprintf(url, sizeof(url), "%s/players", cfg->api_url);

    http_response_t res = http_request(arena, HTTP_METHOD_GET, url, cfg->api_key, 10);
    if (!res.success) {
        ui_error("failed to connect to minecraft-api at %s: %s", cfg->api_url, res.error_msg);
        return 1;
    }

    if (res.status_code < 200 || res.status_code >= 300) {
        char parse_err[128];
        json_value_t *err_root = json_parse(arena, res.body, res.body_len, parse_err, sizeof(parse_err));
        char err_msg[256];
        parse_api_error_message(err_root, res.status_code, err_msg, sizeof(err_msg));
        ui_error("%s", err_msg);
        return 1;
    }

    char parse_err[128];
    json_value_t *root = json_parse(arena, res.body, res.body_len, parse_err, sizeof(parse_err));
    if (!root) {
        ui_error("failed to parse API response: %s", parse_err);
        return 1;
    }

    player_list_t list;
    if (!parse_player_list(arena, root, &list, parse_err, sizeof(parse_err))) {
        ui_error("failed to parse player list: %s", parse_err);
        return 1;
    }

    if (cfg->json_output) {
        ui_print_json_player_list(stdout, &list);
        return 0;
    }

    if (!cfg->quiet) {
        ui_print_banner();
    }

    ui_print_player_list(stdout, &list);
    return 0;
}

static int run_player_get(arena_t *arena, const config_t *cfg, const char *identifier) {
    char escaped_id[256];
    url_path_escape(identifier, escaped_id, sizeof(escaped_id));

    char url[1024];
    snprintf(url, sizeof(url), "%s/admin/players/%s", cfg->api_url, escaped_id);

    http_response_t res = http_request(arena, HTTP_METHOD_GET, url, cfg->api_key, 10);
    if (!res.success) {
        ui_error("failed to connect to minecraft-api at %s: %s", cfg->api_url, res.error_msg);
        return 1;
    }

    if (res.status_code == 404) {
        if (cfg->json_output) {
            ui_print_json_not_found(stdout, identifier);
            return 0;
        }
        ui_error("player \"%s\" not found in SMEssential database", identifier);
        return 1;
    }

    if (res.status_code == 401 || res.status_code == 403) {
        ui_error("unauthorized: missing or invalid admin API key (check ADMIN_API_KEY)");
        return 1;
    }

    char parse_err[128];
    json_value_t *root = json_parse(arena, res.body, res.body_len, parse_err, sizeof(parse_err));
    if (!root) {
        ui_error("failed to parse API response: %s", parse_err);
        return 1;
    }

    if (res.status_code < 200 || res.status_code >= 300) {
        char err_msg[256];
        parse_api_error_message(root, res.status_code, err_msg, sizeof(err_msg));
        ui_error("%s", err_msg);
        return 1;
    }

    player_summary_t summary;
    if (!parse_player_summary(arena, root, &summary, parse_err, sizeof(parse_err))) {
        ui_error("failed to parse player summary: %s", parse_err);
        return 1;
    }

    if (cfg->json_output) {
        ui_print_json_profile(stdout, &summary);
        return 0;
    }

    if (!cfg->quiet) {
        ui_print_banner();
    }

    ui_print_player_profile(stdout, &summary);
    return 0;
}

static int run_player_delete(arena_t *arena, const config_t *cfg, const char *identifier) {
    char escaped_id[256];
    url_path_escape(identifier, escaped_id, sizeof(escaped_id));

    char url[1024];
    snprintf(url, sizeof(url), "%s/admin/players/%s", cfg->api_url, escaped_id);

    // 1. Fetch summary first
    http_response_t res = http_request(arena, HTTP_METHOD_GET, url, cfg->api_key, 10);
    if (!res.success) {
        ui_error("failed to connect to minecraft-api at %s: %s", cfg->api_url, res.error_msg);
        return 1;
    }

    if (res.status_code == 404) {
        if (cfg->json_output) {
            ui_print_json_not_found(stdout, identifier);
            return 0;
        }
        ui_error("player \"%s\" not found in any SMEssential database records", identifier);
        return 1;
    }

    if (res.status_code == 401 || res.status_code == 403) {
        ui_error("unauthorized: missing or invalid admin API key (check ADMIN_API_KEY)");
        return 1;
    }

    char parse_err[128];
    json_value_t *root = json_parse(arena, res.body, res.body_len, parse_err, sizeof(parse_err));
    if (!root) {
        ui_error("failed to parse API response: %s", parse_err);
        return 1;
    }

    if (res.status_code < 200 || res.status_code >= 300) {
        char err_msg[256];
        parse_api_error_message(root, res.status_code, err_msg, sizeof(err_msg));
        ui_error("%s", err_msg);
        return 1;
    }

    player_summary_t summary;
    if (!parse_player_summary(arena, root, &summary, parse_err, sizeof(parse_err))) {
        ui_error("failed to parse player summary: %s", parse_err);
        return 1;
    }

    // Dry-run handling
    if (cfg->dry_run) {
        if (cfg->json_output) {
            ui_print_json_dry_run(stdout, &summary);
            return 0;
        }
        if (!cfg->quiet) {
            ui_print_banner();
        }
        ui_print_player_summary(stdout, &summary);
        ui_warn("Dry run completed: no database modifications were made.");
        return 0;
    }

    // Interactive confirmation
    if (!cfg->yes) {
        if (!cfg->quiet) {
            ui_print_banner();
        }
        ui_print_player_summary(stdout, &summary);

        if (summary.total_records_to_delete == 0) {
            ui_warn("No records found to delete.");
            return 0;
        }

        char prompt[256];
        snprintf(prompt, sizeof(prompt),
                 "Permanently delete player \"%s\" (%s) and all %d related record(s)?",
                 summary.player.username, summary.player.uuid, summary.total_records_to_delete);

        if (!ui_ask_confirmation(prompt)) {
            ui_warn("Deletion cancelled by user. No records were modified.");
            return 0;
        }
    }

    // Execute deletion via API
    http_response_t del_res = http_request(arena, HTTP_METHOD_DELETE, url, cfg->api_key, 10);
    if (!del_res.success) {
        ui_error("deletion failed via minecraft-api: %s", del_res.error_msg);
        return 1;
    }

    if (del_res.status_code < 200 || del_res.status_code >= 300) {
        json_value_t *err_root = json_parse(arena, del_res.body, del_res.body_len, NULL, 0);
        char err_msg[256];
        parse_api_error_message(err_root, del_res.status_code, err_msg, sizeof(err_msg));
        ui_error("deletion failed via minecraft-api: %s", err_msg);
        return 1;
    }

    json_value_t *del_root = json_parse(arena, del_res.body, del_res.body_len, parse_err, sizeof(parse_err));
    if (!del_root) {
        ui_error("failed to parse delete response: %s", parse_err);
        return 1;
    }

    delete_result_t result;
    if (!parse_delete_result(arena, del_root, &result, parse_err, sizeof(parse_err))) {
        ui_error("failed to parse delete result: %s", parse_err);
        return 1;
    }

    if (cfg->json_output) {
        ui_print_json_delete_success(stdout, &result);
        return 0;
    }

    if (cfg->quiet) {
        ui_success("Deleted player %s (UUID: %s) - %ld total records removed",
                   result.player.username, result.player.uuid, (long)result.total_deleted);
        return 0;
    }

    ui_print_delete_success(stdout, &result);
    return 0;
}

int main(int argc, char **argv) {
    ui_init();

    arena_t *arena = arena_create(128 * 1024);
    if (!arena) {
        fprintf(stderr, "Fatal: failed to allocate memory arena\n");
        return 1;
    }

    const char *custom_env_file = NULL;
    const char *flag_api_url = NULL;
    const char *flag_api_key = NULL;
    bool flag_json = false;
    bool flag_quiet = false;
    bool flag_yes = false;
    bool flag_dry_run = false;
    bool flag_help = false;
    bool flag_version = false;

    // Scan for env-file first
    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "--env-file") == 0 && i + 1 < argc) {
            custom_env_file = argv[i + 1];
            i++;
        } else if (strncmp(argv[i], "--env-file=", 11) == 0) {
            custom_env_file = argv[i] + 11;
        }
    }

    config_t *cfg = config_init(arena, custom_env_file);
    if (!cfg) {
        ui_error("failed to load custom env file \"%s\"", custom_env_file);
        arena_destroy(arena);
        return 1;
    }

    const char *subcommands[4] = { NULL };
    int sub_count = 0;

    for (int i = 1; i < argc; i++) {
        const char *arg = argv[i];

        if (strcmp(arg, "-h") == 0 || strcmp(arg, "--help") == 0) {
            flag_help = true;
        } else if (strcmp(arg, "-v") == 0 || strcmp(arg, "--version") == 0) {
            flag_version = true;
        } else if (strcmp(arg, "--json") == 0) {
            flag_json = true;
        } else if (strcmp(arg, "-q") == 0 || strcmp(arg, "--quiet") == 0) {
            flag_quiet = true;
        } else if (strcmp(arg, "-y") == 0 || strcmp(arg, "--yes") == 0 || strcmp(arg, "--force") == 0) {
            flag_yes = true;
        } else if (strcmp(arg, "--dry-run") == 0) {
            flag_dry_run = true;
        } else if (strcmp(arg, "--api-url") == 0 && i + 1 < argc) {
            flag_api_url = argv[++i];
        } else if (strncmp(arg, "--api-url=", 10) == 0) {
            flag_api_url = arg + 10;
        } else if (strcmp(arg, "--api-key") == 0 && i + 1 < argc) {
            flag_api_key = argv[++i];
        } else if (strncmp(arg, "--api-key=", 10) == 0) {
            flag_api_key = arg + 10;
        } else if (strcmp(arg, "--env-file") == 0 && i + 1 < argc) {
            i++; // already handled
        } else if (strncmp(arg, "--env-file=", 11) == 0) {
            // already handled
        } else if (arg[0] == '-') {
            ui_error("unknown flag: %s", arg);
            arena_destroy(arena);
            return 1;
        } else {
            if (sub_count < 4) {
                subcommands[sub_count++] = arg;
            }
        }
    }

    if (flag_api_url) {
        config_set_api_url(arena, cfg, flag_api_url);
    }
    if (flag_api_key) {
        config_set_api_key(arena, cfg, flag_api_key);
    }
    cfg->json_output = flag_json;
    cfg->quiet = flag_quiet;
    cfg->yes = flag_yes;
    cfg->dry_run = flag_dry_run;

    if (flag_version) {
        printf("mc version %s (C11, optimized)\n", VERSION);
        arena_destroy(arena);
        return 0;
    }

    // Route commands
    if (sub_count == 0) {
        print_root_help();
        arena_destroy(arena);
        return 0;
    }

    if (strcmp(subcommands[0], "help") == 0) {
        if (sub_count == 1) {
            print_root_help();
        } else if (is_list_cmd(subcommands[1])) {
            print_player_list_help();
        } else if (is_player_cmd(subcommands[1])) {
            if (sub_count == 2) {
                print_player_help();
            } else if (is_get_cmd(subcommands[2])) {
                print_player_get_help();
            } else if (is_delete_cmd(subcommands[2])) {
                print_player_delete_help();
            } else if (is_list_cmd(subcommands[2])) {
                print_player_list_help();
            } else {
                print_player_help();
            }
        } else {
            print_root_help();
        }
        arena_destroy(arena);
        return 0;
    }

    if (is_list_cmd(subcommands[0])) {
        if (flag_help) {
            print_player_list_help();
            arena_destroy(arena);
            return 0;
        }
        int rc = run_player_list(arena, cfg);
        arena_destroy(arena);
        return rc;
    }

    if (is_player_cmd(subcommands[0])) {
        if (sub_count == 1) {
            if (flag_help) {
                print_player_help();
                arena_destroy(arena);
                return 0;
            }
            print_player_help();
            arena_destroy(arena);
            return 0;
        }

        if (is_list_cmd(subcommands[1])) {
            if (flag_help) {
                print_player_list_help();
                arena_destroy(arena);
                return 0;
            }
            int rc = run_player_list(arena, cfg);
            arena_destroy(arena);
            return rc;
        }

        if (is_get_cmd(subcommands[1])) {
            if (flag_help) {
                print_player_get_help();
                arena_destroy(arena);
                return 0;
            }
            if (sub_count < 3) {
                ui_error("accepts 1 arg(s), received 0\nUsage:  mc player get <username|uuid> [flags]");
                arena_destroy(arena);
                return 1;
            }
            int rc = run_player_get(arena, cfg, subcommands[2]);
            arena_destroy(arena);
            return rc;
        }

        if (is_delete_cmd(subcommands[1])) {
            if (flag_help) {
                print_player_delete_help();
                arena_destroy(arena);
                return 0;
            }
            if (sub_count < 3) {
                ui_error("accepts 1 arg(s), received 0\nUsage:  mc player delete <username|uuid> [flags]");
                arena_destroy(arena);
                return 1;
            }
            int rc = run_player_delete(arena, cfg, subcommands[2]);
            arena_destroy(arena);
            return rc;
        }

        ui_error("unknown command \"%s\" for \"mc player\"", subcommands[1]);
        arena_destroy(arena);
        return 1;
    }

    if (flag_help) {
        print_root_help();
        arena_destroy(arena);
        return 0;
    }

    ui_error("unknown command \"%s\" for \"mc\"", subcommands[0]);
    arena_destroy(arena);
    return 1;
}
