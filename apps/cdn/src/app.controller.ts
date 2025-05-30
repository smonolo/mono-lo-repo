import {
  Controller,
  Get,
  Headers,
  HttpException,
  HttpStatus,
  Param,
  Post,
  Query,
  Res,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common'
import { FileInterceptor } from '@nestjs/platform-express'
import { Response } from 'express'
import { existsSync, readdirSync, statSync } from 'fs'
import { diskStorage } from 'multer'
import { join } from 'path'
import {
  authorizeRequest,
  directories,
  generateFileName,
  imagesFolder,
} from './utils'

@Controller()
export class AppController {
  @Post('upload')
  @UseInterceptors(
    FileInterceptor('file', {
      storage: diskStorage({
        destination: imagesFolder,
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
    @Query('page') page: number = 1,
    @Headers('authorization') authKey: string
  ) {
    authorizeRequest(authKey)

    if (!existsSync(imagesFolder)) {
      throw new HttpException('Directory not found', HttpStatus.NOT_FOUND)
    }

    const files = readdirSync(imagesFolder)
    const mappedFiles = files
      .map(fileName => {
        const filePath = join(imagesFolder, fileName)
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
      (acc, file) => acc + statSync(join(imagesFolder, file.fileName)).size,
      0
    )

    return {
      files: paginatedFiles,
      totalSize: totalSize,
      totalFiles: mappedFiles.length,
      totalPages: Math.ceil(mappedFiles.length / pageSize),
    }
  }

  @Get(':directory/:fileName')
  getFile(
    @Param('directory') directory: string,
    @Param('fileName') fileName: string,
    @Res() res: Response
  ) {
    const filePath = join(directories[directory], fileName)

    if (!existsSync(filePath)) {
      throw new HttpException('File not found', HttpStatus.NOT_FOUND)
    }

    res.setHeader('Content-Disposition', 'inline')
    return res.sendFile(filePath)
  }

  @Get(':fileName')
  getImage(@Param('fileName') fileName: string, @Res() res: Response) {
    res.redirect(`/images/${fileName}`)
  }
}
