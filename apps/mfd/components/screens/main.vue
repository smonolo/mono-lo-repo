<script setup lang="ts">
import type { ScreenConfig } from '~/types/screen'
import { useScreenStore } from '~/stores/screen'

const { setActiveScreen } = useScreenStore()

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
    lower5: {
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
    <div class="p-10">
      <p>Welcome</p>
    </div>
  </div>
</template>
