<script setup lang="ts">
import { useScreenStore } from '~/stores/screen'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import { useOptionsStore } from '~/stores/options'
import { useUpperButtonsActions } from '~/composables/buttons/actions/useUpperButtonsActions'
import { useSideButtonsActions } from '~/composables/buttons/actions/useSideButtonsActions'

defineComponent({ name: 'SettingsScreen' })

defineExpose<ScreenConfig>({
  upperButtonActions: useUpperButtonsActions(),
  lowerButtonActions: { lower9: useMainButtonConfig() },
  sideButtonActions: useSideButtonsActions(),
})

const screenStore = useScreenStore()
const { setOptions } = useOptionsStore()

setOptions([
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
