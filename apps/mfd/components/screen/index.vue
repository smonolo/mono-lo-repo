<script setup lang="ts">
import ScreenTime from '~/components/screen/time.vue'
import ScreenControls from '~/components/screen/controls.vue'

defineComponent({ name: 'Screen' })
</script>

<template>
  <div class="flex h-full flex-col justify-between text-slate-100">
    <div class="h-[600px] p-2">
      <div class="flex h-full justify-between">
        <div class="h-full w-full">
          <slot />
        </div>
        <ScreenTime />
      </div>
    </div>
    <ScreenControls />
  </div>
</template>
