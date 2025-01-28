<script setup lang="ts">
type Props = {
  label?: string
}

defineComponent({ name: 'ScreenControl' })

defineProps<Props>()
</script>

<template>
  <div
    class="flex h-full w-[110px] items-center justify-center break-all border-r border-slate-950 font-bold tracking-wide last:border-none dark:border-slate-100"
  >
    <span v-if="!!label">{{ label }}</span>
  </div>
</template>
