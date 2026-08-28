<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useOptionsStore } from '~/stores/options'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'

const authStore = useAuthStore()
const optionsStore = useOptionsStore()
const googleButtonRef = ref<HTMLElement | null>(null)

const triggerGooglePrompt = () => {
  if (typeof window !== 'undefined' && window.google?.accounts?.id) {
    authStore.initGoogleAuth()
    window.google.accounts.id.prompt()
  }
}

const renderGoogleButton = () => {
  if (
    typeof window !== 'undefined' &&
    window.google?.accounts?.id &&
    googleButtonRef.value
  ) {
    authStore.initGoogleAuth()
    try {
      window.google.accounts.id.renderButton(googleButtonRef.value, {
        theme: 'filled_black',
        size: 'medium',
        type: 'standard',
        text: 'signin_with',
      })
    } catch {
    }
  }
}

onMounted(() => {
  authStore.fetchSession()
  setTimeout(() => {
    renderGoogleButton()
  }, 300)
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower8: {
      label: 'Out',
      action: () => {
        if (authStore.isAuthenticated) {
          authStore.logout()
        }
      },
    },
    lower9: useMainButtonConfig(),
  },
})

const options = computed(() => {
  if (authStore.isAuthenticated && authStore.user) {
    return [
      {
        name: 'user',
        label: 'Account',
        value: authStore.user.name,
      },
      {
        name: 'email',
        label: 'Email',
        value: authStore.user.email,
      },
      {
        name: 'role',
        label: 'Role',
        value: authStore.user.role === 'admin' ? 'Administrator' : 'Standard',
      },
      {
        name: 'logout',
        label: 'Action',
        value: '[ Sign Out ]',
        action: () => authStore.logout(),
      },
    ]
  }

  return [
    {
      name: 'status',
      label: 'Status',
      value: 'Unauthenticated',
    },
    {
      name: 'role',
      label: 'Access Level',
      value: 'Guest',
    },
    {
      name: 'login',
      label: 'Action',
      value: '[ Sign In with Google ]',
      action: triggerGooglePrompt,
    },
  ]
})

watchEffect(() => {
  optionsStore.setOptions(options.value)
})
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>Identity & Authentication</span>
    </div>

    <div class="p-8 space-y-4">
      <div
        v-if="authStore.authError"
        class="border border-red-500 bg-red-950/20 p-2 text-xs text-red-500"
      >
        <span>AUTH ERROR: {{ authStore.authError }}</span>
      </div>

      <OptionsCard
        :header="authStore.isAuthenticated ? 'Active Session' : 'Google Identity'"
        :options="options"
      />

      <div
        v-if="!authStore.isAuthenticated"
        class="mt-4 flex flex-col items-center justify-center p-3 border border-dashed border-slate-800"
      >
        <span class="text-xs text-slate-500 dark:text-slate-400 mb-2"
          >GOOGLE AUTHENTICATION PROVIDER</span
        >
        <div ref="googleButtonRef" class="min-h-[40px] flex items-center justify-center"></div>
      </div>
    </div>
  </div>
</template>
