#include "json.h"
#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    arena_t *arena;
    const char *src;
    size_t len;
    size_t pos;
    char *err_buf;
    size_t err_len;
} json_parser_t;

static void set_error(json_parser_t *p, const char *msg) {
    if (p->err_buf && p->err_len > 0) {
        snprintf(p->err_buf, p->err_len, "JSON parse error at offset %zu: %s", p->pos, msg);
    }
}

static inline void skip_whitespace(json_parser_t *p) {
    while (p->pos < p->len) {
        char c = p->src[p->pos];
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            p->pos++;
        } else {
            break;
        }
    }
}

static json_value_t *parse_value(json_parser_t *p);

static char *parse_string_token(json_parser_t *p) {
    if (p->pos >= p->len || p->src[p->pos] != '"') {
        set_error(p, "expected '\"'");
        return NULL;
    }
    p->pos++; // skip opening quote

    size_t start = p->pos;
    size_t unescaped_len = 0;
    bool has_escapes = false;

    while (p->pos < p->len) {
        char c = p->src[p->pos];
        if (c == '"') {
            break;
        }
        if (c == '\\') {
            has_escapes = true;
            p->pos++;
            if (p->pos >= p->len) {
                set_error(p, "unfinished escape sequence");
                return NULL;
            }
        }
        unescaped_len++;
        p->pos++;
    }

    if (p->pos >= p->len || p->src[p->pos] != '"') {
        set_error(p, "unterminated string");
        return NULL;
    }

    size_t raw_len = p->pos - start;
    p->pos++; // skip closing quote

    if (!has_escapes) {
        return arena_strndup(p->arena, p->src + start, raw_len);
    }

    char *buf = (char *)arena_alloc(p->arena, unescaped_len + 1);
    if (!buf) return NULL;

    size_t w = 0;
    for (size_t r = 0; r < raw_len; r++) {
        char c = p->src[start + r];
        if (c == '\\' && r + 1 < raw_len) {
            r++;
            char esc = p->src[start + r];
            switch (esc) {
                case '"':  buf[w++] = '"'; break;
                case '\\': buf[w++] = '\\'; break;
                case '/':  buf[w++] = '/'; break;
                case 'b':  buf[w++] = '\b'; break;
                case 'f':  buf[w++] = '\f'; break;
                case 'n':  buf[w++] = '\n'; break;
                case 'r':  buf[w++] = '\r'; break;
                case 't':  buf[w++] = '\t'; break;
                case 'u': {
                    if (r + 4 < raw_len) {
                        char hex[5] = { p->src[start + r + 1], p->src[start + r + 2],
                                        p->src[start + r + 3], p->src[start + r + 4], '\0' };
                        long code = strtol(hex, NULL, 16);
                        if (code > 0 && code <= 0x7F) {
                            buf[w++] = (char)code;
                        } else {
                            buf[w++] = '?';
                        }
                        r += 4;
                    } else {
                        buf[w++] = '?';
                    }
                    break;
                }
                default:
                    buf[w++] = esc;
                    break;
            }
        } else {
            buf[w++] = c;
        }
    }
    buf[w] = '\0';
    return buf;
}

static json_value_t *parse_number(json_parser_t *p) {
    size_t start = p->pos;
    bool is_float = false;

    if (p->src[p->pos] == '-') {
        p->pos++;
    }

    while (p->pos < p->len && isdigit((unsigned char)p->src[p->pos])) {
        p->pos++;
    }

    if (p->pos < p->len && p->src[p->pos] == '.') {
        is_float = true;
        p->pos++;
        while (p->pos < p->len && isdigit((unsigned char)p->src[p->pos])) {
            p->pos++;
        }
    }

    if (p->pos < p->len && (p->src[p->pos] == 'e' || p->src[p->pos] == 'E')) {
        is_float = true;
        p->pos++;
        if (p->pos < p->len && (p->src[p->pos] == '+' || p->src[p->pos] == '-')) {
            p->pos++;
        }
        while (p->pos < p->len && isdigit((unsigned char)p->src[p->pos])) {
            p->pos++;
        }
    }

    size_t num_len = p->pos - start;
    char tmp[64];
    if (num_len >= sizeof(tmp)) {
        set_error(p, "number too long");
        return NULL;
    }
    memcpy(tmp, p->src + start, num_len);
    tmp[num_len] = '\0';

    json_value_t *v = (json_value_t *)arena_alloc_zero(p->arena, sizeof(json_value_t));
    v->type = JSON_NUMBER;

    if (!is_float) {
        char *endptr = NULL;
        errno = 0;
        int64_t val = strtoll(tmp, &endptr, 10);
        if (errno == 0 && endptr == tmp + num_len) {
            v->number.is_int = true;
            v->number.int_val = val;
            v->number.num_val = (double)val;
            return v;
        }
    }

    v->number.is_int = false;
    v->number.num_val = strtod(tmp, NULL);
    v->number.int_val = (int64_t)v->number.num_val;
    return v;
}

