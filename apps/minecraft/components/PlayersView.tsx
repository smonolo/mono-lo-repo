'use client'

import Link from 'next/link'
import { useState, useMemo, useEffect } from 'react'
import type { PlayerSummary } from '@/types/minecraft'
import { Card } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { formatDate } from '@/utils/minecraft'
import { getClientPlayersList } from '@/lib/client-players'

type Props = {
  initialPlayers: PlayerSummary[]
  initialOnlineCount?: number
}

type StatusFilter = 'ALL' | 'ONLINE' | 'OFFLINE'

export default function PlayersView({ initialPlayers }: Props) {
  const [players, setPlayers] = useState<PlayerSummary[]>(initialPlayers)
  const [search, setSearch] = useState<string>('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [rankFilter, setRankFilter] = useState<string>('ALL')
  const [currentPage, setCurrentPage] = useState<number>(1)
  const [pageSize, setPageSize] = useState<number>(15)

  useEffect(() => {
    setPlayers(initialPlayers)
  }, [initialPlayers])

  useEffect(() => {
    if (initialPlayers.length === 0) {
      getClientPlayersList().then(fetched => {
        if (fetched && fetched.length > 0) {
          setPlayers(fetched)
        }
      })
    }
  }, [initialPlayers])

  const stats = useMemo(() => {
    const total = players.length
    let online = 0
    let offline = 0

    for (const p of players) {
      if (p.online) {
        online++
      } else {
        offline++
      }
    }

    return { total, online, offline }
  }, [players])

  const availableRanks = useMemo(() => {
    const map = new Map<string, { id: string; name: string; color: string }>()
    for (const p of players) {
      if (p.rank?.name) {
        map.set(p.rank.id || p.rank.name, {
          id: p.rank.id || p.rank.name,
          name: p.rank.name,
          color: p.rank.color || '#9CA3AF',
        })
      }
    }
    return Array.from(map.values())
  }, [players])

  const filteredPlayers = useMemo(() => {
    const q = search.trim().toLowerCase()

    return players.filter(p => {
      if (statusFilter === 'ONLINE' && !p.online) return false
      if (statusFilter === 'OFFLINE' && p.online) return false

      if (rankFilter !== 'ALL') {
        const rId = (p.rank?.id || p.rank?.name || '').toLowerCase()
        if (rId !== rankFilter.toLowerCase()) return false
      }

      if (q) {
        const matchUser = p.username.toLowerCase().includes(q)
        const matchUuid = p.uuid.toLowerCase().includes(q)
        if (!matchUser && !matchUuid) return false
      }

      return true
    })
  }, [players, search, statusFilter, rankFilter])

  useEffect(() => {
    setCurrentPage(1)
  }, [search, statusFilter, rankFilter, pageSize])

  const totalPages = Math.max(1, Math.ceil(filteredPlayers.length / pageSize))

  const paginatedPlayers = useMemo(() => {
    const start = (currentPage - 1) * pageSize
    return filteredPlayers.slice(start, start + pageSize)
  }, [filteredPlayers, currentPage, pageSize])

  const startIdx =
    filteredPlayers.length === 0 ? 0 : (currentPage - 1) * pageSize + 1
  const endIdx = Math.min(currentPage * pageSize, filteredPlayers.length)

  const statusOptions: { id: StatusFilter; label: string }[] = [
    { id: 'ALL', label: 'All Players' },
    { id: 'ONLINE', label: 'Online Only' },
    { id: 'OFFLINE', label: 'Offline' },
  ]

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-3 sm:gap-3">
        <Card className="p-3 sm:p-3.5">
          <span className="text-[11px] font-medium text-neutral-400">
            Total Players
          </span>
          <p className="mt-0.5 text-lg font-semibold text-white sm:text-xl">
            {stats.total}
          </p>
        </Card>
        <Card className="p-3 sm:p-3.5">
          <span className="text-[11px] font-medium text-neutral-400">
            Online Players
          </span>
          <p className="mt-0.5 text-lg font-semibold text-emerald-400 sm:text-xl">
            {stats.online}
          </p>
        </Card>
        <Card className="col-span-2 p-3 sm:col-span-1 sm:p-3.5">
          <span className="text-[11px] font-medium text-neutral-400">
            Offline Players
          </span>
          <p className="mt-0.5 text-lg font-semibold text-white sm:text-xl">
            {stats.offline}
          </p>
        </Card>
      </div>

      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="flex flex-wrap items-center gap-1.5">
          {statusOptions.map(opt => {
            const isActive = statusFilter === opt.id
            return (
              <button
                key={opt.id}
                onClick={() => setStatusFilter(opt.id)}
                className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                  isActive
                    ? 'border border-neutral-700 bg-white/[0.08] text-white'
                    : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:border-neutral-800 hover:text-white'
                }`}
              >
                {opt.label}
              </button>
            )
          })}
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {availableRanks.length > 0 && (
            <select
              value={rankFilter}
              onChange={e => setRankFilter(e.target.value)}
              className="rounded-lg border border-neutral-800 bg-[#121215] px-3 py-1.5 text-xs text-neutral-300 focus:border-neutral-700 focus:outline-none"
            >
              <option value="ALL">All Ranks</option>
              {availableRanks.map(r => (
                <option key={r.id} value={r.id}>
                  {r.name}
                </option>
              ))}
            </select>
          )}

          <input
            type="text"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Filter username or UUID..."
            className="w-full rounded-lg border border-neutral-800 bg-white/[0.02] px-3 py-1.5 text-xs text-white placeholder-neutral-500 focus:border-neutral-700 focus:outline-none sm:w-56"
          />
        </div>
      </div>

      {filteredPlayers.length > 0 ? (
        <div className="space-y-4">
          <Card className="overflow-hidden p-0">
            <div className="hidden overflow-x-auto md:block">
              <table className="w-full text-left text-xs">
                <thead className="border-b border-neutral-800 bg-white/[0.01] text-neutral-400">
                  <tr>
                    <th className="px-4 py-3 font-medium">Player</th>
                    <th className="px-4 py-3 font-medium">Rank</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                    <th className="px-4 py-3 font-medium">Last Seen</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-800 text-neutral-300">
                  {paginatedPlayers.map(p => (
                    <tr
                      key={p.uuid}
                      className="transition-colors hover:bg-white/[0.02]"
                    >
                      <td className="px-4 py-3">
                        <Link
                          href={`/player/${encodeURIComponent(p.username)}`}
                          className="group flex items-center gap-x-2.5 transition-opacity hover:opacity-80"
                        >
                          <img
                            src={`https://skins.mcstats.com/face/${p.uuid}?size=32`}
                            alt={p.username}
                            className="h-6 w-6 shrink-0 rounded bg-neutral-900"
                            style={{ imageRendering: 'pixelated' }}
                          />
                          <span className="font-medium text-white group-hover:underline">
                            {p.username}
                          </span>
                          {p.online && p.afk && (
                            <span className="rounded bg-amber-500/10 px-1.5 py-0.5 text-[10px] font-medium text-amber-400">
                              AFK
                            </span>
                          )}
                        </Link>
                      </td>
                      <td className="px-4 py-3">
                        <Badge color={p.rank?.color || '#9CA3AF'}>
                          {p.rank?.name || 'Default'}
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        {p.online ? (
                          <span className="inline-flex items-center gap-x-1.5 text-xs font-medium text-emerald-400">
                            <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
                            Online
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-x-1.5 text-xs text-neutral-500">
                            <span className="h-1.5 w-1.5 rounded-full bg-neutral-600" />
                            Offline
                          </span>
                        )}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-neutral-400">
                        {p.lastLogin && p.lastLogin > 0
                          ? formatDate(p.lastLogin)
                          : '-'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="divide-y divide-neutral-800 md:hidden">
              {paginatedPlayers.map(p => (
                <div key={p.uuid} className="space-y-2.5 p-4 text-xs">
                  <div className="flex items-center justify-between">
                    <Link
                      href={`/player/${encodeURIComponent(p.username)}`}
                      className="flex items-center gap-x-2"
                    >
                      <img
                        src={`https://skins.mcstats.com/face/${p.uuid}?size=32`}
                        alt={p.username}
                        className="h-6 w-6 shrink-0 rounded bg-neutral-900"
                        style={{ imageRendering: 'pixelated' }}
                      />
                      <span className="font-semibold text-white">
                        {p.username}
                      </span>
                      {p.online && p.afk && (
                        <span className="rounded bg-amber-500/10 px-1 py-0.5 text-[9px] font-medium text-amber-400">
                          AFK
                        </span>
                      )}
                    </Link>
                    <div className="flex items-center gap-1.5">
                      <Badge color={p.rank?.color || '#9CA3AF'}>
                        {p.rank?.name || 'Default'}
                      </Badge>
                      {p.online ? (
                        <span className="inline-flex items-center gap-x-1 text-xs font-medium text-emerald-400">
                          <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
                          Online
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-x-1 text-xs text-neutral-500">
                          <span className="h-1.5 w-1.5 rounded-full bg-neutral-600" />
                          Offline
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-neutral-400">
                    <span>Last Seen</span>
                    <span>
                      {p.lastLogin && p.lastLogin > 0
                        ? formatDate(p.lastLogin)
                        : '-'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </Card>

          <div className="flex flex-col items-center justify-between gap-3 px-1 sm:flex-row">
            <div className="flex items-center gap-x-2 text-xs text-neutral-400">
              <span>
                Showing {startIdx}–{endIdx} of {filteredPlayers.length} results
              </span>
              <span className="text-neutral-600">|</span>
              <div className="flex items-center gap-x-1">
                <span>Per page:</span>
                <select
                  value={pageSize}
                  onChange={e => setPageSize(Number(e.target.value))}
                  className="rounded border border-neutral-800 bg-[#121215] px-1.5 py-0.5 text-xs text-neutral-300 focus:outline-none"
                >
                  <option value={10}>10</option>
                  <option value={15}>15</option>
                  <option value={25}>25</option>
                  <option value={50}>50</option>
                </select>
              </div>
            </div>

            {totalPages > 1 && (
              <div className="flex items-center gap-x-1">
                <button
                  type="button"
                  onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                  disabled={currentPage <= 1}
                  className="rounded-lg border border-neutral-800 bg-white/[0.02] px-2.5 py-1 text-xs font-medium text-neutral-300 transition-colors hover:border-neutral-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-40"
                >
                  Previous
                </button>

                <div className="flex items-center gap-x-1 px-1">
                  {[...Array(totalPages)].map((_, i) => {
                    const pageNum = i + 1
                    const isCurrent = pageNum === currentPage
                    if (
                      totalPages > 7 &&
                      pageNum !== 1 &&
                      pageNum !== totalPages &&
                      Math.abs(pageNum - currentPage) > 1
                    ) {
                      if (
                        (pageNum === 2 && currentPage > 3) ||
                        (pageNum === totalPages - 1 &&
                          currentPage < totalPages - 2)
                      ) {
                        return (
                          <span
                            key={pageNum}
                            className="px-1 text-xs text-neutral-600"
                          >
                            ...
                          </span>
                        )
                      }
                      return null
                    }

                    return (
                      <button
                        key={pageNum}
                        type="button"
                        onClick={() => setCurrentPage(pageNum)}
                        className={`h-7 w-7 rounded-lg text-xs font-medium transition-colors ${
                          isCurrent
                            ? 'border border-neutral-700 bg-white/[0.1] text-white'
                            : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:border-neutral-800 hover:text-white'
                        }`}
                      >
                        {pageNum}
                      </button>
                    )
                  })}
                </div>

                <button
                  type="button"
                  onClick={() =>
                    setCurrentPage(prev => Math.min(totalPages, prev + 1))
                  }
                  disabled={currentPage >= totalPages}
                  className="rounded-lg border border-neutral-800 bg-white/[0.02] px-2.5 py-1 text-xs font-medium text-neutral-300 transition-colors hover:border-neutral-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-40"
                >
                  Next
                </button>
              </div>
            )}
          </div>
        </div>
      ) : (
        <Card className="py-12 text-center text-sm text-neutral-500">
          No players match your filter.
        </Card>
      )}
    </div>
  )
}
