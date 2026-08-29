import type { AuthUser, AuthRole } from '~/types/auth'
import { canAccessScreen, type ScreenName } from '~/config/permissions'

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: any) => void
          prompt: (notification?: (notification: any) => void) => void
          renderButton: (parent: HTMLElement, options: any) => void
          cancel: () => void
        }
        oauth2: {
          initTokenClient: (config: {
            client_id: string
            scope: string
            callback: (response: { access_token?: string; error?: any }) => void
            error_callback?: (err: any) => void
          }) => {
            requestAccessToken: (options?: { prompt?: string }) => void
          }
        }
      }
    }
  }
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthUser | null>(null)
  const isLoading = ref<boolean>(true)
  const authError = ref<string | null>(null)

  const isAuthenticated = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')
  const currentRole = computed<AuthRole>(() => user.value?.role || 'guest')

  const hasScreenPermission = (screen: ScreenName | string): boolean => {
    return canAccessScreen(screen, currentRole.value)
  }

  const fetchSession = async () => {
    isLoading.value = true
    try {
      const data = await $fetch<{
        user: AuthUser | null
        isAuthenticated: boolean
        isAdmin: boolean
      }>('/api/auth/me')

      user.value = data.user
      authError.value = null
    } catch {
      user.value = null
    } finally {
      isLoading.value = false
    }
  }

  const loginWithCredential = async (credential: string) => {
    isLoading.value = true
    authError.value = null
    try {
      const res = await $fetch<{ ok: boolean; user: AuthUser }>(
        '/api/auth/google',
        {
          method: 'POST',
          body: { credential },
        }
      )
      user.value = res.user
      return true
    } catch (err: any) {
      authError.value = err.data?.statusMessage || err.message || 'Login failed'
      return false
    } finally {
      isLoading.value = false
    }
  }

  const loginWithAccessToken = async (accessToken: string) => {
    isLoading.value = true
    authError.value = null
    try {
      const res = await $fetch<{ ok: boolean; user: AuthUser }>(
        '/api/auth/google',
        {
          method: 'POST',
          body: { accessToken },
        }
      )
      user.value = res.user
      return true
    } catch (err: any) {
      authError.value = err.data?.statusMessage || err.message || 'Login failed'
      return false
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    isLoading.value = true
    try {
      await $fetch('/api/auth/logout', { method: 'POST' })
    } catch {
    } finally {
      user.value = null
      isLoading.value = false
    }
  }

  const triggerGooglePopup = (onSuccess?: () => void) => {
    if (typeof window === 'undefined' || !window.google?.accounts?.oauth2) {
      authError.value = 'Google authentication library is loading'
      return
    }

    const config = useRuntimeConfig()
    const clientId = config.public.googleClientId

    if (!clientId) {
      authError.value = 'Missing Google Client ID configuration'
      return
    }

    try {
      const tokenClient = window.google.accounts.oauth2.initTokenClient({
        client_id: clientId,
        scope: 'openid email profile',
        callback: async tokenResponse => {
          if (tokenResponse.error) {
            authError.value =
              typeof tokenResponse.error === 'string'
                ? tokenResponse.error
                : 'Authentication failed or was cancelled'
            return
          }
          if (tokenResponse.access_token) {
            const ok = await loginWithAccessToken(tokenResponse.access_token)
            if (ok && onSuccess) {
              onSuccess()
            }
          }
        },
        error_callback: () => {
          authError.value = 'Google sign-in popup closed or blocked'
        },
      })

      tokenClient.requestAccessToken({ prompt: 'select_account' })
    } catch (err: any) {
      authError.value = err.message || 'Failed to open Google sign-in window'
    }
  }

  return {
    user,
    isLoading,
    authError,
    isAuthenticated,
    isAdmin,
    currentRole,
    hasScreenPermission,
    fetchSession,
    loginWithCredential,
    loginWithAccessToken,
    logout,
    triggerGooglePopup,
  }
})
