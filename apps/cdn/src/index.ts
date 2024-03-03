import * as dotenv from 'dotenv'

dotenv.config()

import express from 'express'
import cors, { type CorsOptions } from 'cors'

const app = express()
const isDev = process.env.NODE_ENV !== 'production'
const corsOptions: CorsOptions = {
  origin: '*',
  methods: ['OPTIONS', 'GET', 'POST', 'PATCH', 'DELETE'],
  allowedHeaders: '*',
  optionsSuccessStatus: 200,
}

if (isDev) {
  app.set('json spaces', 2)
  app.set('env', 'development')
}

app.use(express.json())
app.use(express.urlencoded({ extended: true }))
app.use(cors(corsOptions))

app.options('*', cors(corsOptions))

app.listen(process.env.PORT, () => {
  console.log(`Server is running on port ${process.env.PORT}`)
})
