<script setup lang="ts">
import Buttons from '~/components/buttons/index.vue'
import Screen from '~/components/screen/index.vue'
import { useScreens } from '~/composables/useScreens'
import { useScreenStore } from '~/stores/screen'
import { useAuthStore } from '~/stores/auth'
import MobileDisabled from '~/components/disabled.vue'
import type { ScreenConfig } from '~/types/screen'
import { useLowerButtons } from '~/composables/buttons/useLowerButtons'
import { useUpperButtons } from '~/composables/buttons/useUpperButtons'
import { useSideButtons } from '~/composables/buttons/useSideButtons'

const activeScreen = useTemplateRef<ScreenConfig>('activeScreen')

const upperButtons = useUpperButtons(activeScreen)
const lowerButtons = useLowerButtons(activeScreen)
const sideButtons = useSideButtons(activeScreen)

const { screensConfig } = useScreens()
const screenStore = useScreenStore()
const authStore = useAuthStore()

const isLoading = ref(true)

const htmlAttrs = computed(() => ({ class: screenStore.contrast }))
useHead(() => ({ htmlAttrs: htmlAttrs.value }))

onMounted(async () => {
  try {
    await Promise.allSettled([
      authStore.fetchSession(),
      new Promise(resolve => setTimeout(resolve, 800)),
    ])
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="relative">
    <MobileDisabled />
    <div
      class="hidden min-h-screen select-none items-center justify-center bg-neutral-800 px-6 py-16 text-sm text-white xl:flex"
    >
      <div
        class="flex rounded-3xl bg-black px-36 py-16 shadow-2xl ring-1 ring-neutral-950"
      >
        <div class="flex h-[880px] w-[900px] flex-col justify-between">
          <section class="flex h-[100px] items-center justify-center">
            <Buttons :buttons="upperButtons" />
          </section>
          <main class="h-[680px]">
            <div
              v-if="isLoading"
              class="pointer-events-none flex h-full w-full select-none items-center justify-center bg-slate-900 text-white"
              :style="{ opacity: screenStore.brightness }"
            >
              <span class="text-3xl font-bold tracking-wider text-white">
                Loading...
              </span>
            </div>
            <Screen v-else-if="screenStore.display === 'primary'" :lowerButtons>
              <component
                ref="activeScreen"
                :is="screensConfig[screenStore.activeScreen]"
              />
            </Screen>
          </main>
          <section class="flex h-[100px] items-center justify-center">
            <Buttons type="controls" :buttons="lowerButtons" />
          </section>
        </div>
        <aside class="flex w-[150px]">
          <Buttons direction="vertical" :buttons="sideButtons" />
        </aside>
      </div>
    </div>
  </div>
</template>
