export type AuthRole = 'admin' | 'user' | 'guest'

export type AuthUser = {
  email: string
  name: string
  picture?: string
  role: AuthRole
}

export type AuthState = {
  user: AuthUser | null
  isAuthenticated: boolean
  isAdmin: boolean
  isLoading: boolean
}
