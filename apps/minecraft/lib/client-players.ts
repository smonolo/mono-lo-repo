'use client'

import type { MinecraftPlayersResponse, PlayerSummary } from '@/types/minecraft'

let cachedData: MinecraftPlayersResponse | null = null
let cachedTime = 0
let inFlightPromise: Promise<MinecraftPlayersResponse> | null = null

const CACHE_TTL_MS = 5000

export async function fetchPlayersClient(
  force = false
): Promise<MinecraftPlayersResponse> {
  const now = Date.now()

  if (!force && cachedData && now - cachedTime < CACHE_TTL_MS) {
    return cachedData
  }

  if (inFlightPromise) {
    return inFlightPromise
  }

  inFlightPromise = (async () => {
    try {
      const res = await fetch('/api/players')
      if (res.ok) {
        const data = (await res.json()) as MinecraftPlayersResponse
        cachedData = data
        cachedTime = Date.now()
        return data
      }
      return (
        cachedData || {
          online: false,
          players: [],
          count: 0,
          onlineCount: 0,
        }
      )
    } catch {
      return (
        cachedData || {
          online: false,
          players: [],
          count: 0,
          onlineCount: 0,
        }
      )
    } finally {
      inFlightPromise = null
    }
  })()

  return inFlightPromise
}

export async function getClientPlayersList(
  force = false
): Promise<PlayerSummary[]> {
  const data = await fetchPlayersClient(force)
  return Array.isArray(data.players) ? data.players : []
}
