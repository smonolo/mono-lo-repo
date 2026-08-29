import { Card, CardHeader } from '@/components/ui/Card'

export default function LeaderboardsLoading() {
  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <div className="h-8 w-48 animate-pulse rounded bg-neutral-900" />
        <div className="h-4 w-72 animate-pulse rounded bg-neutral-900" />
      </div>

      <div className="flex gap-2">
        {[...Array(4)].map((_, i) => (
          <div
            key={i}
            className="h-8 w-20 animate-pulse rounded-lg bg-neutral-900"
          />
        ))}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {[...Array(6)].map((_, i) => (
          <Card key={i}>
            <CardHeader>
              <div className="h-5 w-32 animate-pulse rounded bg-neutral-900" />
              <div className="h-3 w-48 animate-pulse rounded bg-neutral-900" />
            </CardHeader>
            <div className="divide-y divide-neutral-800 text-sm">
              {[...Array(5)].map((_, j) => (
                <div key={j} className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-x-2.5">
                    <div className="h-4 w-4 animate-pulse rounded bg-neutral-900" />
                    <div className="h-5 w-5 animate-pulse rounded bg-neutral-900" />
                    <div className="h-4 w-20 animate-pulse rounded bg-neutral-900" />
                  </div>
                  <div className="h-4 w-12 animate-pulse rounded bg-neutral-900" />
                </div>
              ))}
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}
