import { MiddlewareConsumer, Module, RequestMethod } from '@nestjs/common'
import { AppController } from './app.controller'
import { AuthMiddleware } from './auth.middleware'

@Module({ controllers: [AppController] })
export class AppModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(AuthMiddleware)
      .forRoutes({ path: 'upload', method: RequestMethod.POST })
  }
}
