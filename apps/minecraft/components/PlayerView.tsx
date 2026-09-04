'use client'

import Link from 'next/link'
import { useRouter, useSearchParams, usePathname } from 'next/navigation'
import { useCallback, useMemo, useState } from 'react'
import type { PlayerData, RankData } from '@/types/minecraft'
import {
  formatDate,
  formatDistance,
  formatDuration,
  formatNumber,
  getMinecraftRankColor,
  getPunishmentStatus,
  getPunishmentTypeBadge,
  getPunishmentStatusBadge,
} from '@/utils/minecraft'
import { Card, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { DataList, type DataListItem } from '@/components/ui/DataList'

type Props = {
  player: PlayerData
}

type Tab = 'stats' | 'achievements' | 'punishments'

export default function PlayerView({ player }: Props) {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const tabParam = searchParams.get('tab')
  const activeTab: Tab =
    tabParam === 'achievements'
      ? 'achievements'
      : tabParam === 'punishments'
        ? 'punishments'
        : 'stats'

  const handleTabChange = useCallback(
    (newTab: Tab) => {
      const params = new URLSearchParams(searchParams.toString())
      if (newTab === 'stats') {
        params.delete('tab')
      } else {
        params.set('tab', newTab)
      }
      const queryStr = params.toString()
      const url = queryStr ? `${pathname}?${queryStr}` : pathname
      router.replace(url, { scroll: false })
    },
    [router, pathname, searchParams]
  )

  const allPlayerRanks: RankData[] = useMemo(() => {
    const ranks: RankData[] = []
    if (player.primaryRank) {
      ranks.push({ ...player.primaryRank, primary: true })
    } else if (player.rank) {
      ranks.push({ ...player.rank, primary: true })
    }

    if (player.ranks) {
      for (const r of player.ranks) {
        if (!ranks.some(existing => existing.id === r.id)) {
          ranks.push(r)
        }
      }
    }
    return ranks
  }, [player.primaryRank, player.rank, player.ranks])

  const punishments = player.punishments || []

  const stats = player.stats
  const statItems: DataListItem[] = useMemo(() => {
    if (!stats) return []
    const items: DataListItem[] = [
      {
        label: 'Play Time',
        value: formatDuration(stats.playTimeSeconds),
      },
      { label: 'Deaths', value: formatNumber(stats.deaths) },
      { label: 'Mob Kills', value: formatNumber(stats.mobKills) },
      { label: 'Player Kills', value: formatNumber(stats.playerKills) },
      {
        label: 'Damage Dealt',
        value: formatNumber(Math.round(stats.damageDealt)),
      },
      {
        label: 'Damage Taken',
        value: formatNumber(Math.round(stats.damageTaken)),
      },
    ]

    if (typeof stats.damageBlocked === 'number' && stats.damageBlocked > 0) {
      items.push({
        label: 'Damage Blocked',
        value: formatNumber(Math.round(stats.damageBlocked)),
      })
    }
    if (typeof stats.damageResisted === 'number' && stats.damageResisted > 0) {
      items.push({
        label: 'Damage Resisted',
        value: formatNumber(Math.round(stats.damageResisted)),
      })
    }
    if (typeof stats.damageAbsorbed === 'number' && stats.damageAbsorbed > 0) {
      items.push({
        label: 'Damage Absorbed',
        value: formatNumber(Math.round(stats.damageAbsorbed)),
      })
    }

    items.push({ label: 'Jumps', value: formatNumber(stats.jumps) })
    items.push({
      label: 'Walk Distance',
      value: formatDistance(stats.walkDistanceMeters),
    })

    if (
      typeof stats.sprintDistanceMeters === 'number' &&
      stats.sprintDistanceMeters > 0
    ) {
      items.push({
        label: 'Sprint Distance',
        value: formatDistance(stats.sprintDistanceMeters),
      })
    }

    items.push({
      label: 'Fly Distance',
      value: formatDistance(stats.flyDistanceMeters),
    })

    if (
      typeof stats.elytraDistanceMeters === 'number' &&
      stats.elytraDistanceMeters > 0
    ) {
      items.push({
        label: 'Elytra Flight',
        value: formatDistance(stats.elytraDistanceMeters),
      })
    }
    if (
      typeof stats.boatDistanceMeters === 'number' &&
      stats.boatDistanceMeters > 0
    ) {
      items.push({
        label: 'Distance Sailed',
        value: formatDistance(stats.boatDistanceMeters),
      })
    }
    if (
      typeof stats.swimDistanceMeters === 'number' &&
      stats.swimDistanceMeters > 0
    ) {
      items.push({
        label: 'Distance Swum',
        value: formatDistance(stats.swimDistanceMeters),
      })
    }
    if (
      typeof stats.minecartDistanceMeters === 'number' &&
      stats.minecartDistanceMeters > 0
    ) {
      items.push({
        label: 'Minecart Distance',
        value: formatDistance(stats.minecartDistanceMeters),
      })
    }
    if (
      typeof stats.horseDistanceMeters === 'number' &&
      stats.horseDistanceMeters > 0
    ) {
      items.push({
        label: 'Mount Distance',
        value: formatDistance(stats.horseDistanceMeters),
      })
    }
    if (
      typeof stats.climbDistanceMeters === 'number' &&
      stats.climbDistanceMeters > 0
    ) {
      items.push({
        label: 'Distance Climbed',
        value: formatDistance(stats.climbDistanceMeters),
      })
    }
    if (
      typeof stats.sneakTimeSeconds === 'number' &&
      stats.sneakTimeSeconds > 0
    ) {
      items.push({
        label: 'Time Sneaking',
        value: formatDuration(stats.sneakTimeSeconds),
      })
    }

    items.push({
      label: 'Time Since Rest',
      value: formatDuration(stats.timeSinceRestSeconds),
    })

    if (typeof stats.sleeps === 'number' && stats.sleeps > 0) {
      items.push({
        label: 'Times Slept',
        value: formatNumber(stats.sleeps),
      })
    }
    if (typeof stats.chestsOpened === 'number' && stats.chestsOpened > 0) {
      items.push({
        label: 'Chests Opened',
        value: formatNumber(stats.chestsOpened),
      })
    }
    if (typeof stats.itemsEnchanted === 'number' && stats.itemsEnchanted > 0) {
      items.push({
        label: 'Items Enchanted',
        value: formatNumber(stats.itemsEnchanted),
      })
    }
    if (typeof stats.fishCaught === 'number' && stats.fishCaught > 0) {
      items.push({
        label: 'Fish Caught',
        value: formatNumber(stats.fishCaught),
      })
    }
    if (typeof stats.animalsBred === 'number' && stats.animalsBred > 0) {
      items.push({
        label: 'Animals Bred',
        value: formatNumber(stats.animalsBred),
      })
    }
    if (typeof stats.raidsWon === 'number' && stats.raidsWon > 0) {
      items.push({
        label: 'Raids Won',
        value: formatNumber(stats.raidsWon),
      })
    }
    if (typeof stats.raidsTriggered === 'number' && stats.raidsTriggered > 0) {
      items.push({
        label: 'Raids Triggered',
        value: formatNumber(stats.raidsTriggered),
      })
    }
    if (typeof stats.trades === 'number' && stats.trades > 0) {
      items.push({
        label: 'Villager Trades',
        value: formatNumber(stats.trades),
      })
    }
    if (typeof stats.toolsBroken === 'number' && stats.toolsBroken > 0) {
      items.push({
        label: 'Tools Broken',
        value: formatNumber(stats.toolsBroken),
      })
    }
    if (typeof stats.bellRings === 'number' && stats.bellRings > 0) {
      items.push({
        label: 'Bell Rings',
        value: formatNumber(stats.bellRings),
      })
    }
    if (
      typeof stats.musicDiscsPlayed === 'number' &&
      stats.musicDiscsPlayed > 0
    ) {
      items.push({
        label: 'Music Discs Played',
        value: formatNumber(stats.musicDiscsPlayed),
      })
    }

    return items
  }, [stats])

  const activeInfractions = useMemo(
    () => punishments.filter(p => getPunishmentStatus(p) === 'ACTIVE').length,
    [punishments]
  )

  const formattedBiome = useMemo(() => {
    if (!player.biome) return null
    return player.biome
      .toLowerCase()
      .replace(/^minecraft:/, '')
      .replace(/_/g, ' ')
      .replace(/\b\w/g, l => l.toUpperCase())
  }, [player.biome])

  const [achievementCategory, setAchievementCategory] = useState<string>('all')
  const [achievementStatus, setAchievementStatus] = useState<
    'all' | 'completed' | 'locked'
  >('all')
  const [achievementSearch, setAchievementSearch] = useState<string>('')

  const playerAchievements = useMemo(
    () => player.achievements?.list || [],
    [player.achievements]
  )

  const achievementCategories = useMemo(() => {
    const list: { id: string; name: string }[] = []
    for (const a of playerAchievements) {
      if (a.category && !list.some(c => c.id === a.category)) {
        list.push({ id: a.category, name: a.categoryName || a.category })
      }
    }
    return list
  }, [playerAchievements])

  const filteredAchievements = useMemo(() => {
    return playerAchievements.filter(a => {
      if (achievementCategory !== 'all' && a.category !== achievementCategory) {
        return false
      }
      if (achievementStatus === 'completed' && !a.completed) {
        return false
      }
      if (achievementStatus === 'locked' && a.completed) {
        return false
      }
      if (achievementSearch.trim()) {
        const query = achievementSearch.toLowerCase().trim()
        const titleMatch = a.title.toLowerCase().includes(query)
        const descMatch = a.description.toLowerCase().includes(query)
        const catMatch = (a.categoryName || a.category).toLowerCase().includes(query)
        return titleMatch || descMatch || catMatch
      }
      return true
    })
  }, [playerAchievements, achievementCategory, achievementStatus, achievementSearch])

  return (
    <div className="grid grid-cols-1 items-start gap-8 md:grid-cols-[220px_1fr] md:gap-10">
      <div className="flex flex-col space-y-4">
        <div className="flex items-center gap-x-4 md:flex-col md:items-start md:gap-x-0 md:space-y-4">
          <img
            src={`https://skins.mcstats.com/face/${player.uuid}?size=256`}
            alt={player.username}
            className="h-20 w-20 shrink-0 rounded-lg bg-gray-900 sm:h-28 sm:w-28 md:h-32 md:w-32"
            style={{ imageRendering: 'pixelated' }}
          />

          <div className="min-w-0 space-y-1.5">
            <div className="flex items-center gap-x-2.5">
              <h1 className="truncate text-xl font-semibold text-white">
                {player.username}
              </h1>
              <span
                className={`h-2.5 w-2.5 shrink-0 rounded-full ${
                  player.online
                    ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.5)]'
                    : 'bg-rose-500'
                }`}
                title={
                  player.online
                    ? player.afk
                      ? 'Online (AFK)'
                      : 'Online'
                    : 'Offline'
                }
              />
            </div>

            {allPlayerRanks.length > 0 && (
              <div className="flex flex-wrap items-center gap-1.5 pt-0.5">
                {allPlayerRanks.map(r => (
                  <Badge key={r.id} color={getMinecraftRankColor(r.color)}>
                    {r.name}
                  </Badge>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="space-y-2 border-t border-neutral-800 pt-4 text-xs text-gray-400">
          {player.online && formattedBiome && (
            <div className="flex items-center justify-between">
              <span>Biome</span>
              <span className="text-gray-300">{formattedBiome}</span>
            </div>
          )}
          <div className="flex items-center justify-between">
            <span>First Login</span>
            <span suppressHydrationWarning className="text-gray-300">
              {formatDate(player.firstLogin)}
            </span>
          </div>
          <div className="flex items-center justify-between">
            <span>Last Login</span>
            <span suppressHydrationWarning className="text-gray-300">
              {formatDate(player.lastLogin)}
            </span>
          </div>
        </div>
      </div>

      <div className="space-y-4">
        <div className="flex items-center gap-x-2 border-b border-neutral-800 pb-3">
          <button
            type="button"
            onClick={() => handleTabChange('stats')}
            className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
              activeTab === 'stats'
                ? 'border border-neutral-700 bg-white/[0.08] text-white'
                : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:border-neutral-800 hover:text-white'
            }`}
          >
            Statistics
          </button>
          <button
            type="button"
            onClick={() => handleTabChange('achievements')}
            className={`flex items-center gap-x-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
              activeTab === 'achievements'
                ? 'border border-neutral-700 bg-white/[0.08] text-white'
                : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:border-neutral-800 hover:text-white'
            }`}
          >
            <span>Achievements</span>
            {player.achievements && player.achievements.totalCount > 0 && (
              <span className="rounded-full bg-white/[0.08] px-1.5 py-0.5 text-[10px] font-semibold text-neutral-300">
                {player.achievements.completedCount}/{player.achievements.totalCount}
              </span>
            )}
          </button>
          <button
            type="button"
            onClick={() => handleTabChange('punishments')}
            className={`flex items-center gap-x-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
              activeTab === 'punishments'
                ? 'border border-neutral-700 bg-white/[0.08] text-white'
                : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:border-neutral-800 hover:text-white'
            }`}
          >
            <span>Punishments</span>
            {punishments.length > 0 && (
              <span
                className={`py-0.2 rounded-full px-1.5 text-[10px] font-semibold ${
                  activeInfractions > 0
                    ? 'bg-rose-500/20 text-rose-400'
                    : 'bg-white/[0.08] text-neutral-300'
                }`}
              >
                {punishments.length}
              </span>
            )}
          </button>
        </div>

        {activeTab === 'stats' ? (
          <Card>
            <CardHeader>
              <CardTitle>Statistics</CardTitle>
            </CardHeader>
            {statItems.length > 0 ? (
              <DataList items={statItems} />
            ) : (
              <p className="text-sm text-gray-500">
                No statistics recorded for this player.
              </p>
            )}
          </Card>
        ) : activeTab === 'achievements' ? (
          <div className="space-y-6">
            <Card className="space-y-4">
              <div className="flex flex-col gap-y-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h2 className="text-base font-semibold text-white">
                    Advancement Progress
                  </h2>
                  <p className="text-xs text-neutral-400">
                    {player.achievements?.completedCount ?? 0} of{' '}
                    {player.achievements?.totalCount ?? playerAchievements.length}{' '}
                    achievements completed
                  </p>
                </div>
                <div className="text-right">
                  <span className="text-2xl font-bold text-white">
                    {player.achievements?.percentage ?? 0}%
                  </span>
                </div>
              </div>

              <div className="h-2 w-full overflow-hidden rounded-full bg-neutral-800">
                <div
                  className="h-full rounded-full bg-emerald-500 transition-all duration-500"
                  style={{
                    width: `${Math.min(100, Math.max(0, player.achievements?.percentage ?? 0))}%`,
                  }}
                />
              </div>

              <div className="grid grid-cols-3 gap-2 pt-1 text-center text-xs">
                <div className="rounded-lg border border-neutral-800 bg-white/[0.02] p-2">
                  <span className="text-[10px] uppercase tracking-wider text-neutral-400">
                    Tasks
                  </span>
                  <p className="mt-0.5 text-sm font-semibold text-emerald-400">
                    {
                      playerAchievements.filter(
                        a => a.frame === 'TASK' && a.completed
                      ).length
                    }{' '}
                    /{' '}
                    {
                      playerAchievements.filter(a => a.frame === 'TASK').length
                    }
                  </p>
                </div>
                <div className="rounded-lg border border-neutral-800 bg-white/[0.02] p-2">
                  <span className="text-[10px] uppercase tracking-wider text-neutral-400">
                    Goals
                  </span>
                  <p className="mt-0.5 text-sm font-semibold text-amber-400">
                    {
                      playerAchievements.filter(
                        a => a.frame === 'GOAL' && a.completed
                      ).length
                    }{' '}
                    /{' '}
                    {
                      playerAchievements.filter(a => a.frame === 'GOAL').length
                    }
                  </p>
                </div>
                <div className="rounded-lg border border-neutral-800 bg-white/[0.02] p-2">
                  <span className="text-[10px] uppercase tracking-wider text-neutral-400">
                    Challenges
                  </span>
                  <p className="mt-0.5 text-sm font-semibold text-purple-400">
                    {
                      playerAchievements.filter(
                        a => a.frame === 'CHALLENGE' && a.completed
                      ).length
                    }{' '}
                    /{' '}
                    {
                      playerAchievements.filter(
                        a => a.frame === 'CHALLENGE'
                      ).length
                    }
                  </p>
                </div>
              </div>
            </Card>

            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex flex-wrap items-center gap-1.5">
                <button
                  type="button"
                  onClick={() => setAchievementStatus('all')}
                  className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
                    achievementStatus === 'all'
                      ? 'border border-neutral-700 bg-white/[0.08] text-white'
                      : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
                  }`}
                >
                  All ({playerAchievements.length})
                </button>
                <button
                  type="button"
                  onClick={() => setAchievementStatus('completed')}
                  className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
                    achievementStatus === 'completed'
                      ? 'border border-emerald-700/50 bg-emerald-500/20 text-emerald-300'
                      : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
                  }`}
                >
                  Completed (
                  {playerAchievements.filter(a => a.completed).length})
                </button>
                <button
                  type="button"
                  onClick={() => setAchievementStatus('locked')}
                  className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
                    achievementStatus === 'locked'
                      ? 'border border-neutral-700 bg-white/[0.08] text-white'
                      : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
                  }`}
                >
                  Locked (
                  {playerAchievements.filter(a => !a.completed).length})
                </button>
              </div>

              <div className="w-full sm:w-60">
                <input
                  type="text"
                  value={achievementSearch}
                  onChange={e => setAchievementSearch(e.target.value)}
                  placeholder="Search achievements..."
                  className="w-full rounded-lg border border-neutral-800 bg-[#121215] px-3 py-1.5 text-xs text-white placeholder-neutral-500 transition-colors focus:border-neutral-600 focus:outline-none"
                />
              </div>
            </div>

            {achievementCategories.length > 1 && (
              <div className="flex flex-wrap items-center gap-1.5 border-t border-neutral-800/80 pt-3">
                <button
                  type="button"
                  onClick={() => setAchievementCategory('all')}
                  className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
                    achievementCategory === 'all'
                      ? 'border border-neutral-700 bg-white/[0.08] text-white'
                      : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
                  }`}
                >
                  All Categories
                </button>
                {achievementCategories.map(cat => (
                  <button
                    key={cat.id}
                    type="button"
                    onClick={() => setAchievementCategory(cat.id)}
                    className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
                      achievementCategory === cat.id
                        ? 'border border-neutral-700 bg-white/[0.08] text-white'
                        : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
                    }`}
                  >
                    {cat.name}
                  </button>
                ))}
              </div>
            )}

            {filteredAchievements.length > 0 ? (
              <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                {filteredAchievements.map(a => {
                  const frameColor =
                    a.frame === 'CHALLENGE'
                      ? 'purple'
                      : a.frame === 'GOAL'
                        ? 'amber'
                        : 'emerald'
                  const frameLabel =
                    a.frame === 'CHALLENGE'
                      ? 'Challenge'
                      : a.frame === 'GOAL'
                        ? 'Goal'
                        : 'Task'

                  return (
                    <div
                      key={a.id}
                      className={`relative flex flex-col justify-between rounded-xl border p-4 transition-colors ${
                        a.completed
                          ? 'border-emerald-500/25 bg-emerald-950/[0.08] hover:border-emerald-500/40'
                          : 'border-neutral-800/80 bg-white/[0.01] opacity-65 hover:border-neutral-700 hover:opacity-90'
                      }`}
                    >
                      <div className="space-y-2">
                        <div className="flex items-center justify-between gap-x-2">
                          <div className="flex items-center gap-1.5">
                            <Badge color={frameColor}>{frameLabel}</Badge>
                            <span className="text-[11px] text-neutral-400">
                              {a.categoryName || a.category}
                            </span>
                          </div>

                          {a.completed ? (
                            <span className="inline-flex items-center gap-x-1 text-[11px] font-medium text-emerald-400">
                              <svg
                                className="h-3.5 w-3.5"
                                fill="none"
                                viewBox="0 0 24 24"
                                stroke="currentColor"
                                strokeWidth="2.5"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  d="M5 13l4 4L19 7"
                                />
                              </svg>
                              Completed
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-x-1 text-[11px] text-neutral-500">
                              <svg
                                className="h-3 w-3"
                                fill="none"
                                viewBox="0 0 24 24"
                                stroke="currentColor"
                                strokeWidth="2"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                                />
                              </svg>
                              Locked
                            </span>
                          )}
                        </div>

                        <div>
                          <h3 className="text-sm font-medium text-white">
                            {a.title}
                          </h3>
                          <p className="mt-0.5 text-xs text-neutral-400">
                            {a.description}
                          </p>
                        </div>
                      </div>

                      {a.completed && a.completedAt && a.completedAt > 0 && (
                        <div className="mt-3 border-t border-emerald-500/15 pt-2 text-[11px] text-neutral-400">
                          Unlocked{' '}
                          <span className="text-neutral-300">
                            {formatDate(a.completedAt)}
                          </span>
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            ) : (
              <Card className="py-12 text-center text-sm text-neutral-500">
                {achievementSearch.trim()
                  ? 'No achievements found matching your search.'
                  : 'No achievements found for this filter.'}
              </Card>
            )}
          </div>
        ) : (
          <div>
            {punishments.length > 0 ? (
              <Card className="overflow-hidden p-0">
                <div className="hidden overflow-x-auto md:block">
                  <table className="w-full text-left text-xs">
                    <thead className="border-b border-neutral-800 bg-white/[0.01] text-neutral-400">
                      <tr>
                        <th className="px-4 py-3 font-medium">Type</th>
                        <th className="px-4 py-3 font-medium">Reason</th>
                        <th className="px-4 py-3 font-medium">Staff</th>
                        <th className="px-4 py-3 font-medium">Date</th>
                        <th className="px-4 py-3 font-medium">Expires</th>
                        <th className="px-4 py-3 font-medium">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-neutral-800 text-neutral-300">
                      {punishments.map(p => {
                        const status = getPunishmentStatus(p)
                        const typeBadge = getPunishmentTypeBadge(p.type)
                        const statusBadge = getPunishmentStatusBadge(status)
                        const isConsole =
                          p.issuer.toLowerCase() === 'console' ||
                          p.issuer.toLowerCase() === 'system'

                        return (
                          <tr
                            key={p.id}
                            className="transition-colors hover:bg-white/[0.02]"
                          >
                            <td className="px-4 py-3">
                              <Badge color={typeBadge.color}>
                                {typeBadge.label}
                              </Badge>
                            </td>
                            <td
                              className="max-w-xs truncate px-4 py-3"
                              title={p.reason}
                            >
                              {p.reason}
                            </td>
                            <td className="px-4 py-3">
                              {isConsole ? (
                                <span className="font-medium text-neutral-400">
                                  Console
                                </span>
                              ) : (
                                <Link
                                  href={`/player/${encodeURIComponent(p.issuer)}`}
                                  className="group flex items-center gap-x-2.5 transition-opacity hover:opacity-80"
                                >
                                  <img
                                    src={`https://skins.mcstats.com/face/${p.issuerUuid || p.issuer}?size=32`}
                                    alt={p.issuer}
                                    className="h-6 w-6 shrink-0 rounded bg-neutral-900"
                                    style={{ imageRendering: 'pixelated' }}
                                  />
                                  <span className="font-medium text-white group-hover:underline">
                                    {p.issuer}
                                  </span>
                                </Link>
                              )}
                            </td>
                            <td className="whitespace-nowrap px-4 py-3 text-neutral-400">
                              {formatDate(p.created_at)}
                            </td>
                            <td className="whitespace-nowrap px-4 py-3 text-neutral-400">
                              {p.expires_at && p.expires_at > 0
                                ? formatDate(p.expires_at)
                                : status === 'ISSUED'
                                  ? 'N/A'
                                  : 'Permanent'}
                            </td>
                            <td className="px-4 py-3">
                              <Badge color={statusBadge.color}>
                                {statusBadge.label}
                              </Badge>
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                </div>

                <div className="divide-y divide-neutral-800 md:hidden">
                  {punishments.map(p => {
                    const status = getPunishmentStatus(p)
                    const typeBadge = getPunishmentTypeBadge(p.type)
                    const statusBadge = getPunishmentStatusBadge(status)
                    const isConsole =
                      p.issuer.toLowerCase() === 'console' ||
                      p.issuer.toLowerCase() === 'system'

                    return (
                      <div key={p.id} className="space-y-2.5 p-4 text-xs">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-1.5">
                            <Badge color={typeBadge.color}>
                              {typeBadge.label}
                            </Badge>
                            <Badge color={statusBadge.color}>
                              {statusBadge.label}
                            </Badge>
                          </div>
                          <span className="text-neutral-400">
                            {formatDate(p.created_at)}
                          </span>
                        </div>

                        <div className="text-neutral-300">
                          <span className="text-neutral-500">Reason: </span>
                          {p.reason}
                        </div>

                        <div className="flex items-center gap-x-2 text-neutral-400">
                          <span className="text-neutral-500">Staff:</span>
                          {isConsole ? (
                            <span className="font-medium text-neutral-300">
                              Console
                            </span>
                          ) : (
                            <Link
                              href={`/player/${encodeURIComponent(p.issuer)}`}
                              className="flex items-center gap-x-1.5 font-medium text-white hover:underline"
                            >
                              <img
                                src={`https://skins.mcstats.com/face/${p.issuerUuid || p.issuer}?size=32`}
                                alt={p.issuer}
                                className="h-6 w-6 shrink-0 rounded bg-neutral-900"
                                style={{ imageRendering: 'pixelated' }}
                              />
                              <span>{p.issuer}</span>
                            </Link>
                          )}
                        </div>
                      </div>
                    )
                  })}
                </div>
              </Card>
            ) : (
              <Card className="py-12 text-center text-sm text-neutral-500">
                No punishments recorded for this player.
              </Card>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
