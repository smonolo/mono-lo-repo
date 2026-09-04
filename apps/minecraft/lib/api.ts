import type {
  Achievement,
  AchievementsResponse,
  LeaderboardsResponse,
  MinecraftPlayersResponse,
  PlayerData,
  PlayerAchievement,
  PlayerStats,
  Punishment,
  PunishmentsResponse,
  SinglePlayerResponse,
  StatisticLeaderboard,
  WorldResponse,
} from '@/types/minecraft'
import {
  DEFAULT_ACHIEVEMENTS,
  DEFAULT_CATEGORIES,
} from './achievements-catalog'

export function getApiConfig() {
  const rawApiUrl =
    process.env.MINECRAFT_API_URL || 'https://minecraft-api.smnl.dev'
  const apiUrl = rawApiUrl
    .trim()
    .replace(/^["']|["']$/g, '')
    .replace(/\/$/, '')

  return { apiUrl }
}

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
      cache: 'no-store',
      ...options,
      signal: controller.signal,
    })
    return res
  } finally {
    clearTimeout(timeoutId)
  }
}

const normalizeUuid = (u?: string) =>
  u ? u.replace(/-/g, '').toLowerCase() : ''

async function enrichPlayerPunishments(
  player: PlayerData | null
): Promise<PlayerData | null> {
  if (!player) return null
  try {
    const punishmentsData = await fetchPunishments()
    const targetNormUuid = normalizeUuid(player.uuid)
    const targetUsername = player.username.toLowerCase()

    const playerPunishments = punishmentsData.punishments.filter(p => {
      const pNormUuid = normalizeUuid(p.uuid)
      const pUsername = (p.username || '').toLowerCase()
      return (
        (targetNormUuid && pNormUuid === targetNormUuid) ||
        (targetUsername && pUsername === targetUsername)
      )
    })

    return {
      ...player,
      punishments: playerPunishments,
    }
  } catch {
    return player
  }
}

function enrichPlayerAchievements(
  player: PlayerData | null
): PlayerData | null {
  if (!player) return null

  // If the server returned an achievements payload with a list, use it directly
  if (
    player.achievements &&
    Array.isArray(player.achievements.list) &&
    player.achievements.list.length > 0
  ) {
    const list: PlayerAchievement[] = player.achievements.list.map(item => ({
      ...item,
      completed: !!item.completed,
      completedAt:
        item.completed && item.completedAt && item.completedAt > 0
          ? item.completedAt
          : null,
    }))

    const completedCount =
      typeof player.achievements.completedCount === 'number'
        ? player.achievements.completedCount
        : list.filter(a => a.completed).length
    const totalCount =
      typeof player.achievements.totalCount === 'number'
        ? player.achievements.totalCount
        : list.length
    const percentage =
      totalCount > 0
        ? Math.round((completedCount * 1000) / totalCount) / 10
        : 0

    return {
      ...player,
      achievements: {
        completedCount,
        totalCount,
        percentage,
        list,
      },
    }
  }

  // Fallback if the server has no achievements data yet: all locked
  const list: PlayerAchievement[] = DEFAULT_ACHIEVEMENTS.map(a => ({
    id: a.id,
    title: a.title,
    description: a.description,
    frame: a.frame,
    icon: a.icon,
    category: a.category,
    categoryName: a.categoryName,
    parent: a.parent,
    completed: false,
    completedAt: null,
  }))

  return {
    ...player,
    achievements: {
      completedCount: 0,
      totalCount: list.length,
      percentage: 0,
      list,
    },
  }
}

export async function fetchOnlinePlayers(): Promise<MinecraftPlayersResponse> {
  return cachedFetch<MinecraftPlayersResponse>(
    'players_list',
    3000,
    async () => {
      const { apiUrl } = getApiConfig()

      try {
        const res = await fetchWithTimeout(`${apiUrl}/v1/players`)

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
          error: err?.message || 'Failed to reach Minecraft server',
        }
      }
    }
  )
}

