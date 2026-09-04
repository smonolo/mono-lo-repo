import type { Metadata } from 'next'
import Link from 'next/link'
import SearchBar from '@/components/SearchBar'
import CopyServerIp from '@/components/CopyServerIp'
import { Badge } from '@/components/ui/Badge'
import { fetchOnlinePlayers } from '@/lib/api'

export const dynamic = 'force-dynamic'
export const revalidate = 0

export const metadata: Metadata = {
  title: 'Home',
  description:
    'Look up statistics, playtime, telemetry, and records for players on the server.',
}

export default async function HomePage() {
  const data = await fetchOnlinePlayers()

  const players = data.players || []
  const onlinePlayers = players.filter(p => p.online)
  const onlineCount =
    typeof data.onlineCount === 'number'
      ? data.onlineCount
      : onlinePlayers.length

  return (
    <div className="mx-auto flex w-full max-w-5xl flex-col items-center justify-center space-y-12 py-6 sm:py-12">
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
    </div>
  )
}
