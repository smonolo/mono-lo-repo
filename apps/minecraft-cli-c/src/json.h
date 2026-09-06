#ifndef JSON_H
#define JSON_H

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include "arena.h"

typedef enum {
    JSON_NULL = 0,
    JSON_BOOL,
    JSON_NUMBER,
    JSON_STRING,
    JSON_ARRAY,
    JSON_OBJECT
} json_type_t;

typedef struct json_value json_value_t;

typedef struct json_member {
    const char *key;
    json_value_t *value;
    struct json_member *next;
} json_member_t;

struct json_value {
    json_type_t type;
    union {
        bool bool_val;
        struct {
            double num_val;
            int64_t int_val;
            bool is_int;
        } number;
        const char *str_val;
        struct {
            json_value_t **items;
            size_t count;
            size_t capacity;
        } array;
        struct {
            json_member_t *head;
            json_member_t *tail;
            size_t count;
        } object;
    };
};

/* Parsing */
json_value_t *json_parse(arena_t *arena, const char *json_str, size_t len, char *err_buf, size_t err_len);

/* Accessors */
json_value_t *json_get(const json_value_t *obj, const char *key);
const char *json_get_string(const json_value_t *obj, const char *key, const char *default_val);
int64_t json_get_int64(const json_value_t *obj, const char *key, int64_t default_val);
double json_get_double(const json_value_t *obj, const char *key, double default_val);
bool json_get_bool(const json_value_t *obj, const char *key, bool default_val);
const json_value_t *json_get_object(const json_value_t *obj, const char *key);
const json_value_t *json_get_array(const json_value_t *obj, const char *key);

/* Builders */
json_value_t *json_create_null(arena_t *arena);
json_value_t *json_create_bool(arena_t *arena, bool val);
json_value_t *json_create_int(arena_t *arena, int64_t val);
json_value_t *json_create_double(arena_t *arena, double val);
json_value_t *json_create_string(arena_t *arena, const char *str);
json_value_t *json_create_array(arena_t *arena);
void json_array_append(arena_t *arena, json_value_t *arr, json_value_t *item);
json_value_t *json_create_object(arena_t *arena);
void json_object_set(arena_t *arena, json_value_t *obj, const char *key, json_value_t *val);

/* Formatting & Output */
void json_print(const json_value_t *val, FILE *out, bool pretty);
size_t json_to_string(const json_value_t *val, char *buf, size_t buf_size, bool pretty);

#endif /* JSON_H */
