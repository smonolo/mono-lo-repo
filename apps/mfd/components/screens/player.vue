<script setup lang="ts">
import moment from 'moment'
import { useAuthStore } from '~/stores/auth'
import { useScreenStore } from '~/stores/screen'
import { useOptionsStore } from '~/stores/options'
import { usePlayerStore } from '~/stores/player'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'
import type { Option } from '~/types/options'
import type { RankData } from '~/types/minecraft'

const authStore = useAuthStore()
const screenStore = useScreenStore()
const optionsStore = useOptionsStore()
const playerStore = usePlayerStore()

const copiedUuid = ref<boolean>(false)

const copyUuid = async () => {
  if (!player.value?.uuid) return
  try {
    await navigator.clipboard.writeText(player.value.uuid)
    copiedUuid.value = true
    setTimeout(() => {
      copiedUuid.value = false
    }, 2000)
  } catch {}
}

const refreshPlayerData = async () => {
  if (!authStore.hasScreenPermission('player') || !playerStore.selectedUuid)
    return
  await playerStore.fetchPlayerDetails()
}

onMounted(async () => {
  await authStore.fetchSession()
  if (authStore.hasScreenPermission('player') && playerStore.selectedUuid) {
    await playerStore.fetchPlayerDetails()
  }
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower0: {
      label: 'Ref',
      action: refreshPlayerData,
    },
    lower8: {
      label: 'Mc',
      action: () => screenStore.setActiveScreen('mc'),
    },
    lower9: useMainButtonConfig(),
  },
})

const player = computed(() => playerStore.playerData)

const allPlayerRanks = computed<RankData[]>(() => {
  if (!player.value) return []
  const list: RankData[] = []
  if (player.value.primaryRank) {
    list.push({ ...player.value.primaryRank, primary: true })
  } else if (player.value.rank) {
    list.push({ ...player.value.rank, primary: true })
  }

  if (player.value.ranks) {
    for (const r of player.value.ranks) {
      if (!list.some(existing => existing.id === r.id)) {
        list.push(r)
      }
    }
  }
  return list
})

const formatDate = (timestamp?: number | string | null): string => {
  if (!timestamp) return 'Never'
  const num = typeof timestamp === 'string' ? Number(timestamp) : timestamp
  if (typeof num !== 'number' || isNaN(num) || num <= 0) return 'Never'
  const m = moment(num)
  return m.isValid() ? m.format('DD.MM.YYYY HH:mm') : 'Never'
}

const formatDuration = (seconds?: number): string => {
  if (!seconds || seconds <= 0) return '0m'
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (d > 0) return `${d}d ${h}h ${m}m`
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
}

const formatDistance = (meters?: number): string => {
  if (!meters || meters <= 0) return '0 m'
  if (meters >= 1000) {
    return `${(meters / 1000).toFixed(2)} km`
  }
  return `${Math.round(meters)} m`
}

const formatNumber = (num?: number): string => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

const infoOptions = computed<Option[]>(() => {
  if (!player.value) return []
  const p = player.value

  const opts: Option[] = [
    {
      name: 'prof_uuid',
      label: 'UUID',
      value: copiedUuid.value ? 'Copied' : 'Copy',
      action: copyUuid,
    },
    {
      name: 'prof_first_login',
      label: 'First Login',
      value: formatDate(p.firstLogin),
    },
    {
      name: 'prof_last_login',
      label: 'Last Login',
      value: formatDate(p.lastLogin),
    },
    {
      name: 'prof_status',
      label: 'Status',
      value: p.online ? (p.afk ? 'AFK' : 'Online') : 'Offline',
    },
  ]

  if (p.online && p.ping !== undefined && p.ping !== null) {
    opts.push({
      name: 'prof_ping',
      label: 'Ping',
      value: `${p.ping}ms`,
    })
  }

  return opts
})

