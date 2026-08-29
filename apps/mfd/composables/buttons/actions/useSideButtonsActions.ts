import { useOptionsStore } from '~/stores/options'
import type { SideButtonActions } from '~/types/buttons'

export const useSideButtonsActions = (): SideButtonActions => {
  const optionsStore = useOptionsStore()

  const scrollScreen = (direction: 'up' | 'down') => {
    if (typeof document === 'undefined') return
    const container = document.getElementById('screen-viewport')
    if (container) {
      const scrollStep = 120
      container.scrollBy({
        top: direction === 'up' ? -scrollStep : scrollStep,
        behavior: 'smooth',
      })
    }
  }

  return {
    sideUp: {
      action: () => {
        if (optionsStore.options.length > 0) {
          optionsStore.changeOption('previous')
        } else {
          scrollScreen('up')
        }
      },
    },
    sideDo: {
      action: () => {
        if (optionsStore.options.length > 0) {
          optionsStore.changeOption('next')
        } else {
          scrollScreen('down')
        }
      },
    },
    sideE: { action: optionsStore.triggerOption },
  }
}
