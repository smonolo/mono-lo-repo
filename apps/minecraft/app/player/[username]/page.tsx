import { Suspense } from 'react'
import type { Metadata } from 'next'
import Link from 'next/link'
import PlayerView from '@/components/PlayerView'
import SearchBar from '@/components/SearchBar'
import { fetchPlayer } from '@/lib/api'

type Props = {
  params: Promise<{
    username: string
  }>
}

export const dynamic = 'force-dynamic'
export const revalidate = 0

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { username: rawUsername } = await params
  const username = decodeURIComponent(rawUsername)
  return {
    title: username,
    description: `Minecraft profile, statistics, and telemetry for ${username}`,
    robots: {
      index: false,
      follow: false,
    },
    openGraph: {
      title: `${username} - Minecraft Profile`,
      description: `Minecraft profile, statistics, and telemetry for ${username}`,
      images: [
        {
          url: `https://skins.mcstats.com/face/${encodeURIComponent(username)}?size=256`,
          width: 256,
          height: 256,
          alt: `${username}'s Minecraft skin`,
        },
      ],
    },
  }
}

export default async function PlayerPage({ params }: Props) {
  const { username: rawUsername } = await params
  const username = decodeURIComponent(rawUsername)
  const player = await fetchPlayer(username)

  if (!player) {
    return (
      <div className="flex min-h-[50vh] flex-col items-center justify-center py-12 text-center">
        <div className="w-full max-w-xl space-y-6">
          <div className="space-y-2">
            <h1 className="text-3xl font-semibold tracking-tight text-white">
              Player Not Found
            </h1>
            <p className="text-sm text-gray-400">
              No server records found for{' '}
              <span className="text-white">&quot;{username}&quot;</span>.
            </p>
          </div>

          <SearchBar
            size="lg"
            placeholder="Search for another player..."
            autoFocus
          />

          <div className="pt-2">
            <Link
              href="/"
              className="text-xs text-gray-400 underline transition-colors hover:text-white"
            >
              Back to Home
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <Suspense fallback={null}>
        <PlayerView player={player} />
      </Suspense>
    </div>
  )
}
