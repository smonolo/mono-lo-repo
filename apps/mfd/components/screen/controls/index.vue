<script setup lang="ts">
import ScreenControl from '~/components/screen/controls/control.vue'
import type { Button } from '~/types/buttons'

type Props = {
  lowerButtons: Button[]
}

defineProps<Props>()
</script>

<template>
  <div class="flex h-[80px] border-t border-slate-950 dark:border-slate-100">
    <ScreenControl
      v-for="(button, key) in lowerButtons"
      :key
      :label="button.screenLabel"
    />
  </div>
</template>
