<script setup lang="ts">
import ScreenClock from '~/components/screen/clock.vue'
import ScreenControls from '~/components/screen/controls/index.vue'
import { useScreenStore } from '~/stores/screen'
import { useAuthStore } from '~/stores/auth'
import type { Button } from '~/types/buttons'

type Props = {
  lowerButtons: Button[]
}

defineProps<Props>()

const screenStore = useScreenStore()
const authStore = useAuthStore()

onMounted(() => {
  authStore.fetchSession()
})

const screenTitles: Record<string, string> = {
  main: 'Main',
  version: 'Version',
  settings: 'Settings',
  auth: 'Authentication',
  mc: 'Minecraft',
  player: 'Player Profile',
  test: 'Test Suites',
  doc: 'Documentation',
  diag: 'System Diagnostics',
  mixed: 'Mixed Content Test',
}

const currentTitle = computed(
  () => screenTitles[screenStore.activeScreen] || screenStore.activeScreen
)
</script>

<template>
  <div
    class="pointer-events-none flex h-full select-none flex-col justify-between bg-slate-100 text-slate-950 dark:bg-slate-900 dark:text-slate-100"
    :style="{ opacity: screenStore.brightness }"
    @wheel.prevent.stop
  >
    <div class="h-[600px] overflow-hidden p-2">
      <div class="flex h-full justify-between gap-x-4">
        <div class="flex h-full flex-1 flex-col gap-y-4 overflow-hidden">
          <div
            class="w-full border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
          >
            <span>{{ currentTitle }}</span>
          </div>
          <div
            id="screen-viewport"
            class="no-scrollbar h-full w-full overflow-auto scroll-smooth"
            @wheel.prevent.stop
          >
            <slot />
          </div>
        </div>
        <div class="flex w-[200px] shrink-0 flex-col gap-y-4">
          <ScreenClock />
          <ClientOnly>
            <div
              v-if="authStore.isAuthenticated && authStore.user"
              class="w-full overflow-hidden border border-slate-950 px-1.5 py-0.5 text-center font-bold tracking-wide dark:border-slate-100"
            >
              <p class="truncate">{{ authStore.user.name }}</p>
              <p class="truncate">{{ authStore.user.email }}</p>
            </div>
          </ClientOnly>
        </div>
      </div>
    </div>
    <ScreenControls :lowerButtons />
  </div>
</template>
