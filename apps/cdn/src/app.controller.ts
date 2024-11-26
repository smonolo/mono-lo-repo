import {
  Controller,
  Get,
  HttpException,
  HttpStatus,
  Param,
  Post,
  Query,
  Render,
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
import { filesize } from 'filesize'
import * as dayjs from 'dayjs'

@Controller()
export class AppController {
  @Get()
  index() {
    return 'hi, you should know where to go'
  }

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
  @Render('list')
  listFiles(@Query('authKey') authKey: string) {
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
          size: filesize(stats.size),
          birthTime: stats.birthtime,
          formattedBirthTime: dayjs(stats.birthtime).format(
            'DD/MM/YYYY HH:mm:ss'
          ),
        }
      })
      .sort((a, b) => b.birthTime.getTime() - a.birthTime.getTime())
      .map((file, index) => ({
        ...file,
        fileNumber: files.length - index,
      }))

    const totalSize = mappedFiles.reduce(
      (acc, file) => acc + statSync(join(uploadFolder, file.fileName)).size,
      0
    )

    return {
      files: mappedFiles,
      totalSize: filesize(totalSize),
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
