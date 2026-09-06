import {
  Injectable,
  OnModuleInit,
  OnModuleDestroy,
  Logger,
} from '@nestjs/common'
import { Pool } from 'pg'

export type DbUser = {
  uuid: string
  username: string
  first_join: number
  last_join: number
}

export type DbRank = {
  id: string
  name: string
  color: string
  prefix: string
  weight: number
  is_default: boolean
  is_primary: boolean
}

export type DbPunishment = {
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

export type DbWhitelist = {
  uuid: string
  name: string
  added_by: string
  added_at: number
}

export type DbRankSummary = {
  id: string
  name: string
  color: string
  prefix: string
}

export type PlayerSummary = {
  player: {
    uuid: string
    username: string
    first_join: number
    last_join: number
    in_users: boolean
  }
  ranks: DbRankSummary[]
  display_rank: DbRankSummary | null
  target_punishments: {
    id: string
    type: string
    reason: string
    issuer: string
    created_at: number
    expires_at: number
  }[]
  issuer_punishments_count: number
  whitelist: {
    name: string
    added_by: string
    added_at: number
  } | null
  total_records_to_delete: number
}

export type DeletePlayerResult = {
  player: {
    uuid: string
    username: string
  }
  deleted_users: number
  deleted_user_ranks: number
  deleted_user_display_ranks: number
  deleted_punishments: number
  deleted_whitelist: number
  preserved_issuer_punishments: number
  total_deleted: number
}

function normalizeUuid(input: string): string {
  const trimmed = input.trim()
  if (/^[0-9a-fA-F]{32}$/.test(trimmed)) {
    return `${trimmed.substring(0, 8)}-${trimmed.substring(8, 12)}-${trimmed.substring(12, 16)}-${trimmed.substring(16, 20)}-${trimmed.substring(20, 32)}`
  }
  return trimmed
}

function resolveStaffName(
  raw?: string,
  resolvedName?: string
): { name: string; uuid?: string } {
  if (!raw) return { name: 'Console' }
  const trimmed = raw.trim()
  if (
    trimmed.toUpperCase() === 'CONSOLE' ||
    trimmed === '00000000-0000-0000-0000-000000000000'
  ) {
    return { name: 'Console' }
  }

  if (resolvedName && resolvedName.trim()) {
    return { name: resolvedName.trim(), uuid: trimmed }
  }

  const isUuid =
    /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(
      trimmed
    ) || /^[0-9a-fA-F]{32}$/.test(trimmed)

  if (isUuid) {
    return { name: trimmed.substring(0, 8), uuid: trimmed }
  }

  return { name: trimmed }
}

@Injectable()
export class DatabaseService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(DatabaseService.name)
  private pool: Pool | null = null
  private isConnected = false

  onModuleInit() {
    this.initPool()
  }

  onModuleDestroy() {
    if (this.pool) {
      this.pool.end()
    }
  }

  private initPool() {
    this.pool = new Pool({
      host: process.env.POSTGRES_HOST || '127.0.0.1',
      port: parseInt(process.env.POSTGRES_PORT || '5432', 10),
      user: process.env.POSTGRES_USER || 'postgres',
      password: process.env.POSTGRES_PASSWORD || 'postgres',
      database: process.env.POSTGRES_DB || 'minecraft',
      max: 10,
      idleTimeoutMillis: 30000,
    })

    this.pool.on('error', err => {
      this.logger.warn(`PostgreSQL Pool Error: ${err.message}`)
      this.isConnected = false
    })

    this.checkHealth()
  }

  async checkHealth(): Promise<boolean> {
    if (!this.pool) return false
    try {
      await this.pool.query('SELECT 1')
      this.isConnected = true
      return true
    } catch {
      this.isConnected = false
      return false
    }
  }

  get connected(): boolean {
    return this.isConnected
  }

  async getAllUsers(): Promise<DbUser[]> {
    if (!this.pool) return []
    try {
      const res = await this.pool.query<any>(
        'SELECT uuid, username, first_join, last_join FROM smessential_users ORDER BY last_join DESC'
      )
      return res.rows.map(r => ({
        uuid: r.uuid,
        username: r.username,
        first_join: Number(r.first_join) || 0,
        last_join: Number(r.last_join) || 0,
      }))
    } catch (err: any) {
      this.logger.warn(`Failed to fetch users: ${err.message}`)
      return []
    }
  }

  async getUser(query: string): Promise<DbUser | null> {
    if (!this.pool) return null
    try {
      const isUuid =
        /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(
          query
        ) || /^[0-9a-fA-F]{32}$/.test(query)

      const sql = isUuid
        ? 'SELECT uuid, username, first_join, last_join FROM smessential_users WHERE LOWER(uuid) = LOWER($1) LIMIT 1'
        : 'SELECT uuid, username, first_join, last_join FROM smessential_users WHERE LOWER(username) = LOWER($1) LIMIT 1'

      const res = await this.pool.query<any>(sql, [query])
      const row = res.rows[0]
      if (!row) return null
      return {
        uuid: row.uuid,
        username: row.username,
        first_join: Number(row.first_join) || 0,
        last_join: Number(row.last_join) || 0,
      }
    } catch (err: any) {
      this.logger.warn(`Failed to fetch user (${query}): ${err.message}`)
      return null
    }
  }

  async getRanks(): Promise<DbRank[]> {
    if (!this.pool) return []
    try {
      const res = await this.pool.query<any>(
        'SELECT id, name, color, prefix, weight, is_default, is_primary FROM smessential_ranks ORDER BY weight DESC'
      )
      return res.rows.map(r => ({
        id: r.id,
        name: r.name,
        color: r.color,
        prefix: r.prefix || '',
        weight: Number(r.weight) || 0,
        is_default: !!r.is_default,
        is_primary: !!r.is_primary,
      }))
    } catch (err: any) {
      this.logger.warn(`Failed to fetch ranks: ${err.message}`)
      return []
    }
  }

  async getUserRanks(uuid: string): Promise<DbRank[]> {
    if (!this.pool) return []
    try {
      const res = await this.pool.query<any>(
        `SELECT r.id, r.name, r.color, r.prefix, r.weight, r.is_default, r.is_primary
         FROM smessential_user_ranks ur
         JOIN smessential_ranks r ON ur.rank_id = r.id
         WHERE LOWER(ur.uuid) = LOWER($1)
         ORDER BY r.weight DESC`,
        [uuid]
      )
      return res.rows.map(r => ({
        id: r.id,
        name: r.name,
        color: r.color,
        prefix: r.prefix || '',
        weight: Number(r.weight) || 0,
        is_default: !!r.is_default,
        is_primary: !!r.is_primary,
      }))
    } catch (err: any) {
      this.logger.warn(`Failed to fetch user ranks (${uuid}): ${err.message}`)
      return []
    }
  }

  async getUserDisplayRank(uuid: string): Promise<DbRank | null> {
    if (!this.pool) return null
    try {
      const res = await this.pool.query<any>(
        `SELECT r.id, r.name, r.color, r.prefix, r.weight, r.is_default, r.is_primary
         FROM smessential_user_display_ranks udr
         JOIN smessential_ranks r ON udr.rank_id = r.id
         WHERE LOWER(udr.uuid) = LOWER($1)
         LIMIT 1`,
        [uuid]
      )
      const row = res.rows[0]
      if (!row) return null
      return {
        id: row.id,
        name: row.name,
        color: row.color,
        prefix: row.prefix || '',
        weight: Number(row.weight) || 0,
        is_default: !!row.is_default,
        is_primary: !!row.is_primary,
      }
    } catch {
      return null
    }
  }

  async getPunishments(type?: string): Promise<DbPunishment[]> {
    if (!this.pool) return []
    try {
      const sql = `
        SELECT 
          p.id,
          p.uuid,
          p.type,
          COALESCE(u_target.username, p.username) AS username,
          p.reason,
          p.issuer,
          u_issuer.username AS issuer_username,
          p.created_at,
          p.expires_at,
          p.unpunished_at,
          p.unpunished_by,
          u_unpunished.username AS unpunished_by_username
        FROM smessential_punishments p
        LEFT JOIN smessential_users u_target ON LOWER(p.uuid) = LOWER(u_target.uuid)
        LEFT JOIN smessential_users u_issuer ON LOWER(p.issuer) = LOWER(u_issuer.uuid)
        LEFT JOIN smessential_users u_unpunished ON LOWER(p.unpunished_by) = LOWER(u_unpunished.uuid)
        ${type ? 'WHERE LOWER(p.type) = LOWER($1)' : ''}
        ORDER BY p.created_at DESC
      `
      const params = type ? [type] : []
      const res = await this.pool.query<any>(sql, params)
      return res.rows.map(r => {
        const issuerStaff = resolveStaffName(r.issuer, r.issuer_username)
        const unpunishedStaff = r.unpunished_by
          ? resolveStaffName(r.unpunished_by, r.unpunished_by_username).name
          : undefined

        return {
          id: r.id,
          uuid: r.uuid,
          type: r.type,
          username: r.username,
          reason: r.reason,
          issuer: issuerStaff.name,
          issuerUuid: issuerStaff.uuid,
          created_at: Number(r.created_at) || 0,
          expires_at: Number(r.expires_at) || 0,
          unpunished_at: Number(r.unpunished_at) || 0,
          unpunished_by: unpunishedStaff,
        }
      })
    } catch (err: any) {
      this.logger.warn(`Failed to fetch punishments: ${err.message}`)
      return []
    }
  }

  async getWhitelist(): Promise<DbWhitelist[]> {
    if (!this.pool) return []
    try {
      const sql = `
        SELECT 
          w.uuid,
          COALESCE(u.username, w.name) AS name,
          w.added_by,
          u_adder.username AS added_by_username,
          w.added_at
        FROM smessential_whitelist w
        LEFT JOIN smessential_users u ON LOWER(w.uuid) = LOWER(u.uuid)
        LEFT JOIN smessential_users u_adder ON LOWER(w.added_by) = LOWER(u_adder.uuid)
        ORDER BY w.added_at DESC
      `
      const res = await this.pool.query<any>(sql)
      return res.rows.map(r => {
        const addedByStaff = resolveStaffName(r.added_by, r.added_by_username)
        return {
          uuid: r.uuid,
          name: r.name,
          added_by: addedByStaff.name,
          added_at: Number(r.added_at) || 0,
        }
      })
    } catch (err: any) {
      this.logger.warn(`Failed to fetch whitelist: ${err.message}`)
      return []
    }
  }

  async getPlayerSummary(query: string): Promise<PlayerSummary | null> {
    if (!this.pool) return null
    const trimmed = (query || '').trim()
    if (!trimmed) return null

    const normId = normalizeUuid(trimmed)
    let foundUuid = ''
    let foundUsername = ''
    let firstJoin = 0
    let lastJoin = 0
    let inUsers = false

    // 1. Check smessential_users
    try {
      const userRes = await this.pool.query<any>(
        `SELECT uuid, username, first_join, last_join 
         FROM smessential_users 
         WHERE LOWER(uuid) = LOWER($1) OR LOWER(username) = LOWER($1) 
         LIMIT 1`,
        [normId]
      )
      if (userRes.rows.length > 0) {
        const u = userRes.rows[0]
        foundUuid = u.uuid
        foundUsername = u.username
        firstJoin = Number(u.first_join) || 0
        lastJoin = Number(u.last_join) || 0
        inUsers = true
      }
    } catch (err: any) {
      this.logger.warn(`Error querying smessential_users for summary: ${err.message}`)
    }

    // 2. Check whitelist if needed
    if (!foundUuid || !foundUsername) {
      try {
        const wlRes = await this.pool.query<any>(
          `SELECT COALESCE(uuid, '') AS uuid, COALESCE(name, '') AS name 
           FROM smessential_whitelist 
           WHERE LOWER(uuid) = LOWER($1) OR LOWER(name) = LOWER($1) 
           LIMIT 1`,
          [normId]
        )
        if (wlRes.rows.length > 0) {
          if (!foundUuid && wlRes.rows[0].uuid) foundUuid = wlRes.rows[0].uuid
          if (!foundUsername && wlRes.rows[0].name) foundUsername = wlRes.rows[0].name
        }
      } catch (err: any) {
        this.logger.warn(`Error querying whitelist for summary: ${err.message}`)
      }
    }

    // 3. Check punishments if needed
    if (!foundUuid || !foundUsername) {
      try {
        const pRes = await this.pool.query<any>(
          `SELECT uuid, username 
           FROM smessential_punishments 
           WHERE LOWER(uuid) = LOWER($1) OR LOWER(username) = LOWER($1) 
           LIMIT 1`,
          [normId]
        )
        if (pRes.rows.length > 0) {
          if (!foundUuid && pRes.rows[0].uuid) foundUuid = pRes.rows[0].uuid
          if (!foundUsername && pRes.rows[0].username) foundUsername = pRes.rows[0].username
        }
      } catch (err: any) {
        this.logger.warn(`Error querying punishments for summary: ${err.message}`)
      }
    }

    // 4. Check user ranks / display ranks if UUID
    const isUuid =
      /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(
        normId
      )
    if (!foundUuid && isUuid) {
      foundUuid = normId
    }

    if (!foundUuid && !foundUsername) {
      return null
    }
    if (!foundUsername) foundUsername = foundUuid
    if (!foundUuid) foundUuid = foundUsername

    let recordsToDelete = inUsers ? 1 : 0

    // Fetch user ranks with proper casing and colors
    const ranks: DbRankSummary[] = []
    try {
      const rRes = await this.pool.query<any>(
        `SELECT r.id, r.name, r.color, r.prefix 
         FROM smessential_user_ranks ur
         JOIN smessential_ranks r ON ur.rank_id = r.id
         WHERE LOWER(ur.uuid) = LOWER($1)
         ORDER BY r.weight DESC`,
        [foundUuid]
      )
      for (const row of rRes.rows) {
        ranks.push({
          id: row.id,
          name: row.name,
          color: row.color,
          prefix: row.prefix || '',
        })
        recordsToDelete++
      }
    } catch (err: any) {
      this.logger.warn(`Error querying user ranks: ${err.message}`)
    }

    // Fetch user display rank with proper casing and color
    let displayRank: DbRankSummary | null = null
    try {
      const drRes = await this.pool.query<any>(
        `SELECT r.id, r.name, r.color, r.prefix 
         FROM smessential_user_display_ranks udr
         JOIN smessential_ranks r ON udr.rank_id = r.id
         WHERE LOWER(udr.uuid) = LOWER($1) 
         LIMIT 1`,
        [foundUuid]
      )
      if (drRes.rows.length > 0) {
        displayRank = {
          id: drRes.rows[0].id,
          name: drRes.rows[0].name,
          color: drRes.rows[0].color,
          prefix: drRes.rows[0].prefix || '',
        }
        recordsToDelete++
      }
    } catch (err: any) {
      this.logger.warn(`Error querying user display rank: ${err.message}`)
    }

    // Fetch target punishments (strictly excluding issuer punishments)
    const targetPunishments: any[] = []
    try {
      const tpRes = await this.pool.query<any>(
        `SELECT id, type, reason, issuer, created_at, expires_at 
         FROM smessential_punishments 
         WHERE LOWER(uuid) = LOWER($1) 
            OR (LOWER(username) = LOWER($2) AND LOWER(issuer) != LOWER($1) AND LOWER(issuer) != LOWER($2))
         ORDER BY created_at DESC`,
        [foundUuid, foundUsername]
      )
      for (const row of tpRes.rows) {
        targetPunishments.push({
          id: row.id,
          type: row.type,
          reason: row.reason,
          issuer: row.issuer,
          created_at: Number(row.created_at) || 0,
          expires_at: Number(row.expires_at) || 0,
        })
        recordsToDelete++
      }
    } catch (err: any) {
      this.logger.warn(`Error querying target punishments: ${err.message}`)
    }

    // Count issuer punishments (preserved)
    let issuerPunishmentsCount = 0
    try {
      const ipRes = await this.pool.query<any>(
        `SELECT COUNT(*) AS cnt FROM smessential_punishments 
         WHERE LOWER(issuer) = LOWER($1) OR LOWER(issuer) = LOWER($2)`,
        [foundUuid, foundUsername]
      )
      issuerPunishmentsCount = Number(ipRes.rows[0]?.cnt) || 0
    } catch (err: any) {
      this.logger.warn(`Error counting issuer punishments: ${err.message}`)
    }

    // Whitelist entry
    let whitelistEntry: any = null
    try {
      const wlRes = await this.pool.query<any>(
        `SELECT name, added_by, added_at 
         FROM smessential_whitelist 
         WHERE LOWER(uuid) = LOWER($1) OR LOWER(name) = LOWER($2) 
         LIMIT 1`,
        [foundUuid, foundUsername]
      )
      if (wlRes.rows.length > 0) {
        whitelistEntry = {
          name: wlRes.rows[0].name,
          added_by: wlRes.rows[0].added_by,
          added_at: Number(wlRes.rows[0].added_at) || 0,
        }
        recordsToDelete++
      }
    } catch (err: any) {
      this.logger.warn(`Error querying whitelist entry: ${err.message}`)
    }

    return {
      player: {
        uuid: foundUuid,
        username: foundUsername,
        first_join: firstJoin,
        last_join: lastJoin,
        in_users: inUsers,
      },
      ranks,
      display_rank: displayRank,
      target_punishments: targetPunishments,
      issuer_punishments_count: issuerPunishmentsCount,
      whitelist: whitelistEntry,
      total_records_to_delete: recordsToDelete,
    }
  }

  async deletePlayer(query: string): Promise<DeletePlayerResult | null> {
    if (!this.pool) return null
    const summary = await this.getPlayerSummary(query)
    if (!summary) return null

    const uuid = summary.player.uuid
    const username = summary.player.username

    const client = await this.pool.connect()
    try {
      await client.query('BEGIN')

      // 1. Delete target punishments only (preserving issuer records)
      const pRes = await client.query(
        `DELETE FROM smessential_punishments 
         WHERE LOWER(uuid) = LOWER($1) 
            OR (LOWER(username) = LOWER($2) AND LOWER(issuer) != LOWER($1) AND LOWER(issuer) != LOWER($2))`,
        [uuid, username]
      )

      // 2. Delete user ranks
      const urRes = await client.query(
        'DELETE FROM smessential_user_ranks WHERE LOWER(uuid) = LOWER($1)',
        [uuid]
      )

      // 3. Delete user display rank
      const udrRes = await client.query(
        'DELETE FROM smessential_user_display_ranks WHERE LOWER(uuid) = LOWER($1)',
        [uuid]
      )

      // 4. Delete whitelist entries
      const wlRes = await client.query(
        'DELETE FROM smessential_whitelist WHERE LOWER(uuid) = LOWER($1) OR LOWER(name) = LOWER($2)',
        [uuid, username]
      )

      // 5. Delete users profile
      const uRes = await client.query(
        'DELETE FROM smessential_users WHERE LOWER(uuid) = LOWER($1) OR LOWER(username) = LOWER($2)',
        [uuid, username]
      )

      await client.query('COMMIT')

      const deletedUsers = uRes.rowCount || 0
      const deletedUserRanks = urRes.rowCount || 0
      const deletedUserDisplayRanks = udrRes.rowCount || 0
      const deletedPunishments = pRes.rowCount || 0
      const deletedWhitelist = wlRes.rowCount || 0
      const totalDeleted =
        deletedUsers +
        deletedUserRanks +
        deletedUserDisplayRanks +
        deletedPunishments +
        deletedWhitelist

      return {
        player: {
          uuid,
          username,
        },
        deleted_users: deletedUsers,
        deleted_user_ranks: deletedUserRanks,
        deleted_user_display_ranks: deletedUserDisplayRanks,
        deleted_punishments: deletedPunishments,
        deleted_whitelist: deletedWhitelist,
        preserved_issuer_punishments: summary.issuer_punishments_count,
        total_deleted: totalDeleted,
      }
    } catch (err: any) {
      await client.query('ROLLBACK')
      this.logger.error(`Failed to delete player (${query}): ${err.message}`)
      throw err
    } finally {
      client.release()
    }
  }
}
