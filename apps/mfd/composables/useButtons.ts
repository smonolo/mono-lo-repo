import type { Button, ButtonName } from '~/types/buttons'
import { useScreenStore } from '~/stores/screen'

export const useButtons = () => {
  const screenStore = useScreenStore()

  const upperButtons: Button[] = [
    { name: 'up-bri-aus', icon: 'bi bi-sun', label: 'aus' },
    {
      name: 'up-empty-1',
      action: () => screenStore.toggleFunctionOnDisplay('Empty'),
    },
    { name: 'up-i', label: 'i' },
    { name: 'up-st', label: 'St', big: true },
    { name: 'up-v-gt-0', label: 'V>0' },
    { name: 'up-v-eq-0', label: 'V=0' },
    { name: 'up-bri', icon: 'bi bi-sun' },
    { name: 'up-con', icon: 'bi bi-circle-half' },
    { name: 'up-ud', label: 'UD' },
  ]

  const lowerButtons: Button[] = [
    { name: 'lo-1', label: '1' },
    { name: 'lo-2', label: '2' },
    { name: 'lo-3', label: '3' },
    { name: 'lo-4', label: '4' },
    { name: 'lo-5', label: '5' },
    { name: 'lo-6', label: '6' },
    { name: 'lo-7', label: '7' },
    { name: 'lo-8', label: '8' },
    { name: 'lo-9', label: '9' },
    { name: 'lo-0', label: '0' },
  ]

  const sideButtons: Button[] = [
    { name: 'si-c', label: 'C' },
    { name: 'si-up', icon: 'bi bi-caret-up-fill', iconClass: 'text-2xl' },
    { name: 'si-do', icon: 'bi bi-caret-down-fill', iconClass: 'text-2xl' },
    { name: 'si-e', label: 'E', big: true },
    { name: 'si-dot', icon: 'bi bi-circle-fill', iconClass: 'text-base' },
  ]

  const triggerButton = (name: ButtonName) => {
    const buttons = [...upperButtons, ...lowerButtons, ...sideButtons]
    const action = buttons.find(button => button.name === name)?.action

    action?.()
  }

  return { upperButtons, lowerButtons, sideButtons, triggerButton }
}
