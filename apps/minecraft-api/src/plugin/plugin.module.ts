import { Module, Global } from '@nestjs/common'
import { PluginService } from './plugin.service'

@Global()
@Module({
  providers: [PluginService],
  exports: [PluginService],
})
export class PluginModule {}
