import type { LowerButtonConfig } from '~/types/buttons'
import { useScreenStore } from '~/stores/screen'
import { useOptionsStore } from '~/stores/options'

export const useMainButtonConfig = (): LowerButtonConfig => {
  const { setActiveScreen } = useScreenStore()
  const { clearOptions } = useOptionsStore()

  return {
    label: 'Main',
    onClick: () => {
      clearOptions()
      setActiveScreen('main')
    },
  }
}
