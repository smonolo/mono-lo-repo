<script setup lang="ts">
import { useScreenStore } from '~/stores/screen'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import { useOptionsStore } from '~/stores/options'

defineExpose<ScreenConfig>({
  lowerButtonActions: { lower9: useMainButtonConfig() },
})

const screenStore = useScreenStore()
const optionsStore = useOptionsStore()

const options = computed(() => [
  {
    name: 'brightness',
    label: 'Brightness',
    value: `${Math.round(screenStore.brightness * 100)}%`,
    action: screenStore.increaseBrightness,
  },
  {
    name: 'contrast',
    label: 'Contrast',
    value:
      screenStore.contrast.charAt(0).toUpperCase() +
      screenStore.contrast.slice(1),
    action: screenStore.toggleContrast,
  },
])

watchEffect(() => {
  optionsStore.setOptions(options.value)
})
</script>

<template>
  <OptionsCard header="Screen" :options="options" />
</template>
