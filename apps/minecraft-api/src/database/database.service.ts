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
      const res = await this.pool.query<DbUser>(
        'SELECT uuid, username, first_join, last_join FROM smessential_users ORDER BY last_join DESC'
      )
      return res.rows
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

      const res = await this.pool.query<DbUser>(sql, [query])
      return res.rows[0] || null
    } catch (err: any) {
      this.logger.warn(`Failed to fetch user (${query}): ${err.message}`)
      return null
    }
  }

  async getRanks(): Promise<DbRank[]> {
    if (!this.pool) return []
    try {
      const res = await this.pool.query<DbRank>(
        'SELECT id, name, color, prefix, weight, is_default, is_primary FROM smessential_ranks ORDER BY weight DESC'
      )
      return res.rows
    } catch (err: any) {
      this.logger.warn(`Failed to fetch ranks: ${err.message}`)
      return []
    }
  }

  async getUserRanks(uuid: string): Promise<DbRank[]> {
    if (!this.pool) return []
    try {
      const res = await this.pool.query<DbRank>(
        `SELECT r.id, r.name, r.color, r.prefix, r.weight, r.is_default, r.is_primary
         FROM smessential_user_ranks ur
         JOIN smessential_ranks r ON ur.rank_id = r.id
         WHERE LOWER(ur.uuid) = LOWER($1)
         ORDER BY r.weight DESC`,
        [uuid]
      )
      return res.rows
    } catch (err: any) {
      this.logger.warn(`Failed to fetch user ranks (${uuid}): ${err.message}`)
      return []
    }
  }

  async getUserDisplayRank(uuid: string): Promise<DbRank | null> {
    if (!this.pool) return null
    try {
      const res = await this.pool.query<DbRank>(
        `SELECT r.id, r.name, r.color, r.prefix, r.weight, r.is_default, r.is_primary
         FROM smessential_user_display_ranks udr
         JOIN smessential_ranks r ON udr.rank_id = r.id
         WHERE LOWER(udr.uuid) = LOWER($1)
         LIMIT 1`,
        [uuid]
      )
      return res.rows[0] || null
    } catch {
      return null
    }
  }

  async getPunishments(type?: string): Promise<DbPunishment[]> {
    if (!this.pool) return []
    try {
      if (type) {
        const res = await this.pool.query<DbPunishment>(
          'SELECT * FROM smessential_punishments WHERE LOWER(type) = LOWER($1) ORDER BY created_at DESC',
          [type]
        )
        return res.rows
      }
      const res = await this.pool.query<DbPunishment>(
        'SELECT * FROM smessential_punishments ORDER BY created_at DESC'
      )
      return res.rows
    } catch (err: any) {
      this.logger.warn(`Failed to fetch punishments: ${err.message}`)
      return []
    }
  }

  async getWhitelist(): Promise<DbWhitelist[]> {
    if (!this.pool) return []
    try {
      const res = await this.pool.query<DbWhitelist>(
        'SELECT * FROM smessential_whitelist ORDER BY added_at DESC'
      )
      return res.rows
    } catch (err: any) {
      this.logger.warn(`Failed to fetch whitelist: ${err.message}`)
      return []
    }
  }
}
