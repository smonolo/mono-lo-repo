import type { Control, ControlName, ControlsConfig } from '~/types/controls'

const defaultControls: Control[] = [
  { name: 'version', label: 'Ver', slot: 'lo-1' },
  { name: 'logs', label: 'Log', slot: 'lo-2' },
  { name: 'main', label: 'Main', slot: 'lo-0' },
]

const controlsConfig: Record<ControlsConfig, ControlName[]> = {
  main: ['version', 'logs', 'main'],
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

  const removeControls = (key: keyof Control, values: string[]) => {
    controls.value = controls.value.filter(c => !values.includes(c[key]))
  }

  const clearControls = () => {
    controls.value = controls.value.filter(c => c.name === 'main')
  }

  return {
    controls,
    addControl,
    restoreControls,
    removeControls,
    clearControls,
  }
})
