#ifndef ARENA_H
#define ARENA_H

#include <stddef.h>
#include <stdint.h>

typedef struct arena_block {
    struct arena_block *next;
    size_t capacity;
    size_t used;
    uint8_t data[];
} arena_block_t;

typedef struct arena {
    arena_block_t *current;
    size_t default_block_size;
} arena_t;

arena_t *arena_create(size_t default_block_size);
void *arena_alloc(arena_t *arena, size_t size);
void *arena_alloc_zero(arena_t *arena, size_t size);
char *arena_strdup(arena_t *arena, const char *s);
char *arena_strndup(arena_t *arena, const char *s, size_t n);
void arena_reset(arena_t *arena);
void arena_destroy(arena_t *arena);

#endif /* ARENA_H */
