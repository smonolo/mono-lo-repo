import type { AuthUser } from '~/types/auth'

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

  const initGoogleAuth = (onSuccess?: () => void) => {
    if (typeof window === 'undefined' || !window.google) return

    const config = useRuntimeConfig()
    const clientId = config.public.googleClientId

    if (!clientId) return

    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: async (response: { credential: string }) => {
        if (response.credential) {
          const ok = await loginWithCredential(response.credential)
          if (ok && onSuccess) {
            onSuccess()
          }
        }
      },
    })
  }

  return {
    user,
    isLoading,
    authError,
    isAuthenticated,
    isAdmin,
    fetchSession,
    loginWithCredential,
    logout,
    initGoogleAuth,
  }
})
