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

  const renderGoogleButton = (parent: HTMLElement, onSuccess?: () => void) => {
    if (typeof window === 'undefined' || !window.google?.accounts?.id) return

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
      ux_mode: 'popup',
    })

    window.google.accounts.id.renderButton(parent, {
      type: 'standard',
      theme: 'outline',
      size: 'large',
    })
  }

  const triggerGoogleSignIn = (parentContainer?: HTMLElement | null) => {
    const el =
      parentContainer || document.getElementById('google-signin-hidden-btn')
    if (el) {
      const btn =
        (el.querySelector('div[role="button"]') as HTMLElement) ||
        (el.querySelector('button') as HTMLElement) ||
        (el.firstElementChild as HTMLElement)
      if (btn) {
        btn.click()
        return
      }
    }
    if (typeof window !== 'undefined' && window.google?.accounts?.id) {
      window.google.accounts.id.prompt()
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
    logout,
    renderGoogleButton,
    triggerGoogleSignIn,
  }
})
