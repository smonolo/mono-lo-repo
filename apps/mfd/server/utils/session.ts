import crypto from 'node:crypto'
import type { H3Event } from 'h3'
import type { AuthUser } from '~/types/auth'

const SESSION_COOKIE_NAME = 'mfd_session'

export const signSession = (payload: AuthUser, secret: string): string => {
  const data = Buffer.from(JSON.stringify(payload)).toString('base64url')
  const signature = crypto
    .createHmac('sha256', secret)
    .update(data)
    .digest('base64url')
  return `${data}.${signature}`
}

export const verifySession = (
  cookieValue: string,
  secret: string
): AuthUser | null => {
  try {
    const parts = cookieValue.split('.')
    if (parts.length !== 2) return null
    const [data, signature] = parts

    const expectedSignature = crypto
      .createHmac('sha256', secret)
      .update(data)
      .digest('base64url')

    const sigBuf = Buffer.from(signature, 'utf8')
    const expBuf = Buffer.from(expectedSignature, 'utf8')

    if (
      sigBuf.length !== expBuf.length ||
      !crypto.timingSafeEqual(sigBuf, expBuf)
    ) {
      return null
    }

    const json = Buffer.from(data, 'base64url').toString('utf8')
    return JSON.parse(json) as AuthUser
  } catch {
    return null
  }
}

export const getAdminEmails = (): string[] => {
  const config = useRuntimeConfig()
  const raw =
    process.env.ADMIN_EMAIL ||
    process.env.NUXT_ADMIN_EMAIL ||
    (config.adminEmail as string) ||
    ''
  return raw
    .split(',')
    .map(e =>
      e
        .trim()
        .replace(/^["']|["']$/g, '')
        .toLowerCase()
    )
    .filter(Boolean)
}

export const checkIsAdmin = (email?: string | null): boolean => {
  if (!email) return false
  const adminEmails = getAdminEmails()
  const normalized = email.trim().toLowerCase()
  return adminEmails.includes(normalized)
}

export const getUserSession = (event: H3Event): AuthUser | null => {
  const cookie = getCookie(event, SESSION_COOKIE_NAME)
  if (!cookie) return null

  const config = useRuntimeConfig()
  const secret =
    process.env.SESSION_SECRET ||
    process.env.NUXT_SESSION_SECRET ||
    config.sessionSecret ||
    'mfd-session-secret-change-in-production'
  const user = verifySession(cookie, secret)
  if (!user) return null

  // Dynamically ensure role is always accurate to current ADMIN_EMAIL
  user.role = checkIsAdmin(user.email) ? 'admin' : 'user'
  return user
}

export const setSessionCookie = (event: H3Event, user: AuthUser): void => {
  const config = useRuntimeConfig()
  const secret =
    process.env.SESSION_SECRET ||
    process.env.NUXT_SESSION_SECRET ||
    config.sessionSecret ||
    'mfd-session-secret-change-in-production'
  user.role = checkIsAdmin(user.email) ? 'admin' : 'user'
  const signed = signSession(user, secret)

  setCookie(event, SESSION_COOKIE_NAME, signed, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    path: '/',
    maxAge: 60 * 60 * 24 * 7, // 7 days
  })
}

export const clearSessionCookie = (event: H3Event): void => {
  deleteCookie(event, SESSION_COOKIE_NAME, {
    path: '/',
  })
}

export const requireAdmin = (event: H3Event): AuthUser => {
  const user = getUserSession(event)
  if (!user) {
    throw createError({
      statusCode: 401,
      statusMessage: 'Unauthorized - Please sign in',
    })
  }
  if (user.role !== 'admin') {
    throw createError({
      statusCode: 403,
      statusMessage: 'Forbidden - Administrator access required',
    })
  }
  return user
}
