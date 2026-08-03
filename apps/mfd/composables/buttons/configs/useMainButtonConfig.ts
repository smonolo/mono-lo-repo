import { useScreenStore } from '~/stores/screen'
import type { ButtonConfig } from '~/types/buttons'

export const useMainButtonConfig = (): ButtonConfig => {
  const { setActiveScreen } = useScreenStore()

  return {
    label: 'Main',
    action: () => {
      setActiveScreen('main')
    },
  }
}