static json_value_t *parse_array(json_parser_t *p) {
    p->pos++; // skip '['
    skip_whitespace(p);

    json_value_t *v = (json_value_t *)arena_alloc_zero(p->arena, sizeof(json_value_t));
    v->type = JSON_ARRAY;
    v->array.capacity = 8;
    v->array.items = (json_value_t **)arena_alloc(p->arena, v->array.capacity * sizeof(json_value_t *));
    v->array.count = 0;

    if (p->pos < p->len && p->src[p->pos] == ']') {
        p->pos++;
        return v;
    }

    while (p->pos < p->len) {
        skip_whitespace(p);
        json_value_t *item = parse_value(p);
        if (!item) return NULL;

        if (v->array.count >= v->array.capacity) {
            size_t new_cap = v->array.capacity * 2;
            json_value_t **new_items = (json_value_t **)arena_alloc(p->arena, new_cap * sizeof(json_value_t *));
            if (new_items) {
                memcpy(new_items, v->array.items, v->array.count * sizeof(json_value_t *));
                v->array.items = new_items;
                v->array.capacity = new_cap;
            }
        }
        v->array.items[v->array.count++] = item;

        skip_whitespace(p);
        if (p->pos < p->len && p->src[p->pos] == ',') {
            p->pos++;
        } else if (p->pos < p->len && p->src[p->pos] == ']') {
            p->pos++;
            break;
        } else {
            set_error(p, "expected ',' or ']' in array");
            return NULL;
        }
    }

    return v;
}

static json_value_t *parse_object(json_parser_t *p) {
    p->pos++; // skip '{'
    skip_whitespace(p);

    json_value_t *v = (json_value_t *)arena_alloc_zero(p->arena, sizeof(json_value_t));
    v->type = JSON_OBJECT;

    if (p->pos < p->len && p->src[p->pos] == '}') {
        p->pos++;
        return v;
    }

    while (p->pos < p->len) {
        skip_whitespace(p);
        if (p->pos >= p->len || p->src[p->pos] != '"') {
            set_error(p, "expected string key in object");
            return NULL;
        }

        char *key = parse_string_token(p);
        if (!key) return NULL;

        skip_whitespace(p);
        if (p->pos >= p->len || p->src[p->pos] != ':') {
            set_error(p, "expected ':' after key in object");
            return NULL;
        }
        p->pos++; // skip ':'

        skip_whitespace(p);
        json_value_t *val = parse_value(p);
        if (!val) return NULL;

        json_member_t *m = (json_member_t *)arena_alloc(p->arena, sizeof(json_member_t));
        m->key = key;
        m->value = val;
        m->next = NULL;

        if (!v->object.head) {
            v->object.head = m;
            v->object.tail = m;
        } else {
            v->object.tail->next = m;
            v->object.tail = m;
        }
        v->object.count++;

        skip_whitespace(p);
        if (p->pos < p->len && p->src[p->pos] == ',') {
            p->pos++;
        } else if (p->pos < p->len && p->src[p->pos] == '}') {
            p->pos++;
            break;
        } else {
            set_error(p, "expected ',' or '}' in object");
            return NULL;
        }
    }

    return v;
}

static json_value_t *parse_value(json_parser_t *p) {
    skip_whitespace(p);
    if (p->pos >= p->len) {
        set_error(p, "unexpected end of input");
        return NULL;
    }

    char c = p->src[p->pos];

    if (c == '"') {
        char *s = parse_string_token(p);
        if (!s) return NULL;
        json_value_t *v = (json_value_t *)arena_alloc_zero(p->arena, sizeof(json_value_t));
        v->type = JSON_STRING;
        v->str_val = s;
        return v;
    }

    if (c == '{') {
        return parse_object(p);
    }

    if (c == '[') {
        return parse_array(p);
    }

    if (c == 't' && p->pos + 4 <= p->len && memcmp(p->src + p->pos, "true", 4) == 0) {
        p->pos += 4;
        json_value_t *v = (json_value_t *)arena_alloc_zero(p->arena, sizeof(json_value_t));
        v->type = JSON_BOOL;
        v->bool_val = true;
        return v;
    }

    if (c == 'f' && p->pos + 5 <= p->len && memcmp(p->src + p->pos, "false", 5) == 0) {
        p->pos += 5;
        json_value_t *v = (json_value_t *)arena_alloc_zero(p->arena, sizeof(json_value_t));
        v->type = JSON_BOOL;
        v->bool_val = false;
        return v;
    }

    if (c == 'n' && p->pos + 4 <= p->len && memcmp(p->src + p->pos, "null", 4) == 0) {
        p->pos += 4;
        json_value_t *v = (json_value_t *)arena_alloc_zero(p->arena, sizeof(json_value_t));
        v->type = JSON_NULL;
        return v;
    }

    if (c == '-' || isdigit((unsigned char)c)) {
        return parse_number(p);
    }

    set_error(p, "unexpected character");
    return NULL;
}

