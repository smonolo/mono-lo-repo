export type RankData = {
  id: string
  name: string
  color: string
  prefix: string
  primary?: boolean
}

export type PlayerStats = {
  playTimeSeconds: number
  deaths: number
  mobKills: number
  playerKills: number
  damageDealt: number
  damageTaken: number
  damageBlocked?: number
  damageResisted?: number
  damageAbsorbed?: number
  jumps: number
  walkDistanceMeters: number
  sprintDistanceMeters?: number
  flyDistanceMeters: number
  elytraDistanceMeters?: number
  boatDistanceMeters?: number
  minecartDistanceMeters?: number
  horseDistanceMeters?: number
  swimDistanceMeters?: number
  climbDistanceMeters?: number
  sneakTimeSeconds?: number
  timeSinceRestSeconds: number
  sleeps?: number
  chestsOpened?: number
  itemsEnchanted?: number
  fishCaught?: number
  animalsBred?: number
  raidsWon?: number
  raidsTriggered?: number
  trades?: number
  toolsBroken?: number
  bellRings?: number
  musicDiscsPlayed?: number
}

export type PlayerData = {
  uuid: string
  username: string
  displayName: string
  online: boolean
  firstLogin: number
  lastLogin: number
  ping: number
  afk: boolean
  world: string
  biome?: string
  gamemode?: string
  health?: number
  food?: number
  level?: number
  rank: RankData
  primaryRank?: RankData
  ranks?: RankData[]
  stats?: PlayerStats
  punishments?: Punishment[]
  achievements?: PlayerAchievementsSummary
}

export type AchievementFrame = 'TASK' | 'GOAL' | 'CHALLENGE'

export type Achievement = {
  id: string
  title: string
  description: string
  frame: AchievementFrame
  icon: string
  category: string
  categoryName: string
  parent?: string | null
  criteriaCount?: number
  completedCount?: number
  completedPercentage?: number
}

export type PlayerAchievement = {
  id: string
  title: string
  description: string
  frame: AchievementFrame
  icon: string
  category: string
  categoryName: string
  parent?: string | null
  completed: boolean
  completedAt?: number | null
}

export type PlayerAchievementsSummary = {
  completedCount: number
  totalCount: number
  percentage: number
  list: PlayerAchievement[]
}

export type AchievementsResponse = {
  online: boolean
  total: number
  categories: string[]
  achievements: Achievement[]
  globalStats?: {
    totalAchievements: number
    totalCompletions: number
    trackedPlayers: number
  }
  error?: string
}

export type PlayerSummary = {
  uuid: string
  username: string
  online: boolean
  ping?: number
  afk?: boolean
  world?: string
  biome?: string
  lastLogin?: number
  rank: RankData
}

export type SinglePlayerResponse = {
  online: boolean
  player?: PlayerData
  error?: string
}

export type MinecraftPlayersResponse = {
  online: boolean
  players: PlayerSummary[]
  count: number
  onlineCount?: number
  error?: string
}

export type LeaderboardEntry = {
  rank: number
  position?: number
  uuid: string
  username: string
  score: number
  formattedValue: string
  rankData?: RankData
  playerRank?: RankData
}

export type StatisticLeaderboard = {
  key: string
  name: string
  description: string
  top: LeaderboardEntry[]
}

export type LeaderboardsResponse = {
  online: boolean
  leaderboards: StatisticLeaderboard[]
  error?: string
}

export type Punishment = {
  id: string
  uuid: string
  type: string
  username: string
  reason: string
  issuer: string
  issuerUuid?: string
  created_at: number
  expires_at: number
  unpunished_at: number
  unpunished_by?: string
}

export type PunishmentsResponse = {
  online: boolean
  punishments: Punishment[]
  error?: string
}

export type WorldAge = {
  ticks: number
  fullTimeTicks?: number
  days: number
  formatted: string
}

export type WorldTime = {
  ticks: number
  timeOfDay: string
  phase: string
  isDay: boolean
}

export type MoonPhase = {
  phase: number
  name: string
}

export type WorldWeather = {
  isRaining: boolean
  isThundering: boolean
  status: string
  weatherDurationSeconds?: number
  thunderDurationSeconds?: number
  clearWeatherDurationSeconds?: number
}

export type WorldBorderData = {
  size: number
  centerX: number
  centerZ: number
  damageBuffer: number
  damageAmount: number
  warningDistance: number
}

export type DimensionData = {
  name: string
  environment: 'NORMAL' | 'NETHER' | 'THE_END' | string
  difficulty: string
  hardcore: boolean
  pvp: boolean
  seaLevel: number
  minHeight: number
  maxHeight: number
  loadedChunks: number
  entitiesCount: number
  livingEntitiesCount: number
  playersCount: number
  spawn: {
    x: number
    y: number
    z: number
  }
  worldBorder?: WorldBorderData
}

export type WorldAggregates = {
  play_time?: number
  deaths?: number
  player_kills?: number
  mob_kills?: number
  damage_dealt?: number
  damage_taken?: number
  damage_blocked?: number
  damage_resisted?: number
  damage_absorbed?: number
  time_since_death?: number
  walk_distance?: number
  sprint_distance?: number
  fly_distance?: number
  elytra_distance?: number
  boat_distance?: number
  minecart_distance?: number
  horse_distance?: number
  swim_distance?: number
  climb_distance?: number
  sneak_time?: number
  jumps?: number
  sleeps?: number
  chests_opened?: number
  items_enchanted?: number
  fish_caught?: number
  animals_bred?: number
  raids_won?: number
  raids_triggered?: number
  trades?: number
  tools_broken?: number
  bell_rings?: number
  music_discs_played?: number
  [key: string]: number | undefined
}

export type WorldResponse = {
  online: boolean
  worldAge?: WorldAge
  time?: WorldTime
  moonPhase?: MoonPhase
  weather?: WorldWeather
  dimensions?: DimensionData[]
  aggregates?: WorldAggregates
  error?: string
}
