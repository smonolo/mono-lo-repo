import { homedir } from 'os'
import { resolve } from 'path'

export const uploadFolder = resolve(homedir(), 'dev/cdn/images')
export const keysFile = resolve(homedir(), 'dev/cdn/config/keys.json')
export const allowedExtensions = ['jpg', 'jpeg', 'png', 'gif']
