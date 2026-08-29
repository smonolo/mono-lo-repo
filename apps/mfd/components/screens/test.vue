<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useScreenStore } from '~/stores/screen'
import { useOptionsStore } from '~/stores/options'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'
import type { Option } from '~/types/options'

const authStore = useAuthStore()
const screenStore = useScreenStore()
const optionsStore = useOptionsStore()

onMounted(() => {
  authStore.fetchSession()
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower9: useMainButtonConfig(),
  },
})

const options = computed<Option[]>(() => {
  if (!authStore.isAdmin) return []

  return [
    {
      name: 'test_doc',
      label: 'Documentation (Long Text)',
      value: 'View Page',
      action: () => screenStore.setActiveScreen('doc'),
    },
    {
      name: 'test_diag',
      label: 'Diagnostics (Long Options)',
      value: 'View Page',
      action: () => screenStore.setActiveScreen('diag'),
    },
  ]
})

watchEffect(() => {
  if (options.value.length) {
    optionsStore.setOptions(options.value)
  }
})
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>Test Suites</span>
    </div>

    <!-- Unauthenticated / Non-Admin View -->
    <div v-if="!authStore.isAdmin" class="p-10 space-y-4">
      <div class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <p class="font-bold tracking-wide">Restricted System</p>
        <p>
          Administrator authorization is required to access diagnostic test suites.
        </p>
        <p>
          Please navigate to the Auth screen to sign in with an authorized Google account.
        </p>
      </div>
    </div>

    <!-- Authenticated Admin View -->
    <div v-else class="p-10 space-y-4">
      <OptionsCard header="Test Pages" :options="options" />
    </div>
  </div>
</template>
