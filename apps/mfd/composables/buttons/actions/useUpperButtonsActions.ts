import { useScreenStore } from '~/stores/screen'
import type { UpperButtonActions } from '~/types/buttons'

export const useUpperButtonsActions = (): UpperButtonActions => {
  const { increaseBrightness, toggleContrast } = useScreenStore()

  return {
    upperBriSun: { action: increaseBrightness },
    upperBri: { action: increaseBrightness },
    upperCon: { action: toggleContrast },
  }
}
