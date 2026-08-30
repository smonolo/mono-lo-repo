import { NextResponse } from 'next/server'
import { fetchPunishments } from '@/lib/api'

export const dynamic = 'force-dynamic'
export const revalidate = 0

export async function GET() {
  const result = await fetchPunishments()
  return NextResponse.json(result, {
    status: result.online ? 200 : 500,
    headers: {
      'Cache-Control': 'no-store, no-cache, must-revalidate, proxy-revalidate',
      Pragma: 'no-cache',
      Expires: '0',
    },
  })
}
