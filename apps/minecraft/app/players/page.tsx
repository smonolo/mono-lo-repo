import type { Metadata } from 'next'
import PlayersView from '@/components/PlayersView'
import { fetchOnlinePlayers } from '@/lib/api'

export const revalidate = 15

export const metadata: Metadata = {
  title: 'Players',
  description:
    'Directory of all players on the server, online status, and ranks',
}

export default async function PlayersPage() {
  const data = await fetchOnlinePlayers()

  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold tracking-tight text-white">
          Players
        </h1>
        <p className="text-sm text-neutral-400">
          Directory of registered players, online status, and ranks.
        </p>
      </div>

      <PlayersView
        initialPlayers={data.players || []}
        initialOnlineCount={data.onlineCount}
      />
    </div>
  )
}
