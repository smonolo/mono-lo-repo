import { defineEventHandler } from 'h3'
import { getUserSession } from '~/server/utils/session'

export default defineEventHandler(event => {
  const user = getUserSession(event)

  return {
    user: user || null,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'admin',
  }
})
