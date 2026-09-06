#include "uuid.h"
#include <ctype.h>
#include <stdio.h>
#include <string.h>

static inline bool is_hex_char(char c) {
    return (c >= '0' && c <= '9') ||
           (c >= 'a' && c <= 'f') ||
           (c >= 'A' && c <= 'F');
}

const char *trim_whitespace_const(const char *str, size_t *out_len) {
    if (!str) {
        if (out_len) *out_len = 0;
        return "";
    }
    while (*str && isspace((unsigned char)*str)) {
        str++;
    }
    size_t len = strlen(str);
    while (len > 0 && isspace((unsigned char)str[len - 1])) {
        len--;
    }
    if (out_len) *out_len = len;
    return str;
}

char *trim_whitespace(char *str) {
    if (!str) return NULL;
    while (*str && isspace((unsigned char)*str)) {
        str++;
    }
    size_t len = strlen(str);
    while (len > 0 && isspace((unsigned char)str[len - 1])) {
        str[len - 1] = '\0';
        len--;
    }
    return str;
}

bool is_uuid(const char *input) {
    size_t len = 0;
    const char *s = trim_whitespace_const(input, &len);

    if (len == 32) {
        for (size_t i = 0; i < 32; i++) {
            if (!is_hex_char(s[i])) return false;
        }
        return true;
    }

    if (len == 36) {
        if (s[8] != '-' || s[13] != '-' || s[18] != '-' || s[23] != '-') {
            return false;
        }
        for (size_t i = 0; i < 36; i++) {
            if (i == 8 || i == 13 || i == 18 || i == 23) continue;
            if (!is_hex_char(s[i])) return false;
        }
        return true;
    }

    return false;
}

bool normalize_uuid(const char *input, char *output, size_t out_size) {
    size_t len = 0;
    const char *s = trim_whitespace_const(input, &len);

    if (len == 32) {
        for (size_t i = 0; i < 32; i++) {
            if (!is_hex_char(s[i])) goto fallback;
        }
        if (out_size < 37) return false;
        // Format as 8-4-4-4-12
        int written = snprintf(output, out_size,
            "%.8s-%.4s-%.4s-%.4s-%.12s",
            s, s + 8, s + 12, s + 16, s + 20);
        return written > 0 && (size_t)written < out_size;
    }

fallback:
    if (out_size <= len) return false;
    memcpy(output, s, len);
    output[len] = '\0';
    return true;
}

size_t url_path_escape(const char *input, char *output, size_t out_size) {
    if (!input || !output || out_size == 0) return 0;
    size_t out_idx = 0;
    const char hex_chars[] = "0123456789ABCDEF";

    for (size_t i = 0; input[i] != '\0'; i++) {
        unsigned char c = (unsigned char)input[i];
        if ((c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            (c >= '0' && c <= '9') ||
            c == '-' || c == '_' || c == '.' || c == '~') {
            if (out_idx + 1 >= out_size) break;
            output[out_idx++] = (char)c;
        } else {
            if (out_idx + 3 >= out_size) break;
            output[out_idx++] = '%';
            output[out_idx++] = hex_chars[(c >> 4) & 0x0F];
            output[out_idx++] = hex_chars[c & 0x0F];
        }
    }
    output[out_idx] = '\0';
    return out_idx;
}