const statsOptions = computed<Option[]>(() => {
  if (!player.value || !player.value.stats) return []
  const s = player.value.stats

  return [
    {
      name: 'stat_playtime',
      label: 'Play Time',
      value: formatDuration(s.playTimeSeconds),
    },
    {
      name: 'stat_deaths',
      label: 'Deaths',
      value: formatNumber(s.deaths),
    },
    {
      name: 'stat_mob_kills',
      label: 'Mob Kills',
      value: formatNumber(s.mobKills),
    },
    {
      name: 'stat_player_kills',
      label: 'Player Kills',
      value: formatNumber(s.playerKills),
    },
    {
      name: 'stat_damage_dealt',
      label: 'Damage Dealt',
      value: formatNumber(Math.round(s.damageDealt)),
    },
    {
      name: 'stat_damage_taken',
      label: 'Damage Taken',
      value: formatNumber(Math.round(s.damageTaken)),
    },
    {
      name: 'stat_jumps',
      label: 'Jumps',
      value: formatNumber(s.jumps),
    },
    {
      name: 'stat_walk',
      label: 'Walk Distance',
      value: formatDistance(s.walkDistanceMeters),
    },
    {
      name: 'stat_fly',
      label: 'Fly Distance',
      value: formatDistance(s.flyDistanceMeters),
    },
    {
      name: 'stat_rest',
      label: 'Time Since Rest',
      value: formatDuration(s.timeSinceRestSeconds),
    },
  ]
})

const allOptions = computed<Option[]>(() => [
  ...infoOptions.value,
  ...statsOptions.value,
])

watchEffect(() => {
  if (allOptions.value.length) {
    optionsStore.setOptions(allOptions.value)
  }
})
</script>

<template>
  <div class="h-full w-full">
    <div v-if="!authStore.hasScreenPermission('player')" class="space-y-4">
      <div class="space-y-2 border border-slate-950 p-4 dark:border-slate-100">
        <p class="font-bold tracking-wide">Restricted System</p>
        <p>Authorization is required to access player telemetry.</p>
        <p>
          Please navigate to the Auth screen to sign in with an authorized
          account.
        </p>
      </div>
    </div>

    <div v-else-if="!playerStore.selectedUuid" class="space-y-4">
      <div class="space-y-2 border border-slate-950 p-4 dark:border-slate-100">
        <p class="font-bold tracking-wide">No Player Selected</p>
        <p>Select a player from the Minecraft screen to view their profile.</p>
      </div>
    </div>

    <div v-else-if="playerStore.loading" class="space-y-4">
      <div class="border border-slate-950 p-4 dark:border-slate-100">
        <span>Loading player telemetry & statistics...</span>
      </div>
    </div>

    <div v-else-if="playerStore.error" class="space-y-4">
      <div
        class="space-y-1 border border-red-500 p-4 text-red-500 dark:border-red-400 dark:text-red-400"
      >
        <p class="font-bold tracking-wide">Error Loading Profile</p>
        <p>{{ playerStore.error }}</p>
      </div>
    </div>

    <div v-else-if="player" class="space-y-6">
      <div class="grid grid-cols-2 items-start gap-6">
        <div class="space-y-4">
          <div
            class="flex items-center gap-x-4 border border-slate-950 p-4 dark:border-slate-100"
          >
            <img
              :src="`https://skins.mcstats.com/face/${player.uuid}`"
              :alt="player.username"
              class="h-16 w-16 shrink-0"
              style="image-rendering: pixelated"
              loading="lazy"
            />

            <div class="min-w-0 space-y-2">
              <p class="truncate font-bold tracking-wide">
                {{ player.username }}
              </p>

              <div
                v-if="allPlayerRanks.length"
                class="flex flex-wrap items-center gap-2"
              >
                <span
                  v-for="r in allPlayerRanks"
                  :key="r.id"
                  class="border px-1.5 py-0.5 font-bold tracking-wide"
                  :style="{
                    borderColor: getMinecraftRankColor(
                      r.color,
                      screenStore.contrast
                    ),
                    color: getMinecraftRankColor(r.color, screenStore.contrast),
                  }"
                >
                  {{ r.name }}
                </span>
              </div>
            </div>
          </div>

          <OptionsCard header="Info" :options="infoOptions" />
        </div>

        <div class="space-y-4">
          <OptionsCard header="Stats" :options="statsOptions" />
        </div>
      </div>
    </div>
  </div>
</template>
