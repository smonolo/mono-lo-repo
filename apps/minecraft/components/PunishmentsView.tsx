'use client'

import Link from 'next/link'
import { useState, useMemo, useEffect } from 'react'
import type { Punishment } from '@/types/minecraft'
import { Card } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import {
  formatDate,
  getPunishmentStatus,
  getPunishmentTypeBadge as getTypeBadge,
  getPunishmentStatusBadge as getStatusBadge,
} from '@/utils/minecraft'

type Props = {
  initialPunishments: Punishment[]
}

type TypeFilter = 'ALL' | 'BAN' | 'MUTE' | 'KICK' | 'WARN' | 'FREEZE'
type StatusFilter = 'ALL' | 'ACTIVE' | 'EXPIRED' | 'PARDONED'

export default function PunishmentsView({ initialPunishments }: Props) {
  const [search, setSearch] = useState<string>('')
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [currentPage, setCurrentPage] = useState<number>(1)
  const [pageSize, setPageSize] = useState<number>(15)

  const stats = useMemo(() => {
    let total = initialPunishments.length
    let active = 0
    let bans = 0
    let mutes = 0

    for (const p of initialPunishments) {
      const status = getPunishmentStatus(p)
      if (status === 'ACTIVE') active++
      const type = p.type.toUpperCase()
      if (type.includes('BAN')) bans++
      if (type.includes('MUTE')) mutes++
    }

    return { total, active, bans, mutes }
  }, [initialPunishments])

  const filteredPunishments = useMemo(() => {
    const q = search.trim().toLowerCase()

    return initialPunishments.filter(p => {
      if (typeFilter !== 'ALL') {
        const pType = p.type.toUpperCase()
        if (typeFilter === 'BAN' && !pType.includes('BAN')) return false
        if (typeFilter === 'MUTE' && !pType.includes('MUTE')) return false
        if (typeFilter === 'KICK' && !pType.includes('KICK')) return false
        if (typeFilter === 'WARN' && !pType.includes('WARN')) return false
        if (typeFilter === 'FREEZE' && !pType.includes('FREEZE')) return false
      }

      const status = getPunishmentStatus(p)
      if (statusFilter !== 'ALL' && status !== statusFilter) {
        return false
      }

      if (q) {
        const matchUser = p.username.toLowerCase().includes(q)
        const matchIssuer = p.issuer.toLowerCase().includes(q)
        const matchReason = p.reason.toLowerCase().includes(q)
        const matchUuid = p.uuid.toLowerCase().includes(q)
        if (!matchUser && !matchIssuer && !matchReason && !matchUuid) {
          return false
        }
      }

      return true
    })
  }, [initialPunishments, search, typeFilter, statusFilter])

  useEffect(() => {
    setCurrentPage(1)
  }, [search, typeFilter, statusFilter, pageSize])

  const totalPages = Math.max(
    1,
    Math.ceil(filteredPunishments.length / pageSize)
  )

  const paginatedPunishments = useMemo(() => {
    const start = (currentPage - 1) * pageSize
    return filteredPunishments.slice(start, start + pageSize)
  }, [filteredPunishments, currentPage, pageSize])

  const startIdx =
    filteredPunishments.length === 0 ? 0 : (currentPage - 1) * pageSize + 1
  const endIdx = Math.min(currentPage * pageSize, filteredPunishments.length)

  const typeOptions: { id: TypeFilter; label: string }[] = [
    { id: 'ALL', label: 'All Types' },
    { id: 'BAN', label: 'Bans' },
    { id: 'MUTE', label: 'Mutes' },
    { id: 'FREEZE', label: 'Freezes' },
    { id: 'KICK', label: 'Kicks' },
    { id: 'WARN', label: 'Warns' },
  ]

  const statusOptions: { id: StatusFilter; label: string }[] = [
    { id: 'ALL', label: 'All Status' },
    { id: 'ACTIVE', label: 'Active Only' },
    { id: 'EXPIRED', label: 'Expired' },
    { id: 'PARDONED', label: 'Pardoned' },
  ]

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-4 sm:gap-3">
        <Card className="p-3 sm:p-3.5">
          <span className="text-[11px] font-medium text-neutral-400">
            Total Punishments
          </span>
          <p className="mt-0.5 text-lg font-semibold text-white sm:text-xl">
            {stats.total}
          </p>
        </Card>
        <Card className="p-3 sm:p-3.5">
          <span className="text-[11px] font-medium text-neutral-400">
            Active Infractions
          </span>
          <p className="mt-0.5 text-lg font-semibold text-rose-400 sm:text-xl">
            {stats.active}
          </p>
        </Card>
        <Card className="p-3 sm:p-3.5">
          <span className="text-[11px] font-medium text-neutral-400">
            Total Bans
          </span>
          <p className="mt-0.5 text-lg font-semibold text-white sm:text-xl">
            {stats.bans}
          </p>
        </Card>
        <Card className="p-3 sm:p-3.5">
          <span className="text-[11px] font-medium text-neutral-400">
            Total Mutes
          </span>
          <p className="mt-0.5 text-lg font-semibold text-white sm:text-xl">
            {stats.mutes}
          </p>
        </Card>
      </div>

      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="flex flex-wrap items-center gap-1.5">
          {typeOptions.map(opt => {
            const isActive = typeFilter === opt.id
            return (
              <button
                key={opt.id}
                onClick={() => setTypeFilter(opt.id)}
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
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value as StatusFilter)}
            className="rounded-lg border border-neutral-800 bg-[#121215] px-3 py-1.5 text-xs text-neutral-300 focus:border-neutral-700 focus:outline-none"
          >
            {statusOptions.map(opt => (
              <option key={opt.id} value={opt.id}>
                {opt.label}
              </option>
            ))}
          </select>

          <input
            type="text"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Filter player, staff, reason..."
            className="w-full rounded-lg border border-neutral-800 bg-white/[0.02] px-3 py-1.5 text-xs text-white placeholder-neutral-500 focus:border-neutral-700 focus:outline-none sm:w-56"
          />
        </div>
      </div>

      {filteredPunishments.length > 0 ? (
        <div className="space-y-4">
          <Card className="overflow-hidden p-0">
            <div className="hidden overflow-x-auto md:block">
              <table className="w-full text-left text-xs">
                <thead className="border-b border-neutral-800 bg-white/[0.01] text-neutral-400">
                  <tr>
                    <th className="px-4 py-3 font-medium">Player</th>
                    <th className="px-4 py-3 font-medium">Type</th>
                    <th className="px-4 py-3 font-medium">Reason</th>
                    <th className="px-4 py-3 font-medium">Staff</th>
                    <th className="px-4 py-3 font-medium">Date</th>
                    <th className="px-4 py-3 font-medium">Expires</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-800 text-neutral-300">
                  {paginatedPunishments.map(p => {
                    const status = getPunishmentStatus(p)
                    const typeBadge = getTypeBadge(p.type)
                    const statusBadge = getStatusBadge(status)
                    const isConsole =
                      p.issuer.toLowerCase() === 'console' ||
                      p.issuer.toLowerCase() === 'system'

                    return (
                      <tr
                        key={p.id}
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
                          </Link>
                        </td>
                        <td className="px-4 py-3">
                          <Badge color={typeBadge.color}>
                            {typeBadge.label}
                          </Badge>
                        </td>
                        <td
                          className="max-w-xs truncate px-4 py-3"
                          title={p.reason}
                        >
                          {p.reason}
                        </td>
                        <td className="px-4 py-3">
                          {isConsole ? (
                            <span className="font-medium text-neutral-400">
                              Console
                            </span>
                          ) : (
                            <Link
                              href={`/player/${encodeURIComponent(p.issuer)}`}
                              className="group flex items-center gap-x-2.5 transition-opacity hover:opacity-80"
                            >
                              <img
                                src={`https://skins.mcstats.com/face/${p.issuerUuid || p.issuer}?size=32`}
                                alt={p.issuer}
                                className="h-6 w-6 shrink-0 rounded bg-neutral-900"
                                style={{ imageRendering: 'pixelated' }}
                              />
                              <span className="font-medium text-white group-hover:underline">
                                {p.issuer}
                              </span>
                            </Link>
                          )}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-neutral-400">
                          {formatDate(p.created_at)}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-neutral-400">
                          {p.expires_at && p.expires_at > 0
                            ? formatDate(p.expires_at)
                            : 'Permanent'}
                        </td>
                        <td className="px-4 py-3">
                          <Badge color={statusBadge.color}>
                            {statusBadge.label}
                          </Badge>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>

            <div className="divide-y divide-neutral-800 md:hidden">
              {paginatedPunishments.map(p => {
                const status = getPunishmentStatus(p)
                const typeBadge = getTypeBadge(p.type)
                const statusBadge = getStatusBadge(status)
                const isConsole =
                  p.issuer.toLowerCase() === 'console' ||
                  p.issuer.toLowerCase() === 'system'

                return (
                  <div key={p.id} className="space-y-2.5 p-4 text-xs">
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
                      </Link>
                      <div className="flex items-center gap-1.5">
                        <Badge color={typeBadge.color}>{typeBadge.label}</Badge>
                        <Badge color={statusBadge.color}>
                          {statusBadge.label}
                        </Badge>
                      </div>
                    </div>

                    <div className="text-neutral-300">
                      <span className="text-neutral-500">Reason: </span>
                      {p.reason}
                    </div>

                    <div className="flex flex-wrap items-center justify-between gap-y-1.5 text-neutral-400">
                      <div className="flex items-center gap-x-2">
                        <span className="text-neutral-500">Staff:</span>
                        {isConsole ? (
                          <span className="font-medium text-neutral-300">
                            Console
                          </span>
                        ) : (
                          <Link
                            href={`/player/${encodeURIComponent(p.issuer)}`}
                            className="flex items-center gap-x-1.5 font-medium text-white hover:underline"
                          >
                            <img
                              src={`https://skins.mcstats.com/face/${p.issuerUuid || p.issuer}?size=32`}
                              alt={p.issuer}
                              className="h-6 w-6 shrink-0 rounded bg-neutral-900"
                              style={{ imageRendering: 'pixelated' }}
                            />
                            <span>{p.issuer}</span>
                          </Link>
                        )}
                      </div>
                      <span>{formatDate(p.created_at)}</span>
                    </div>
                  </div>
                )
              })}
            </div>
          </Card>

          <div className="flex flex-col items-center justify-between gap-3 px-1 sm:flex-row">
            <div className="flex items-center gap-x-2 text-xs text-neutral-400">
              <span>
                Showing {startIdx}–{endIdx} of {filteredPunishments.length}{' '}
                results
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
          No punishments match your filter.
        </Card>
      )}
    </div>
  )
}
