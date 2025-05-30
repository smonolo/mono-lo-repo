<script setup lang="ts">
import { useSideButtonsActions } from '~/composables/buttons/actions/useSideButtonsActions'
import { useUpperButtonsActions } from '~/composables/buttons/actions/useUpperButtonsActions'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import { useFiles } from '~/composables/screens/useFiles'
import type { ScreenConfig } from '~/types/screen'
import OptionsCard from '~/components/common/options-card.vue'
import { filesize } from 'filesize'
import dayjs from 'dayjs'
import { useOptionsStore } from '~/stores/options'

const logged = ref(false)
const page = ref(1)

const { data, isFetching } = useFiles(logged, page)
const optionsStore = useOptionsStore()

defineExpose<ScreenConfig>({
  upperButtonActions: useUpperButtonsActions(),
  lowerButtonActions: {
    lower0: {
      label: 'Token',
      action: () => {
        const token = prompt('Enter your token')

        if (token) {
          localStorage.setItem('filesScreen_token', token)
          logged.value = true
        }
      },
    },
    lower1: {
      label: 'Clr Token',
      action: () => {
        localStorage.removeItem('filesScreen_token')
        logged.value = false
        page.value = 1
        optionsStore.clearOptions()
      },
    },
    lower3: {
      label: 'Prev Pg',
      action: () => {
        if (page.value > 1 && logged.value && !isFetching.value) {
          page.value--
        }
      },
    },
    lower4: {
      label: 'Next Pg',
      action: () => {
        if (logged.value && !isFetching.value) {
          page.value++
        }
      },
    },
    lower9: useMainButtonConfig(),
  },
  sideButtonActions: useSideButtonsActions(),
})

onMounted(() => (logged.value = !!localStorage.getItem('filesScreen_token')))

const options = computed(() => {
  if (!data.value) return []

  return data.value.files?.map(file => ({
    name: file.fileName,
    label: file.fileName,
    value: `${filesize(file.size)} - ${dayjs(file.birthTime).format('DD/MM/YYYY HH:mm')}`,
    action: () => {
      window.open(`https://cdn.smnl.dev/images/${file.fileName}`, '_blank')
    },
  }))
})

watch(options, opts => {
  if (!!opts?.length) {
    optionsStore.setOptions(opts)
  }
})
</script>

<template>
  <div class="p-10">
    <p v-if="!logged">Token authentication required to load files</p>
    <p v-else-if="isFetching">Loading files...</p>
    <p v-else-if="!data?.files?.length">No files found</p>
    <OptionsCard v-else :header="`Files (Page ${page})`" :options />
  </div>
</template>
