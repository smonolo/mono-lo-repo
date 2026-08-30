'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useEffect, useState } from 'react'
import SearchBar from '@/components/SearchBar'

export default function Navbar() {
  const pathname = usePathname()
  const [onlineCount, setOnlineCount] = useState<number | null>(null)
  const [isOnline, setIsOnline] = useState<boolean>(true)
  const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false)

  useEffect(() => {
    let active = true

    const fetchPlayers = async () => {
      try {
        const res = await fetch('/api/players', { cache: 'no-store' })
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
    }, 10000)
    return () => {
      active = false
      clearInterval(interval)
    }
  }, [])

  useEffect(() => {
    setIsDrawerOpen(false)
  }, [pathname])

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setIsDrawerOpen(false)
      }
    }

    if (isDrawerOpen) {
      document.body.style.overflow = 'hidden'
      window.addEventListener('keydown', handleKeyDown)
    } else {
      document.body.style.overflow = ''
    }

    return () => {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [isDrawerOpen])

  const navLinks = [
    { label: 'Home', href: '/', active: pathname === '/' },
    {
      label: 'Leaderboards',
      href: '/leaderboards',
      active: pathname?.startsWith('/leaderboards'),
    },
  ]

  return (
    <>
      <header className="bg-sm-black w-full border-b border-gray-800">
        <div className="mx-auto flex h-14 w-full items-center justify-between gap-x-2 px-3 sm:px-6 md:px-8">
          <Link
            href="/"
            className="flex shrink-0 items-center gap-x-2 transition-opacity hover:opacity-80"
            aria-label="Minecraft Home"
          >
            <div className="bg-sm-blue h-5 w-5 rounded sm:h-6 sm:w-6" />
            <span className="text-xs font-medium text-white sm:text-sm">
              Minecraft
            </span>
          </Link>

          <div className="flex items-center gap-x-2 sm:gap-x-3">
            {onlineCount !== null && (
              <div className="flex shrink-0 items-center gap-x-1.5 text-xs text-gray-400">
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
                  <span className="hidden md:inline">
                    {onlineCount === 1 ? 'player' : 'players'} online
                  </span>
                </span>
              </div>
            )}

            <div className="xs:w-36 w-28 shrink-0 sm:w-48 md:w-56">
              <SearchBar size="sm" placeholder="Search..." />
            </div>

            <button
              type="button"
              onClick={() => setIsDrawerOpen(prev => !prev)}
              aria-label="Toggle navigation menu"
              aria-expanded={isDrawerOpen}
              className="flex h-8 w-8 shrink-0 items-center justify-center rounded border border-neutral-800 bg-white/[0.02] text-neutral-400 transition-colors hover:border-neutral-700 hover:text-white focus:outline-none"
            >
              <svg
                className="h-4 w-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>
          </div>
        </div>
      </header>

      {isDrawerOpen && (
        <div
          className="backdrop-blur-xs fixed inset-0 z-50 bg-black/60 transition-opacity"
          onClick={() => setIsDrawerOpen(false)}
          aria-hidden="true"
        />
      )}

      <div
        className={`fixed bottom-0 right-0 top-0 z-50 flex w-64 max-w-[80vw] flex-col justify-between border-l border-neutral-800 bg-[#121215] p-6 shadow-2xl transition-transform duration-200 ease-in-out ${
          isDrawerOpen ? 'translate-x-0' : 'translate-x-full'
        }`}
        role="dialog"
        aria-modal="true"
        aria-label="Navigation Menu"
      >
        <div>
          <div className="flex items-center justify-between border-b border-neutral-800 pb-4">
            <span className="text-sm font-semibold text-white">Menu</span>
            <button
              type="button"
              onClick={() => setIsDrawerOpen(false)}
              aria-label="Close navigation menu"
              className="flex h-7 w-7 items-center justify-center rounded border border-neutral-800 bg-white/[0.02] text-neutral-400 transition-colors hover:border-neutral-700 hover:text-white"
            >
              <svg
                className="h-3.5 w-3.5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <nav className="mt-6 flex flex-col gap-y-1">
            {navLinks.map(link => (
              <Link
                key={link.href}
                href={link.href}
                className={`flex items-center rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  link.active
                    ? 'bg-white/[0.06] text-white'
                    : 'text-neutral-400 hover:bg-white/[0.03] hover:text-white'
                }`}
              >
                {link.label}
              </Link>
            ))}
          </nav>
        </div>
      </div>
    </>
  )
}
