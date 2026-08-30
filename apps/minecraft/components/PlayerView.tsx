'use client'

import Link from 'next/link'
import { useRouter, useSearchParams, usePathname } from 'next/navigation'
import { useCallback, useMemo } from 'react'
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

type Tab = 'stats' | 'punishments'

export default function PlayerView({ player }: Props) {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const tabParam = searchParams.get('tab')
  const activeTab: Tab = tabParam === 'punishments' ? 'punishments' : 'stats'

  const handleTabChange = useCallback(
    (newTab: Tab) => {
      const params = new URLSearchParams(searchParams.toString())
      if (newTab === 'punishments') {
        params.set('tab', 'punishments')
      } else {
        params.delete('tab')
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
  const statItems: DataListItem[] = useMemo(
    () =>
      stats
        ? [
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
            { label: 'Jumps', value: formatNumber(stats.jumps) },
            {
              label: 'Walk Distance',
              value: formatDistance(stats.walkDistanceMeters),
            },
            {
              label: 'Fly Distance',
              value: formatDistance(stats.flyDistanceMeters),
            },
            {
              label: 'Time Since Rest',
              value: formatDuration(stats.timeSinceRestSeconds),
            },
          ]
        : [],
    [stats]
  )

  const activeInfractions = useMemo(
    () => punishments.filter(p => getPunishmentStatus(p) === 'ACTIVE').length,
    [punishments]
  )

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
