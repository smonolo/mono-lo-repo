import type { ScreenName } from '~/types/screen'

export const useScreenStore = defineStore('screen', () => {
  const activeScreen = ref<ScreenName>('main')
  const functionsOnDisplay = ref<string[]>([])

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

  return {
    activeScreen,
    setActiveScreen,
    functionsOnDisplay,
    toggleFunctionOnDisplay,
  }
})
