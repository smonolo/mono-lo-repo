<script setup lang="ts">
defineComponent({ name: 'SettingsScreen' })

const screenStore = useScreenStore()

const screenSettings = computed(() => ({
  brightness: {
    label: 'Brightness',
    value: screenStore.brightness,
  },
  theme: {
    label: 'Contrast',
    value: screenStore.theme,
  },
}))
</script>

<template>
  <div class="p-10">
    <span class="px-2 font-bold tracking-wide">Display</span>
    <div
      v-for="(content, _, index) in screenSettings"
      :key="index"
      class="mt-2 flex justify-between border border-slate-950 px-2 py-1 dark:border-slate-100"
    >
      <span>{{ content.label }}</span>
      <span>{{ content.value }}</span>
    </div>
  </div>
</template>
