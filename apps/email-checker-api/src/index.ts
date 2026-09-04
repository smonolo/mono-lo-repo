import express, { Request, Response, NextFunction } from 'express'
import cors from 'cors'
import dotenv from 'dotenv'
import { verifyEmailReachability } from './smtp-verifier.js'

import os from 'node:os'

dotenv.config()

const app = express()
const PORT = process.env.PORT || 4000
const API_SECRET_KEY = process.env.API_SECRET_KEY
const SENDER_DOMAIN = process.env.SENDER_DOMAIN || os.hostname() || 'localhost'
const SENDER_ADDRESS =
  process.env.SENDER_ADDRESS !== undefined
    ? process.env.SENDER_ADDRESS
    : (process.env.SENDER_DOMAIN ? `check@${process.env.SENDER_DOMAIN}` : '<>')
const MAX_CONCURRENT_PROBES = Number(process.env.MAX_CONCURRENT_PROBES || 10)
const CACHE_TTL_MS = 15 * 60 * 1000 // 15 minutes
const MAX_CACHE_SIZE = 2000

// In-Memory Result Cache
interface CacheEntry {
  result: any
  expiresAt: number
}

const cache = new Map<string, CacheEntry>()
let activeProbes = 0

function getFromCache(email: string) {
  const key = email.trim().toLowerCase()
  const entry = cache.get(key)
  if (!entry) return null
  if (Date.now() > entry.expiresAt) {
    cache.delete(key)
    return null
  }
  return entry.result
}

function setToCache(email: string, result: any) {
  if (cache.size >= MAX_CACHE_SIZE) {
    const keys = Array.from(cache.keys())
    for (let i = 0; i < Math.floor(MAX_CACHE_SIZE * 0.2); i++) {
      cache.delete(keys[i])
    }
  }
  cache.set(email.trim().toLowerCase(), {
    result,
    expiresAt: Date.now() + CACHE_TTL_MS,
  })
}

app.use(cors())
app.use(express.json())

// Optional Auth Middleware
const authMiddleware = (req: Request, res: Response, next: NextFunction) => {
  if (!API_SECRET_KEY) {
    return next()
  }

  const apiKeyHeader = req.headers['x-api-key']
  const authHeader = req.headers.authorization
  const bearerToken = authHeader?.startsWith('Bearer ')
    ? authHeader.substring(7)
    : null

  if (apiKeyHeader === API_SECRET_KEY || bearerToken === API_SECRET_KEY) {
    return next()
  }

  res.status(401).json({
    error: 'Unauthorized',
    message: 'Invalid or missing API key',
  })
}

app.get('/health', (req: Request, res: Response) => {
  res.json({
    status: 'ok',
    service: 'email-checker-api',
    activeProbes,
    maxConcurrentProbes: MAX_CONCURRENT_PROBES,
    cacheEntries: cache.size,
    senderDomain: SENDER_DOMAIN,
    uptime: process.uptime(),
    timestamp: new Date().toISOString(),
  })
})

const handleVerification = async (req: Request, res: Response) => {
  const email = (req.body?.email || req.query?.email) as string

  if (!email || typeof email !== 'string') {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'Email parameter is required',
    })
  }

  const trimmed = email.trim()

  // Return cached result if available
  const cached = getFromCache(trimmed)
  if (cached) {
    return res.json({
      ...cached,
      durationMs: 0,
      cached: true,
    })
  }

  // Enforce concurrency limit
  if (activeProbes >= MAX_CONCURRENT_PROBES) {
    return res.status(429).json({
      error: 'Too Many Requests',
      message:
        'Server is currently handling peak verification volume. Please try again in a moment.',
    })
  }

  const timeoutMs = req.body?.timeoutMs ? Number(req.body.timeoutMs) : 7000

  activeProbes++
  try {
    const result = await verifyEmailReachability(trimmed, {
      senderDomain: SENDER_DOMAIN,
      senderAddress: SENDER_ADDRESS,
      timeoutMs,
    })

    // Cache conclusive responses
    if (result.verdict === 'deliverable' || result.verdict === 'undeliverable') {
      setToCache(trimmed, result)
    }

    return res.json(result)
  } catch (err: any) {
    return res.status(500).json({
      error: 'Internal Server Error',
      message: err.message || 'Verification failed unexpectedly',
    })
  } finally {
    activeProbes = Math.max(0, activeProbes - 1)
  }
}

app.post('/verify', authMiddleware, handleVerification)
app.get('/verify', authMiddleware, handleVerification)

app.listen(PORT, () => {
  console.log(
    `[email-checker-api] Server listening on http://localhost:${PORT}`
  )
  console.log(`[email-checker-api] Sender domain: ${SENDER_DOMAIN}`)
  console.log(`[email-checker-api] Sender address: ${SENDER_ADDRESS || '<>'}`)
  console.log(
    `[email-checker-api] Max concurrent probes: ${MAX_CONCURRENT_PROBES}`
  )
  console.log(
    `[email-checker-api] API key protection: ${API_SECRET_KEY ? 'ENABLED' : 'DISABLED (Open)'}`
  )
  if (!process.env.SENDER_DOMAIN) {
    console.warn(
      `[email-checker-api] NOTICE: SENDER_DOMAIN is not set in environment. Using '${SENDER_DOMAIN}' and sender '${SENDER_ADDRESS || '<>'}'. Set SENDER_DOMAIN in .env for production.`
    )
  }
})
