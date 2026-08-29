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

const htmlAttrs = computed(() => ({ class: screenStore.contrast }))

useHead(() => ({ htmlAttrs: htmlAttrs.value }))
</script>

<template>
  <div
    class="flex h-full flex-col justify-between bg-slate-100 text-slate-950 dark:bg-slate-900 dark:text-slate-100 pointer-events-none select-none"
    :style="{ opacity: screenStore.brightness }"
    @wheel.prevent.stop
  >
    <div class="h-[600px] overflow-hidden p-2">
      <div class="flex h-full justify-between">
        <div
          id="screen-viewport"
          class="h-full w-full overflow-auto no-scrollbar scroll-smooth"
          @wheel.prevent.stop
        >
          <slot />
        </div>
        <div class="flex w-[200px] flex-col gap-y-4">
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
