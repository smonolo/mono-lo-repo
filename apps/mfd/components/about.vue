<script setup lang="ts">
import { useOptionsStore } from '~/stores/options'

defineComponent({ name: 'About' })

const optionsStore = useOptionsStore()

const buttons = [
  { name: 'site', label: 'Go to site' },
  { name: 'code', label: 'View source code' },
]
</script>

<template>
  <div class="h-full w-full bg-slate-900 p-10">
    <h1 class="text-2xl font-bold">Multi-Function Display</h1>
    <p class="mt-2">
      This is the simulation of a Bombardier MFD used in some German trains.
    </p>
    <p class="mt-2">You are looking at the secondary display though.</p>
    <p class="mt-8 flex items-center gap-x-1">
      Press
      <span
        class="block h-7 w-7 rounded border border-white bg-black p-0.5 text-xs leading-none"
      >
        UD
      </span>
      to go back to the primary display.
    </p>
    <div class="mt-8 flex gap-x-4">
      <div
        v-for="(button, key) in buttons"
        :key
        class="rounded-lg px-4 py-2"
        :class="
          optionsStore.selectedOption === button.name
            ? 'bg-slate-500'
            : 'bg-slate-700'
        "
      >
        {{ button.label }}
      </div>
    </div>
  </div>
</template>
