<script setup lang="ts">
import Button from '~/components/buttons/button.vue'
import type {
  ButtonName,
  Button as ButtonType,
  ButtonsDirection,
  ButtonsType,
} from '~/types/buttons'

type Props = {
  type?: ButtonsType
  direction?: ButtonsDirection
  buttons?: ButtonType[]
}

type Emits = {
  trigger: [name: ButtonName]
}

defineComponent({ name: 'Buttons' })

withDefaults(defineProps<Props>(), {
  type: 'functions',
  direction: 'horizontal',
  buttons: () => [],
})

defineEmits<Emits>()
</script>

<template>
  <div
    class="flex w-full"
    :class="
      direction === 'vertical'
        ? 'flex-col items-center justify-center gap-y-3'
        : 'flex-row justify-between'
    "
  >
    <Button
      v-for="(button, key) in buttons"
      :key
      :direction="direction"
      :button="button"
      @trigger="$emit('trigger', button.name)"
    />
  </div>
</template>
