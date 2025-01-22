<script setup lang="ts">
import ScreenControl from '~/components/screen/control.vue'

defineComponent({ name: 'ScreenControls' })

const { lowerButtons } = useButtons()
const controlsStore = useControlsStore()
</script>

<template>
  <div class="flex h-[80px] border-t border-slate-950 dark:border-slate-100">
    <ScreenControl
      v-for="(button, key) in lowerButtons"
      :key
      :control="controlsStore.controls.find(c => c.slot === button.name)"
    />
  </div>
</template>
