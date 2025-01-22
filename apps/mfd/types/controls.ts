import type { ButtonName } from '~/types/buttons'

export type ControlName = 'version' | 'logs' | 'main'

export type Control = {
  name: ControlName
  label: string
  slot: ButtonName
}

export type ControlsConfig = 'main'
