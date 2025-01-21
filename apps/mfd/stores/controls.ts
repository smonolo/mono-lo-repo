import type { ButtonName } from '~/types/buttons'
import type { Control, ControlName, ControlsConfig } from '~/types/controls'

const defaultControls: Control[] = [
  { name: 'journey', label: 'Jo', slot: 'lo-1' },
  { name: 'chaos', label: 'Chaos', slot: 'lo-2' },
  { name: 'attempts', label: 'At', slot: 'lo-3' },
  { name: 'main', label: 'Main', slot: 'lo-0' },
]

const controlsConfig: Record<ControlsConfig, ControlName[]> = {
  main: ['journey', 'chaos', 'attempts'],
}

export const useControlsStore = defineStore('controls', () => {
  const controls = ref<Control[]>([...defaultControls])

  const addControl = (control: Control) => {
    const exists = controls.value.some(
      c => c.name === control.name || c.slot === control.slot
    )

    if (!exists) {
      controls.value.push(control)
    }
  }

  const restoreControls = (config: ControlsConfig) => {
    const controlNames = controlsConfig[config]

    controls.value = defaultControls.filter(c => controlNames.includes(c.name))
  }

  const removeControl = (
    property: keyof Control,
    value: ControlName | ButtonName
  ) => {
    controls.value = controls.value.filter(c => c[property] !== value)
  }

  const clearControls = () => {
    controls.value = controls.value.filter(c => c.name === 'main')
  }

  return {
    controls,
    addControl,
    restoreControls,
    removeControl,
    clearControls,
  }
})
