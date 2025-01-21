<script setup lang="ts">
import Button from '~/components/buttons/button.vue'
import type {
  Button as ButtonType,
  ButtonName,
  ButtonsDirection,
  ButtonsType,
} from '~/types/buttons'

type Props = {
  type?: ButtonsType
  direction?: ButtonsDirection
  buttons?: ButtonType[]
}

defineComponent({ name: 'Buttons' })

const props = withDefaults(defineProps<Props>(), {
  type: 'functions',
  direction: 'horizontal',
  buttons: () => [],
})

const { triggerControl } = useControls()
const { triggerButton } = useButtons()

const trigger = (name: ButtonName) => {
  if (props.type === 'controls') {
    triggerControl('slot', name)
  } else {
    triggerButton(name)
  }
}
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
      @trigger="trigger"
    />
  </div>
</template>
