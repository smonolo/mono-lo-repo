export type ButtonsType = 'functions' | 'controls'

export type ButtonsDirection = 'horizontal' | 'vertical'

export type ButtonName =
  | 'up-bri-aus'
  | 'up-empty-1'
  | 'up-i'
  | 'up-st'
  | 'up-v-gt-0'
  | 'up-v-eq-0'
  | 'up-bri'
  | 'up-con'
  | 'up-ud'
  | `lo-${number}`
  | 'si-c'
  | 'si-up'
  | 'si-do'
  | 'si-e'
  | 'si-dot'

export type Button = {
  name: ButtonName
  label?: string
  icon?: string
  iconClass?: string
  big?: boolean
  action?: () => void
}
