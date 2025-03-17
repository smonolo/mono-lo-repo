import { Injectable, NestMiddleware } from '@nestjs/common'
import { authorizeRequest } from './utils'

@Injectable()
export class AuthMiddleware implements NestMiddleware {
  use(req: any, _res: any, next: () => void) {
    const authKey = req.headers['authkey']

    authorizeRequest(authKey)
    next()
  }
}
