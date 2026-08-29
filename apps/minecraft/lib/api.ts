import type {
  LeaderboardsResponse,
  MinecraftPlayersResponse,
  PlayerData,
  SinglePlayerResponse,
  StatisticLeaderboard,
} from '@/types/minecraft'

export function getApiConfig() {
  const rawApiUrl =
    process.env.SMESSENTIAL_API_URL ||
    process.env.NEXT_PUBLIC_SMESSENTIAL_API_URL ||
    'http://127.0.0.1:25580'
  const apiUrl = rawApiUrl
    .trim()
    .replace(/^["']|["']$/g, '')
    .replace(/\/$/, '')

  const rawSecret =
    process.env.SMESSENTIAL_API_SECRET ||
    process.env.NEXT_PUBLIC_SMESSENTIAL_API_SECRET ||
    'smessential-secret-key'
  const apiSecret = rawSecret.trim().replace(/^["']|["']$/g, '')

  return { apiUrl, apiSecret }
}

// In-memory cache & request deduplication to protect SMEssential Minecraft plugin
type CacheEntry<T> = {
  data: T
  expiresAt: number
}

const memoryCache = new Map<string, CacheEntry<any>>()
const inFlightRequests = new Map<string, Promise<any>>()

async function cachedFetch<T>(
  key: string,
  ttlMs: number,
  fetcher: () => Promise<T>
): Promise<T> {
  const now = Date.now()
  const cached = memoryCache.get(key)
  if (cached && cached.expiresAt > now) {
    return cached.data
  }

  const existingInFlight = inFlightRequests.get(key)
  if (existingInFlight) {
    return existingInFlight as Promise<T>
  }

  const promise = fetcher()
    .then(data => {
      memoryCache.set(key, { data, expiresAt: Date.now() + ttlMs })
      inFlightRequests.delete(key)
      return data
    })
    .catch(err => {
      inFlightRequests.delete(key)
      // Stale-if-error fallback
      if (cached) {
        return cached.data
      }
      throw err
    })

  inFlightRequests.set(key, promise)
  return promise
}

async function fetchWithTimeout(
  url: string,
  options: RequestInit = {},
  timeoutMs = 4000
): Promise<Response> {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs)
  try {
    const res = await fetch(url, {
      ...options,
      signal: controller.signal,
    })
    return res
  } finally {
    clearTimeout(timeoutId)
  }
}

export async function fetchOnlinePlayers(): Promise<MinecraftPlayersResponse> {
  return cachedFetch<MinecraftPlayersResponse>(
    'players_list',
    15000, // 15 seconds cache
    async () => {
      const { apiUrl, apiSecret } = getApiConfig()

      try {
        const res = await fetchWithTimeout(`${apiUrl}/api/players`, {
          headers: { Authorization: `Bearer ${apiSecret}` },
        })

        if (!res.ok) {
          return {
            online: false,
            players: [],
            count: 0,
            onlineCount: 0,
            error: `HTTP ${res.status}`,
          }
        }

        const data = await res.json()
        const players = data.players || []
        const onlineCount =
          typeof data.onlineCount === 'number'
            ? data.onlineCount
            : players.filter((p: any) => p.online).length

        return {
          online: true,
          players,
          count: data.count || players.length,
          onlineCount,
        }
      } catch (err: any) {
        return {
          online: false,
          players: [],
          count: 0,
          onlineCount: 0,
          error: err?.message || 'Minecraft server is offline or unreachable',
        }
      }
    }
  )
}

export async function fetchPlayerByUuid(
  uuid: string
): Promise<PlayerData | null> {
  const cacheKey = `player_uuid_${uuid.toLowerCase()}`
  return cachedFetch<PlayerData | null>(
    cacheKey,
    30000, // 30 seconds cache
    async () => {
      const { apiUrl, apiSecret } = getApiConfig()

      try {
        const res = await fetchWithTimeout(
          `${apiUrl}/api/player?uuid=${encodeURIComponent(uuid)}`,
          {
            headers: { Authorization: `Bearer ${apiSecret}` },
          }
        )

        if (!res.ok) return null
        const data: SinglePlayerResponse = await res.json()
        return data?.player || null
      } catch {
        return null
      }
    }
  )
}

export async function fetchPlayer(query: string): Promise<PlayerData | null> {
  const trimmed = query.trim()
  if (!trimmed) return null

  const isUuid =
    /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(
      trimmed
    ) || /^[0-9a-fA-F]{32}$/.test(trimmed)

  if (isUuid) {
    return fetchPlayerByUuid(trimmed)
  }

  const cacheKey = `player_name_${trimmed.toLowerCase()}`
  return cachedFetch<PlayerData | null>(
    cacheKey,
    30000, // 30 seconds cache
    async () => {
      const { apiUrl, apiSecret } = getApiConfig()

      // 1. Direct name query on SMEssential backend API
      try {
        const res = await fetchWithTimeout(
          `${apiUrl}/api/player?name=${encodeURIComponent(trimmed)}`,
          {
            headers: { Authorization: `Bearer ${apiSecret}` },
          }
        )
        if (res.ok) {
          const data: SinglePlayerResponse = await res.json()
          if (data?.player) return data.player
        }
      } catch {}

      // 2. Check players list from SMEssential
      try {
        const playersData = await fetchOnlinePlayers()
        const match = playersData.players?.find(
          p => p.username.toLowerCase() === trimmed.toLowerCase()
        )
        if (match?.uuid) {
          const player = await fetchPlayerByUuid(match.uuid)
          if (player) return player
        }
      } catch {}

      return null
    }
  )
}

export async function fetchLeaderboards(): Promise<LeaderboardsResponse> {
  return cachedFetch<LeaderboardsResponse>(
    'leaderboards_all',
    30000, // 30 seconds cache
    async () => {
      const { apiUrl, apiSecret } = getApiConfig()

      try {
        const res = await fetchWithTimeout(`${apiUrl}/api/leaderboards`, {
          headers: { Authorization: `Bearer ${apiSecret}` },
        })

        if (!res.ok) {
          return {
            online: false,
            leaderboards: [],
            error: `HTTP ${res.status}`,
          }
        }

        const data = await res.json()
        return {
          online: true,
          leaderboards: Array.isArray(data.leaderboards)
            ? (data.leaderboards as StatisticLeaderboard[])
            : [],
        }
      } catch (err: any) {
        return {
          online: false,
          leaderboards: [],
          error: err?.message || 'Failed to load leaderboards from server',
        }
      }
    }
  )
}
