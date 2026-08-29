import Link from 'next/link'
import SearchBar from '@/components/SearchBar'

export default function NotFound() {
  return (
    <div className="flex min-h-[50vh] flex-col items-center justify-center py-12 text-center">
      <div className="w-full max-w-xl space-y-6">
        <div className="space-y-2">
          <h1 className="text-3xl font-semibold tracking-tight text-white">
            Page Not Found
          </h1>
          <p className="text-sm text-gray-400">
            The page you are looking for does not exist.
          </p>
        </div>

        <SearchBar size="lg" placeholder="Search for a player..." autoFocus />

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
