import { defineEventHandler, readBody, createError } from 'h3'
import { setSessionCookie, checkIsAdmin } from '~/server/utils/session'
import type { AuthUser } from '~/types/auth'

export default defineEventHandler(async event => {
  const body = await readBody<{ credential?: string; accessToken?: string }>(
    event
  )
  const credential = body?.credential
  const accessToken = body?.accessToken

  if (!credential && !accessToken) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Missing Google authentication token',
    })
  }

  const config = useRuntimeConfig()

  try {
    let email: string | undefined
    let name: string | undefined
    let picture: string | undefined
    let isVerified = false

    if (accessToken) {
      const userInfo = await $fetch<{
        email?: string
        email_verified?: string | boolean
        name?: string
        picture?: string
      }>('https://www.googleapis.com/oauth2/v3/userinfo', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      })

      email = userInfo.email
      name = userInfo.name
      picture = userInfo.picture
      isVerified =
        userInfo.email_verified === 'true' || userInfo.email_verified === true
    } else if (credential) {
      const tokenInfo = await $fetch<{
        email?: string
        email_verified?: string | boolean
        name?: string
        picture?: string
        aud?: string
      }>(`https://oauth2.googleapis.com/tokeninfo?id_token=${credential}`)

      if (
        config.public.googleClientId &&
        tokenInfo.aud !== config.public.googleClientId
      ) {
        throw createError({
          statusCode: 401,
          statusMessage: 'Google client ID mismatch',
        })
      }

      email = tokenInfo.email
      name = tokenInfo.name
      picture = tokenInfo.picture
      isVerified =
        tokenInfo.email_verified === 'true' || tokenInfo.email_verified === true
    }

    if (!email || !isVerified) {
      throw createError({
        statusCode: 401,
        statusMessage: 'Invalid Google token or unverified email',
      })
    }

    const isAdmin = checkIsAdmin(email)

    const user: AuthUser = {
      email,
      name: name || email.split('@')[0],
      picture,
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
