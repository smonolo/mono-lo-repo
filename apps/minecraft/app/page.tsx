import type { Metadata } from 'next'
import Link from 'next/link'
import SearchBar from '@/components/SearchBar'
import CopyServerIp from '@/components/CopyServerIp'
import { Card } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { fetchOnlinePlayers } from '@/lib/api'

export const revalidate = 15

export const metadata: Metadata = {
  title: 'Home',
  description:
    'Look up statistics, playtime, telemetry, and records for players on the server.',
}

export default async function HomePage() {
  const data = await fetchOnlinePlayers()
  const players = data.players || []
  const onlinePlayers = players.filter(p => p.online)
  const totalCount = data.count || players.length
  const onlineCount =
    typeof data.onlineCount === 'number'
      ? data.onlineCount
      : onlinePlayers.length

  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col items-center justify-center space-y-12 py-6 sm:py-12">
      <div className="w-full max-w-xl space-y-6 text-center">
        <div className="flex justify-center">
          <CopyServerIp
            ip="mc.smnl.dev"
            onlineCount={onlineCount}
            isOnline={data.online}
          />
        </div>

        <div className="space-y-2">
          <h1 className="text-3xl font-semibold tracking-tight text-white sm:text-4xl">
            Search Player
          </h1>
          <p className="text-sm text-neutral-400">
            Look up statistics, playtime, and telemetry records for players on
            the server.
          </p>
        </div>

        <SearchBar
          size="lg"
          placeholder="Enter Minecraft username..."
          autoFocus
        />

        {onlinePlayers.length > 0 && (
          <div className="flex flex-col items-center gap-y-2 pt-2 text-xs text-neutral-400">
            <span className="text-[11px] font-medium uppercase tracking-wider text-neutral-500">
              Online Right Now
            </span>
            <div className="flex flex-wrap items-center justify-center gap-2">
              {onlinePlayers.slice(0, 8).map(player => (
                <Link
                  key={player.uuid}
                  href={`/player/${encodeURIComponent(player.username)}`}
                  className="group flex items-center gap-x-2 rounded-lg border border-neutral-800 bg-white/[0.02] px-2.5 py-1.5 transition-all hover:border-neutral-700 hover:bg-white/[0.05]"
                >
                  <img
                    src={`https://skins.mcstats.com/face/${player.uuid}?size=32`}
                    alt={player.username}
                    className="h-5 w-5 shrink-0 rounded bg-neutral-900"
                    style={{ imageRendering: 'pixelated' }}
                  />
                  <span className="font-medium text-neutral-300 group-hover:text-white">
                    {player.username}
                  </span>
                  {player.rank?.name && (
                    <Badge color={player.rank.color} className="scale-90">
                      {player.rank.name}
                    </Badge>
                  )}
                </Link>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="grid w-full grid-cols-1 gap-4 sm:grid-cols-3">
        <Link href="/players" className="group block">
          <Card className="h-full space-y-2 p-4 transition-all group-hover:border-neutral-700 group-hover:bg-white/[0.03]">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-white group-hover:text-blue-400">
                Players
              </span>
              <span className="text-[11px] font-medium text-neutral-500">
                {totalCount} registered
              </span>
            </div>
            <p className="text-xs text-neutral-400">
              Directory of all registered players, online status, ranks, and
              live telemetry.
            </p>
          </Card>
        </Link>

        <Link href="/leaderboards" className="group block">
          <Card className="h-full space-y-2 p-4 transition-all group-hover:border-neutral-700 group-hover:bg-white/[0.03]">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-white group-hover:text-amber-400">
                Leaderboards
              </span>
              <span className="text-[11px] font-medium text-neutral-500">
                Top rankings
              </span>
            </div>
            <p className="text-xs text-neutral-400">
              Top 10 player rankings in combat, survival travel, playtime, and
              activity records.
            </p>
          </Card>
        </Link>

        <Link href="/punishments" className="group block">
          <Card className="h-full space-y-2 p-4 transition-all group-hover:border-neutral-700 group-hover:bg-white/[0.03]">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-white group-hover:text-rose-400">
                Punishments
              </span>
              <span className="text-[11px] font-medium text-neutral-500">
                Public log
              </span>
            </div>
            <p className="text-xs text-neutral-400">
              Public record of moderation actions, bans, mutes, freezes, kicks,
              and warnings.
            </p>
          </Card>
        </Link>
      </div>
    </div>
  )
}
