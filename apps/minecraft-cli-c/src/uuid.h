#ifndef UUID_UTILS_H
#define UUID_UTILS_H

#include <stdbool.h>
#include <stddef.h>

bool is_uuid(const char *input);
bool normalize_uuid(const char *input, char *output, size_t out_size);
size_t url_path_escape(const char *input, char *output, size_t out_size);
char *trim_whitespace(char *str);
const char *trim_whitespace_const(const char *str, size_t *out_len);

#endif /* UUID_UTILS_H */
