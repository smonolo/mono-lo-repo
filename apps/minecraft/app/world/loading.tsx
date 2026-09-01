import { Card, CardHeader } from '@/components/ui/Card'

export default function WorldLoading() {
  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <div className="h-8 w-48 animate-pulse rounded bg-neutral-900" />
        <div className="h-4 w-72 animate-pulse rounded bg-neutral-900" />
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[...Array(4)].map((_, i) => (
          <Card key={i} className="h-28 animate-pulse bg-neutral-900/50" />
        ))}
      </div>

      <div className="space-y-4">
        <div className="h-6 w-36 animate-pulse rounded bg-neutral-900" />
        <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="h-64 animate-pulse bg-neutral-900/50" />
          ))}
        </div>
      </div>
    </div>
  )
}
