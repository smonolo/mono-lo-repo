import { resolve } from 'path'
import { existsSync, readFileSync } from 'fs'
import { HttpException, HttpStatus } from '@nestjs/common'

export const imagesFolder = resolve('/images')
export const filesFolder = resolve('/files')

export const directories = {
  images: imagesFolder,
  files: filesFolder,
}

export const authorizeRequest = (authKey: string) => {
  if (!authKey) {
    throw new HttpException(
      'Authentication key is required',
      HttpStatus.BAD_REQUEST
    )
  }

  const keysFile = resolve('/config/keys.json')

  if (!existsSync(keysFile)) {
    throw new HttpException(
      'Keys file not found',
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
    throw new HttpException('Invalid authentication key', HttpStatus.FORBIDDEN)
  }
}

export const isAllowedFile = (fileName: string) => {
  return ['jpg', 'jpeg', 'png', 'gif'].includes(fileName.split('.').pop())
}

export const generateFileName = (length = 10) => {
  const characters =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

  return Array.from({ length }, () =>
    characters.charAt(Math.floor(Math.random() * characters.length))
  ).join('')
}
