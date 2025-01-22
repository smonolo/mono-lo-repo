import type { Control, ControlName } from '~/types/controls'
import type { ScreenName } from '~/types/screen'

const controlsList: Control[] = [
  { name: 'version', label: 'Ver', slot: 'lo-0' },
  { name: 'settings', label: 'S', slot: 'lo-5' },
  { name: 'main', label: 'Main', slot: 'lo-9' },
]

const controlsConfig: Record<ScreenName, ControlName[]> = {
  main: ['version', 'settings', 'main'],
  version: ['main'],
  settings: ['main'],
}

export const useControlsStore = defineStore('controls', () => {
  const controls = ref<Control[]>([
    ...controlsList.filter(c => controlsConfig.main.includes(c.name)),
  ])

  const setControls = (screenName: ScreenName) => {
    const controlNames = controlsConfig[screenName]

    controls.value = controlsList.filter(c => controlNames.includes(c.name))
  }

  const clearControls = () => {
    controls.value = controls.value.filter(c => c.name === 'main')
  }

  return {
    controls,
    setControls,
    clearControls,
  }
})
