import type { Control, ControlName } from '~/types/controls'
import type { ButtonName } from '~/types/buttons'
import { useControlsStore } from '~/stores/controls'
import { useScreenStore } from '~/stores/screen'
import type { ScreenName } from '~/types/screen'

export const useControls = () => {
  const controlsStore = useControlsStore()
  const screenStore = useScreenStore()

  const setScreenAndClear = (screenName: ScreenName) => {
    screenStore.setActiveScreen(screenName)
    controlsStore.clearControls()
  }

  const controlActions: Record<string, () => void> = {
    main: () => {
      screenStore.setActiveScreen('main')
      controlsStore.restoreControls('main')
    },
    version: () => setScreenAndClear('version'),
    logs: () => setScreenAndClear('logs'),
    settings: () => setScreenAndClear('settings'),
  }

  const triggerControl = (
    property: keyof Control,
    value: ControlName | ButtonName
  ) => {
    const control = controlsStore.controls.find(c => c[property] === value)

    if (!!control) {
      const action = controlActions[control.name]

      action?.()
    }
  }

  return { triggerControl }
}
