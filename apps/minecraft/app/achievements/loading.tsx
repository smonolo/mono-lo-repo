import { Card } from '@/components/ui/Card'

export default function AchievementsLoading() {
  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <div className="h-8 w-48 animate-pulse rounded bg-neutral-900" />
        <div className="h-4 w-80 animate-pulse rounded bg-neutral-900" />
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
        {[...Array(6)].map((_, i) => (
          <Card key={i} className="space-y-2 p-4">
            <div className="h-3 w-16 animate-pulse rounded bg-neutral-900" />
            <div className="h-6 w-12 animate-pulse rounded bg-neutral-900" />
            <div className="h-2.5 w-20 animate-pulse rounded bg-neutral-900" />
          </Card>
        ))}
      </div>

      <div className="space-y-4 rounded-xl border border-neutral-800 bg-[#121215] p-4 sm:p-5">
        <div className="h-9 w-full animate-pulse rounded-lg bg-neutral-900" />
        <div className="flex gap-2">
          {[...Array(5)].map((_, i) => (
            <div
              key={i}
              className="h-7 w-20 animate-pulse rounded-lg bg-neutral-900"
            />
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {[...Array(9)].map((_, i) => (
          <Card key={i} className="space-y-4 p-5">
            <div className="flex items-center justify-between">
              <div className="h-4 w-16 animate-pulse rounded bg-neutral-900" />
              <div className="h-4 w-10 animate-pulse rounded bg-neutral-900" />
            </div>
            <div className="space-y-2">
              <div className="h-5 w-36 animate-pulse rounded bg-neutral-900" />
              <div className="h-3.5 w-full animate-pulse rounded bg-neutral-900" />
              <div className="h-3.5 w-3/4 animate-pulse rounded bg-neutral-900" />
            </div>
            <div className="h-2 w-full animate-pulse rounded-full bg-neutral-900" />
          </Card>
        ))}
      </div>
    </div>
  )
}
