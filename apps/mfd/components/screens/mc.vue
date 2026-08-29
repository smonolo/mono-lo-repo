<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useScreenStore } from '~/stores/screen'
import { useOptionsStore } from '~/stores/options'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'

type PlayerData = {
  uuid: string
  username: string
  displayName: string
  rank: {
    id: string
    name: string
    color: string
    prefix: string
  }
  ping: number
  afk: boolean
  world: string
}

type MinecraftApiResponse = {
  online: boolean
  players: PlayerData[]
  count: number
  error?: string
}

const authStore = useAuthStore()
const screenStore = useScreenStore()
const optionsStore = useOptionsStore()

const loading = ref<boolean>(false)
const serverData = ref<MinecraftApiResponse | null>(null)
const selectedPlayer = ref<PlayerData | null>(null)

const fetchPlayers = async () => {
  if (!authStore.isAdmin) return
  loading.value = true
  try {
    const data = await $fetch<MinecraftApiResponse>('/api/minecraft/players')
    serverData.value = data
  } catch (err: any) {
    serverData.value = {
      online: false,
      players: [],
      count: 0,
      error: err.data?.statusMessage || 'Failed to connect to Minecraft API',
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await authStore.fetchSession()
  if (authStore.isAdmin) {
    await fetchPlayers()
  }
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower0: {
      label: 'Ref',
      action: fetchPlayers,
    },
    lower8: {
      label: 'Ath',
      action: () => screenStore.setActiveScreen('auth'),
    },
    lower9: useMainButtonConfig(),
  },
})

const options = computed(() => {
  if (!serverData.value || !serverData.value.players.length) {
    return []
  }

  return serverData.value.players.map(p => {
    const rankPrefix = p.rank.prefix ? `${p.rank.prefix} ` : ''
    const afkTag = p.afk ? ' [AFK]' : ''
    return {
      name: p.uuid,
      label: `${rankPrefix}${p.username}${afkTag}`,
      value: `${p.ping}ms (${p.world})`,
      action: () => {
        selectedPlayer.value = p
      },
    }
  })
})

watchEffect(() => {
  if (options.value.length) {
    optionsStore.setOptions(options.value)
  }
})
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>Minecraft</span>
    </div>

    <!-- Unauthenticated / Non-Admin View -->
    <div v-if="!authStore.isAdmin" class="p-10 space-y-4">
      <div class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <p class="font-bold tracking-wide">Restricted System</p>
        <p>
          Administrator authorization is required to access live Minecraft server telemetry.
        </p>
        <p>
          Please navigate to the Auth screen to sign in with an authorized Google account.
        </p>
      </div>
    </div>

    <!-- Authenticated Admin View -->
    <div v-else class="p-10 space-y-4">
      <!-- Server offline warning -->
      <div
        v-if="serverData && !serverData.online"
        class="border border-red-500 p-4 text-red-500 dark:border-red-400 dark:text-red-400 space-y-1"
      >
        <p class="font-bold tracking-wide">Server Unreachable</p>
        <p>{{ serverData.error || 'The Paper server is currently offline or unreachable.' }}</p>
      </div>

      <!-- Online players list -->
      <div v-else-if="serverData && serverData.online">
        <div
          v-if="serverData.players.length === 0"
          class="border border-slate-950 p-4 dark:border-slate-100"
        >
          <span>No players currently online</span>
        </div>
        <div v-else>
          <OptionsCard header="Online Players" :options="options" />
        </div>
      </div>

      <!-- Loading state -->
      <div v-else-if="loading" class="border border-slate-950 p-4 dark:border-slate-100">
        <span>Querying server telemetry...</span>
      </div>
    </div>
  </div>
</template>
