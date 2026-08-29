import { useScreenStore } from '~/stores/screen'
import type { UpperButtonActions } from '~/types/buttons'

export const useUpperButtonsActions = (): UpperButtonActions => {
  const { increaseBrightness, toggleContrast, setActiveScreen } =
    useScreenStore()

  return {
    upperBriSun: { action: increaseBrightness },
    upperI: { action: () => setActiveScreen('version') },
    upperBri: { action: increaseBrightness },
    upperCon: { action: toggleContrast },
  }
}
