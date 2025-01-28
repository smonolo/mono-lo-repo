import type { Button } from '~/types/buttons'
import { useOptionsStore } from '~/stores/options'

export const useSideButtons = () => {
  const optionsStore = useOptionsStore()

  const buttonSkeletons: Button[] = [
    { name: 'sideC', label: 'C' },
    {
      name: 'sideUp',
      icon: 'bi bi-caret-up-fill',
      iconClass: 'text-2xl',
      action: () => optionsStore.changeOption('previous'),
    },
    {
      name: 'sideDo',
      icon: 'bi bi-caret-down-fill',
      iconClass: 'text-2xl',
      action: () => optionsStore.changeOption('next'),
    },
    {
      name: 'sideE',
      label: 'E',
      big: true,
      action: optionsStore.triggerOption,
    },
    { name: 'sideDot', icon: 'bi bi-circle-fill', iconClass: 'text-sm' },
  ]

  return buttonSkeletons.map(sk => ({
    ...sk,
    screenLabel: sk.label,
    action: sk.action,
  }))
}
