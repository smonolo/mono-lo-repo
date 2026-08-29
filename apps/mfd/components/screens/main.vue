<script setup lang="ts">
import type { ScreenConfig } from '~/types/screen'
import { useScreenStore } from '~/stores/screen'
import { useAuthStore } from '~/stores/auth'

const { setActiveScreen } = useScreenStore()
const authStore = useAuthStore()

onMounted(() => {
  authStore.fetchSession()
})

const lowerButtonActions = reactive<
  Record<string, { label: string; action: () => void }>
>({})

watchEffect(() => {
  for (const key of Object.keys(lowerButtonActions)) {
    delete lowerButtonActions[key]
  }

  if (authStore.hasScreenPermission('version')) {
    lowerButtonActions.lower0 = {
      label: 'Ver',
      action: () => setActiveScreen('version'),
    }
  }

  if (authStore.hasScreenPermission('mc')) {
    lowerButtonActions.lower1 = {
      label: 'Mc',
      action: () => setActiveScreen('mc'),
    }
  }

  if (authStore.hasScreenPermission('test')) {
    lowerButtonActions.lower2 = {
      label: 'Test',
      action: () => setActiveScreen('test'),
    }
  }

  if (authStore.hasScreenPermission('auth')) {
    lowerButtonActions.lower8 = {
      label: 'Auth',
      action: () => setActiveScreen('auth'),
    }
  }

  if (authStore.hasScreenPermission('settings')) {
    lowerButtonActions.lower9 = {
      label: 'Set',
      action: () => setActiveScreen('settings'),
    }
  }
})

defineExpose<ScreenConfig>({
  lowerButtonActions,
})
</script>

<template>
  <div class="space-y-2">
    <p>Welcome</p>
    <p v-if="authStore.isAuthenticated && authStore.user">
      You are logged in as {{ authStore.user.name }} ({{
        authStore.user.email
      }})
    </p>
  </div>
</template>
