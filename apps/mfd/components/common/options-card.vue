<script setup lang="ts">
import { useOptionsStore } from '~/stores/options'

type Props = {
  header: string
  options: Record<string, { label: string; value: string | number }>
}

defineComponent({ name: 'OptionsCard' })

defineProps<Props>()

const optionsStore = useOptionsStore()
</script>

<template>
  <div class="border border-slate-950 dark:border-slate-100">
    <div class="border-b border-slate-950 p-2 dark:border-slate-100">
      <span class="font-bold tracking-wide">{{ header }}</span>
    </div>
    <div class="p-1">
      <div
        v-for="(value, key, index) in options"
        :key="index"
        class="flex justify-between p-1"
        :class="{
          'bg-slate-950 text-slate-100 dark:bg-slate-100 dark:text-slate-950':
            optionsStore.selectedOption === key,
        }"
      >
        <span>{{ value.label }}</span>
        <span>{{ value.value }}</span>
      </div>
    </div>
  </div>
</template>
