'use client'

import { useMemo } from 'react'
import type { WorldResponse, DimensionData } from '@/types/minecraft'
import { Card, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { DataList, type DataListItem } from '@/components/ui/DataList'
import {
  formatDistance,
  formatDuration,
  formatNumber,
  formatWorldName,
} from '@/utils/minecraft'

type Props = {
  worldData: WorldResponse
}

export default function WorldView({ worldData }: Props) {
  const {
    online = false,
    worldAge,
    time,
    moonPhase,
    weather,
    dimensions = [],
    aggregates = {},
  } = worldData || {}

  const weatherBadge = useMemo(() => {
    if (!weather) return { label: 'Unknown', color: '#9CA3AF' }
    if (weather.isThundering) return { label: 'Thunderstorm', color: '#EF4444' }
    if (weather.isRaining) return { label: 'Raining', color: '#38BDF8' }
    return { label: 'Clear Skies', color: '#22C55E' }
  }, [weather])

  const timePhaseBadge = useMemo(() => {
    if (!time) return { label: 'Day', color: '#F59E0B' }
    if (time.isDay) return { label: time.phase || 'Day', color: '#F59E0B' }
    return { label: time.phase || 'Night', color: '#818CF8' }
  }, [time])

  const aggregateCombatItems: DataListItem[] = useMemo(() => {
    const items: DataListItem[] = []
    if (aggregates.play_time) {
      items.push({
        label: 'Total Playtime',
        value: formatDuration(Math.floor(aggregates.play_time / 20)),
      })
    }
    if (aggregates.mob_kills) {
      items.push({
        label: 'Total Mob Kills',
        value: formatNumber(aggregates.mob_kills),
      })
    }
    if (aggregates.player_kills) {
      items.push({
        label: 'Total Player Kills',
        value: formatNumber(aggregates.player_kills),
      })
    }
    if (aggregates.deaths) {
      items.push({
        label: 'Total Player Deaths',
        value: formatNumber(aggregates.deaths),
      })
    }
    if (aggregates.damage_dealt) {
      items.push({
        label: 'Damage Dealt',
        value: formatNumber(Math.round(aggregates.damage_dealt)),
      })
    }
    if (aggregates.damage_taken) {
      items.push({
        label: 'Damage Taken',
        value: formatNumber(Math.round(aggregates.damage_taken)),
      })
    }
    if (aggregates.damage_blocked) {
      items.push({
        label: 'Damage Blocked by Shield',
        value: formatNumber(Math.round(aggregates.damage_blocked)),
      })
    }
    if (aggregates.raids_won) {
      items.push({
        label: 'Raids Won',
        value: formatNumber(aggregates.raids_won),
      })
    }
    return items
  }, [aggregates])

  const aggregateTravelItems: DataListItem[] = useMemo(() => {
    const items: DataListItem[] = []
    if (aggregates.walk_distance) {
      items.push({
        label: 'Distance Walked',
        value: formatDistance(aggregates.walk_distance / 100),
      })
    }
    if (aggregates.sprint_distance) {
      items.push({
        label: 'Distance Sprinted',
        value: formatDistance(aggregates.sprint_distance / 100),
      })
    }
    if (aggregates.elytra_distance) {
      items.push({
        label: 'Elytra Gliding',
        value: formatDistance(aggregates.elytra_distance / 100),
      })
    } else if (aggregates.fly_distance) {
      items.push({
        label: 'Distance Flown',
        value: formatDistance(aggregates.fly_distance / 100),
      })
    }
    if (aggregates.boat_distance) {
      items.push({
        label: 'Distance Sailed',
        value: formatDistance(aggregates.boat_distance / 100),
      })
    }
    if (aggregates.minecart_distance) {
      items.push({
        label: 'Railways Traveled',
        value: formatDistance(aggregates.minecart_distance / 100),
      })
    }
    if (aggregates.horse_distance) {
      items.push({
        label: 'Mounts Ridden',
        value: formatDistance(aggregates.horse_distance / 100),
      })
    }
    if (aggregates.swim_distance) {
      items.push({
        label: 'Distance Swum',
        value: formatDistance(aggregates.swim_distance / 100),
      })
    }
    if (aggregates.jumps) {
      items.push({
        label: 'Total Jumps',
        value: formatNumber(aggregates.jumps),
      })
    }
    return items
  }, [aggregates])

  const aggregateLifeItems: DataListItem[] = useMemo(() => {
    const items: DataListItem[] = []
    if (aggregates.sleeps) {
      items.push({
        label: 'Nights Slept',
        value: formatNumber(aggregates.sleeps),
      })
    }
    if (aggregates.chests_opened) {
      items.push({
        label: 'Chests Opened',
        value: formatNumber(aggregates.chests_opened),
      })
    }
    if (aggregates.items_enchanted) {
      items.push({
        label: 'Items Enchanted',
        value: formatNumber(aggregates.items_enchanted),
      })
    }
    if (aggregates.trades) {
      items.push({
        label: 'Villager Trades',
        value: formatNumber(aggregates.trades),
      })
    }
    if (aggregates.animals_bred) {
      items.push({
        label: 'Animals Bred',
        value: formatNumber(aggregates.animals_bred),
      })
    }
    if (aggregates.fish_caught) {
      items.push({
        label: 'Fish Caught',
        value: formatNumber(aggregates.fish_caught),
      })
    }
    if (aggregates.tools_broken) {
      items.push({
        label: 'Tools Broken',
        value: formatNumber(aggregates.tools_broken),
      })
    }
    if (aggregates.music_discs_played) {
      items.push({
        label: 'Music Records Played',
        value: formatNumber(aggregates.music_discs_played),
      })
    }
    return items
  }, [aggregates])

  if (!online) {
    return (
      <div className="rounded-xl border border-neutral-800 bg-white/[0.02] p-8 text-center">
        <p className="text-sm text-neutral-400">
          Server is offline or world data is currently unavailable.
        </p>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      {/* Real-time Environment Overview Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card className="flex flex-col justify-between">
          <div className="space-y-1">
            <span className="text-xs font-medium uppercase tracking-wider text-neutral-400">
              World Age
            </span>
            <div className="text-2xl font-bold tracking-tight text-white">
              {worldAge?.formatted || 'Day 1'}
            </div>
          </div>
          <p className="mt-3 text-xs text-neutral-400">
            {worldAge?.ticks
              ? `${formatNumber(worldAge.ticks)} world ticks elapsed`
              : 'Continuous game time'}
          </p>
        </Card>

        <Card className="flex flex-col justify-between">
          <div className="space-y-1">
            <div className="flex items-center justify-between">
              <span className="text-xs font-medium uppercase tracking-wider text-neutral-400">
                In-Game Time
              </span>
              <Badge color={timePhaseBadge.color}>{timePhaseBadge.label}</Badge>
            </div>
            <div className="text-2xl font-bold tracking-tight text-white">
              {time?.timeOfDay || '12:00'}
            </div>
          </div>
          <p className="mt-3 text-xs text-neutral-400">
            {time?.isDay ? 'Sunlit hours' : 'Monsters actively roaming'}
          </p>
        </Card>

        <Card className="flex flex-col justify-between">
          <div className="space-y-1">
            <span className="text-xs font-medium uppercase tracking-wider text-neutral-400">
              Lunar Phase
            </span>
            <div className="text-2xl font-bold tracking-tight text-white">
              {moonPhase?.name || 'Full Moon'}
            </div>
          </div>
          <p className="mt-3 text-xs text-neutral-400">
            Phase #{typeof moonPhase?.phase === 'number' ? moonPhase.phase : 0}{' '}
            (affects slime spawns & difficulty)
          </p>
        </Card>

        <Card className="flex flex-col justify-between">
          <div className="space-y-1">
            <div className="flex items-center justify-between">
              <span className="text-xs font-medium uppercase tracking-wider text-neutral-400">
                Weather
              </span>
              <Badge color={weatherBadge.color}>{weatherBadge.label}</Badge>
            </div>
            <div className="text-2xl font-bold tracking-tight text-white">
              {weather?.status || 'Clear'}
            </div>
          </div>
          <p className="mt-3 text-xs text-neutral-400">
            {weather?.weatherDurationSeconds
              ? `Forecast window: ${formatDuration(weather.weatherDurationSeconds)}`
              : 'Atmospheric conditions stable'}
          </p>
        </Card>
      </div>

      {/* Dimensions Breakdown */}
      <div className="space-y-4">
        <h2 className="text-lg font-semibold tracking-tight text-white">
          Active Dimensions
        </h2>
        <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
          {dimensions.map((dim: DimensionData) => {
            const dimItems: DataListItem[] = [
              {
                label: 'Environment',
                value: dim.environment,
              },
              {
                label: 'Difficulty',
                value: dim.difficulty,
              },
              {
                label: 'Loaded Chunks',
                value: formatNumber(dim.loadedChunks),
              },
              {
                label: 'Active Entities',
                value: `${formatNumber(dim.livingEntitiesCount)} living (${formatNumber(dim.entitiesCount)} total)`,
              },
              {
                label: 'Height Range',
                value: `Y ${dim.minHeight} to ${dim.maxHeight}`,
              },
              {
                label: 'Sea Level',
                value: `Y ${dim.seaLevel}`,
              },
            ]

            if (dim.worldBorder?.size) {
              dimItems.push({
                label: 'World Border',
                value: `±${formatNumber(Math.round(dim.worldBorder.size / 2))} blocks`,
              })
            }

            if (dim.spawn) {
              dimItems.push({
                label: 'Spawn Point',
                value: `${dim.spawn.x}, ${dim.spawn.y}, ${dim.spawn.z}`,
              })
            }

            return (
              <Card key={dim.name} className="flex flex-col justify-between">
                <div>
                  <CardHeader className="flex flex-row items-center justify-between pb-2">
                    <CardTitle className="text-base font-semibold">
                      {formatWorldName(dim.name)}
                    </CardTitle>
                    <div className="flex items-center gap-1.5">
                      {dim.pvp && <Badge color="#EF4444">PvP</Badge>}
                      {dim.hardcore && <Badge color="#A855F7">Hardcore</Badge>}
                      {dim.playersCount > 0 && (
                        <Badge color="#10B981">{dim.playersCount} online</Badge>
                      )}
                    </div>
                  </CardHeader>
                  <DataList items={dimItems} />
                </div>
              </Card>
            )
          })}
        </div>
      </div>

      {/* Global Community Totals */}
      {(aggregateCombatItems.length > 0 ||
        aggregateTravelItems.length > 0 ||
        aggregateLifeItems.length > 0) && (
        <div className="space-y-4">
          <div className="space-y-1">
            <h2 className="text-lg font-semibold tracking-tight text-white">
              Server-Wide Community Totals
            </h2>
            <p className="text-xs text-neutral-400">
              Aggregated native statistics recorded across all registered
              players since world genesis.
            </p>
          </div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
            {aggregateCombatItems.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Combat & Defense</CardTitle>
                </CardHeader>
                <DataList items={aggregateCombatItems} />
              </Card>
            )}

            {aggregateTravelItems.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Travel & Exploration</CardTitle>
                </CardHeader>
                <DataList items={aggregateTravelItems} />
              </Card>
            )}

            {aggregateLifeItems.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Building & Economy</CardTitle>
                </CardHeader>
                <DataList items={aggregateLifeItems} />
              </Card>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
