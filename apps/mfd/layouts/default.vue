<script setup lang="ts">
import Buttons from '~/components/buttons/index.vue'
import Screen from '~/components/screen/index.vue'
import { useButtons } from '~/composables/useButtons'
import { useScreens } from '~/composables/useScreens'
import { useScreenStore } from '~/stores/screen'
import MobileDisabled from '~/components/disabled.vue'

const { upperButtons, lowerButtons, sideButtons } = useButtons()
const { screensConfig } = useScreens()
const screenStore = useScreenStore()
</script>

<template>
  <div class="relative">
    <MobileDisabled />
    <div
      class="flex h-screen select-none items-center justify-center bg-black text-white"
    >
      <div class="flex h-[900px] w-[1100px]">
        <div class="flex h-full w-[900px] flex-col justify-between">
          <section class="flex h-[100px] items-center justify-center">
            <Buttons :buttons="upperButtons" />
          </section>
          <main
            class="h-[680px] bg-slate-100 text-slate-100 dark:bg-slate-900 dark:text-slate-950"
          >
            <Screen>
              <component :is="screensConfig[screenStore.activeScreen]" />
            </Screen>
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
