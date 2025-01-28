import type { Button, LowerButtonName } from '~/types/buttons'
import type { ScreenConfig } from '~/types/screen'

export const useLowerButtons = (
  screen: Ref<ScreenConfig | null>
): ComputedRef<Button[]> => {
  const buttonSkeletons = Array.from({ length: 10 }, (_, i) => ({
    name: `lower${i}` as LowerButtonName,
    label: i.toString(),
  }))

  return computed(() =>
    buttonSkeletons.map(s => ({
      ...s,
      screenLabel: screen.value?.lowerButtonActions?.[s.name]?.label,
      action: screen.value?.lowerButtonActions?.[s.name]?.action,
    }))
  )
}
