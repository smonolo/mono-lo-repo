import {
  CanActivate,
  ExecutionContext,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common'
import { Request } from 'express'

@Injectable()
export class AdminGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest<Request>()
    const expectedKey = process.env.ADMIN_API_KEY

    if (!expectedKey) {
      throw new UnauthorizedException('ADMIN_API_KEY is not configured on the server')
    }

    const authHeader = request.headers['authorization']
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new UnauthorizedException('Missing or malformed Authorization header (expected Bearer token)')
    }

    const token = authHeader.slice(7).trim()
    if (token !== expectedKey.trim()) {
      throw new UnauthorizedException('Invalid admin API token')
    }

    return true
  }
}
