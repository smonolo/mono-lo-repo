<script setup lang="ts">
import { useSideButtonsActions } from '~/composables/buttons/actions/useSideButtonsActions'
import { useUpperButtonsActions } from '~/composables/buttons/actions/useUpperButtonsActions'
import type { ScreenConfig } from '~/types/screen'

defineComponent({ name: 'MainScreen' })

const { setActiveScreen } = useScreenStore()

defineExpose<ScreenConfig>({
  upperButtonActions: useUpperButtonsActions(),
  lowerButtonActions: {
    lower0: {
      label: 'Ver',
      action: () => setActiveScreen('version'),
    },
    lower5: {
      label: 'S',
      action: () => setActiveScreen('settings'),
    },
  },
  sideButtonActions: useSideButtonsActions(),
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
