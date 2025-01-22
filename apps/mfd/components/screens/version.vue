<script setup lang="ts">
import info from '~/package.json'

defineComponent({ name: 'VersionScreen' })
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-100 px-1.5 py-0.5 font-bold tracking-wide"
    >
      <span>Version</span>
    </div>
    <div class="flex w-full items-center justify-center py-10">
      <div>
        <p>MFD v{{ info.version }}</p>
        <p>Contact: {{ info.author }}</p>
      </div>
    </div>
  </div>
</template>
