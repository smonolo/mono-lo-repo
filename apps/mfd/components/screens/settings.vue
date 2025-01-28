<script setup lang="ts">
import { useScreenStore } from '~/stores/screen'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenComponent } from '~/types/buttons'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import { useOptionsStore } from '~/stores/options'

defineComponent({ name: 'SettingsScreen' })

defineExpose<ScreenComponent>({
  lowerButtonActions: {
    lower9: useMainButtonConfig(),
  },
})

const screenStore = useScreenStore()
const optionsStore = useOptionsStore()

optionsStore.setOptions([
  { name: 'brightness', action: screenStore.increaseBrightness },
  { name: 'theme', action: screenStore.toggleContrast },
])

const displayOptions = computed(() => ({
  brightness: {
    label: 'Brightness',
    value: screenStore.brightness,
  },
  theme: {
    label: 'Contrast',
    value: screenStore.contrast,
  },
}))
</script>

<template>
  <div class="p-10">
    <OptionsCard header="Screen" :options="displayOptions" />
  </div>
</template>
