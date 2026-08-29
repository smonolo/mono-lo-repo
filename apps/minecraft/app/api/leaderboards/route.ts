import { NextResponse } from 'next/server'
import { fetchLeaderboards } from '@/lib/api'

export async function GET() {
  const result = await fetchLeaderboards()
  return NextResponse.json(result, {
    status: result.online ? 200 : 500,
    headers: {
      'Cache-Control': 'public, s-maxage=30, stale-while-revalidate=60',
    },
  })
}
