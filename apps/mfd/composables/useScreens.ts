import type { Component } from 'vue'
import type { ScreenName } from '~/types/screen'
import MainScreen from '~/components/screens/main.vue'
import VersionScreen from '~/components/screens/version.vue'
import SettingsScreen from '~/components/screens/settings.vue'

export const useScreens = () => {
  const screensConfig: Record<ScreenName, Component> = {
    main: MainScreen,
    version: VersionScreen,
    settings: SettingsScreen,
  }

  return { screensConfig }
}
