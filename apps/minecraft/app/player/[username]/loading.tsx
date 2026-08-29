import { Card, CardHeader, CardTitle } from '@/components/ui/Card'

export default function PlayerLoading() {
  return (
    <div className="grid grid-cols-1 items-start gap-8 md:grid-cols-[220px_1fr] md:gap-10">
      {/* Left Column Skeleton */}
      <div className="flex flex-col space-y-4">
        <div className="flex items-center gap-x-4 md:flex-col md:items-start md:gap-x-0 md:space-y-4">
          <div className="h-20 w-20 animate-pulse rounded-lg bg-neutral-900 sm:h-28 sm:w-28 md:h-32 md:w-32" />
          <div className="space-y-2">
            <div className="h-6 w-32 animate-pulse rounded bg-neutral-900" />
            <div className="h-4 w-16 animate-pulse rounded bg-neutral-900" />
          </div>
        </div>

        <div className="space-y-2 border-t border-neutral-800 pt-4">
          <div className="h-4 w-full animate-pulse rounded bg-neutral-900" />
          <div className="h-4 w-full animate-pulse rounded bg-neutral-900" />
        </div>
      </div>

      {/* Right Column Skeleton */}
      <Card>
        <CardHeader>
          <CardTitle>Stats</CardTitle>
        </CardHeader>
        <div className="divide-y divide-neutral-800 text-sm">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="flex items-center justify-between py-2.5">
              <div className="h-4 w-24 animate-pulse rounded bg-neutral-900" />
              <div className="h-4 w-16 animate-pulse rounded bg-neutral-900" />
            </div>
          ))}
        </div>
      </Card>
    </div>
  )
}
