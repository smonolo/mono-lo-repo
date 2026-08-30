import { Controller, Get, Query } from '@nestjs/common'
import { DatabaseService } from '../database/database.service'

@Controller()
export class AdminController {
  constructor(private readonly databaseService: DatabaseService) {}

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
}
