# Minecraft CLI (`minecraft-cli` / `mc`)

High-performance Go CLI administration toolkit for the Minecraft project, communicating directly through `minecraft-api`.

---

## Features

- **Extensible Architecture**: Built with [Cobra](https://github.com/spf13/cobra) to support administrative commands.
- **minecraft-api Integration**: Communicates via standard authenticated REST endpoints (`GET /v1/admin/players/:id` and `DELETE /v1/admin/players/:id`).
- **Live Cache Invalidation**: Immediately purges in-memory player caches upon player deletion.
- **Player Inspection**: Retrieves player profile (UUID, username, first login, last login, ranks with exact Minecraft colors and casing).
- **Player Purge & Deletion**: Completely removes a player from SMEssential across all related tables:
  - `smessential_users` (profile and join history)
  - `smessential_user_ranks` (assigned ranks)
  - `smessential_user_display_ranks` (custom display rank)
  - `smessential_punishments` (only as target, preserving staff audit trail when the player was issuer)
  - `smessential_whitelist` (whitelist entries targeting this player)
- **Data Integrity & Audit Preservation**: Staff audit logs are strictly protected; punishments issued by the player as a staff member are never deleted.
- **Transaction Safety**: All deletions are executed inside an atomic PostgreSQL transaction on the API (`BEGIN ... COMMIT`).
- **Flexible Identifier Lookup**: Accepts either player username or UUID (standard dashed or raw 32-hex).
- **Safety Flags**:
  - `--dry-run`: Previews all records slated for deletion without touching the database.
  - `-y` / `--yes` / `--force`: Skips interactive confirmation prompt.
  - `--json`: Outputs structured JSON for integration and automation.

---

## Installation & Build

```bash
cd apps/minecraft-cli
go build -o bin/mc .
```

Or using Turborepo / npm from the repository root:
```bash
npm run build:minecraft-cli
```

---

## Usage

### Getting Player Profile

By username:
```bash
./bin/mc player get Notch
```

By UUID:
```bash
./bin/mc player get 7cd493a1-1214-4da3-9ac1-a0bfef50b75c
```

JSON output:
```bash
./bin/mc player get Notch --json
```

### Deleting a Player

By username:
```bash
./bin/mc player delete Notch
```

By UUID:
```bash
./bin/mc player delete 7cd493a1-1214-4da3-9ac1-a0bfef50b75c
```

### Dry Run (Preview without Deleting)

```bash
./bin/mc player delete Notch --dry-run
```

### Non-Interactive / Force Delete (Scripts & CI)

```bash
./bin/mc player delete Notch -y
```

### JSON Output

```bash
./bin/mc player delete Notch --dry-run --json
```

---

## Configuration

The CLI loads configuration from environment variables or `.env` files (searches current directory, `apps/minecraft-cli/.env`, and `apps/minecraft-api/.env`).

| Variable | Flag | Default | Description |
|---|---|---|---|
| `MINECRAFT_API_URL` | `--api-url` | `http://localhost:3002` | `minecraft-api` service endpoint |
| `ADMIN_API_KEY` | `--api-key` | - | Admin API key for `minecraft-api` |
| - | `--env-file` | - | Custom path to `.env` file |
| - | `--json` | `false` | Output results in JSON format |
| - | `-q, --quiet` | `false` | Suppress banner and non-essential output |
