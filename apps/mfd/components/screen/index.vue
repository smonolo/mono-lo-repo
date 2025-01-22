<script setup lang="ts">
import ScreenTime from '~/components/screen/time.vue'
import ScreenControls from '~/components/screen/controls.vue'
import ScreenFunctions from '~/components/screen/functions.vue'
import { useScreenStore } from '~/stores/screen'

defineComponent({ name: 'Screen' })

const screenStore = useScreenStore()

const htmlAttrs = computed(() => ({
  class: screenStore.theme,
}))

useHead({ htmlAttrs })
</script>

<template>
  <div
    class="flex h-full flex-col justify-between text-slate-950 dark:text-slate-100"
    :style="{ opacity: screenStore.brightness }"
  >
    <div class="h-[600px] p-2">
      <div class="flex h-full justify-between">
        <div class="h-full w-full">
          <slot />
        </div>
        <div class="flex w-[200px] flex-col gap-y-4">
          <ScreenTime />
          <ScreenFunctions />
        </div>
      </div>
    </div>
    <ScreenControls />
  </div>
</template>
