import type { LowerButtonConfig } from '~/types/buttons'

export const useMainButtonConfig = (): LowerButtonConfig => {
  const { setActiveScreen } = useScreenStore()

  return {
    label: 'Main',
    onClick: () => setActiveScreen('main'),
  }
}
