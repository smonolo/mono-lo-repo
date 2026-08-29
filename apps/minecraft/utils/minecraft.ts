export const getMinecraftRankColor = (colorStr?: string): string => {
  if (!colorStr) return '#f3f4f6'

  const upper = colorStr.toUpperCase().trim()

  const map: Record<string, string> = {
    BLACK: '#9ca3af',
    DARK_BLUE: '#3B82F6',
    DARK_GREEN: '#22C55E',
    DARK_AQUA: '#06B6D4',
    DARK_RED: '#EF4444',
    DARK_PURPLE: '#C084FC',
    GOLD: '#F59E0B',
    GRAY: '#9ca3af',
    DARK_GRAY: '#6b7280',
    BLUE: '#60A5FA',
    GREEN: '#4ADE80',
    AQUA: '#38BDF8',
    RED: '#F87171',
    LIGHT_PURPLE: '#E879F9',
    YELLOW: '#FDE047',
    WHITE: '#f3f4f6',
  }

  if (map[upper]) return map[upper]
  if (colorStr.startsWith('#')) return colorStr
  return '#f3f4f6'
}

export const formatDate = (timestamp?: number): string => {
  if (!timestamp || timestamp <= 0) return 'Never'
  const date = new Date(timestamp)
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
  const parts = num.toString().split('.')
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return parts.join('.')
}
