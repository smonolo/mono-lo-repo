export type ButtonsType = 'functions' | 'controls'

export type ButtonsDirection = 'horizontal' | 'vertical'

export type ButtonName =
  | LowerButtonName
  | 'up-bri-aus'
  | 'up-empty-1'
  | 'up-i'
  | 'up-st'
  | 'up-v-gt-0'
  | 'up-v-eq-0'
  | 'up-bri'
  | 'up-con'
  | 'up-ud'
  | 'si-c'
  | 'si-up'
  | 'si-do'
  | 'si-e'
  | 'si-dot'

export type Button = {
  name: ButtonName
  label?: string
  screenLabel?: string
  icon?: string
  iconClass?: string
  big?: boolean
  action?: () => void
}

export type UpperButtonActions = {
  onBrisAusClick: () => void
  onEmpty1Click: () => void
  onIClick: () => void
  onStClick: () => void
  onVGt0Click: () => void
  onVEq0Click: () => void
  onBriClick: () => void
  onConClick: () => void
  onUDClick: () => void
}

export type SideButtonActions = {
  onCClick: () => void
  onUpClick: () => void
  onDoClick: () => void
  onEClick: () => void
  onDotClick: () => void
}

type LowerButtonNumber = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
export type LowerButtonName = `lower${LowerButtonNumber}`
export type LowerButtonConfig = { label: string; onClick: () => void }

export type LowerButtonActions = Partial<
  Record<LowerButtonName, LowerButtonConfig>
>

export type ViewComponent = {
  // upperButtonActions: UpperButtonActions // add if linking specific view actions to upper buttons is needed
  // sideButtonActions: SideButtonActions // add if linking specific view actions to side buttons is needed
  lowerButtonActions: LowerButtonActions
}
