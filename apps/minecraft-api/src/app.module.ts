import { Module } from '@nestjs/common'
import { ThrottlerModule, ThrottlerGuard } from '@nestjs/throttler'
import { APP_GUARD } from '@nestjs/core'
import { DatabaseModule } from './database/database.module'
import { PluginModule } from './plugin/plugin.module'
import { StatusController } from './status/status.controller'
import { PlayersController } from './players/players.controller'
import { LeaderboardsController } from './leaderboards/leaderboards.controller'
import { AdminController } from './admin/admin.controller'

@Module({
  imports: [
    ThrottlerModule.forRoot([
      {
        ttl: 60000,
        limit: 120,
      },
    ]),
    DatabaseModule,
    PluginModule,
  ],
  controllers: [
    StatusController,
    PlayersController,
    LeaderboardsController,
    AdminController,
  ],
  providers: [
    {
      provide: APP_GUARD,
      useClass: ThrottlerGuard,
    },
  ],
})
export class AppModule {}
