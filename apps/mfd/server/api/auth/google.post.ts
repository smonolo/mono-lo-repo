import { defineEventHandler, readBody, createError } from 'h3'
import { setSessionCookie, checkIsAdmin } from '~/server/utils/session'
import type { AuthUser } from '~/types/auth'

export default defineEventHandler(async event => {
  const body = await readBody<{ credential?: string }>(event)
  const credential = body?.credential

  if (!credential) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Missing Google credential token',
    })
  }

  const config = useRuntimeConfig()

  try {
    const tokenInfo = await $fetch<{
      email?: string
      email_verified?: string | boolean
      name?: string
      picture?: string
      aud?: string
    }>(`https://oauth2.googleapis.com/tokeninfo?id_token=${credential}`)

    const isVerified =
      tokenInfo.email_verified === 'true' || tokenInfo.email_verified === true

    if (!tokenInfo.email || !isVerified) {
      throw createError({
        statusCode: 401,
        statusMessage: 'Invalid Google token or unverified email',
      })
    }

    if (
      config.public.googleClientId &&
      tokenInfo.aud !== config.public.googleClientId
    ) {
      throw createError({
        statusCode: 401,
        statusMessage: 'Google client ID mismatch',
      })
    }

    const isAdmin = checkIsAdmin(tokenInfo.email)

    const user: AuthUser = {
      email: tokenInfo.email,
      name: tokenInfo.name || tokenInfo.email.split('@')[0],
      picture: tokenInfo.picture,
      role: isAdmin ? 'admin' : 'user',
    }

    setSessionCookie(event, user)

    return {
      ok: true,
      user,
    }
  } catch (err: any) {
    if (err.statusCode) throw err
    throw createError({
      statusCode: 401,
      statusMessage: 'Failed to verify Google token with OAuth provider',
    })
  }
})
