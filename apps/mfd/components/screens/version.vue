<script setup lang="ts">
import info from '~/package.json'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import { useOptionsStore } from '~/stores/options'

const section = ref<'info' | 'deps'>('info')
const optionsStore = useOptionsStore()

const infoOptions = [
  { name: 'name', label: 'Name', value: info.name },
  { name: 'version', label: 'Version', value: info.version },
]

const depsOptions = Object.entries({
  ...info.dependencies,
  ...info.devDependencies,
}).map(([key, value]) => ({ name: key, label: key, value }))

watchEffect(() => {
  optionsStore.setOptions(section.value === 'info' ? infoOptions : depsOptions)
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower0: {
      label: 'Info',
      action: () => {
        section.value = 'info'
      },
    },
    lower1: {
      label: 'Deps',
      action: () => {
        section.value = 'deps'
      },
    },
    lower9: useMainButtonConfig(),
  },
})
</script>

<template>
  <div class="flex flex-col gap-y-5">
    <OptionsCard
      v-if="section === 'info'"
      header="Info"
      :options="infoOptions"
    />
    <OptionsCard
      v-if="section === 'deps'"
      header="Dependencies"
      :options="depsOptions"
    />
  </div>
</template>
