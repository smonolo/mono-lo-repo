import { existsSync, readFileSync } from 'fs'
import { allowedExtensions, keysFile } from './consts'
import { HttpException, HttpStatus } from '@nestjs/common'

export const authorizeRequest = (authKey: string) => {
  if (!authKey) {
    throw new HttpException('authkey is required', HttpStatus.BAD_REQUEST)
  }

  if (!existsSync(keysFile)) {
    throw new HttpException(
      'keys file file not found',
      HttpStatus.INTERNAL_SERVER_ERROR
    )
  }

  let keys: string[]
  try {
    const keysData = JSON.parse(readFileSync(keysFile, 'utf-8'))
    keys = keysData.keys
  } catch (error) {
    throw new HttpException(
      'Failed to load keys file',
      HttpStatus.INTERNAL_SERVER_ERROR
    )
  }

  if (!keys.includes(authKey)) {
    throw new HttpException('Invalid authkey', HttpStatus.FORBIDDEN)
  }
}

export const isAllowedFile = (fileName: string) => {
  return allowedExtensions.includes(fileName.split('.').pop())
}

export const generateString = () => {
  return Math.random().toString(36).substring(2, 12)
}
