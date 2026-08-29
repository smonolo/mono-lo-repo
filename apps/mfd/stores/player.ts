import { defineStore } from 'pinia'
import type { PlayerData, SinglePlayerResponse } from '~/types/minecraft'

export const usePlayerStore = defineStore('player', () => {
  const selectedUuid = ref<string | null>(null)
  const playerData = ref<PlayerData | null>(null)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  const selectPlayer = (uuid: string) => {
    selectedUuid.value = uuid
    playerData.value = null
    error.value = null
  }

  const fetchPlayerDetails = async (uuidParam?: string) => {
    const uuid = uuidParam || selectedUuid.value
    if (!uuid) return

    loading.value = true
    error.value = null

    try {
      const res = await $fetch<SinglePlayerResponse>('/api/minecraft/player', {
        query: { uuid },
      })

      if (res.player) {
        playerData.value = res.player
      } else {
        error.value = res.error || 'Player profile not found'
      }
    } catch (err: any) {
      error.value =
        err.data?.statusMessage || err.message || 'Failed to load player'
    } finally {
      loading.value = false
    }
  }

  const clearPlayer = () => {
    selectedUuid.value = null
    playerData.value = null
    error.value = null
  }

  return {
    selectedUuid,
    playerData,
    loading,
    error,
    selectPlayer,
    fetchPlayerDetails,
    clearPlayer,
  }
})
