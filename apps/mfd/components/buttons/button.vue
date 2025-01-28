<script setup lang="ts">
import type { Button, ButtonsDirection } from '~/types/buttons'

type Props = {
  direction: ButtonsDirection
  button: Button
}

defineComponent({ name: 'Button' })

const props = defineProps<Props>()

const getButtonSize = (button: Button) => {
  if (props.direction === 'vertical') {
    return button.big ? 'h-[160px] w-[80px]' : 'h-[80px] w-[80px]'
  }

  return button.big ? 'h-[80px] w-[160px]' : 'h-[80px] w-[80px]'
}
</script>

<template>
  <div
    class="flex cursor-pointer flex-col justify-between break-all rounded-lg border-2 border-white p-1.5 text-xl leading-none"
    :class="getButtonSize(button)"
    @click="button.action?.()"
  >
    <i v-if="!!button.icon" :class="[button.icon, button.iconClass]" />
    <span
      v-if="!!button.label"
      :class="!!button.icon ? 'text-right' : 'text-left'"
    >
      {{ button.label }}
    </span>
  </div>
</template>
