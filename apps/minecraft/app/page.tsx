import type { Metadata } from 'next'
import SearchBar from '@/components/SearchBar'

export const metadata: Metadata = {
  title: 'Search Player',
  description:
    'Look up statistics, playtime, and telemetry for players on the server.',
}

export default function HomePage() {
  return (
    <div className="flex min-h-[50vh] flex-col items-center justify-center py-12 text-center">
      <div className="w-full max-w-xl space-y-6">
        <div className="space-y-2">
          <h1 className="text-3xl font-semibold tracking-tight text-white">
            Search Player
          </h1>
          <p className="text-sm text-gray-400">
            Look up statistics, playtime, and telemetry for players on the
            server.
          </p>
        </div>

        <SearchBar
          size="lg"
          placeholder="Enter Minecraft username..."
          autoFocus
        />
      </div>
    </div>
  )
}
