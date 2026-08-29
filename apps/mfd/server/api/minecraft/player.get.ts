import { defineEventHandler, getQuery, createError } from 'h3'
import { requireAdmin } from '~/server/utils/session'
import type { SinglePlayerResponse } from '~/types/minecraft'

export default defineEventHandler(async event => {
  // 1. Enforce admin-only authentication
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
    process.env.SMESSENTIAL_API_URL ||
    process.env.NUXT_SMESSENTIAL_API_URL ||
    (config.smessentialApiUrl as string) ||
    'http://127.0.0.1:25580'
  const apiUrl = rawApiUrl
    .trim()
    .replace(/^["']|["']$/g, '')
    .replace(/\/$/, '')

  const rawSecret =
    process.env.SMESSENTIAL_API_SECRET ||
    process.env.NUXT_SMESSENTIAL_API_SECRET ||
    (config.smessentialApiSecret as string) ||
    'smessential-secret-key'
  const apiSecret = rawSecret.trim().replace(/^["']|["']$/g, '')

  try {
    const response = await $fetch<SinglePlayerResponse>(
      `${apiUrl}/api/player?uuid=${encodeURIComponent(uuid)}`,
      {
        headers: {
          Authorization: `Bearer ${apiSecret}`,
        },
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
