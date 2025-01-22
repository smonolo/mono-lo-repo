import type { Control, ControlName } from '~/types/controls'
import type { ButtonName } from '~/types/buttons'
import { useControlsStore } from '~/stores/controls'
import { useScreenStore } from '~/stores/screen'
import type { ScreenName } from '~/types/screen'
import { useOptionsStore } from '~/stores/options'

export const useControls = () => {
  const controlsStore = useControlsStore()
  const screenStore = useScreenStore()
  const optionsStore = useOptionsStore()

  const setScreen = (screenName: ScreenName) => {
    screenStore.setActiveScreen(screenName)
    controlsStore.setControls(screenName)
    optionsStore.clearOptions()
  }

  const controlActions: Record<string, () => void> = {
    main: () => setScreen('main'),
    version: () => setScreen('version'),
    settings: () => {
      setScreen('settings')
      optionsStore.setOptions([
        { name: 'brightness', action: screenStore.increaseBrightness },
        { name: 'theme', action: screenStore.toggleTheme },
      ])
    },
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
