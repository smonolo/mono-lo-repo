import 'reflect-metadata'
import * as dotenv from 'dotenv'
dotenv.config()

import { NestFactory } from '@nestjs/core'
import { Logger } from '@nestjs/common'
import { ExpressAdapter, NestExpressApplication } from '@nestjs/platform-express'
import { AppModule } from './app.module'

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(
    AppModule,
    new ExpressAdapter()
  )
  const logger = new Logger('MinecraftApi')

  app.setGlobalPrefix('v1')

  app.enableCors({
    origin: true,
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS',
    credentials: true,
  })

  const port = process.env.PORT || 3002
  await app.listen(port, '0.0.0.0')

  logger.log(`Minecraft API is running on http://localhost:${port}/v1`)
}

bootstrap()
