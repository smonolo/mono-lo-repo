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
    const connectionString = process.env.DATABASE_URL
    if (connectionString) {
      this.pool = new Pool({ connectionString })
    } else {
      this.pool = new Pool({
        host: process.env.POSTGRES_HOST || '127.0.0.1',
        port: parseInt(process.env.POSTGRES_PORT || '5432', 10),
        user: process.env.POSTGRES_USER || 'postgres',
        password: process.env.POSTGRES_PASSWORD || 'postgres',
        database: process.env.POSTGRES_DB || 'minecraft',
        max: 10,
        idleTimeoutMillis: 30000,
      })
    }

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
}
