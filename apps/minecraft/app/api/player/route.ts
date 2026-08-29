import { NextResponse } from 'next/server'
import { fetchPlayer, fetchPlayerByUuid } from '@/lib/api'

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url)
  const uuid = searchParams.get('uuid')?.trim()
  const username = searchParams.get('username')?.trim()

  if (!uuid && !username) {
    return NextResponse.json(
      { online: false, error: 'Missing uuid or username query parameter' },
      { status: 400 }
    )
  }

  try {
    const player = uuid
      ? await fetchPlayerByUuid(uuid)
      : await fetchPlayer(username!)

    if (!player) {
      return NextResponse.json(
        { online: false, error: 'Player profile not found' },
        { status: 404 }
      )
    }

    return NextResponse.json(
      {
        online: true,
        player,
      },
      {
        headers: {
          'Cache-Control': 'public, s-maxage=20, stale-while-revalidate=60',
        },
      }
    )
  } catch (err: any) {
    return NextResponse.json(
      {
        online: false,
        error: err?.message || 'Failed to fetch player details from server',
      },
      { status: 500 }
    )
  }
}
