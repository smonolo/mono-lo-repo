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
  if (!authStore.hasScreenPermission('test')) return []

  const opts: Option[] = []

  if (authStore.hasScreenPermission('doc')) {
    opts.push({
      name: 'test_doc',
      label: 'Documentation (Long Text)',
      action: () => screenStore.setActiveScreen('doc'),
    })
  }

  if (authStore.hasScreenPermission('diag')) {
    opts.push({
      name: 'test_diag',
      label: 'Diagnostics (Long Options)',
      action: () => screenStore.setActiveScreen('diag'),
    })
  }

  if (authStore.hasScreenPermission('mixed')) {
    opts.push({
      name: 'test_mixed',
      label: 'Mixed (Long Text & Options)',
      action: () => screenStore.setActiveScreen('mixed'),
    })
  }

  return opts
})

watchEffect(() => {
  if (options.value.length) {
    optionsStore.setOptions(options.value)
  }
})
</script>

<template>
  <div class="h-full w-full">
    <!-- Unauthorized View -->
    <div v-if="!authStore.hasScreenPermission('test')" class="space-y-4">
      <div class="space-y-2 border border-slate-950 p-4 dark:border-slate-100">
        <p class="font-bold tracking-wide">Restricted System</p>
        <p>Authorization is required to access diagnostic test suites.</p>
        <p>
          Please navigate to the Auth screen to sign in with an authorized
          account.
        </p>
      </div>
    </div>

    <!-- Authenticated View -->
    <div v-else class="space-y-4">
      <OptionsCard header="Test Pages" :options="options" />
    </div>
  </div>
</template>
