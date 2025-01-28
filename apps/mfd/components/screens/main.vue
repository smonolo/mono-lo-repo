<script setup lang="ts">
import type { ScreenComponent } from '~/types/buttons'

defineComponent({ name: 'MainScreen' })

const screenStore = useScreenStore()

defineExpose<ScreenComponent>({
  lowerButtonActions: {
    lower0: {
      label: 'Ver',
      onClick: () => screenStore.setActiveScreen('version'),
    },
    lower5: {
      label: 'S',
      onClick: () => screenStore.setActiveScreen('settings'),
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
