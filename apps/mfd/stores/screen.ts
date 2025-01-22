import type { ScreenTheme, ScreenName } from '~/types/screen'

export const useScreenStore = defineStore('screen', () => {
  const activeScreen = ref<ScreenName>('main')
  const functionsOnDisplay = ref<string[]>([])
  const brightness = ref<number>(1)
  const theme = ref<ScreenTheme>('dark')

  const setActiveScreen = (screen: ScreenName) => {
    activeScreen.value = screen
  }

  const toggleFunctionOnDisplay = (functionName: string) => {
    if (!functionsOnDisplay.value.includes(functionName)) {
      functionsOnDisplay.value.push(functionName)
    } else {
      functionsOnDisplay.value = functionsOnDisplay.value.filter(
        name => name !== functionName
      )
    }
  }

  const increaseBrightness = () => {
    brightness.value = brightness.value >= 1 ? 0.2 : brightness.value + 0.2
  }

  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  return {
    activeScreen,
    setActiveScreen,
    functionsOnDisplay,
    toggleFunctionOnDisplay,
    brightness,
    increaseBrightness,
    theme,
    toggleTheme,
  }
})
