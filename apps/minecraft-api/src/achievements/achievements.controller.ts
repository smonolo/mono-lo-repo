import { Controller, Get } from '@nestjs/common'
import { PluginService } from '../plugin/plugin.service'

@Controller()
export class AchievementsController {
  constructor(private readonly pluginService: PluginService) {}

  @Get('achievements')
  async getAchievements() {
    return this.pluginService.getAchievements()
  }
}
