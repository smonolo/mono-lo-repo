import type {
  LowerButtonActions,
  SideButtonActions,
  UpperButtonActions,
} from '~/types/buttons'

export type ScreenName = 'main' | 'version' | 'settings'
export type ScreenTheme = 'light' | 'dark'
export type ScreenDisplay = 'primary' | 'about'

export type ScreenConfig = {
  upperButtonActions?: UpperButtonActions
  sideButtonActions?: SideButtonActions
  lowerButtonActions?: LowerButtonActions
}
