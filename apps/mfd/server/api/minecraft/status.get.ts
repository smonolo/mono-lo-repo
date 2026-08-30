import { defineEventHandler } from 'h3'
import { requireAdmin } from '~/server/utils/session'

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
    const response = await $fetch<{
      online: boolean
      onlinePlayers: number
      maxPlayers: number
      version: string
      minecraftVersion: string
      tps: number
      mspt: number
      memory?: {
        usedMb: number
        allocatedMb: number
        maxMb: number
      }
      uptimeSeconds: number
    }>(`${apiUrl}/v1/status`, {
      timeout: 5000,
    })

    return response
  } catch (err: any) {
    const errorMsg =
      err?.data?.error ||
      err?.data?.statusMessage ||
      err?.message ||
      'Minecraft server is offline or unreachable'

    return {
      online: false,
      error: errorMsg,
      onlinePlayers: 0,
      maxPlayers: 0,
      version: '',
      minecraftVersion: '',
      tps: 0,
      mspt: 0,
      uptimeSeconds: 0,
    }
  }
})
