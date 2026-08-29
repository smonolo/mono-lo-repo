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
    if (newSelected && itemRefs.value[newSelected]) {
      nextTick(() => {
        itemRefs.value[newSelected]?.scrollIntoView({
          block: 'nearest',
          behavior: 'smooth',
        })
      })
    }
  },
  { flush: 'post' }
)
</script>

<template>
  <div class="border border-slate-950 dark:border-slate-100">
    <div class="flex justify-between items-center border-b border-slate-950 p-2 dark:border-slate-100">
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
        <span>{{ option.label }}</span>
        <span>{{ option.value }}</span>
      </div>
    </div>
  </div>
</template>
