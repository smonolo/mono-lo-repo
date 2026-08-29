<script setup lang="ts">
import type { ScreenConfig } from '~/types/screen'
import { useScreenStore } from '~/stores/screen'
import { useAuthStore } from '~/stores/auth'

const { setActiveScreen } = useScreenStore()
const authStore = useAuthStore()

onMounted(() => {
  authStore.fetchSession()
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower0: {
      label: 'Ver',
      action: () => setActiveScreen('version'),
    },
    lower1: {
      label: 'Ath',
      action: () => setActiveScreen('auth'),
    },
    lower2: {
      label: 'Mc',
      action: () => setActiveScreen('mc'),
    },
    lower3: {
      label: 'Tst',
      action: () => setActiveScreen('test'),
    },
    lower9: {
      label: 'Set',
      action: () => setActiveScreen('settings'),
    },
  },
})
</script>

<template>
  <div>
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>Main</span>
    </div>
    <div class="p-10 space-y-2">
      <p>Welcome</p>
      <p v-if="authStore.isAuthenticated && authStore.user">
        You are logged in as {{ authStore.user.name }} ({{ authStore.user.email }})
      </p>
    </div>
  </div>
</template>

