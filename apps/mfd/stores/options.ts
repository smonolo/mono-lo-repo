import type { Option } from '~/types/options'

export const useOptionsStore = defineStore('options', () => {
  const options = ref<Option[]>([])
  const selectedOption = ref<string>('')

  const setOptions = (newOptions: Option[]) => {
    options.value = newOptions
    selectedOption.value = newOptions[0].name
  }

  const changeOption = (direction: 'next' | 'previous') => {
    if (options.value.length) {
      const currentIndex = options.value.findIndex(
        option => option.name === selectedOption.value
      )
      if (direction === 'next') {
        selectedOption.value =
          options.value[
            currentIndex === options.value.length - 1 ? 0 : currentIndex + 1
          ].name
      } else {
        selectedOption.value =
          options.value[
            currentIndex === 0 ? options.value.length - 1 : currentIndex - 1
          ].name
      }
    }
  }

  const triggerOption = () => {
    const selected = options.value.find(
      option => option.name === selectedOption.value
    )

    if (selected) {
      selected.action?.()
    }
  }

  const clearOptions = () => {
    options.value = []
    selectedOption.value = ''
  }

  return {
    options,
    setOptions,
    selectedOption,
    changeOption,
    triggerOption,
    clearOptions,
  }
})
