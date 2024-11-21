import {
  Injectable,
  NestMiddleware,
  HttpException,
  HttpStatus,
} from '@nestjs/common'
import { existsSync, readFileSync } from 'fs'
import { keysFile } from './consts'

@Injectable()
export class AuthMiddleware implements NestMiddleware {
  private readonly validKeys: string[]

  constructor() {
    if (!existsSync(keysFile)) {
      throw new Error('keys.json file not found')
    }

    const keysFileContent = readFileSync(keysFile, 'utf8')
    const keysData = JSON.parse(keysFileContent)
    this.validKeys = keysData.keys || []
  }

  use(req: any, _res: any, next: () => void) {
    const authKey = req.headers['authkey']

    if (!authKey || !this.validKeys.includes(authKey)) {
      throw new HttpException('Unauthorized', HttpStatus.UNAUTHORIZED)
    }

    next()
  }
}
