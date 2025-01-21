<script setup lang="ts">
import ScreenControl from '~/components/screen/control.vue'
import { useControlsStore } from '~/stores/controls'

defineComponent({ name: 'ScreenControls' })

const { controls } = useControlsStore()
const { lowerButtons } = useButtons()
</script>

<template>
  <div class="flex h-[80px] border-t border-slate-100">
    <ScreenControl
      v-for="(button, key) in lowerButtons"
      :key
      :control="controls.find(c => c.slot === button.name)"
    />
  </div>
</template>
