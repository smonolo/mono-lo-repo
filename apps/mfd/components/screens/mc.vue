<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useScreenStore } from '~/stores/screen'
import { useOptionsStore } from '~/stores/options'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'
import type { Option } from '~/types/options'

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

type MinecraftPlayersResponse = {
  online: boolean
  players: PlayerData[]
  count: number
  error?: string
}

type MinecraftStatusResponse = {
  online: boolean
  onlinePlayers: number
  maxPlayers: number
  version: string
  minecraftVersion: string
  tps: number
  mspt: number
  memory?: {
    usedMb: number
    allocatedMb: number
    maxMb: number
  }
  uptimeSeconds: number
  error?: string
}

const authStore = useAuthStore()
const screenStore = useScreenStore()
const optionsStore = useOptionsStore()

const loading = ref<boolean>(false)
const playersData = ref<MinecraftPlayersResponse | null>(null)
const statusData = ref<MinecraftStatusResponse | null>(null)
const errorMessage = ref<string | null>(null)

const fetchServerData = async () => {
  if (!authStore.isAdmin) return
  loading.value = true
  errorMessage.value = null
  try {
    const [players, status] = await Promise.all([
      $fetch<MinecraftPlayersResponse>('/api/minecraft/players').catch(
        err => ({
          online: false,
          players: [],
          count: 0,
          error: err.data?.error || err.message,
        })
      ),
      $fetch<MinecraftStatusResponse>('/api/minecraft/status').catch(err => ({
        online: false,
        onlinePlayers: 0,
        maxPlayers: 0,
        version: '',
        minecraftVersion: '',
        tps: 0,
        mspt: 0,
        uptimeSeconds: 0,
        error: err.data?.error || err.message,
      })),
    ])

    playersData.value = players
    statusData.value = status

    if (!players.online && players.error) {
      errorMessage.value = players.error
    } else if (!status.online && status.error) {
      errorMessage.value = status.error
    }
  } catch (err: any) {
    errorMessage.value = err.data?.error || err.message || 'Failed to connect to Minecraft API'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await authStore.fetchSession()
  if (authStore.isAdmin) {
    await fetchServerData()
  }
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower0: {
      label: 'Ref',
      action: fetchServerData,
    },
    lower8: {
      label: 'Ath',
      action: () => screenStore.setActiveScreen('auth'),
    },
    lower9: useMainButtonConfig(),
  },
})

const formatUptime = (seconds: number): string => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return `${h}h ${m}m`
}

const statusOptions = computed<Option[]>(() => {
  if (!statusData.value || !statusData.value.online) return []
  const s = statusData.value
  const opts: Option[] = [
    {
      name: 'tps',
      label: 'TPS',
      value: `${s.tps.toFixed(2)} (${s.mspt}ms)`,
    },
    {
      name: 'players_count',
      label: 'Players',
      value: `${s.onlinePlayers} / ${s.maxPlayers}`,
    },
  ]

  if (s.memory) {
    opts.push({
      name: 'memory',
      label: 'Memory',
      value: `${s.memory.usedMb}MB / ${s.memory.maxMb}MB`,
    })
  }

  if (s.uptimeSeconds) {
    opts.push({
      name: 'uptime',
      label: 'Uptime',
      value: formatUptime(s.uptimeSeconds),
    })
  }

  return opts
})

const playerOptions = computed<Option[]>(() => {
  if (!playersData.value || !playersData.value.players.length) {
    return []
  }

  return playersData.value.players.map(p => {
    const rankPrefix = p.rank.prefix ? `${p.rank.prefix} ` : ''
    const afkTag = p.afk ? ' [AFK]' : ''
    return {
      name: p.uuid,
      label: `${rankPrefix}${p.username}${afkTag}`,
      value: `${p.ping}ms (${p.world})`,
    }
  })
})

const allOptions = computed<Option[]>(() => {
  return [...statusOptions.value, ...playerOptions.value]
})

watchEffect(() => {
  if (allOptions.value.length) {
    optionsStore.setOptions(allOptions.value)
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
        v-if="errorMessage || (statusData && !statusData.online)"
        class="border border-red-500 p-4 text-red-500 dark:border-red-400 dark:text-red-400 space-y-1"
      >
        <p class="font-bold tracking-wide">Server Unreachable</p>
        <p>{{ errorMessage || 'The Paper server is currently offline or unreachable.' }}</p>
      </div>

      <!-- Online Status Cards -->
      <div v-else-if="statusData && statusData.online" class="space-y-4">
        <OptionsCard header="Server Status" :options="statusOptions" />

        <div v-if="playerOptions.length">
          <OptionsCard header="Players Online" :options="playerOptions" />
        </div>
        <div
          v-else
          class="border border-slate-950 dark:border-slate-100"
        >
          <div class="border-b border-slate-950 p-2 dark:border-slate-100">
            <span class="font-bold tracking-wide">Players Online</span>
          </div>
          <div class="p-3 text-sm">
            <span>No players currently online</span>
          </div>
        </div>
      </div>

      <!-- Loading state -->
      <div v-else-if="loading" class="border border-slate-950 p-4 dark:border-slate-100">
        <span>Querying server telemetry...</span>
      </div>
    </div>
  </div>
</template>
