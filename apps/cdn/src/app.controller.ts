import {
  Controller,
  Delete,
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
import { existsSync, readdirSync, statSync, unlinkSync } from 'fs'
import { uploadFolder } from './consts'
import { join } from 'path'
import { Response } from 'express'
import { authorizeRequest, generateString } from './utils'
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
        filename: (_req, _file, cb) => {
          cb(null, generateString())
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
  listFiles(@Query('authkey') authKey: string) {
    authorizeRequest(authKey)

    if (!existsSync(uploadFolder)) {
      throw new HttpException('Directory not found', HttpStatus.NOT_FOUND)
    }

    const files = readdirSync(uploadFolder).map(fileName => {
      const filePath = join(uploadFolder, fileName)
      const stats = statSync(filePath)

      return {
        fileName,
        size: filesize(stats.size),
        birthTime: dayjs(stats.birthtime).format('DD/MM/YYYY HH:mm:ss'),
      }
    })

    return { files }
  }

  @Delete(':fileName')
  deleteFile(@Param('fileName') fileName: string) {
    const filePath = join(uploadFolder, fileName)

    if (!existsSync(filePath)) {
      throw new HttpException('File not found', HttpStatus.NOT_FOUND)
    }

    try {
      unlinkSync(filePath)
      return { message: `File '${fileName}' deleted successfully` }
    } catch (error) {
      throw new HttpException(
        'Failed to delete file',
        HttpStatus.INTERNAL_SERVER_ERROR
      )
    }
  }

  @Get(':fileName')
  getFile(@Param('fileName') fileName: string, @Res() res: Response) {
    const filePath = join(uploadFolder, fileName)

    if (!existsSync(filePath)) {
      throw new HttpException('File not found', HttpStatus.NOT_FOUND)
    }

    return res.sendFile(filePath)
  }
}
