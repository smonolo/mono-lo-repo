import type { Button } from '~/types/buttons'
import type { ScreenConfig } from '~/types/screen'

export const useUpperButtons = (
  screen: Ref<ScreenConfig | null>
): ComputedRef<Button[]> => {
  return computed(() => [
    {
      name: 'upperBriSun',
      icon: 'bi bi-sun',
      label: 'aus',
      action: screen.value?.upperButtonActions?.upperBriSun?.action,
    },
    {
      name: 'upperEmpty1',
      action: screen.value?.upperButtonActions?.upperEmpty1?.action,
    },
    {
      name: 'upperI',
      label: 'i',
      action: screen.value?.upperButtonActions?.upperI?.action,
    },
    {
      name: 'upperSt',
      label: 'St',
      big: true,
      action: screen.value?.upperButtonActions?.upperSt?.action,
    },
    {
      name: 'upperVGt0',
      label: 'V>0',
      action: screen.value?.upperButtonActions?.upperVGt0?.action,
    },
    {
      name: 'upperVEq0',
      label: 'V=0',
      action: screen.value?.upperButtonActions?.upperVEq0?.action,
    },
    {
      name: 'upperBri',
      icon: 'bi bi-sun',
      action: screen.value?.upperButtonActions?.upperBri?.action,
    },
    {
      name: 'upperCon',
      icon: 'bi bi-circle-half',
      action: screen.value?.upperButtonActions?.upperCon?.action,
    },
    {
      name: 'upperUd',
      label: 'UD',
      action: screen.value?.upperButtonActions?.upperUd?.action,
    },
  ])
}
