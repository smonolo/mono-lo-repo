import type { Punishment } from '@/types/minecraft'

export const getMinecraftRankColor = (colorStr?: string): string => {
  if (!colorStr) return '#9CA3AF'

  const cleaned = colorStr.trim()
  if (cleaned.startsWith('#')) {
    return cleaned
  }

  const upper = cleaned.toUpperCase().replace(/[\s-]+/g, '_')

  const map: Record<string, string> = {
    BLACK: '#9CA3AF',
    DARK_BLUE: '#3B82F6',
    DARKBLUE: '#3B82F6',
    DARK_GREEN: '#22C55E',
    DARKGREEN: '#22C55E',
    DARK_AQUA: '#06B6D4',
    DARKAQUA: '#06B6D4',
    DARK_RED: '#EF4444',
    DARKRED: '#EF4444',
    DARK_PURPLE: '#A855F7',
    DARKPURPLE: '#A855F7',
    PURPLE: '#A855F7',
    GOLD: '#F59E0B',
    ORANGE: '#F97316',
    GRAY: '#9CA3AF',
    GREY: '#9CA3AF',
    DARK_GRAY: '#6B7280',
    DARKGRAY: '#6B7280',
    DARK_GREY: '#6B7280',
    BLUE: '#60A5FA',
    GREEN: '#4ADE80',
    AQUA: '#38BDF8',
    CYAN: '#06B6D4',
    RED: '#F87171',
    LIGHT_PURPLE: '#E879F9',
    LIGHTPURPLE: '#E879F9',
    PINK: '#EC4899',
    YELLOW: '#FDE047',
    WHITE: '#F3F4F6',
  }

  if (map[upper]) return map[upper]
  return '#9CA3AF'
}

export const formatDate = (timestamp?: number | string | null): string => {
  if (!timestamp) return 'Never'
  const num = typeof timestamp === 'string' ? Number(timestamp) : timestamp
  if (typeof num !== 'number' || isNaN(num) || num <= 0) return 'Never'
  const date = new Date(num)
  if (isNaN(date.getTime())) return 'Never'
  const d = String(date.getDate()).padStart(2, '0')
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const y = date.getFullYear()
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${d}.${m}.${y} ${hh}:${mm}`
}

export const formatDuration = (seconds?: number): string => {
  if (!seconds || seconds <= 0) return '0m'
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (d > 0) return `${d}d ${h}h ${m}m`
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
}

export const formatDistance = (meters?: number): string => {
  if (!meters || meters <= 0) return '0 m'
  if (meters >= 1000) {
    return `${(meters / 1000).toFixed(2)} km`
  }
  return `${formatNumber(Math.round(meters))} m`
}

export const formatNumber = (num?: number): string => {
  if (num === undefined || num === null) return '0'
  return new Intl.NumberFormat('en-US').format(num)
}

export const normalizeUuid = (u?: string): string => {
  return u ? u.replace(/-/g, '').toLowerCase() : ''
}

export const formatWorldName = (raw?: string): string => {
  if (!raw || raw === 'Offline') return 'Offline'
  const cleaned = raw.replace(/^minecraft:/i, '')
  if (cleaned.includes('nether')) return 'The Nether'
  if (cleaned.includes('end')) return 'The End'
  if (cleaned.includes('overworld') || cleaned === 'world') return 'Overworld'
  return cleaned.charAt(0).toUpperCase() + cleaned.slice(1).replace(/_/g, ' ')
}

export const getPunishmentStatus = (
  p: Punishment,
  referenceNow: number = Date.now()
): 'ACTIVE' | 'EXPIRED' | 'PARDONED' | 'ISSUED' => {
  const upper = (p.type || '').toUpperCase()
  if (upper.includes('WARN') || upper.includes('KICK')) {
    return 'ISSUED'
  }
  if (p.unpunished_at && p.unpunished_at > 0) {
    return 'PARDONED'
  }
  if (p.expires_at && p.expires_at > 0 && p.expires_at <= referenceNow) {
    return 'EXPIRED'
  }
  return 'ACTIVE'
}

export const getPunishmentTypeBadge = (
  type: string
): { label: string; color: string } => {
  const upper = (type || '').toUpperCase()
  if (upper.includes('BAN')) {
    return { label: 'Ban', color: '#EF4444' }
  }
  if (upper.includes('MUTE')) {
    return { label: 'Mute', color: '#F59E0B' }
  }
  if (upper.includes('FREEZE')) {
    return { label: 'Freeze', color: '#06B6D4' }
  }
  if (upper.includes('KICK')) {
    return { label: 'Kick', color: '#FB923C' }
  }
  if (upper.includes('WARN')) {
    return { label: 'Warn', color: '#FBBF24' }
  }
  const formatted =
    type.charAt(0).toUpperCase() +
    type.slice(1).toLowerCase().replace(/_/g, ' ')
  return { label: formatted, color: '#9CA3AF' }
}

export const getPunishmentStatusBadge = (
  status: 'ACTIVE' | 'EXPIRED' | 'PARDONED' | 'ISSUED'
): { label: string; color: string } => {
  switch (status) {
    case 'ACTIVE':
      return { label: 'Active', color: '#EF4444' }
    case 'ISSUED':
      return { label: 'Issued', color: '#3B82F6' }
    case 'PARDONED':
      return { label: 'Pardoned', color: '#10B981' }
    case 'EXPIRED':
      return { label: 'Expired', color: '#6B7280' }
  }
}
