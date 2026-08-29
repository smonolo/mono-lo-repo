<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useOptionsStore } from '~/stores/options'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'

const authStore = useAuthStore()
const optionsStore = useOptionsStore()

onMounted(() => {
  authStore.fetchSession()
})

const lowerActions = reactive({
  lower0: {
    label: 'In',
    action: () => {
      if (authStore.isAuthenticated) {
        authStore.logout()
      } else {
        authStore.triggerGooglePopup()
      }
    },
  },
  lower9: useMainButtonConfig(),
})

watchEffect(() => {
  lowerActions.lower0.label = authStore.isAuthenticated ? 'Out' : 'In'
})

defineExpose<ScreenConfig>({
  lowerButtonActions: lowerActions,
})

const options = computed(() => {
  if (authStore.isAuthenticated && authStore.user) {
    return [
      {
        name: 'name',
        label: 'Name',
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
  ]
})

watchEffect(() => {
  optionsStore.setOptions(options.value)
})
</script>

<template>
  <div class="space-y-4">
    <div
      v-if="authStore.authError"
      class="border border-red-500 p-2 text-red-500 dark:border-red-400 dark:text-red-400"
    >
      <span>AUTH ERROR: {{ authStore.authError }}</span>
    </div>

    <OptionsCard header="Account" :options="options" />
  </div>
</template>
