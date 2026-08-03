export type ButtonsType = 'functions' | 'controls'
export type ButtonsDirection = 'horizontal' | 'vertical'

export type UpperButtonName =
  | 'upperBriSun'
  | 'upperEmpty1'
  | 'upperI'
  | 'upperSt'
  | 'upperVGt0'
  | 'upperVEq0'
  | 'upperBri'
  | 'upperCon'
  | 'upperUd'

export type LowerButtonName =
  | 'lower0'
  | 'lower1'
  | 'lower2'
  | 'lower3'
  | 'lower4'
  | 'lower5'
  | 'lower6'
  | 'lower7'
  | 'lower8'
  | 'lower9'

export type SideButtonName = 'sideC' | 'sideUp' | 'sideDo' | 'sideE' | 'sideDot'

export type ButtonName = LowerButtonName | UpperButtonName | SideButtonName

export type ButtonConfig = {
  label?: string
  action: () => void
}

export type UpperButtonActions = Partial<Record<UpperButtonName, ButtonConfig>>
export type LowerButtonActions = Partial<Record<LowerButtonName, ButtonConfig>>
export type SideButtonActions = Partial<Record<SideButtonName, ButtonConfig>>

export type Button = Partial<ButtonConfig> & {
  name: ButtonName
  screenLabel?: string
  icon?: string
  iconClass?: string
  big?: boolean
}
