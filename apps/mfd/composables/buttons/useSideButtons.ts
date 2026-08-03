import type { Button } from '~/types/buttons'
import type { ScreenConfig } from '~/types/screen'
import { useSideButtonsActions } from '~/composables/buttons/actions/useSideButtonsActions'

export const useSideButtons = (
  screen: Ref<ScreenConfig | null>
): ComputedRef<Button[]> => {
  const defaultActions = useSideButtonsActions()

  return computed(() => [
    {
      name: 'sideC',
      label: 'C',
      action:
        screen.value?.sideButtonActions?.sideC?.action ??
        defaultActions.sideC?.action,
    },
    {
      name: 'sideUp',
      icon: 'bi bi-caret-up-fill',
      iconClass: 'text-2xl',
      action:
        screen.value?.sideButtonActions?.sideUp?.action ??
        defaultActions.sideUp?.action,
    },
    {
      name: 'sideDo',
      icon: 'bi bi-caret-down-fill',
      iconClass: 'text-2xl',
      action:
        screen.value?.sideButtonActions?.sideDo?.action ??
        defaultActions.sideDo?.action,
    },
    {
      name: 'sideE',
      label: 'E',
      big: true,
      action:
        screen.value?.sideButtonActions?.sideE?.action ??
        defaultActions.sideE?.action,
    },
    {
      name: 'sideDot',
      icon: 'bi bi-circle-fill',
      iconClass: 'text-sm',
      action:
        screen.value?.sideButtonActions?.sideDot?.action ??
        defaultActions.sideDot?.action,
    },
  ])
}
