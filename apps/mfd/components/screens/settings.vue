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
    <div class="border border-slate-950 dark:border-slate-100">
      <div class="border-b border-slate-950 p-2 dark:border-slate-100">
        <span class="font-bold tracking-wide">Display</span>
      </div>
      <div class="p-2">
        <div
          v-for="(content, _, index) in screenSettings"
          :key="index"
          class="flex justify-between"
        >
          <span>{{ content.label }}</span>
          <span>{{ content.value }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
