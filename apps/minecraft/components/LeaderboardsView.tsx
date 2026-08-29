'use client'

/* eslint-disable @next/next/no-img-element */

import Link from 'next/link'
import { useState } from 'react'
import type { StatisticLeaderboard } from '@/types/minecraft'
import { Card, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { getMinecraftRankColor } from '@/utils/minecraft'

type Props = {
  leaderboards: StatisticLeaderboard[]
}

export default function LeaderboardsView({ leaderboards }: Props) {
  const [selectedCategory, setSelectedCategory] = useState<string>('all')

  const categories: { id: string; label: string; keys: string[] }[] = [
    {
      id: 'all',
      label: 'All Stats',
      keys: leaderboards.map(l => l.key),
    },
    {
      id: 'combat',
      label: 'Combat',
      keys: [
        'player_kills',
        'mob_kills',
        'damage_dealt',
        'damage_taken',
        'deaths',
        'time_since_death',
        'raids_won',
      ],
    },
    {
      id: 'movement',
      label: 'Travel',
      keys: ['walk_distance', 'sprint_distance', 'fly_distance', 'jumps'],
    },
    {
      id: 'activities',
      label: 'Activities',
      keys: [
        'play_time',
        'chests_opened',
        'items_enchanted',
        'fish_caught',
        'animals_bred',
        'trades',
        'sleeps',
      ],
    },
  ]

  const activeCategory =
    categories.find(c => c.id === selectedCategory) || categories[0]
  const displayedLeaderboards = leaderboards.filter(l =>
    activeCategory.keys.includes(l.key)
  )

  const getRankBadgeStyle = (rank: number) => {
    switch (rank) {
      case 1:
        return 'text-amber-400 font-semibold'
      case 2:
        return 'text-neutral-300 font-medium'
      case 3:
        return 'text-amber-600 font-medium'
      default:
        return 'text-neutral-500 text-xs'
    }
  }

  return (
    <div className="space-y-8">
      {/* Category Pills */}
      <div className="flex flex-wrap items-center gap-2">
        {categories.map(cat => {
          const isActive = cat.id === selectedCategory
          return (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                isActive
                  ? 'border border-neutral-700 bg-white/[0.08] text-white'
                  : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:border-neutral-800 hover:text-white'
              }`}
            >
              {cat.label}
            </button>
          )
        })}
      </div>

      {/* Leaderboards Grid */}
      {displayedLeaderboards.length > 0 ? (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {displayedLeaderboards.map(board => (
            <Card key={board.key} className="flex flex-col justify-between">
              <div>
                <CardHeader>
                  <CardTitle>{board.name}</CardTitle>
                  <p className="text-xs text-neutral-400">
                    {board.description}
                  </p>
                </CardHeader>

                {board.top.length > 0 ? (
                  <div className="divide-y divide-neutral-800 text-sm">
                    {board.top.map((entry, idx) => {
                      const rankNum =
                        typeof entry.position === 'number'
                          ? entry.position
                          : typeof entry.rank === 'number'
                            ? entry.rank
                            : idx + 1
                      const playerRank =
                        entry.rankData ||
                        entry.playerRank ||
                        (typeof entry.rank === 'object'
                          ? (entry.rank as any)
                          : undefined)

                      return (
                        <div
                          key={`${board.key}_${entry.uuid}`}
                          className="flex items-center justify-between py-2"
                        >
                          <div className="flex items-center gap-x-2.5">
                            <span
                              className={`w-5 shrink-0 text-center text-xs ${getRankBadgeStyle(
                                rankNum
                              )}`}
                            >
                              #{rankNum}
                            </span>

                            <Link
                              href={`/player/${encodeURIComponent(entry.username)}`}
                              className="group flex items-center gap-x-2 transition-opacity hover:opacity-80"
                            >
                              <img
                                src={`https://skins.mcstats.com/face/${entry.uuid}?size=32`}
                                alt={entry.username}
                                className="h-5 w-5 shrink-0 rounded bg-neutral-900"
                                style={{ imageRendering: 'pixelated' }}
                              />
                              <span className="truncate text-xs font-medium text-white group-hover:underline sm:text-sm">
                                {entry.username}
                              </span>
                            </Link>

                            {playerRank && (
                              <Badge
                                color={getMinecraftRankColor(playerRank.color)}
                                className="hidden scale-90 sm:inline-flex"
                              >
                                {playerRank.name}
                              </Badge>
                            )}
                          </div>

                          <span
                            suppressHydrationWarning
                            className="shrink-0 text-xs font-medium text-neutral-300 sm:text-sm"
                          >
                            {entry.formattedValue}
                          </span>
                        </div>
                      )
                    })}
                  </div>
                ) : (
                  <p className="py-4 text-center text-xs text-neutral-500">
                    No records logged yet.
                  </p>
                )}
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <p className="py-12 text-center text-sm text-neutral-500">
          No statistics available.
        </p>
      )}
    </div>
  )
}
