'use client'

/* eslint-disable @next/next/no-img-element */

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState, type FormEvent } from 'react'
import type { PlayerSummary } from '@/types/minecraft'

type Props = {
  size?: 'sm' | 'lg'
  placeholder?: string
  autoFocus?: boolean
}

let globalPlayersCache: PlayerSummary[] | null = null
let globalPlayersPromise: Promise<PlayerSummary[]> | null = null

async function getCachedPlayersList(): Promise<PlayerSummary[]> {
  if (globalPlayersCache) return globalPlayersCache
  if (globalPlayersPromise) return globalPlayersPromise

  const promise: Promise<PlayerSummary[]> = fetch('/api/players')
    .then(res => (res.ok ? res.json() : { players: [] }))
    .then(data => {
      const players = Array.isArray(data.players)
        ? (data.players as PlayerSummary[])
        : []
      globalPlayersCache = players
      globalPlayersPromise = null
      return players
    })
    .catch(() => {
      globalPlayersPromise = null
      return []
    })

  globalPlayersPromise = promise
  return promise
}

export default function SearchBar({
  size = 'sm',
  placeholder = 'Search username...',
  autoFocus = false,
}: Props) {
  const router = useRouter()
  const [query, setQuery] = useState('')
  const [allPlayers, setAllPlayers] = useState<PlayerSummary[]>(
    () => globalPlayersCache || []
  )
  const [isOpen, setIsOpen] = useState(false)
  const [selectedIndex, setSelectedIndex] = useState<number>(-1)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    let active = true
    if (!globalPlayersCache) {
      getCachedPlayersList().then(players => {
        if (active) setAllPlayers(players)
      })
    }
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(e.target as Node)
      ) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const filteredPlayers = query.trim()
    ? allPlayers
        .filter(p =>
          p.username.toLowerCase().includes(query.trim().toLowerCase())
        )
        .slice(0, 6)
    : []

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (selectedIndex >= 0 && filteredPlayers[selectedIndex]) {
      const selected = filteredPlayers[selectedIndex]
      setIsOpen(false)
      router.push(`/player/${encodeURIComponent(selected.username)}`)
      return
    }
    const trimmed = query.trim()
    if (!trimmed) return
    setIsOpen(false)
    router.push(`/player/${encodeURIComponent(trimmed)}`)
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!isOpen || filteredPlayers.length === 0) return

    if (e.key === 'ArrowDown') {
      e.preventDefault()
      setSelectedIndex(prev =>
        prev < filteredPlayers.length - 1 ? prev + 1 : 0
      )
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      setSelectedIndex(prev =>
        prev > 0 ? prev - 1 : filteredPlayers.length - 1
      )
    } else if (e.key === 'Escape') {
      setIsOpen(false)
    }
  }

  const isLarge = size === 'lg'

  return (
    <div ref={containerRef} className="relative w-full">
      <form onSubmit={handleSubmit} className="w-full">
        <input
          type="text"
          name="search-query"
          id="search-query"
          value={query}
          onChange={e => {
            setQuery(e.target.value)
            setIsOpen(true)
            setSelectedIndex(-1)
          }}
          onFocus={() => {
            if (query.trim()) setIsOpen(true)
          }}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          autoFocus={autoFocus}
          autoComplete="off"
          autoCorrect="off"
          autoCapitalize="off"
          spellCheck={false}
          data-bwignore="true"
          data-1p-ignore="true"
          data-lpignore="true"
          data-form-type="other"
          className={
            isLarge
              ? 'w-full rounded-lg border border-neutral-800 bg-white/[0.02] px-4 py-3.5 text-base text-white placeholder-neutral-500 transition-colors focus:border-neutral-600 focus:outline-none focus:ring-0 focus:ring-offset-0'
              : 'w-36 rounded border border-neutral-800 bg-white/[0.02] px-2.5 py-1.5 text-xs text-white placeholder-neutral-500 transition-colors focus:border-neutral-600 focus:outline-none focus:ring-0 focus:ring-offset-0 sm:w-56 sm:px-3 sm:text-sm'
          }
        />
      </form>

      {/* Suggestion Dropdown */}
      {isOpen && filteredPlayers.length > 0 && (
        <div className="absolute left-0 right-0 top-full z-50 mt-1 max-h-72 overflow-y-auto rounded-lg border border-neutral-800 bg-[#151518] py-1 shadow-2xl backdrop-blur-sm">
          {filteredPlayers.map((player, idx) => {
            const isSelected = idx === selectedIndex
            return (
              <Link
                key={player.uuid}
                href={`/player/${encodeURIComponent(player.username)}`}
                onClick={() => {
                  setIsOpen(false)
                  setQuery('')
                }}
                className={`flex items-center px-3 py-2 text-sm transition-colors ${
                  isSelected ? 'bg-white/[0.05]' : 'hover:bg-white/[0.03]'
                }`}
              >
                <div className="flex items-center gap-x-2.5">
                  <img
                    src={`https://skins.mcstats.com/face/${player.uuid}?size=64`}
                    alt={player.username}
                    className="h-6 w-6 shrink-0 rounded bg-neutral-900"
                    style={{ imageRendering: 'pixelated' }}
                  />
                  <span className="font-medium text-white">
                    {player.username}
                  </span>
                </div>
              </Link>
            )
          })}
        </div>
      )}
    </div>
  )
}
