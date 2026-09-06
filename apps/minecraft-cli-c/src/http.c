#include "http.h"
#include <ctype.h>
#include <dlfcn.h>
#include <errno.h>
#include <netdb.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <unistd.h>

#define DEFAULT_TIMEOUT_SEC 10

typedef struct {
    char scheme[16];
    char host[256];
    char port[16];
    char path[1024];
} parsed_url_t;

static bool parse_url(const char *url, parsed_url_t *out) {
    if (!url || !out) return false;
    memset(out, 0, sizeof(parsed_url_t));

    const char *p = url;
    const char *scheme_end = strstr(p, "://");
    if (!scheme_end) {
        // Assume http
        strncpy(out->scheme, "http", sizeof(out->scheme) - 1);
    } else {
        size_t slen = (size_t)(scheme_end - p);
        if (slen >= sizeof(out->scheme)) slen = sizeof(out->scheme) - 1;
        memcpy(out->scheme, p, slen);
        out->scheme[slen] = '\0';
        p = scheme_end + 3;
    }

    // Parse host and optional port
    const char *path_start = strchr(p, '/');
    size_t host_port_len = path_start ? (size_t)(path_start - p) : strlen(p);
    char host_port[256];
    if (host_port_len >= sizeof(host_port)) host_port_len = sizeof(host_port) - 1;
    memcpy(host_port, p, host_port_len);
    host_port[host_port_len] = '\0';

    char *colon = strchr(host_port, ':');
    if (colon) {
        *colon = '\0';
        strncpy(out->host, host_port, sizeof(out->host) - 1);
        strncpy(out->port, colon + 1, sizeof(out->port) - 1);
    } else {
        strncpy(out->host, host_port, sizeof(out->host) - 1);
        if (strcmp(out->scheme, "https") == 0) {
            strncpy(out->port, "443", sizeof(out->port) - 1);
        } else {
            strncpy(out->port, "80", sizeof(out->port) - 1);
        }
    }

    if (path_start) {
        strncpy(out->path, path_start, sizeof(out->path) - 1);
    } else {
        strncpy(out->path, "/", sizeof(out->path) - 1);
    }

    return true;
}

/* ================= Dynamic buffer for HTTP responses ================= */
typedef struct {
    char *data;
    size_t size;
    size_t capacity;
} buffer_t;

static void buf_init(buffer_t *b, size_t initial_cap) {
    b->capacity = initial_cap ? initial_cap : 16384;
    b->data = (char *)malloc(b->capacity);
    b->size = 0;
    if (b->data) b->data[0] = '\0';
}

static bool buf_append(buffer_t *b, const char *data, size_t len) {
    if (b->size + len + 1 > b->capacity) {
        size_t new_cap = b->capacity * 2;
        if (new_cap < b->size + len + 1) new_cap = b->size + len + 1;
        char *new_data = (char *)realloc(b->data, new_cap);
        if (!new_data) return false;
        b->data = new_data;
        b->capacity = new_cap;
    }
    memcpy(b->data + b->size, data, len);
    b->size += len;
    b->data[b->size] = '\0';
    return true;
}

static void buf_free(buffer_t *b) {
    if (b->data) {
        free(b->data);
        b->data = NULL;
    }
    b->size = 0;
    b->capacity = 0;
}