json_value_t *json_parse(arena_t *arena, const char *json_str, size_t len, char *err_buf, size_t err_len) {
    if (!arena || !json_str) return NULL;
    if (err_buf && err_len > 0) err_buf[0] = '\0';

    json_parser_t p = {
        .arena = arena,
        .src = json_str,
        .len = len,
        .pos = 0,
        .err_buf = err_buf,
        .err_len = err_len,
    };

    json_value_t *root = parse_value(&p);
    if (!root) return NULL;

    skip_whitespace(&p);
    return root;
}

json_value_t *json_get(const json_value_t *obj, const char *key) {
    if (!obj || obj->type != JSON_OBJECT || !key) return NULL;
    for (json_member_t *m = obj->object.head; m != NULL; m = m->next) {
        if (strcmp(m->key, key) == 0) {
            return m->value;
        }
    }
    return NULL;
}

const char *json_get_string(const json_value_t *obj, const char *key, const char *default_val) {
    json_value_t *v = json_get(obj, key);
    if (v && v->type == JSON_STRING) {
        return v->str_val;
    }
    return default_val;
}

int64_t json_get_int64(const json_value_t *obj, const char *key, int64_t default_val) {
    json_value_t *v = json_get(obj, key);
    if (v && v->type == JSON_NUMBER) {
        return v->number.int_val;
    }
    return default_val;
}

double json_get_double(const json_value_t *obj, const char *key, double default_val) {
    json_value_t *v = json_get(obj, key);
    if (v && v->type == JSON_NUMBER) {
        return v->number.num_val;
    }
    return default_val;
}

bool json_get_bool(const json_value_t *obj, const char *key, bool default_val) {
    json_value_t *v = json_get(obj, key);
    if (v && v->type == JSON_BOOL) {
        return v->bool_val;
    }
    return default_val;
}

const json_value_t *json_get_object(const json_value_t *obj, const char *key) {
    json_value_t *v = json_get(obj, key);
    if (v && v->type == JSON_OBJECT) {
        return v;
    }
    return NULL;
}

const json_value_t *json_get_array(const json_value_t *obj, const char *key) {
    json_value_t *v = json_get(obj, key);
    if (v && v->type == JSON_ARRAY) {
        return v;
    }
    return NULL;
}

json_value_t *json_create_null(arena_t *arena) {
    json_value_t *v = (json_value_t *)arena_alloc_zero(arena, sizeof(json_value_t));
    v->type = JSON_NULL;
    return v;
}

json_value_t *json_create_bool(arena_t *arena, bool val) {
    json_value_t *v = (json_value_t *)arena_alloc_zero(arena, sizeof(json_value_t));
    v->type = JSON_BOOL;
    v->bool_val = val;
    return v;
}

json_value_t *json_create_int(arena_t *arena, int64_t val) {
    json_value_t *v = (json_value_t *)arena_alloc_zero(arena, sizeof(json_value_t));
    v->type = JSON_NUMBER;
    v->number.is_int = true;
    v->number.int_val = val;
    v->number.num_val = (double)val;
    return v;
}

json_value_t *json_create_double(arena_t *arena, double val) {
    json_value_t *v = (json_value_t *)arena_alloc_zero(arena, sizeof(json_value_t));
    v->type = JSON_NUMBER;
    v->number.is_int = false;
    v->number.num_val = val;
    v->number.int_val = (int64_t)val;
    return v;
}

json_value_t *json_create_string(arena_t *arena, const char *str) {
    json_value_t *v = (json_value_t *)arena_alloc_zero(arena, sizeof(json_value_t));
    v->type = JSON_STRING;
    v->str_val = arena_strdup(arena, str ? str : "");
    return v;
}

json_value_t *json_create_array(arena_t *arena) {
    json_value_t *v = (json_value_t *)arena_alloc_zero(arena, sizeof(json_value_t));
    v->type = JSON_ARRAY;
    v->array.capacity = 4;
    v->array.items = (json_value_t **)arena_alloc(arena, v->array.capacity * sizeof(json_value_t *));
    v->array.count = 0;
    return v;
}

