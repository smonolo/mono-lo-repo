import type { Metadata } from 'next'
import WorldView from '@/components/WorldView'
import { fetchWorldStats } from '@/lib/api'

export const dynamic = 'force-dynamic'
export const revalidate = 0

export const metadata: Metadata = {
  title: 'World Telemetry',
  description: 'Minecraft server world telemetry, dimensions, environment, and global statistics',
  robots: {
    index: false,
    follow: false,
  },
}

export default async function WorldPage() {
  const worldData = await fetchWorldStats()

  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold tracking-tight text-white">
          World Telemetry
        </h1>
        <p className="text-sm text-neutral-400">
          Real-time environment cycle, dimension telemetry, and global server milestones.
        </p>
      </div>

      <WorldView worldData={worldData} />
    </div>
  )
}