export async function fetchPlayerByUuid(
  uuid: string
): Promise<PlayerData | null> {
  if (!uuid) return null
  const cacheKey = `player_uuid_${uuid.toLowerCase()}`

  const player = await cachedFetch<PlayerData | null>(
    cacheKey,
    10000,
    async () => {
      const { apiUrl } = getApiConfig()

      try {
        const res = await fetchWithTimeout(
          `${apiUrl}/v1/player?uuid=${encodeURIComponent(uuid)}`
        )

        if (!res.ok) return null
        const data: SinglePlayerResponse = await res.json()
        return data?.player || null
      } catch {
        return null
      }
    }
  )

  return enrichPlayerAchievements(await enrichPlayerPunishments(player))
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
  const player = await cachedFetch<PlayerData | null>(
    cacheKey,
    10000,
    async () => {
      const { apiUrl } = getApiConfig()

      try {
        const res = await fetchWithTimeout(
          `${apiUrl}/v1/player?name=${encodeURIComponent(trimmed)}`
        )
        if (res.ok) {
          const data: SinglePlayerResponse = await res.json()
          if (data?.player) return data.player
        }
      } catch {}

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

  return enrichPlayerAchievements(await enrichPlayerPunishments(player))
}

export async function fetchLeaderboards(): Promise<LeaderboardsResponse> {
  return cachedFetch<LeaderboardsResponse>(
    'leaderboards_all',
    30000,
    async () => {
      const { apiUrl } = getApiConfig()

      try {
        const res = await fetchWithTimeout(`${apiUrl}/v1/leaderboards`)

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

export async function fetchPunishments(): Promise<PunishmentsResponse> {
  return cachedFetch<PunishmentsResponse>(
    'punishments_all',
    15000,
    async () => {
      const { apiUrl } = getApiConfig()

      try {
        const res = await fetchWithTimeout(`${apiUrl}/v1/punishments`)

        if (!res.ok) {
          return {
            online: false,
            punishments: [],
            error: `HTTP ${res.status}`,
          }
        }

        const data = await res.json()
        return {
          online: true,
          punishments: Array.isArray(data.punishments)
            ? (data.punishments as Punishment[])
            : [],
        }
      } catch (err: any) {
        return {
          online: false,
          punishments: [],
          error: err?.message || 'Failed to load punishments from server',
        }
      }
    }
  )
}

export async function fetchWorldStats(): Promise<WorldResponse> {
  return cachedFetch<WorldResponse>('world_stats', 5000, async () => {
    const { apiUrl } = getApiConfig()

    try {
      const res = await fetchWithTimeout(`${apiUrl}/v1/world`)

      if (!res.ok) {
        return {
          online: false,
          error: `HTTP ${res.status}`,
        }
      }

      const data = await res.json()
      return {
        online: data.online !== false,
        worldAge: data.worldAge,
        time: data.time,
        moonPhase: data.moonPhase,
        weather: data.weather,
        dimensions: Array.isArray(data.dimensions) ? data.dimensions : [],
        aggregates: data.aggregates || {},
      }
    } catch (err: any) {
      return {
        online: false,
        error: err?.message || 'Failed to reach Minecraft server',
      }
    }
  })
}

export async function fetchAchievements(): Promise<AchievementsResponse> {
  return cachedFetch<AchievementsResponse>(
    'achievements_all',
    30000,
    async () => {
      const { apiUrl } = getApiConfig()

      const urlsToTry = [`${apiUrl}/v1/achievements`]
      if (!urlsToTry.includes('http://localhost:3002/v1/achievements')) {
        urlsToTry.push('http://localhost:3002/v1/achievements')
      }

      for (const url of urlsToTry) {
        try {
          const res = await fetchWithTimeout(url)
          if (res.ok) {
            const data = await res.json()
            if (
              data &&
              Array.isArray(data.achievements) &&
              data.achievements.length > 0
            ) {
              return {
                online: data.online !== false,
                total: data.total || data.achievements.length,
                categories:
                  Array.isArray(data.categories) && data.categories.length > 0
                    ? data.categories
                    : DEFAULT_CATEGORIES,
                achievements: data.achievements,
                globalStats: data.globalStats,
              }
            }
          }
        } catch {}
      }

      return {
        online: true,
        total: DEFAULT_ACHIEVEMENTS.length,
        categories: DEFAULT_CATEGORIES,
        achievements: DEFAULT_ACHIEVEMENTS,
        globalStats: {
          totalAchievements: DEFAULT_ACHIEVEMENTS.length,
          totalCompletions: DEFAULT_ACHIEVEMENTS.filter(
            a => (a.completedCount ?? 0) > 0
          ).length,
          trackedPlayers: 1,
        },
      }
    }
  )
}
