import type { ScreenTheme } from '~/types/screen'

export const getMinecraftRankColor = (
  colorStr?: string,
  theme: ScreenTheme = 'dark'
): string => {
  if (!colorStr) return theme === 'dark' ? '#F8FAFC' : '#0F172A'

  const upper = colorStr.toUpperCase().trim()

  if (theme === 'light') {
    const lightThemeMap: Record<string, string> = {
      BLACK: '#000000',
      DARK_BLUE: '#0000AA',
      DARK_GREEN: '#008000',
      DARK_AQUA: '#008B8B',
      DARK_RED: '#AA0000',
      DARK_PURPLE: '#800080',
      GOLD: '#D97706',
      GRAY: '#475569',
      DARK_GRAY: '#334155',
      BLUE: '#2563EB',
      GREEN: '#16A34A',
      AQUA: '#0284C7',
      RED: '#DC2626',
      LIGHT_PURPLE: '#9333EA',
      YELLOW: '#B45309',
      WHITE: '#0F172A',
    }
    if (lightThemeMap[upper]) return lightThemeMap[upper]
    if (colorStr.startsWith('#')) return colorStr
    return '#0F172A'
  }

  // Dark Theme
  const darkThemeMap: Record<string, string> = {
    BLACK: '#94A3B8',
    DARK_BLUE: '#3B82F6',
    DARK_GREEN: '#22C55E',
    DARK_AQUA: '#06B6D4',
    DARK_RED: '#EF4444',
    DARK_PURPLE: '#C084FC',
    GOLD: '#F59E0B',
    GRAY: '#94A3B8',
    DARK_GRAY: '#64748B',
    BLUE: '#60A5FA',
    GREEN: '#4ADE80',
    AQUA: '#38BDF8',
    RED: '#F87171',
    LIGHT_PURPLE: '#E879F9',
    YELLOW: '#FDE047',
    WHITE: '#F8FAFC',
  }
  if (darkThemeMap[upper]) return darkThemeMap[upper]
  if (colorStr.startsWith('#')) return colorStr
  return '#F8FAFC'
}
