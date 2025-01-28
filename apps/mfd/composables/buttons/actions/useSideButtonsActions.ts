import { useOptionsStore } from '~/stores/options'
import type { SideButtonActions } from '~/types/buttons'

export const useSideButtonsActions = (): SideButtonActions => {
  const { changeOption, triggerOption } = useOptionsStore()

  return {
    sideUp: { action: () => changeOption('previous') },
    sideDo: { action: () => changeOption('next') },
    sideE: { action: triggerOption },
  }
}
