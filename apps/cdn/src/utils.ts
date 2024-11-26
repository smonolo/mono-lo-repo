import { existsSync, readFileSync } from 'fs'
import { allowedExtensions, keysFile } from './consts'
import { HttpException, HttpStatus } from '@nestjs/common'

export const authorizeRequest = (authKey: string) => {
  if (!authKey) {
    throw new HttpException('authKey is required', HttpStatus.BAD_REQUEST)
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
    throw new HttpException('Invalid authKey', HttpStatus.FORBIDDEN)
  }
}

export const isAllowedFile = (fileName: string) => {
  return allowedExtensions.includes(fileName.split('.').pop())
}

export const generateFileName = (length = 10) => {
  const characters =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

  return Array.from({ length }, () =>
    characters.charAt(Math.floor(Math.random() * characters.length))
  ).join('')
}
