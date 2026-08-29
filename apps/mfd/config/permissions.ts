import type { AuthRole } from '~/types/auth'

export type ScreenName =
  | 'main'
  | 'version'
  | 'settings'
  | 'auth'
  | 'mc'
  | 'player'
  | 'test'
  | 'doc'
  | 'diag'
  | 'mixed'

export const ROLE_PERMISSIONS: Record<AuthRole, ScreenName[]> = {
  admin: [
    'main',
    'version',
    'settings',
    'auth',
    'mc',
    'player',
    'test',
    'doc',
    'diag',
    'mixed',
  ],
  user: ['main', 'version', 'settings', 'auth'],
  guest: ['main', 'version', 'settings', 'auth'],
}

export const canAccessScreen = (
  screen: ScreenName | string,
  role: AuthRole = 'guest'
): boolean => {
  const allowed = ROLE_PERMISSIONS[role] || ROLE_PERMISSIONS.guest
  return allowed.includes(screen as ScreenName)
}
