<script setup lang="ts">
import ScreenTime from '~/components/screen/time.vue'
import ScreenControls from '~/components/screen/controls.vue'
import ScreenFunctions from '~/components/screen/functions.vue'

defineComponent({ name: 'Screen' })
</script>

<template>
  <div class="flex h-full flex-col justify-between text-slate-100">
    <div class="h-[600px] p-2">
      <div class="flex h-full justify-between">
        <div class="h-full w-full">
          <slot />
        </div>
        <div class="flex w-[200px] flex-col gap-y-4">
          <ScreenTime />
          <ScreenFunctions />
        </div>
      </div>
    </div>
    <ScreenControls />
  </div>
</template>
