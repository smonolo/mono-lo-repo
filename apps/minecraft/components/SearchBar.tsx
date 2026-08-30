'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState, type FormEvent } from 'react'
import type { PlayerSummary } from '@/types/minecraft'

type Props = {
  size?: 'sm' | 'lg'
  placeholder?: string
  autoFocus?: boolean
}

import { getClientPlayersList } from '@/lib/client-players'

export default function SearchBar({
  size = 'sm',
  placeholder = 'Search username...',
  autoFocus = false,
}: Props) {
  const router = useRouter()
  const [query, setQuery] = useState('')
  const [allPlayers, setAllPlayers] = useState<PlayerSummary[]>([])
  const [isOpen, setIsOpen] = useState(false)
  const [selectedIndex, setSelectedIndex] = useState<number>(-1)
  const containerRef = useRef<HTMLDivElement>(null)

  const ensurePlayersLoaded = () => {
    if (!allPlayers.length) {
      getClientPlayersList().then(players => {
        setAllPlayers(players)
      })
    }
  }

  useEffect(() => {
    if (autoFocus) {
      ensurePlayersLoaded()
    }
  }, [autoFocus])

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
    <div
      ref={containerRef}
      className="relative w-full"
      onMouseEnter={ensurePlayersLoaded}
    >
      <form onSubmit={handleSubmit} className="w-full">
        <input
          type="text"
          name="search-query"
          id="search-query"
          value={query}
          onChange={e => {
            ensurePlayersLoaded()
            setQuery(e.target.value)
            setIsOpen(true)
            setSelectedIndex(-1)
          }}
          onFocus={() => {
            ensurePlayersLoaded()
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
              : 'w-full rounded border border-neutral-800 bg-white/[0.02] px-2.5 py-1.5 text-xs text-white placeholder-neutral-500 transition-colors focus:border-neutral-600 focus:outline-none focus:ring-0 focus:ring-offset-0 sm:px-3 sm:text-sm'
          }
        />
      </form>

      {isOpen && filteredPlayers.length > 0 && (
        <div className="absolute right-0 top-full z-50 mt-1 max-h-72 w-full min-w-[200px] max-w-[calc(100vw-24px)] overflow-y-auto rounded-lg border border-neutral-800 bg-[#151518] py-1 shadow-2xl backdrop-blur-sm">
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