/* ================= Pure POSIX Socket HTTP/1.1 Client ================= */
static http_response_t http_request_socket(arena_t *arena,
                                          http_method_t method,
                                          const parsed_url_t *u,
                                          const char *api_key,
                                          int timeout_seconds) {
    http_response_t res;
    memset(&res, 0, sizeof(res));

    struct addrinfo hints, *servinfo, *p;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    int rv = getaddrinfo(u->host, u->port, &hints, &servinfo);
    if (rv != 0) {
        snprintf(res.error_msg, sizeof(res.error_msg),
                 "failed to resolve host %s: %s", u->host, gai_strerror(rv));
        return res;
    }

    int sockfd = -1;
    for (p = servinfo; p != NULL; p = p->ai_next) {
        sockfd = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
        if (sockfd == -1) continue;

        struct timeval tv;
        tv.tv_sec = timeout_seconds > 0 ? timeout_seconds : DEFAULT_TIMEOUT_SEC;
        tv.tv_usec = 0;
        setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(tv));
        setsockopt(sockfd, SOL_SOCKET, SO_SNDTIMEO, &tv, sizeof(tv));

        int flag = 1;
        setsockopt(sockfd, IPPROTO_TCP, TCP_NODELAY, &flag, sizeof(flag));

        if (connect(sockfd, p->ai_addr, p->ai_addrlen) == -1) {
            close(sockfd);
            sockfd = -1;
            continue;
        }
        break;
    }

    freeaddrinfo(servinfo);

    if (sockfd == -1) {
        snprintf(res.error_msg, sizeof(res.error_msg),
                 "failed to connect to %s:%s: %s", u->host, u->port, strerror(errno));
        return res;
    }

    // Build HTTP request
    char req[2048];
    const char *method_str = (method == HTTP_METHOD_DELETE) ? "DELETE" : "GET";
    int req_len = 0;

    if (api_key && *api_key) {
        req_len = snprintf(req, sizeof(req),
            "%s %s HTTP/1.1\r\n"
            "Host: %s:%s\r\n"
            "User-Agent: minecraft-cli-c/1.0.0\r\n"
            "Accept: application/json\r\n"
            "Authorization: Bearer %s\r\n"
            "Connection: close\r\n\r\n",
            method_str, u->path, u->host, u->port, api_key);
    } else {
        req_len = snprintf(req, sizeof(req),
            "%s %s HTTP/1.1\r\n"
            "Host: %s:%s\r\n"
            "User-Agent: minecraft-cli-c/1.0.0\r\n"
            "Accept: application/json\r\n"
            "Connection: close\r\n\r\n",
            method_str, u->path, u->host, u->port);
    }

    ssize_t sent = 0;
    while (sent < req_len) {
        ssize_t n = send(sockfd, req + sent, req_len - sent, 0);
        if (n <= 0) {
            close(sockfd);
            snprintf(res.error_msg, sizeof(res.error_msg),
                     "failed to send HTTP request: %s", strerror(errno));
            return res;
        }
        sent += n;
    }

    // Read response
    buffer_t raw;
    buf_init(&raw, 32768);
    char chunk[4096];
    while (1) {
        ssize_t n = recv(sockfd, chunk, sizeof(chunk), 0);
        if (n > 0) {
            buf_append(&raw, chunk, (size_t)n);
        } else if (n == 0) {
            break; // Connection closed
        } else {
            if (errno == EAGAIN || errno == EWOULDBLOCK) {
                // Timeout
                if (raw.size == 0) {
                    close(sockfd);
                    buf_free(&raw);
                    snprintf(res.error_msg, sizeof(res.error_msg), "connection timed out");
                    return res;
                }
                break;
            }
            close(sockfd);
            buf_free(&raw);
            snprintf(res.error_msg, sizeof(res.error_msg), "read error: %s", strerror(errno));
            return res;
        }
    }
    close(sockfd);

    if (raw.size == 0) {
        buf_free(&raw);
        snprintf(res.error_msg, sizeof(res.error_msg), "received empty response from server");
        return res;
    }

    // Parse HTTP response
    char *header_end = strstr(raw.data, "\r\n\r\n");
    if (!header_end) {
        header_end = strstr(raw.data, "\n\n");
    }

    if (!header_end) {
        buf_free(&raw);
        snprintf(res.error_msg, sizeof(res.error_msg), "malformed HTTP response headers");
        return res;
    }

    // Parse status line
    int status_code = 0;
    if (sscanf(raw.data, "HTTP/%*s %d", &status_code) != 1) {
        buf_free(&raw);
        snprintf(res.error_msg, sizeof(res.error_msg), "invalid HTTP status line");
        return res;
    }

    res.status_code = status_code;

    // Check headers for chunked encoding
    bool is_chunked = false;
    char *headers_lower = (char *)malloc((size_t)(header_end - raw.data + 1));
    if (headers_lower) {
        for (size_t i = 0; i < (size_t)(header_end - raw.data); i++) {
            headers_lower[i] = (char)tolower((unsigned char)raw.data[i]);
        }
        headers_lower[header_end - raw.data] = '\0';
        if (strstr(headers_lower, "transfer-encoding: chunked") != NULL) {
            is_chunked = true;
        }
        free(headers_lower);
    }

    char *body_start = header_end + (strncmp(header_end, "\r\n\r\n", 4) == 0 ? 4 : 2);
    size_t raw_body_len = raw.size - (size_t)(body_start - raw.data);

    if (is_chunked) {
        // Decode chunked transfer
        buffer_t unchunked;
        buf_init(&unchunked, raw_body_len);
        char *cp = body_start;
        while (cp < raw.data + raw.size) {
            char *next_crlf = strstr(cp, "\r\n");
            if (!next_crlf) break;
            *next_crlf = '\0';
            long chunk_sz = strtol(cp, NULL, 16);
            if (chunk_sz <= 0) break;
            char *chunk_data = next_crlf + 2;
            if (chunk_data + chunk_sz > raw.data + raw.size) {
                chunk_sz = (long)(raw.data + raw.size - chunk_data);
            }
            buf_append(&unchunked, chunk_data, (size_t)chunk_sz);
            cp = chunk_data + chunk_sz + 2; // skip chunk data + \r\n
        }

        res.body = arena_strndup(arena, unchunked.data, unchunked.size);
        res.body_len = unchunked.size;
        buf_free(&unchunked);
    } else {
        res.body = arena_strndup(arena, body_start, raw_body_len);
        res.body_len = raw_body_len;
    }

    buf_free(&raw);
    res.success = true;
    return res;
}

