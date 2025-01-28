<script setup lang="ts">
import info from '~/package.json'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import { useUpperButtonsActions } from '~/composables/buttons/actions/useUpperButtonsActions'
import { useSideButtonsActions } from '~/composables/buttons/actions/useSideButtonsActions'

defineComponent({ name: 'VersionScreen' })

defineExpose<ScreenConfig>({
  upperButtonActions: useUpperButtonsActions(),
  lowerButtonActions: { lower9: useMainButtonConfig() },
  sideButtonActions: useSideButtonsActions(),
})

const infoOptions = {
  name: { label: 'Name', value: info.name },
  version: { label: 'Version', value: info.version },
}

const depsOptions = Object.fromEntries(
  Object.entries({
    ...info.dependencies,
    ...info.devDependencies,
  }).map(([key, value]) => [key, { label: key, value }])
)
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>Version</span>
    </div>
    <div class="flex flex-col gap-y-5 p-10">
      <OptionsCard header="Info" :options="infoOptions" />
      <OptionsCard header="Dependencies" :options="depsOptions" />
    </div>
  </div>
</template>
