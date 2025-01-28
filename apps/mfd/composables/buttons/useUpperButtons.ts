import type { Button } from '~/types/buttons'
import { useScreenStore } from '~/stores/screen'

export const useUpperButtons = () => {
  const screenStore = useScreenStore()

  const buttonSkeletons: Button[] = [
    {
      name: 'upperBriSun',
      icon: 'bi bi-sun',
      label: 'aus',
      action: screenStore.increaseBrightness,
    },
    {
      name: 'upperEmpty1',
      action: () => screenStore.toggleFunctionOnDisplay('Empty'),
    },
    { name: 'upperI', label: 'i' },
    { name: 'upperSt', label: 'St', big: true },
    { name: 'upperVGt0', label: 'V>0' },
    { name: 'upperVEq0', label: 'V=0' },
    {
      name: 'upperBri',
      icon: 'bi bi-sun',
      action: screenStore.increaseBrightness,
    },
    {
      name: 'upperCon',
      icon: 'bi bi-circle-half',
      action: screenStore.toggleContrast,
    },
    { name: 'upperUd', label: 'UD' },
  ]

  return buttonSkeletons.map(sk => ({
    ...sk,
    screenLabel: sk.label,
    action: sk.action,
  }))
}
