import type { Control, ControlName } from '~/types/controls'
import type { ButtonName } from '~/types/buttons'
import { useControlsStore } from '~/stores/controls'

export const useControls = () => {
  const router = useRouter()
  const controlsStore = useControlsStore()

  const redirectAndClear = (path: string) => {
    router.push(path)
    controlsStore.clearControls()
  }

  const controlActions: Record<string, () => void> = {
    journey: () => redirectAndClear('/journey'),
    chaos: () => redirectAndClear('/chaos'),
    attempts: () => redirectAndClear('/attempts'),
    main: () => {
      router.push('/')
      controlsStore.restoreControls('main')
    },
  }

  const triggerControl = (
    property: keyof Control,
    value: ControlName | ButtonName
  ) => {
    const control = controlsStore.controls.find(c => c[property] === value)

    if (!!control) {
      const action = controlActions[control.name]

      action?.()
    }
  }

  return { triggerControl }
}
