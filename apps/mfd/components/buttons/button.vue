<script setup lang="ts">
import type { Button, ButtonsDirection } from '~/types/buttons'
import type { ButtonName } from '~/types/buttons'

type Props = {
  direction: ButtonsDirection
  button: Button
}

type Emits = {
  trigger: [name: ButtonName]
}

defineComponent({ name: 'Button' })

const props = defineProps<Props>()

defineEmits<Emits>()

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
    @click="$emit('trigger', button.name)"
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
