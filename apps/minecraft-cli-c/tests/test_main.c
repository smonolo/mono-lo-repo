#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "../src/arena.h"
#include "../src/config.h"
#include "../src/json.h"
#include "../src/player.h"
#include "../src/uuid.h"

static int g_tests_run = 0;
static int g_tests_passed = 0;

#define RUN_TEST(fn) do { \
    g_tests_run++; \
    printf("Running %s... ", #fn); \
    fn(); \
    g_tests_passed++; \
    printf("PASSED\n"); \
} while (0)

static void test_uuid_normalization(void) {
    char out[64];

    // Already dashed
    assert(normalize_uuid("7cd493a1-1214-4da3-9ac1-a0bfef50b75c", out, sizeof(out)));
    assert(strcmp(out, "7cd493a1-1214-4da3-9ac1-a0bfef50b75c") == 0);

    // Un-dashed 32 hex chars
    assert(normalize_uuid("7cd493a112144da39ac1a0bfef50b75c", out, sizeof(out)));
    assert(strcmp(out, "7cd493a1-1214-4da3-9ac1-a0bfef50b75c") == 0);

    // Plain username
    assert(normalize_uuid("smnl", out, sizeof(out)));
    assert(strcmp(out, "smnl") == 0);

    // Username with whitespace
    assert(normalize_uuid("  Notch  ", out, sizeof(out)));
    assert(strcmp(out, "Notch") == 0);
}

static void test_is_uuid(void) {
    assert(is_uuid("7cd493a1-1214-4da3-9ac1-a0bfef50b75c") == true);
    assert(is_uuid("7CD493A1-1214-4DA3-9AC1-A0BFEF50B75C") == true);
    assert(is_uuid("7cd493a112144da39ac1a0bfef50b75c") == true);
    assert(is_uuid("smnl") == false);
    assert(is_uuid("invalid-uuid-format-here-12345") == false);
    assert(is_uuid("") == false);
    assert(is_uuid("   ") == false);
}

static void test_url_path_escape(void) {
    char out[128];
    url_path_escape("Notch", out, sizeof(out));
    assert(strcmp(out, "Notch") == 0);

    url_path_escape("foo bar", out, sizeof(out));
    assert(strcmp(out, "foo%20bar") == 0);

    url_path_escape("7cd493a1-1214-4da3-9ac1-a0bfef50b75c", out, sizeof(out));
    assert(strcmp(out, "7cd493a1-1214-4da3-9ac1-a0bfef50b75c") == 0);
}

static void test_arena_allocator(void) {
    arena_t *a = arena_create(1024);
    assert(a != NULL);

    void *p1 = arena_alloc(a, 64);
    assert(p1 != NULL);

    char *s = arena_strdup(a, "hello arena");
    assert(s != NULL);
    assert(strcmp(s, "hello arena") == 0);

    arena_reset(a);
    char *s2 = arena_strdup(a, "after reset");
    assert(strcmp(s2, "after reset") == 0);

    arena_destroy(a);
}

static void test_json_parser(void) {
    arena_t *a = arena_create(4096);
    const char *json_text =
        "{\n"
        "  \"name\": \"Steve\",\n"
        "  \"age\": 42,\n"
        "  \"online\": true,\n"
        "  \"score\": 99.5,\n"
        "  \"inventory\": [\"sword\", \"pickaxe\"],\n"
        "  \"meta\": null\n"
        "}";

    char err[128];
    json_value_t *root = json_parse(a, json_text, strlen(json_text), err, sizeof(err));
    assert(root != NULL);
    assert(root->type == JSON_OBJECT);

    assert(strcmp(json_get_string(root, "name", ""), "Steve") == 0);
    assert(json_get_int64(root, "age", 0) == 42);
    assert(json_get_bool(root, "online", false) == true);
    assert(json_get_double(root, "score", 0.0) == 99.5);

    const json_value_t *inv = json_get_array(root, "inventory");
    assert(inv != NULL);
    assert(inv->array.count == 2);
    assert(strcmp(inv->array.items[0]->str_val, "sword") == 0);
    assert(strcmp(inv->array.items[1]->str_val, "pickaxe") == 0);

    const json_value_t *meta = json_get(root, "meta");
    assert(meta != NULL);
    assert(meta->type == JSON_NULL);

    arena_destroy(a);
}

