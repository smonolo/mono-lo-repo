import { NestFactory } from '@nestjs/core'
import { AppModule } from './app.module'
import { NestExpressApplication } from '@nestjs/platform-express'
import { CorsOptions } from '@nestjs/common/interfaces/external/cors-options.interface'

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule)

  const corsOptions: CorsOptions = {
    origin: '*',
    methods: ['OPTIONS', 'GET', 'POST'],
    allowedHeaders: '*',
  }

  app.enableCors(corsOptions)

  await app.listen(49160)
}

bootstrap()
