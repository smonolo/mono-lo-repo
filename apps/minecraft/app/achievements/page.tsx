import type { Metadata } from 'next'
import AchievementsView from '@/components/AchievementsView'
import { fetchAchievements } from '@/lib/api'

export const dynamic = 'force-dynamic'
export const revalidate = 0

export const metadata: Metadata = {
  title: 'Achievements',
  description:
    'Explore all Minecraft advancements, server milestones, and player unlocks',
  robots: {
    index: false,
    follow: false,
  },
}

export default async function AchievementsPage() {
  const data = await fetchAchievements()

  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold tracking-tight text-white">
          Achievements
        </h1>
        <p className="text-sm text-neutral-400">
          Server-wide Minecraft advancements, milestones, and player unlock
          statistics.
        </p>
      </div>

      <AchievementsView
        achievements={data.achievements}
        categories={data.categories}
        globalStats={data.globalStats}
      />
    </div>
  )
}
