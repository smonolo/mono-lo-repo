import type { Button, LowerButtonName, ViewComponent } from '~/types/buttons'

export const useLowerButtons = (view: Ref<ViewComponent | null>) => {
  const buttonSkeletons = Array.from({ length: 10 }, (_, i) => ({
    name: `lower${i}` as LowerButtonName,
    label: i.toString(),
  }))

  return computed<Button[]>(() =>
    buttonSkeletons.map(sk => ({
      ...sk,
      screenLabel: view.value?.lowerButtonActions[sk.name]?.label,
      action: view.value?.lowerButtonActions[sk.name]?.onClick,
    }))
  )
}
