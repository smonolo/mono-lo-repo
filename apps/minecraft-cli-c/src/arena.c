#include "arena.h"
#include <stdlib.h>
#include <string.h>

#define DEFAULT_BLOCK_SIZE (64 * 1024)
#define ALIGN_UP(size, align) (((size) + (align) - 1) & ~((align) - 1))
#define ARENA_ALIGNMENT (sizeof(void *))

static arena_block_t *arena_block_create(size_t capacity) {
    arena_block_t *block = (arena_block_t *)malloc(sizeof(arena_block_t) + capacity);
    if (!block) return NULL;
    block->next = NULL;
    block->capacity = capacity;
    block->used = 0;
    return block;
}

arena_t *arena_create(size_t default_block_size) {
    arena_t *arena = (arena_t *)malloc(sizeof(arena_t));
    if (!arena) return NULL;
    if (default_block_size == 0) {
        default_block_size = DEFAULT_BLOCK_SIZE;
    }
    arena->default_block_size = default_block_size;
    arena->current = arena_block_create(default_block_size);
    if (!arena->current) {
        free(arena);
        return NULL;
    }
    return arena;
}

void *arena_alloc(arena_t *arena, size_t size) {
    if (!arena) return NULL;
    size = ALIGN_UP(size, ARENA_ALIGNMENT);

    if (arena->current && arena->current->used + size <= arena->current->capacity) {
        void *ptr = arena->current->data + arena->current->used;
        arena->current->used += size;
        return ptr;
    }

    size_t new_cap = arena->default_block_size;
    if (size > new_cap) {
        new_cap = size;
    }

    arena_block_t *new_block = arena_block_create(new_cap);
    if (!new_block) return NULL;

    new_block->next = arena->current;
    arena->current = new_block;

    void *ptr = new_block->data;
    new_block->used = size;
    return ptr;
}

void *arena_alloc_zero(arena_t *arena, size_t size) {
    void *ptr = arena_alloc(arena, size);
    if (ptr) {
        memset(ptr, 0, size);
    }
    return ptr;
}

char *arena_strdup(arena_t *arena, const char *s) {
    if (!s) return NULL;
    size_t len = strlen(s);
    char *dest = (char *)arena_alloc(arena, len + 1);
    if (dest) {
        memcpy(dest, s, len);
        dest[len] = '\0';
    }
    return dest;
}

char *arena_strndup(arena_t *arena, const char *s, size_t n) {
    if (!s) return NULL;
    char *dest = (char *)arena_alloc(arena, n + 1);
    if (dest) {
        memcpy(dest, s, n);
        dest[n] = '\0';
    }
    return dest;
}

void arena_reset(arena_t *arena) {
    if (!arena) return;
    arena_block_t *cur = arena->current;
    while (cur && cur->next) {
        arena_block_t *to_free = cur;
        cur = cur->next;
        free(to_free);
    }
    arena->current = cur;
    if (arena->current) {
        arena->current->used = 0;
    }
}

void arena_destroy(arena_t *arena) {
    if (!arena) return;
    arena_block_t *cur = arena->current;
    while (cur) {
        arena_block_t *next = cur->next;
        free(cur);
        cur = next;
    }
    free(arena);
}
