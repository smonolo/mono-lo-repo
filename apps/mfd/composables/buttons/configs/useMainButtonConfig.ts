import { useScreenStore } from '~/stores/screen'
import { useOptionsStore } from '~/stores/options'
import type { ButtonConfig } from '~/types/buttons'

export const useMainButtonConfig = (): ButtonConfig => {
  const { setActiveScreen } = useScreenStore()
  const { clearOptions } = useOptionsStore()

  return {
    label: 'Main',
    action: () => {
      clearOptions()
      setActiveScreen('main')
    },
  }
}
