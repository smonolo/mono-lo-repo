<script setup lang="ts">
import type { Control } from '~/types/controls'

type Props = {
  control?: Control
}

defineComponent({ name: 'ScreenControl' })

defineProps<Props>()
</script>

<template>
  <div
    class="flex h-full w-[110px] items-center justify-center break-all border-r border-slate-100 font-bold tracking-wide last:border-none"
  >
    <span v-if="!!control">{{ control.label }}</span>
  </div>
</template>
