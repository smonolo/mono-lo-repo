import type { ButtonName } from '~/types/buttons'

export type ControlName = 'version' | 'logs' | 'main' | 'settings'

export type Control = {
  name: ControlName
  label: string
  slot: ButtonName
}

export type ControlsConfig = 'main'
