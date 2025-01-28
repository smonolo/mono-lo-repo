<script setup lang="ts">
import Button from '~/components/buttons/button.vue'
import type {
  Button as ButtonType,
  ButtonsDirection,
  ButtonsType,
} from '~/types/buttons'

type Props = {
  type?: ButtonsType
  direction?: ButtonsDirection
  buttons?: ButtonType[]
}

defineComponent({ name: 'Buttons' })

withDefaults(defineProps<Props>(), {
  type: 'functions',
  direction: 'horizontal',
  buttons: () => [],
})
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
    />
  </div>
</template>
