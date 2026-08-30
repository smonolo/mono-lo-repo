import { Card } from '@/components/ui/Card'

export default function PunishmentsLoading() {
  return (
    <div className="animate-pulse space-y-8">
      <div className="space-y-2">
        <div className="h-8 w-48 rounded bg-neutral-800" />
        <div className="h-4 w-72 rounded bg-neutral-900" />
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 sm:gap-4">
        {[...Array(4)].map((_, i) => (
          <Card key={i} className="p-4">
            <div className="h-3 w-24 rounded bg-neutral-800" />
            <div className="mt-2 h-7 w-16 rounded bg-neutral-700" />
          </Card>
        ))}
      </div>

      <Card className="p-4">
        <div className="space-y-3">
          {[...Array(6)].map((_, i) => (
            <div
              key={i}
              className="flex items-center justify-between border-b border-neutral-800/60 pb-3"
            >
              <div className="flex items-center gap-x-3">
                <div className="h-6 w-6 rounded bg-neutral-800" />
                <div className="h-4 w-28 rounded bg-neutral-800" />
              </div>
              <div className="h-4 w-16 rounded bg-neutral-800" />
              <div className="h-4 w-40 rounded bg-neutral-800" />
              <div className="h-4 w-20 rounded bg-neutral-800" />
            </div>
          ))}
        </div>
      </Card>
    </div>
  )
}
