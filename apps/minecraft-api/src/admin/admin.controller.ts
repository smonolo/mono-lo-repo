import {
  Controller,
  Get,
  Delete,
  Param,
  Query,
  UseGuards,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common'
import { DatabaseService } from '../database/database.service'
import { PluginService } from '../plugin/plugin.service'
import { AdminGuard } from './admin.guard'

@Controller()
export class AdminController {
  constructor(
    private readonly databaseService: DatabaseService,
    private readonly pluginService: PluginService
  ) {}

  @Get('ranks')
  async getRanks() {
    const ranks = await this.databaseService.getRanks()
    return {
      online: true,
      ranks,
    }
  }

  @Get('punishments')
  async getPunishments(@Query('type') type?: string) {
    const punishments = await this.databaseService.getPunishments(type)
    return {
      online: true,
      punishments,
    }
  }

  @Get('whitelist')
  async getWhitelist() {
    const whitelist = await this.databaseService.getWhitelist()
    return {
      online: true,
      whitelist,
    }
  }

  @Get('admin/players/:id')
  @UseGuards(AdminGuard)
  async getPlayerSummary(@Param('id') id: string) {
    const query = id.trim()
    if (!query) {
      throw new BadRequestException('Missing player identifier')
    }
    const summary = await this.databaseService.getPlayerSummary(query)
    if (!summary) {
      throw new NotFoundException(`Player '${query}' not found`)
    }
    return {
      success: true,
      summary,
    }
  }

  @Delete('admin/players/:id')
  @UseGuards(AdminGuard)
  async deletePlayer(@Param('id') id: string) {
    const query = id.trim()
    if (!query) {
      throw new BadRequestException('Missing player identifier')
    }
    const result = await this.databaseService.deletePlayer(query)
    if (!result) {
      throw new NotFoundException(`Player '${query}' not found`)
    }

    // Invalidate memory caches
    this.pluginService.clearCache(result.player.username)
    this.pluginService.clearCache(result.player.uuid)
    this.pluginService.clearCache('players')

    return {
      success: true,
      result,
    }
  }
}
