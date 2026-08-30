import { defineEventHandler } from 'h3'
import { requireAdmin } from '~/server/utils/session'
import type { MinecraftPlayersResponse } from '~/types/minecraft'

export default defineEventHandler(async event => {
  requireAdmin(event)

  const config = useRuntimeConfig()
  const rawApiUrl =
    (config.minecraftApiUrl as string) ||
    process.env.MINECRAFT_API_URL ||
    'https://minecraft-api.smnl.dev'
  const apiUrl = rawApiUrl
    .trim()
    .replace(/^["']|["']$/g, '')
    .replace(/\/$/, '')

  try {
    const response = await $fetch<MinecraftPlayersResponse>(
      `${apiUrl}/v1/players`,
      {
        timeout: 5000,
      }
    )

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
