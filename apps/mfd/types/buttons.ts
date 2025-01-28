export type ButtonsType = 'functions' | 'controls'
export type ButtonsDirection = 'horizontal' | 'vertical'

export type UpperButtonName = `upper${string}`
export type UpperButtonActions = {
  onBriAusClick: () => void
  onEmpty1Click: () => void
  onIClick: () => void
  onStClick: () => void
  onVGt0Click: () => void
  onVEq0Click: () => void
  onBriClick: () => void
  onConClick: () => void
  onUDClick: () => void
}

export type LowerButtonName = `lower${number}`
export type LowerButtonConfig = { label: string; onClick: () => void }
export type LowerButtonActions = Partial<
  Record<LowerButtonName, LowerButtonConfig>
>

export type SideButtonName = `side${string}`
export type SideButtonActions = {
  onCClick: () => void
  onUpClick: () => void
  onDoClick: () => void
  onEClick: () => void
  onDotClick: () => void
}

export type ButtonName = LowerButtonName | UpperButtonName | SideButtonName

export type Button = {
  name: ButtonName
  label?: string
  screenLabel?: string
  icon?: string
  iconClass?: string
  big?: boolean
  action?: () => void
}

export type ScreenComponent = {
  upperButtonActions?: UpperButtonActions
  sideButtonActions?: SideButtonActions
  lowerButtonActions?: LowerButtonActions
}
