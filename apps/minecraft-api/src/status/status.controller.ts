import { Controller, Get } from '@nestjs/common'
import { DatabaseService } from '../database/database.service'
import { PluginService } from '../plugin/plugin.service'

@Controller('status')
export class StatusController {
  constructor(
    private readonly databaseService: DatabaseService,
    private readonly pluginService: PluginService
  ) {}

  @Get()
  async getStatus() {
    const dbHealthy = await this.databaseService.checkHealth()
    const pluginStatus = await this.pluginService.getStatus()

    const tpsArray = pluginStatus.tps || [20.0, 20.0, 20.0]
    const primaryTps =
      Array.isArray(tpsArray) && tpsArray.length > 0 ? tpsArray[0] : 20.0

    return {
      online: pluginStatus.online !== false,
      onlinePlayers: pluginStatus.onlinePlayers || 0,
      maxPlayers: pluginStatus.maxPlayers || 20,
      version: pluginStatus.version || 'Paper 1.21',
      minecraftVersion:
        pluginStatus.bukkitVersion || pluginStatus.version || '1.21.4',
      bukkitVersion: pluginStatus.bukkitVersion || '1.21.4',
      motd: pluginStatus.motd || '',
      tps: primaryTps,
      tpsArray,
      mspt: pluginStatus.mspt || 0,
      memory: pluginStatus.memory || null,
      uptimeSeconds: pluginStatus.uptimeSeconds || 0,
      database: {
        connected: dbHealthy,
      },
      server: {
        name: pluginStatus.serverName || 'Headquarters',
        version: pluginStatus.version || 'Paper 1.21',
        bukkitVersion: pluginStatus.bukkitVersion,
        motd: pluginStatus.motd,
        maxPlayers: pluginStatus.maxPlayers || 20,
        onlinePlayers: pluginStatus.onlinePlayers || 0,
        tps: tpsArray,
        memory: pluginStatus.memory || null,
        uptimeSeconds: pluginStatus.uptimeSeconds || 0,
      },
      timestamp: Date.now(),
    }
  }
}
