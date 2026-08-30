import { defineEventHandler, getQuery, createError } from 'h3'
import { requireAdmin } from '~/server/utils/session'
import type { SinglePlayerResponse } from '~/types/minecraft'

export default defineEventHandler(async event => {
  requireAdmin(event)

  const query = getQuery(event)
  const uuid = query.uuid as string

  if (!uuid) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Missing uuid query parameter',
    })
  }

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
    const response = await $fetch<SinglePlayerResponse>(
      `${apiUrl}/v1/player?uuid=${encodeURIComponent(uuid)}`,
      {
        timeout: 5000,
      }
    )

    return response
  } catch (err: any) {
    const errorMsg =
      err?.data?.error ||
      err?.data?.statusMessage ||
      err?.message ||
      'Failed to fetch player details from server'

    return {
      online: false,
      error: errorMsg,
    }
  }
})
