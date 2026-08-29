import type { ScreenTheme, ScreenName, ScreenDisplay } from '~/types/screen'
import { useOptionsStore } from '~/stores/options'
import { useAuthStore } from '~/stores/auth'

export const useScreenStore = defineStore('screen', () => {
  const activeScreen = ref<ScreenName>('main')
  const brightness = ref<number>(1)
  const contrast = ref<ScreenTheme>('dark')
  const display = ref<ScreenDisplay>('primary')

  const setActiveScreen = (screen: ScreenName) => {
    const optionsStore = useOptionsStore()
    optionsStore.clearOptions()

    const authStore = useAuthStore()
    if (screen !== 'main' && !authStore.hasScreenPermission(screen)) {
      activeScreen.value = 'main'
      return
    }

    activeScreen.value = screen
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
    brightness,
    increaseBrightness,
    contrast,
    toggleContrast,
    display,
    switchDisplay,
  }
})