/* ================= Libcurl Support (for HTTPS / Fallback) ================= */
struct curl_slist {
    char *data;
    struct curl_slist *next;
};

typedef void *(*curl_init_fn)(void);
typedef int (*curl_setopt_fn)(void *, int, ...);
typedef int (*curl_perform_fn)(void *);
typedef void (*curl_cleanup_fn)(void *);
typedef const char *(*curl_strerror_fn)(int);
typedef int (*curl_getinfo_fn)(void *, int, ...);
typedef struct curl_slist *(*curl_slist_append_fn)(struct curl_slist *, const char *);
typedef void (*curl_slist_free_all_fn)(struct curl_slist *);

#define CURL_OPT_URL 10002
#define CURL_OPT_HTTPHEADER 10023
#define CURL_OPT_WRITEFUNCTION 20011
#define CURL_OPT_WRITEDATA 10001
#define CURL_OPT_TIMEOUT 13
#define CURL_OPT_CUSTOMREQUEST 10036
#define CURL_OPT_NOSIGNAL 99
#define CURL_OPT_FOLLOWLOCATION 52
#define CURL_INFO_RESPONSE_CODE 2097154

static size_t curl_write_cb(void *contents, size_t size, size_t nmemb, void *userp) {
    size_t realsize = size * nmemb;
    buffer_t *mem = (buffer_t *)userp;
    buf_append(mem, (char *)contents, realsize);
    return realsize;
}

