'use client'

/* eslint-disable @next/next/no-img-element */

import type { PlayerData, RankData } from '@/types/minecraft'
import {
  formatDate,
  formatDistance,
  formatDuration,
  formatNumber,
  getMinecraftRankColor,
} from '@/utils/minecraft'
import { Card, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { DataList, type DataListItem } from '@/components/ui/DataList'

type Props = {
  player: PlayerData
}

export default function PlayerView({ player }: Props) {
  const allPlayerRanks: RankData[] = []
  if (player.primaryRank) {
    allPlayerRanks.push({ ...player.primaryRank, primary: true })
  } else if (player.rank) {
    allPlayerRanks.push({ ...player.rank, primary: true })
  }

  if (player.ranks) {
    for (const r of player.ranks) {
      if (!allPlayerRanks.some(existing => existing.id === r.id)) {
        allPlayerRanks.push(r)
      }
    }
  }

  const stats = player.stats
  const statItems: DataListItem[] = stats
    ? [
        { label: 'Play Time', value: formatDuration(stats.playTimeSeconds) },
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
    : []

  return (
    <div className="grid grid-cols-1 items-start gap-8 md:grid-cols-[220px_1fr] md:gap-10">
      {/* Left Column / Top Section on Mobile */}
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

      {/* Right Column / Stats Section */}
      <Card>
        <CardHeader>
          <CardTitle>Stats</CardTitle>
        </CardHeader>
        {statItems.length > 0 ? (
          <DataList items={statItems} />
        ) : (
          <p className="text-sm text-gray-500">
            No statistics recorded for this player.
          </p>
        )}
      </Card>
    </div>
  )
}
