import type { Component } from 'vue'

export const useScreens = () => {
  const screenModules = import.meta.glob<{ default: Component }>(
    '~/components/screens/*.vue',
    { eager: true }
  )

  const screensConfig = Object.entries(screenModules).reduce(
    (acc, [path, module]) => {
      const name = path.split('/').pop()?.replace('.vue', '')

      if (name) {
        acc[name] = module.default
      }

      return acc
    },
    {} as Record<string, Component>
  )

  return { screensConfig }
}
