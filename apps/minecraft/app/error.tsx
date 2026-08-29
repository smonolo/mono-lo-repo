'use client'

import Link from 'next/link'

type Props = {
  error: Error & { digest?: string }
  reset: () => void
}

export default function ErrorPage({ reset }: Props) {
  return (
    <div className="space-y-4 py-8">
      <h1 className="text-2xl font-semibold text-white">
        Something went wrong
      </h1>
      <p className="text-sm text-gray-400">
        An error occurred while communicating with the server.
      </p>
      <div className="flex items-center gap-x-4 pt-2">
        <button
          onClick={() => reset()}
          className="text-sm text-gray-300 underline transition-colors hover:text-white"
        >
          Try again
        </button>
        <Link
          href="/"
          className="text-sm text-gray-400 underline transition-colors hover:text-white"
        >
          Back to Home
        </Link>
      </div>
    </div>
  )
}
