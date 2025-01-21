<script setup lang="ts">
import Buttons from '~/components/buttons/index.vue'
import Screen from '~/components/screen/index.vue'

const { upperButtons, lowerButtons, sideButtons } = useButtons()
const { controls, clearControls } = useControlsStore()
</script>

<template>
  <div
    class="flex h-screen select-none items-center justify-center bg-black text-white"
  >
    <div class="flex h-[900px] w-[1100px]">
      <div class="flex h-full w-[900px] flex-col justify-between">
        <section class="flex h-[100px] items-center justify-center">
          <Buttons :buttons="upperButtons" />
        </section>
        <main class="h-[680px] bg-slate-900" @click="clearControls">
          <Screen>
            {{ controls }}
            <slot />
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
</template>