static void test_player_summary_deserialization(void) {
    arena_t *a = arena_create(8192);
    const char *payload =
        "{\n"
        "  \"success\": true,\n"
        "  \"summary\": {\n"
        "    \"player\": {\n"
        "      \"uuid\": \"7cd493a1-1214-4da3-9ac1-a0bfef50b75c\",\n"
        "      \"username\": \"Notch\",\n"
        "      \"first_join\": 1600000000000,\n"
        "      \"last_join\": 1700000000000,\n"
        "      \"in_users\": true\n"
        "    },\n"
        "    \"ranks\": [\n"
        "      {\n"
        "        \"id\": \"admin\",\n"
        "        \"name\": \"Admin\",\n"
        "        \"color\": \"red\",\n"
        "        \"prefix\": \"[Admin] \"\n"
        "      }\n"
        "    ],\n"
        "    \"display_rank\": {\n"
        "      \"id\": \"vip\",\n"
        "      \"name\": \"VIP\",\n"
        "      \"color\": \"gold\",\n"
        "      \"prefix\": \"[VIP] \"\n"
        "    },\n"
        "    \"target_punishments\": [\n"
        "      {\n"
        "        \"id\": \"p1\",\n"
        "        \"type\": \"WARN\",\n"
        "        \"reason\": \"Griefing test\",\n"
        "        \"issuer\": \"Moderator\",\n"
        "        \"created_at\": 1650000000000,\n"
        "        \"expires_at\": 0\n"
        "      }\n"
        "    ],\n"
        "    \"issuer_punishments_count\": 5,\n"
        "    \"whitelist\": {\n"
        "      \"name\": \"Notch\",\n"
        "      \"added_by\": \"Console\",\n"
        "      \"added_at\": 1600000000000\n"
        "    },\n"
        "    \"total_records_to_delete\": 4\n"
        "  }\n"
        "}";

    json_value_t *root = json_parse(a, payload, strlen(payload), NULL, 0);
    assert(root != NULL);

    player_summary_t sum;
    char err[128];
    assert(parse_player_summary(a, root, &sum, err, sizeof(err)));

    assert(strcmp(sum.player.username, "Notch") == 0);
    assert(strcmp(sum.player.uuid, "7cd493a1-1214-4da3-9ac1-a0bfef50b75c") == 0);
    assert(sum.player.first_join == 1600000000000LL);
    assert(sum.player.last_join == 1700000000000LL);
    assert(sum.player.in_users == true);

    assert(sum.ranks_count == 1);
    assert(strcmp(sum.ranks[0].name, "Admin") == 0);
    assert(strcmp(sum.ranks[0].color, "red") == 0);

    assert(sum.display_rank != NULL);
    assert(strcmp(sum.display_rank->name, "VIP") == 0);
    assert(strcmp(sum.display_rank->color, "gold") == 0);

    assert(sum.punishments_count == 1);
    assert(strcmp(sum.target_punishments[0].type, "WARN") == 0);
    assert(strcmp(sum.target_punishments[0].reason, "Griefing test") == 0);
    assert(strcmp(sum.target_punishments[0].issuer, "Moderator") == 0);

    assert(sum.issuer_punishments_count == 5);

    assert(sum.whitelist != NULL);
    assert(strcmp(sum.whitelist->name, "Notch") == 0);
    assert(strcmp(sum.whitelist->added_by, "Console") == 0);

    assert(sum.total_records_to_delete == 4);

    arena_destroy(a);
}

static void test_delete_result_deserialization(void) {
    arena_t *a = arena_create(4096);
    const char *payload =
        "{\n"
        "  \"success\": true,\n"
        "  \"result\": {\n"
        "    \"player\": {\n"
        "      \"uuid\": \"7cd493a1-1214-4da3-9ac1-a0bfef50b75c\",\n"
        "      \"username\": \"Notch\",\n"
        "      \"first_join\": 1600000000000,\n"
        "      \"last_join\": 1700000000000,\n"
        "      \"in_users\": true\n"
        "    },\n"
        "    \"deleted_users\": 1,\n"
        "    \"deleted_user_ranks\": 2,\n"
        "    \"deleted_user_display_ranks\": 1,\n"
        "    \"deleted_punishments\": 3,\n"
        "    \"deleted_whitelist\": 1,\n"
        "    \"preserved_issuer_punishments\": 7,\n"
        "    \"total_deleted\": 8\n"
        "  }\n"
        "}";

    json_value_t *root = json_parse(a, payload, strlen(payload), NULL, 0);
    assert(root != NULL);

    delete_result_t res;
    char err[128];
    assert(parse_delete_result(a, root, &res, err, sizeof(err)));

    assert(strcmp(res.player.username, "Notch") == 0);
    assert(res.deleted_users == 1);
    assert(res.deleted_user_ranks == 2);
    assert(res.deleted_user_display_ranks == 1);
    assert(res.deleted_punishments == 3);
    assert(res.deleted_whitelist == 1);
    assert(res.preserved_issuer_punishments == 7);
    assert(res.total_deleted == 8);

    arena_destroy(a);
}

int main(void) {
    printf("=== Running minecraft-cli-c unit tests ===\n");
    RUN_TEST(test_uuid_normalization);
    RUN_TEST(test_is_uuid);
    RUN_TEST(test_url_path_escape);
    RUN_TEST(test_arena_allocator);
    RUN_TEST(test_json_parser);
    RUN_TEST(test_player_summary_deserialization);
    RUN_TEST(test_delete_result_deserialization);
    printf("All %d/%d tests passed successfully!\n", g_tests_passed, g_tests_run);
    return 0;
}