void json_array_append(arena_t *arena, json_value_t *arr, json_value_t *item) {
    if (!arr || arr->type != JSON_ARRAY || !item) return;
    if (arr->array.count >= arr->array.capacity) {
        size_t new_cap = arr->array.capacity * 2;
        json_value_t **new_items = (json_value_t **)arena_alloc(arena, new_cap * sizeof(json_value_t *));
        if (new_items) {
            memcpy(new_items, arr->array.items, arr->array.count * sizeof(json_value_t *));
            arr->array.items = new_items;
            arr->array.capacity = new_cap;
        }
    }
    arr->array.items[arr->array.count++] = item;
}

json_value_t *json_create_object(arena_t *arena) {
    json_value_t *v = (json_value_t *)arena_alloc_zero(arena, sizeof(json_value_t));
    v->type = JSON_OBJECT;
    return v;
}

void json_object_set(arena_t *arena, json_value_t *obj, const char *key, json_value_t *val) {
    if (!obj || obj->type != JSON_OBJECT || !key || !val) return;
    for (json_member_t *m = obj->object.head; m != NULL; m = m->next) {
        if (strcmp(m->key, key) == 0) {
            m->value = val;
            return;
        }
    }
    json_member_t *m = (json_member_t *)arena_alloc(arena, sizeof(json_member_t));
    m->key = arena_strdup(arena, key);
    m->value = val;
    m->next = NULL;

    if (!obj->object.head) {
        obj->object.head = m;
        obj->object.tail = m;
    } else {
        obj->object.tail->next = m;
        obj->object.tail = m;
    }
    obj->object.count++;
}

static void print_indent(FILE *out, int depth) {
    for (int i = 0; i < depth; i++) {
        fputs("  ", out);
    }
}

static void print_escaped_string(FILE *out, const char *s) {
    fputc('"', out);
    if (s) {
        for (; *s; s++) {
            switch (*s) {
                case '"':  fputs("\\\"", out); break;
                case '\\': fputs("\\\\", out); break;
                case '\b': fputs("\\b", out); break;
                case '\f': fputs("\\f", out); break;
                case '\n': fputs("\\n", out); break;
                case '\r': fputs("\\r", out); break;
                case '\t': fputs("\\t", out); break;
                default:
                    if ((unsigned char)*s < 0x20) {
                        fprintf(out, "\\u%04x", (unsigned char)*s);
                    } else {
                        fputc(*s, out);
                    }
                    break;
            }
        }
    }
    fputc('"', out);
}

static void print_value(const json_value_t *val, FILE *out, bool pretty, int depth) {
    if (!val) {
        fputs("null", out);
        return;
    }

    switch (val->type) {
        case JSON_NULL:
            fputs("null", out);
            break;
        case JSON_BOOL:
            fputs(val->bool_val ? "true" : "false", out);
            break;
        case JSON_NUMBER:
            if (val->number.is_int) {
                fprintf(out, "%ld", (long)val->number.int_val);
            } else {
                fprintf(out, "%.17g", val->number.num_val);
            }
            break;
        case JSON_STRING:
            print_escaped_string(out, val->str_val);
            break;
        case JSON_ARRAY: {
            if (val->array.count == 0) {
                fputs("[]", out);
                break;
            }
            fputc('[', out);
            if (pretty) fputc('\n', out);
            for (size_t i = 0; i < val->array.count; i++) {
                if (pretty) print_indent(out, depth + 1);
                print_value(val->array.items[i], out, pretty, depth + 1);
                if (i + 1 < val->array.count) {
                    fputc(',', out);
                }
                if (pretty) fputc('\n', out);
            }
            if (pretty) print_indent(out, depth);
            fputc(']', out);
            break;
        }
        case JSON_OBJECT: {
            if (val->object.count == 0) {
                fputs("{}", out);
                break;
            }
            fputc('{', out);
            if (pretty) fputc('\n', out);
            size_t idx = 0;
            for (json_member_t *m = val->object.head; m != NULL; m = m->next, idx++) {
                if (pretty) print_indent(out, depth + 1);
                print_escaped_string(out, m->key);
                fputs(pretty ? ": " : ":", out);
                print_value(m->value, out, pretty, depth + 1);
                if (idx + 1 < val->object.count) {
                    fputc(',', out);
                }
                if (pretty) fputc('\n', out);
            }
            if (pretty) print_indent(out, depth);
            fputc('}', out);
            break;
        }
    }
}

void json_print(const json_value_t *val, FILE *out, bool pretty) {
    print_value(val, out, pretty, 0);
    fputc('\n', out);
}
