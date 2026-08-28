import { defineEventHandler } from 'h3'
import { requireAdmin } from '~/server/utils/session'

export default defineEventHandler(async event => {
  // 1. Enforce admin-only authentication
  requireAdmin(event)

  const config = useRuntimeConfig()
  const apiUrl =
    (config.smessentialApiUrl || 'http://127.0.0.1:25580').replace(/\/$/, '')
  const apiSecret = config.smessentialApiSecret || 'smessential-secret-key'

  try {
    const response = await $fetch<{
      online: boolean
      players?: Array<{
        uuid: string
        username: string
        displayName: string
        rank: {
          id: string
          name: string
          color: string
          prefix: string
        }
        ping: number
        afk: boolean
        world: string
      }>
      count?: number
    }>(`${apiUrl}/api/players/online`, {
      headers: {
        Authorization: `Bearer ${apiSecret}`,
      },
      timeout: 3000,
    })

    return {
      online: true,
      players: response.players || [],
      count: response.count || (response.players ? response.players.length : 0),
    }
  } catch (err: any) {
    return {
      online: false,
      error: 'Minecraft server is offline or unreachable',
      players: [],
      count: 0,
    }
  }
})
