import type { Button, ButtonName } from '~/types/buttons'
import { useScreenStore } from '~/stores/screen'
import { useOptionsStore } from '~/stores/options'

const lowerButtons: Button[] = Array.from({ length: 10 }, (_, i) => ({
  name: `lo-${i}`,
  label: `${i}`,
}))

export const useButtons = () => {
  const screenStore = useScreenStore()
  const optionsStore = useOptionsStore()

  const upperButtons: Button[] = [
    {
      name: 'up-bri-aus',
      icon: 'bi bi-sun',
      label: 'aus',
      action: screenStore.increaseBrightness,
    },
    {
      name: 'up-empty-1',
      action: () => screenStore.toggleFunctionOnDisplay('Empty'),
    },
    { name: 'up-i', label: 'i' },
    { name: 'up-st', label: 'St', big: true },
    { name: 'up-v-gt-0', label: 'V>0' },
    { name: 'up-v-eq-0', label: 'V=0' },
    {
      name: 'up-bri',
      icon: 'bi bi-sun',
      action: screenStore.increaseBrightness,
    },
    {
      name: 'up-con',
      icon: 'bi bi-circle-half',
      action: screenStore.toggleContrast,
    },
    { name: 'up-ud', label: 'UD' },
  ]

  const sideButtons: Button[] = [
    { name: 'si-c', label: 'C' },
    {
      name: 'si-up',
      icon: 'bi bi-caret-up-fill',
      iconClass: 'text-2xl',
      action: optionsStore.previousOption,
    },
    {
      name: 'si-do',
      icon: 'bi bi-caret-down-fill',
      iconClass: 'text-2xl',
      action: optionsStore.nextOption,
    },
    { name: 'si-e', label: 'E', big: true, action: optionsStore.triggerOption },
    { name: 'si-dot', icon: 'bi bi-circle-fill', iconClass: 'text-sm' },
  ]

  const triggerButton = (name: ButtonName) => {
    const buttons = [...upperButtons, ...lowerButtons, ...sideButtons]
    const action = buttons.find(button => button.name === name)?.action

    action?.()
  }

  return { upperButtons, lowerButtons, sideButtons, triggerButton }
}
