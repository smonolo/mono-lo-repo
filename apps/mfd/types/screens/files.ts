type File = {
  fileName: string
  size: number
  birthTime: string
}

export type FilesResponse = {
  files: File[]
  totalSize: number
  totalFiles: number
  totalPages: number
}
