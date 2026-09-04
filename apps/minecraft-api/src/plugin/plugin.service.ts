import { Injectable, Logger } from '@nestjs/common'

type CacheItem<T> = {
  data: T
  expiresAt: number
}

@Injectable()
export class PluginService {
  private readonly logger = new Logger(PluginService.name)
  private readonly memoryCache = new Map<string, CacheItem<any>>()
  private readonly inFlight = new Map<string, Promise<any>>()

  private get apiUrl(): string {
    const raw = process.env.SMESSENTIAL_API_URL || 'http://127.0.0.1:25580'
    return raw
      .trim()
      .replace(/^["']|["']$/g, '')
      .replace(/\/$/, '')
  }

  private get apiSecret(): string {
    const raw = process.env.SMESSENTIAL_API_SECRET || 'smessential-secret-key'
    return raw.trim().replace(/^["']|["']$/g, '')
  }

  private async cachedFetch<T>(
    key: string,
    ttlMs: number,
    fetcher: () => Promise<T>
  ): Promise<T> {
    const now = Date.now()

    if (this.memoryCache.size > 500) {
      for (const [k, v] of this.memoryCache.entries()) {
        if (v.expiresAt <= now) {
          this.memoryCache.delete(k)
        }
      }
      if (this.memoryCache.size > 500) {
        const excess = this.memoryCache.size - 400
        const keysToDelete = Array.from(this.memoryCache.keys()).slice(0, excess)
        for (const k of keysToDelete) {
          this.memoryCache.delete(k)
        }
      }
    }

    const cached = this.memoryCache.get(key)
    if (cached && cached.expiresAt > now) {
      return cached.data
    }

    const pending = this.inFlight.get(key)
    if (pending) {
      return pending as Promise<T>
    }

    const promise = fetcher()
      .then(data => {
        this.memoryCache.set(key, { data, expiresAt: Date.now() + ttlMs })
        this.inFlight.delete(key)
        return data
      })
      .catch(err => {
        this.inFlight.delete(key)
        if (cached) {
          return cached.data
        }
        throw err
      })

    this.inFlight.set(key, promise)
    return promise
  }

  private async executeFetch<T>(
    path: string
  ): Promise<{ data: T | null; status: number }> {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000)

    try {
      const url = `${this.apiUrl}${path.startsWith('/') ? path : `/${path}`}`
      const res = await fetch(url, {
        headers: {
          Authorization: `Bearer ${this.apiSecret}`,
        },
        signal: controller.signal,
      })

      if (res.ok) {
        const data = (await res.json()) as T
        return { data, status: res.status }
      }

      this.logger.warn(
        `Plugin API responded with HTTP ${res.status} for ${url}`
      )
      return { data: null, status: res.status }
    } catch (err: any) {
      this.logger.warn(
        `Failed to connect to Plugin API (${path}): ${err?.message || err}`
      )
      return { data: null, status: 0 }
    } finally {
      clearTimeout(timeoutId)
    }
  }

  private async fetchFromPlugin<T>(endpoint: string): Promise<T | null> {
    const primary = await this.executeFetch<T>(endpoint)
    if (primary.data !== null) {
      return primary.data
    }

    if (primary.status === 404) {
      const fallbackEndpoint = endpoint.startsWith('/v1/')
        ? endpoint.replace(/^\/v1\//, '/api/')
        : endpoint.startsWith('/api/')
          ? endpoint.replace(/^\/api\//, '/v1/')
          : null

      if (fallbackEndpoint) {
        const fallback = await this.executeFetch<T>(fallbackEndpoint)
        if (fallback.data !== null) {
          return fallback.data
        }
      }
    }

    return null
  }

  async getStatus(): Promise<any> {
    return this.cachedFetch('plugin_status', 3000, async () => {
      const data = await this.fetchFromPlugin<any>('/v1/status')
      return data || { online: false }
    })
  }

  async getPlayers(): Promise<{
    online: boolean
    players: any[]
    count: number
    onlineCount: number
  }> {
    return this.cachedFetch('plugin_players', 3000, async () => {
      const data = await this.fetchFromPlugin<any>('/v1/players')
      if (!data) {
        return { online: false, players: [], count: 0, onlineCount: 0 }
      }
      return {
        online: data.online !== false,
        players: Array.isArray(data.players) ? data.players : [],
        count: data.count || 0,
        onlineCount: data.onlineCount || 0,
      }
    })
  }

  async getPlayer(query: string): Promise<any | null> {
    const trimmed = query ? query.trim() : ''
    if (!trimmed || trimmed.length > 64) {
      return null
    }

    const isUuid =
      /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(
        trimmed
      ) || /^[0-9a-fA-F]{32}$/.test(trimmed)

    const cacheKey = `plugin_player_${trimmed.toLowerCase()}`
    return this.cachedFetch(cacheKey, 10000, async () => {
      const param = isUuid
        ? `uuid=${encodeURIComponent(trimmed)}`
        : `name=${encodeURIComponent(trimmed)}`
      const data = await this.fetchFromPlugin<any>(`/v1/player?${param}`)
      return data?.player || null
    })
  }

  async getLeaderboards(statKey?: string): Promise<any> {
    const trimmed = statKey ? statKey.trim() : undefined
    if (trimmed && trimmed.length > 64) {
      return { online: false, leaderboards: [] }
    }

    const cacheKey = `plugin_leaderboards_${trimmed ? trimmed.toLowerCase() : 'all'}`
    return this.cachedFetch(cacheKey, 15000, async () => {
      const endpoint = trimmed
        ? `/v1/leaderboards?stat=${encodeURIComponent(trimmed)}`
        : '/v1/leaderboards'
      const data = await this.fetchFromPlugin<any>(endpoint)
      return data || { online: false, leaderboards: [] }
    })
  }

  async getWorld(): Promise<any> {
    return this.cachedFetch('plugin_world', 5000, async () => {
      const data = await this.fetchFromPlugin<any>('/v1/world')
      return (
        data || {
          online: false,
          worldAge: null,
          time: null,
          moonPhase: null,
          weather: null,
          dimensions: [],
          aggregates: null,
        }
      )
    })
  }

  async getAchievements(): Promise<any> {
    return this.cachedFetch('plugin_achievements', 30000, async () => {
      const data = await this.fetchFromPlugin<any>('/v1/achievements')
      return (
        data || {
          online: false,
          total: 0,
          categories: [],
          achievements: [],
          globalStats: {
            totalAchievements: 0,
            totalCompletions: 0,
            trackedPlayers: 0,
          },
        }
      )
    })
  }
}
