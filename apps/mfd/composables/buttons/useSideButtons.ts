import type { Button } from '~/types/buttons'
import type { ScreenConfig } from '~/types/screen'

export const useSideButtons = (
  screen: Ref<ScreenConfig | null>
): ComputedRef<Button[]> => {
  return computed(() => [
    {
      name: 'sideC',
      label: 'C',
      action: screen.value?.sideButtonActions?.sideC?.action,
    },
    {
      name: 'sideUp',
      icon: 'bi bi-caret-up-fill',
      iconClass: 'text-2xl',
      action: screen.value?.sideButtonActions?.sideUp?.action,
    },
    {
      name: 'sideDo',
      icon: 'bi bi-caret-down-fill',
      iconClass: 'text-2xl',
      action: screen.value?.sideButtonActions?.sideDo?.action,
    },
    {
      name: 'sideE',
      label: 'E',
      big: true,
      action: screen.value?.sideButtonActions?.sideE?.action,
    },
    {
      name: 'sideDot',
      icon: 'bi bi-circle-fill',
      iconClass: 'text-sm',
      action: screen.value?.sideButtonActions?.sideDot?.action,
    },
  ])
}
