import type { Metadata } from 'next'
import LeaderboardsView from '@/components/LeaderboardsView'
import { fetchLeaderboards } from '@/lib/api'

export const dynamic = 'force-dynamic'
export const revalidate = 0

export const metadata: Metadata = {
  title: 'Leaderboards',
  description: 'Top 10 Minecraft player rankings for all server statistics',
  robots: {
    index: false,
    follow: false,
  },
}

export default async function LeaderboardsPage() {
  const data = await fetchLeaderboards()

  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold tracking-tight text-white">
          Leaderboards
        </h1>
        <p className="text-sm text-neutral-400">
          Top 10 players across all server statistics and telemetry records.
        </p>
      </div>

      <LeaderboardsView leaderboards={data.leaderboards} />
    </div>
  )
}
