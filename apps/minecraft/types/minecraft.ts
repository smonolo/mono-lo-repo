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
  jumps: number
  walkDistanceMeters: number
  flyDistanceMeters: number
  timeSinceRestSeconds: number
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
  gamemode?: string
  health?: number
  food?: number
  level?: number
  rank: RankData
  primaryRank?: RankData
  ranks?: RankData[]
  stats?: PlayerStats
  punishments?: Punishment[]
}

export type PlayerSummary = {
  uuid: string
  username: string
  online: boolean
  ping?: number
  afk?: boolean
  world?: string
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
