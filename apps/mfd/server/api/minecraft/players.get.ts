import { defineEventHandler } from 'h3'
import { requireAdmin } from '~/server/utils/session'

export default defineEventHandler(async event => {
  // 1. Enforce admin-only authentication
  requireAdmin(event)

  const config = useRuntimeConfig()
  const rawApiUrl =
    process.env.SMESSENTIAL_API_URL ||
    process.env.NUXT_SMESSENTIAL_API_URL ||
    (config.smessentialApiUrl as string) ||
    'http://127.0.0.1:25580'
  const apiUrl = rawApiUrl.trim().replace(/^["']|["']$/g, '').replace(/\/$/, '')

  const rawSecret =
    process.env.SMESSENTIAL_API_SECRET ||
    process.env.NUXT_SMESSENTIAL_API_SECRET ||
    (config.smessentialApiSecret as string) ||
    'smessential-secret-key'
  const apiSecret = rawSecret.trim().replace(/^["']|["']$/g, '')

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
      timeout: 5000,
    })

    return {
      online: true,
      players: response.players || [],
      count: response.count || (response.players ? response.players.length : 0),
    }
  } catch (err: any) {
    const errorMsg =
      err?.data?.error ||
      err?.data?.statusMessage ||
      err?.message ||
      'Minecraft server is offline or unreachable'

    return {
      online: false,
      error: errorMsg,
      players: [],
      count: 0,
    }
  }
})

