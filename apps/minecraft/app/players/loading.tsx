import { Card } from '@/components/ui/Card'

export default function PlayersLoading() {
  return (
    <div className="animate-pulse space-y-8">
      <div className="space-y-2">
        <div className="h-8 w-40 rounded bg-white/[0.05]" />
        <div className="h-4 w-72 rounded bg-white/[0.03]" />
      </div>

      <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-3 sm:gap-3">
        {[...Array(3)].map((_, i) => (
          <Card key={i} className="p-3 sm:p-3.5">
            <div className="h-3 w-20 rounded bg-white/[0.03]" />
            <div className="mt-2 h-6 w-12 rounded bg-white/[0.05]" />
          </Card>
        ))}
      </div>

      <div className="h-9 w-64 rounded-lg bg-white/[0.03]" />

      <Card className="h-96 p-0">
        <div className="h-full w-full bg-white/[0.02]" />
      </Card>
    </div>
  )
}
