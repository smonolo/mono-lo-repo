import { Controller, Get } from '@nestjs/common'
import { PluginService } from '../plugin/plugin.service'

@Controller()
export class WorldController {
  constructor(private readonly pluginService: PluginService) {}

  @Get('world')
  async getWorld() {
    return this.pluginService.getWorld()
  }
}
