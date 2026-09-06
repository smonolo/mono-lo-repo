#ifndef HTTP_H
#define HTTP_H

#include <stdbool.h>
#include <stddef.h>
#include "arena.h"

typedef enum {
    HTTP_METHOD_GET = 0,
    HTTP_METHOD_DELETE
} http_method_t;

typedef struct {
    int status_code;
    char *body;
    size_t body_len;
    char error_msg[256];
    bool success;
} http_response_t;

http_response_t http_request(arena_t *arena,
                             http_method_t method,
                             const char *url,
                             const char *api_key,
                             int timeout_seconds);

#endif /* HTTP_H */
