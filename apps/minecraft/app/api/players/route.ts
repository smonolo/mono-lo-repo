import { NextResponse } from 'next/server'
import { fetchOnlinePlayers } from '@/lib/api'

export async function GET() {
  const result = await fetchOnlinePlayers()
  return NextResponse.json(result, {
    status: result.online ? 200 : 500,
    headers: {
      'Cache-Control': 'public, s-maxage=10, stale-while-revalidate=30',
    },
  })
}