static http_response_t http_request_curl(arena_t *arena,
                                         http_method_t method,
                                         const char *url,
                                         const char *api_key,
                                         int timeout_seconds) {
    http_response_t res;
    memset(&res, 0, sizeof(res));

    void *lib = dlopen("libcurl.so.4", RTLD_NOW | RTLD_LOCAL);
    if (!lib) {
        lib = dlopen("libcurl.so", RTLD_NOW | RTLD_LOCAL);
    }
    if (!lib) {
        snprintf(res.error_msg, sizeof(res.error_msg),
                 "HTTPS requires libcurl (libcurl.so.4 not found)");
        return res;
    }

    curl_init_fn fn_init;
    curl_setopt_fn fn_setopt;
    curl_perform_fn fn_perform;
    curl_cleanup_fn fn_cleanup;
    curl_strerror_fn fn_strerror;
    curl_getinfo_fn fn_getinfo;
    curl_slist_append_fn fn_slist_append;
    curl_slist_free_all_fn fn_slist_free;

    *(void **)(&fn_init) = dlsym(lib, "curl_easy_init");
    *(void **)(&fn_setopt) = dlsym(lib, "curl_easy_setopt");
    *(void **)(&fn_perform) = dlsym(lib, "curl_easy_perform");
    *(void **)(&fn_cleanup) = dlsym(lib, "curl_easy_cleanup");
    *(void **)(&fn_strerror) = dlsym(lib, "curl_easy_strerror");
    *(void **)(&fn_getinfo) = dlsym(lib, "curl_easy_getinfo");
    *(void **)(&fn_slist_append) = dlsym(lib, "curl_slist_append");
    *(void **)(&fn_slist_free) = dlsym(lib, "curl_slist_free_all");

    if (!fn_init || !fn_setopt || !fn_perform || !fn_cleanup || !fn_getinfo || !fn_slist_append || !fn_slist_free) {
        dlclose(lib);
        snprintf(res.error_msg, sizeof(res.error_msg), "failed to resolve libcurl symbols");
        return res;
    }

    void *curl = fn_init();
    if (!curl) {
        dlclose(lib);
        snprintf(res.error_msg, sizeof(res.error_msg), "failed to initialize curl handle");
        return res;
    }

    buffer_t buf;
    buf_init(&buf, 16384);

    struct curl_slist *headers = NULL;
    headers = fn_slist_append(headers, "Accept: application/json");
    if (api_key && *api_key) {
        char auth_hdr[512];
        snprintf(auth_hdr, sizeof(auth_hdr), "Authorization: Bearer %s", api_key);
        headers = fn_slist_append(headers, auth_hdr);
    }

    fn_setopt(curl, CURL_OPT_URL, url);
    fn_setopt(curl, CURL_OPT_HTTPHEADER, headers);
    fn_setopt(curl, CURL_OPT_WRITEFUNCTION, curl_write_cb);
    fn_setopt(curl, CURL_OPT_WRITEDATA, (void *)&buf);
    fn_setopt(curl, CURL_OPT_TIMEOUT, (long)(timeout_seconds > 0 ? timeout_seconds : DEFAULT_TIMEOUT_SEC));
    fn_setopt(curl, CURL_OPT_NOSIGNAL, 1L);
    fn_setopt(curl, CURL_OPT_FOLLOWLOCATION, 1L);

    if (method == HTTP_METHOD_DELETE) {
        fn_setopt(curl, CURL_OPT_CUSTOMREQUEST, "DELETE");
    }

    int c_res = fn_perform(curl);
    if (c_res != 0) {
        snprintf(res.error_msg, sizeof(res.error_msg),
                 "connection failed: %s", fn_strerror ? fn_strerror(c_res) : "curl error");
        buf_free(&buf);
        fn_slist_free(headers);
        fn_cleanup(curl);
        dlclose(lib);
        return res;
    }

    long http_code = 0;
    fn_getinfo(curl, CURL_INFO_RESPONSE_CODE, &http_code);
    res.status_code = (int)http_code;
    res.body = arena_strndup(arena, buf.data, buf.size);
    res.body_len = buf.size;
    res.success = true;

    buf_free(&buf);
    fn_slist_free(headers);
    fn_cleanup(curl);
    dlclose(lib);
    return res;
}

http_response_t http_request(arena_t *arena,
                             http_method_t method,
                             const char *url,
                             const char *api_key,
                             int timeout_seconds) {
    parsed_url_t u;
    if (!parse_url(url, &u)) {
        http_response_t res;
        memset(&res, 0, sizeof(res));
        snprintf(res.error_msg, sizeof(res.error_msg), "invalid URL: %s", url);
        return res;
    }

    if (strcmp(u.scheme, "https") == 0) {
        return http_request_curl(arena, method, url, api_key, timeout_seconds);
    }

    // Default fast native socket
    return http_request_socket(arena, method, &u, api_key, timeout_seconds);
}
