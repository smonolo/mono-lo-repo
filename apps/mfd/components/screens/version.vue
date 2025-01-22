<script setup lang="ts">
import info from '~/package.json'

defineComponent({ name: 'VersionScreen' })

const deps = {
  ...info.dependencies,
  ...info.devDependencies,
}
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>Version</span>
    </div>
    <div class="p-10">
      <div>
        <p class="uppercase">{{ info.name }} {{ info.version }}</p>
        <p>Contact: {{ info.author }}</p>
      </div>
      <div class="mt-5 border border-slate-950 dark:border-slate-100">
        <div class="border-b border-slate-950 p-2 dark:border-slate-100">
          <span class="font-bold tracking-wide">Dependencies</span>
        </div>
        <div class="p-2">
          <div v-for="(value, key) in deps" :key class="flex justify-between">
            <p>{{ key }}</p>
            <p>{{ value }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
