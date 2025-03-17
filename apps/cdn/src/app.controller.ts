import {
  Controller,
  Get,
  HttpException,
  HttpStatus,
  Param,
  Post,
  Query,
  Res,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common'
import { existsSync, readdirSync, statSync } from 'fs'
import { uploadFolder } from './consts'
import { join } from 'path'
import { Response } from 'express'
import { authorizeRequest, generateFileName } from './utils'
import { FileInterceptor } from '@nestjs/platform-express'
import { diskStorage } from 'multer'

@Controller()
export class AppController {
  @Post('upload')
  @UseInterceptors(
    FileInterceptor('file', {
      storage: diskStorage({
        destination: uploadFolder,
        filename: (_req, file, cb) => {
          const extension = file.originalname.split('.').pop()
          cb(null, `${generateFileName()}.${extension}`)
        },
      }),
    })
  )
  uploadFile(@UploadedFile() file: Express.Multer.File) {
    if (!file) {
      throw new HttpException('No file uploaded', HttpStatus.BAD_REQUEST)
    }

    return `https://cdn.smnl.it/${file.filename}`
  }

  @Get('list')
  listFiles(
    @Query('authKey') authKey: string,
    @Query('page') page: number = 1
  ) {
    authorizeRequest(authKey)

    if (!existsSync(uploadFolder)) {
      throw new HttpException('Directory not found', HttpStatus.NOT_FOUND)
    }

    const files = readdirSync(uploadFolder)
    const mappedFiles = files
      .map(fileName => {
        const filePath = join(uploadFolder, fileName)
        const stats = statSync(filePath)

        return {
          fileName,
          size: stats.size,
          birthTime: stats.birthtime,
        }
      })
      .sort((a, b) => b.birthTime.getTime() - a.birthTime.getTime())

    const pageSize = 10
    const startIndex = (page - 1) * pageSize
    const paginatedFiles = mappedFiles.slice(startIndex, startIndex + pageSize)

    const totalSize = mappedFiles.reduce(
      (acc, file) => acc + statSync(join(uploadFolder, file.fileName)).size,
      0
    )

    return {
      files: paginatedFiles,
      totalSize: totalSize,
      totalFiles: mappedFiles.length,
      totalPages: Math.ceil(mappedFiles.length / pageSize),
    }
  }

  @Get(':fileName')
  getFile(@Param('fileName') fileName: string, @Res() res: Response) {
    const filePath = join(uploadFolder, fileName)

    if (!existsSync(filePath)) {
      throw new HttpException('File not found', HttpStatus.NOT_FOUND)
    }

    res.setHeader('Content-Disposition', 'inline')
    return res.sendFile(filePath)
  }
}
