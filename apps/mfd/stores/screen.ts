import type { ScreenTheme, ScreenName, ScreenDisplay } from '~/types/screen'

export const useScreenStore = defineStore('screen', () => {
  const activeScreen = ref<ScreenName>('main')
  const functionsOnDisplay = ref<string[]>([])
  const brightness = ref<number>(1)
  const contrast = ref<ScreenTheme>('dark')
  const display = ref<ScreenDisplay>('primary')

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

  const toggleContrast = () => {
    contrast.value = contrast.value === 'light' ? 'dark' : 'light'
  }

  const switchDisplay = () => {
    display.value = display.value === 'primary' ? 'about' : 'primary'
  }

  return {
    activeScreen,
    setActiveScreen,
    functionsOnDisplay,
    toggleFunctionOnDisplay,
    brightness,
    increaseBrightness,
    contrast,
    toggleContrast,
    display,
    switchDisplay,
  }
})
