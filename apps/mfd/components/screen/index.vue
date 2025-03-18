<script setup lang="ts">
import ScreenClock from '~/components/screen/clock.vue'
import ScreenControls from '~/components/screen/controls/index.vue'
import { useScreenStore } from '~/stores/screen'
import type { Button } from '~/types/buttons'

type Props = {
  lowerButtons: Button[]
}

defineComponent({ name: 'Screen' })

defineProps<Props>()

const screenStore = useScreenStore()

const htmlAttrs = computed(() => ({ class: screenStore.contrast }))

useHead(() => ({ htmlAttrs: htmlAttrs.value }))
</script>

<template>
  <div
    class="flex h-full flex-col justify-between bg-slate-100 text-slate-950 dark:bg-slate-900 dark:text-slate-100"
    :style="{ opacity: screenStore.brightness }"
  >
    <div class="h-[600px] overflow-hidden p-2">
      <div class="flex h-full justify-between">
        <div class="h-full w-full overflow-auto">
          <slot />
        </div>
        <div class="flex w-[200px] flex-col gap-y-4">
          <ScreenClock />
        </div>
      </div>
    </div>
    <ScreenControls :lowerButtons />
  </div>
</template>
