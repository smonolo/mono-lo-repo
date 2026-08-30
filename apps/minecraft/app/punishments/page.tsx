import type { Metadata } from 'next'
import PunishmentsView from '@/components/PunishmentsView'
import { fetchPunishments } from '@/lib/api'

export const dynamic = 'force-dynamic'
export const revalidate = 0

export const metadata: Metadata = {
  title: 'Punishments',
  description: 'Public record of all moderation actions and player infractions',
  robots: {
    index: false,
    follow: false,
  },
}

export default async function PunishmentsPage() {
  const data = await fetchPunishments()

  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold tracking-tight text-white">
          Punishments
        </h1>
        <p className="text-sm text-neutral-400">
          Public record of all moderation actions, bans, mutes, and player
          infractions.
        </p>
      </div>

      <PunishmentsView initialPunishments={data.punishments} />
    </div>
  )
}
