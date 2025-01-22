import type { ButtonName } from '~/types/buttons'

export type ControlName = 'version' | 'main' | 'settings'

export type Control = {
  name: ControlName
  label: string
  slot: ButtonName
}
