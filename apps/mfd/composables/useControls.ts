import type { Control, ControlName } from '~/types/controls'
import type { ButtonName } from '~/types/buttons'

export const useControls = () => {
  const router = useRouter()
  const { controls, restoreControls, clearControls } = useControlsStore()

  const redirectAndClear = (path: string) => {
    router.push(path)
    clearControls()
  }

  const controlActions: Record<string, () => void> = {
    journey: () => redirectAndClear('/journey'),
    chaos: () => redirectAndClear('/chaos'),
    attempts: () => redirectAndClear('/attempts'),
    main: () => {
      router.push('/')
      restoreControls('main')
    },
  }

  const triggerControl = (
    property: keyof Control,
    value: ControlName | ButtonName
  ) => {
    const control = controls.find(c => c[property] === value)

    if (!!control) {
      const action = controlActions[control.name]

      action?.()
    }
  }

  return { triggerControl }
}
