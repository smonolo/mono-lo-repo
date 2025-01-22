<script setup lang="ts">
import { useScreenStore } from '~/stores/screen'
import OptionsCard from '~/components/common/options-card.vue'

defineComponent({ name: 'SettingsScreen' })

const screenStore = useScreenStore()

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
