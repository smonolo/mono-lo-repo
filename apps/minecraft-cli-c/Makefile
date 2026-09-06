CC ?= gcc
CFLAGS ?= -std=c11 -O3 -march=native -flto -fomit-frame-pointer -fno-plt -pipe -Wall -Wextra -Wpedantic -D_GNU_SOURCE
LDFLAGS ?= -flto -ldl

SRCDIR = src
TESTDIR = tests
BINDIR = bin

TARGET = $(BINDIR)/mc
TEST_TARGET = $(BINDIR)/test_suite

SRCS = $(SRCDIR)/arena.c \
       $(SRCDIR)/uuid.c \
       $(SRCDIR)/json.c \
       $(SRCDIR)/player.c \
       $(SRCDIR)/config.c \
       $(SRCDIR)/http.c \
       $(SRCDIR)/ui.c

MAIN_SRC = $(SRCDIR)/main.c
TEST_SRCS = $(TESTDIR)/test_main.c $(SRCS)

OBJS = $(SRCS:.c=.o)
MAIN_OBJ = $(MAIN_SRC:.c=.o)

.PHONY: all build test clean pgo install help

all: build

build: $(TARGET)

$(BINDIR):
	mkdir -p $(BINDIR)

$(TARGET): $(OBJS) $(MAIN_OBJ) | $(BINDIR)
	$(CC) $(CFLAGS) $(OBJS) $(MAIN_OBJ) -o $(TARGET) $(LDFLAGS)
	strip -s $(TARGET) 2>/dev/null || true
	@echo "Built optimized binary: $(TARGET)"

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

test: $(TEST_TARGET)
	@echo "Running tests..."
	@$(TEST_TARGET)

$(TEST_TARGET): $(TEST_SRCS) | $(BINDIR)
	$(CC) $(CFLAGS) $(TEST_SRCS) -o $(TEST_TARGET) $(LDFLAGS)

pgo: clean | $(BINDIR)
	@echo "== Stage 1: Compiling with profile generation =="
	$(CC) $(CFLAGS) -fprofile-generate $(SRCS) $(MAIN_SRC) -o $(TARGET) $(LDFLAGS)
	@echo "== Stage 2: Profiling common execution paths =="
	./$(TARGET) --help > /dev/null 2>&1 || true
	./$(TARGET) player --help > /dev/null 2>&1 || true
	./$(TARGET) player get --help > /dev/null 2>&1 || true
	./$(TARGET) player delete --help > /dev/null 2>&1 || true
	./$(TARGET) --version > /dev/null 2>&1 || true
	@echo "== Stage 3: Compiling with profile feedback =="
	$(CC) $(CFLAGS) -fprofile-use $(SRCS) $(MAIN_SRC) -o $(TARGET) $(LDFLAGS)
	strip -s $(TARGET) 2>/dev/null || true
	rm -f *.gcda *.gcno $(SRCDIR)/*.gcda $(SRCDIR)/*.gcno
	@echo "Profile-Guided Optimization complete: $(TARGET)"

clean:
	rm -rf $(BINDIR) $(SRCDIR)/*.o $(TESTDIR)/*.o *.gcda *.gcno $(SRCDIR)/*.gcda $(SRCDIR)/*.gcno

help:
	@echo "Available make targets:"
	@echo "  build (default) : Compile highly optimized stripped binary (bin/mc)"
	@echo "  test            : Build and run the test suite"
	@echo "  pgo             : Compile with Profile-Guided Optimization (PGO)"
	@echo "  clean           : Remove build artifacts and binary"
