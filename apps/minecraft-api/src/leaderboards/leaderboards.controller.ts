import { Controller, Get, Query } from '@nestjs/common'
import { PluginService } from '../plugin/plugin.service'

@Controller()
export class LeaderboardsController {
  constructor(private readonly pluginService: PluginService) {}

  @Get('leaderboards')
  async getLeaderboards(@Query('stat') statQuery?: string) {
    return this.pluginService.getLeaderboards(statQuery)
  }

  @Get('leaderboard')
  async getLeaderboard(@Query('stat') statQuery?: string) {
    return this.pluginService.getLeaderboards(statQuery)
  }
}
