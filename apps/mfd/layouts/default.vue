<script setup lang="ts">
import Buttons from '~/components/buttons/index.vue'
import Screen from '~/components/screen/index.vue'
import { useButtons } from '~/composables/useButtons'
import { useScreens } from '~/composables/useScreens'
import { useScreenStore } from '~/stores/screen'
import MobileDisabled from '~/components/disabled.vue'
import About from '~/components/about.vue'
import type { ViewComponent } from '~/types/buttons'

const mountedView = useTemplateRef<ViewComponent>('mountedView')
const { upperButtons, sideButtons } = useButtons()
const lowerButtons = useLowerButtons(mountedView)
const { screensConfig } = useScreens()
const screenStore = useScreenStore()
</script>

<template>
  <div class="relative">
    <MobileDisabled />
    <div
      class="hidden h-screen select-none items-center justify-center bg-black text-sm text-white xl:flex"
    >
      <div class="flex h-[900px] w-[1100px]">
        <div class="flex h-full w-[900px] flex-col justify-between">
          <section class="flex h-[100px] items-center justify-center">
            <Buttons :buttons="upperButtons" />
          </section>
          <main class="h-[680px]">
            <Screen v-if="screenStore.display === 'primary'" :lowerButtons>
              <component
                ref="mountedView"
                :is="screensConfig[screenStore.activeScreen]"
              />
            </Screen>
            <About v-else-if="screenStore.display === 'about'" />
          </main>
          <section class="flex h-[100px] items-center justify-center">
            <Buttons type="controls" :buttons="lowerButtons" />
          </section>
        </div>
        <aside class="flex w-[200px]">
          <Buttons direction="vertical" :buttons="sideButtons" />
        </aside>
      </div>
    </div>
  </div>
</template>
