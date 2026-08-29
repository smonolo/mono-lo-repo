'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import SearchBar from '@/components/SearchBar'

export default function Navbar() {
  const [onlineCount, setOnlineCount] = useState<number | null>(null)
  const [isOnline, setIsOnline] = useState<boolean>(true)

  useEffect(() => {
    let active = true

    const fetchPlayers = async () => {
      try {
        const res = await fetch('/api/players')
        if (res.ok) {
          const data = await res.json()
          if (active) {
            const count =
              typeof data.onlineCount === 'number'
                ? data.onlineCount
                : Array.isArray(data.players)
                  ? data.players.filter((p: any) => p.online).length
                  : 0
            setOnlineCount(count)
            setIsOnline(data.online !== false)
          }
        }
      } catch {
        if (active) {
          setIsOnline(false)
          setOnlineCount(0)
        }
      }
    }

    fetchPlayers()
    const interval = setInterval(() => {
      if (typeof document !== 'undefined' && document.hidden) return
      fetchPlayers()
    }, 30000)
    return () => {
      active = false
      clearInterval(interval)
    }
  }, [])

  return (
    <header className="bg-sm-black w-full border-b border-gray-800">
      <div className="mx-auto flex h-14 w-full items-center justify-between px-4 md:px-8">
        <div className="flex items-center gap-x-6">
          <Link
            href="/"
            className="flex items-center gap-x-2 transition-opacity hover:opacity-80"
            aria-label="Minecraft Home"
          >
            <div className="bg-sm-blue h-6 w-6 rounded" />
            <span className="text-sm font-medium text-white">Minecraft</span>
          </Link>

          <Link
            href="/leaderboards"
            className="text-xs font-medium text-neutral-400 transition-colors hover:text-white sm:text-sm"
          >
            Leaderboards
          </Link>
        </div>

        <div className="flex items-center gap-x-3 sm:gap-x-5">
          {onlineCount !== null && (
            <div className="flex items-center gap-x-1.5 text-xs text-gray-400">
              <span
                className={`h-2 w-2 rounded-full ${
                  isOnline && onlineCount > 0
                    ? 'bg-emerald-500 shadow-[0_0_6px_rgba(16,185,129,0.4)]'
                    : isOnline
                      ? 'bg-gray-500'
                      : 'bg-rose-500'
                }`}
              />
              <span>
                {onlineCount}{' '}
                <span className="hidden sm:inline">
                  {onlineCount === 1 ? 'player' : 'players'} online
                </span>
              </span>
            </div>
          )}

          <div className="w-36 sm:w-56">
            <SearchBar size="sm" placeholder="Search username..." />
          </div>
        </div>
      </div>
    </header>
  )
}
