<script setup lang="ts">
import type { ViewComponent } from '~/types/buttons'

defineComponent({ name: 'MainScreen' })

const screenStore = useScreenStore()

const goToVersionView = () => {
  screenStore.setActiveScreen('version')
}

defineExpose<ViewComponent>({
  lowerButtonActions: {
    lower0: {
      label: 'Ver',
      onClick: goToVersionView,
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
