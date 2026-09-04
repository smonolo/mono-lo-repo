import { NextResponse } from 'next/server'
import { fetchAchievements } from '@/lib/api'

export const dynamic = 'force-dynamic'
export const revalidate = 0

export async function GET() {
  try {
    const data = await fetchAchievements()
    return NextResponse.json(data, {
      headers: {
        'Cache-Control':
          'no-store, no-cache, must-revalidate, proxy-revalidate',
        Pragma: 'no-cache',
        Expires: '0',
      },
    })
  } catch (err: any) {
    return NextResponse.json(
      {
        online: false,
        total: 0,
        categories: [],
        achievements: [],
        error: err?.message || 'Failed to fetch achievements from server',
      },
      { status: 500 }
    )
  }
}
