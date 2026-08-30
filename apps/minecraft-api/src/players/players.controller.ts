import {
  Controller,
  Get,
  Param,
  Query,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common'
import { DatabaseService } from '../database/database.service'
import { PluginService } from '../plugin/plugin.service'

@Controller()
export class PlayersController {
  constructor(
    private readonly databaseService: DatabaseService,
    private readonly pluginService: PluginService
  ) {}

  @Get('players')
  async getPlayers() {
    const [dbUsers, pluginData, allRanks] = await Promise.all([
      this.databaseService.getAllUsers(),
      this.pluginService.getPlayers(),
      this.databaseService.getRanks(),
    ])

    const defaultRank = allRanks.find(r => r.is_default) || {
      id: 'default',
      name: 'Default',
      color: 'gray',
      prefix: '',
    }

    const onlineMap = new Map<string, any>()
    for (const p of pluginData.players) {
      if (p.uuid) {
        onlineMap.set(p.uuid.toLowerCase(), p)
      }
      if (p.username) {
        onlineMap.set(p.username.toLowerCase(), p)
      }
    }

    const allUuids = new Set<string>()
    const playerSummaries: any[] = []

    for (const user of dbUsers) {
      allUuids.add(user.uuid.toLowerCase())
      const live = onlineMap.get(user.uuid.toLowerCase())
      const isOnline = !!live?.online

      playerSummaries.push({
        uuid: user.uuid,
        username: user.username,
        online: isOnline,
        lastLogin:
          isOnline && live?.lastLogin ? live.lastLogin : user.last_join,
        rank: live?.rank || defaultRank,
        ping: live?.ping || 0,
        afk: !!live?.afk,
        world: live?.world || (isOnline ? 'world' : 'Offline'),
      })
    }

    for (const p of pluginData.players) {
      if (p.uuid && !allUuids.has(p.uuid.toLowerCase())) {
        playerSummaries.push({
          uuid: p.uuid,
          username: p.username,
          online: !!p.online,
          lastLogin: p.lastLogin || Date.now(),
          rank: p.rank || defaultRank,
          ping: p.ping || 0,
          afk: !!p.afk,
          world: p.world || 'world',
        })
      }
    }

    playerSummaries.sort((a, b) => {
      if (a.online !== b.online) {
        return a.online ? -1 : 1
      }
      return (b.lastLogin || 0) - (a.lastLogin || 0)
    })

    return {
      online: true,
      players: playerSummaries,
      count: playerSummaries.length,
      onlineCount:
        pluginData.onlineCount || playerSummaries.filter(p => p.online).length,
    }
  }

  @Get('player')
  async getPlayerByQuery(
    @Query('uuid') uuidParam?: string,
    @Query('name') nameParam?: string
  ) {
    const query = (uuidParam || nameParam || '').trim()
    if (!query) {
      throw new BadRequestException('Missing uuid or name query parameter')
    }
    return this.resolvePlayer(query)
  }

  @Get('player/:id')
  async getPlayerByParam(@Param('id') id: string) {
    const query = (id || '').trim()
    if (!query) {
      throw new BadRequestException('Missing player identifier')
    }
    return this.resolvePlayer(query)
  }

  private async resolvePlayer(query: string) {
    const [dbUser, pluginPlayer] = await Promise.all([
      this.databaseService.getUser(query),
      this.pluginService.getPlayer(query),
    ])

    if (!dbUser && !pluginPlayer) {
      throw new NotFoundException('Player not found')
    }

    const uuid = dbUser?.uuid || pluginPlayer?.uuid || query
    const [assignedRanks, displayRank] = await Promise.all([
      this.databaseService.getUserRanks(uuid),
      this.databaseService.getUserDisplayRank(uuid),
    ])

    const primaryRank = assignedRanks.find(r => r.is_primary) ||
      pluginPlayer?.primaryRank || {
        id: 'default',
        name: 'Default',
        color: 'gray',
        prefix: '',
      }

    const activeDisplayRank = displayRank || pluginPlayer?.rank || primaryRank

    const player = {
      uuid,
      username: dbUser?.username || pluginPlayer?.username || query,
      displayName: pluginPlayer?.displayName || dbUser?.username || query,
      online: !!pluginPlayer?.online,
      firstLogin: dbUser?.first_join || pluginPlayer?.firstLogin || 0,
      lastLogin: pluginPlayer?.lastLogin || dbUser?.last_join || 0,
      ping: pluginPlayer?.ping || 0,
      afk: !!pluginPlayer?.afk,
      world: pluginPlayer?.world || 'Offline',
      gamemode: pluginPlayer?.gamemode || 'SURVIVAL',
      health: pluginPlayer?.health ?? 20,
      food: pluginPlayer?.food ?? 20,
      level: pluginPlayer?.level ?? 0,
      rank: activeDisplayRank,
      primaryRank,
      ranks:
        assignedRanks.length > 0
          ? assignedRanks
          : pluginPlayer?.ranks || [primaryRank],
      stats: pluginPlayer?.stats || {
        playTimeSeconds: 0,
        deaths: 0,
        mobKills: 0,
        playerKills: 0,
        damageDealt: 0,
        damageTaken: 0,
        jumps: 0,
        walkDistanceMeters: 0,
        flyDistanceMeters: 0,
        timeSinceRestSeconds: 0,
      },
    }

    return {
      online: true,
      player,
    }
  }
}
