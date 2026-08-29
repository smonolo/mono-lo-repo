<script setup lang="ts">
import { useOptionsStore } from '~/stores/options'
import type { Option } from '~/types/options'

type Props = {
  header: string
  options: Option[]
}

const props = defineProps<Props>()
const optionsStore = useOptionsStore()

const itemRefs = ref<Record<string, HTMLElement | null>>({})

const setItemRef = (name: string, el: any) => {
  if (el) {
    itemRefs.value[name] = el as HTMLElement
  }
}

watch(
  () => optionsStore.selectedOption,
  newSelected => {
    if (!newSelected) return

    nextTick(() => {
      // If we are at the very first option across the entire screen, scroll all the way to the top
      const isFirstGlobalOption =
        optionsStore.options.length > 0 &&
        optionsStore.options[0].name === newSelected

      const container = document.getElementById('screen-viewport')

      if (isFirstGlobalOption && container) {
        container.scrollTo({
          top: 0,
          behavior: 'smooth',
        })
      } else if (itemRefs.value[newSelected]) {
        itemRefs.value[newSelected]?.scrollIntoView({
          block: 'nearest',
          behavior: 'smooth',
        })
      }
    })
  },
  { flush: 'post' }
)
</script>

<template>
  <div class="border border-slate-950 dark:border-slate-100">
    <div
      class="flex items-center justify-between border-b border-slate-950 p-2 dark:border-slate-100"
    >
      <span class="font-bold tracking-wide">{{ header }}</span>
    </div>
    <div class="p-1">
      <div
        v-for="(option, index) in options"
        :key="index"
        :ref="el => setItemRef(option.name, el)"
        class="flex justify-between p-1 transition-colors"
        :class="{
          'bg-slate-950 text-slate-100 dark:bg-slate-100 dark:text-slate-950':
            optionsStore.selectedOption === option.name,
        }"
      >
        <span
          :style="
            optionsStore.selectedOption === option.name
              ? {}
              : option.color
                ? { color: option.color }
                : {}
          "
        >
          {{ option.label }}
        </span>
        <span
          v-if="
            option.value !== undefined &&
            option.value !== null &&
            option.value !== ''
          "
        >
          {{ option.value }}
        </span>
      </div>
    </div>
  </div>
</template>
