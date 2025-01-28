export type ButtonsType = 'functions' | 'controls'
export type ButtonsDirection = 'horizontal' | 'vertical'

export type UpperButtonName = `upper${string}`
export type UpperButtonActions = Partial<Record<UpperButtonName, ButtonConfig>>

export type LowerButtonName = `lower${number}`
export type LowerButtonActions = Partial<Record<LowerButtonName, ButtonConfig>>

export type SideButtonName = `side${string}`
export type SideButtonActions = Partial<Record<SideButtonName, ButtonConfig>>

export type ButtonName = LowerButtonName | UpperButtonName | SideButtonName

export type ButtonConfig = {
  label?: string
  action: () => void
}

export type Button = Partial<ButtonConfig> & {
  name: ButtonName
  screenLabel?: string
  icon?: string
  iconClass?: string
  big?: boolean
}
