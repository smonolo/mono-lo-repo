import { NextRequest, NextResponse } from 'next/server'

interface RateLimitRecord {
  timestamps: number[]
}

// In-Memory IP rate limiter
const ipRateLimits = new Map<string, RateLimitRecord>()

// In-Memory short-lived frontend cache (5 minutes)
interface CacheRecord {
  data: any
  expiresAt: number
}
const frontendCache = new Map<string, CacheRecord>()

// Periodic cleanup
if (typeof setInterval !== 'undefined') {
  setInterval(() => {
    const now = Date.now()
    // Cleanup rate limits older than 24h
    for (const [ip, record] of ipRateLimits.entries()) {
      record.timestamps = record.timestamps.filter(
        t => now - t < 24 * 60 * 60 * 1000
      )
      if (record.timestamps.length === 0) {
        ipRateLimits.delete(ip)
      }
    }
    // Cleanup expired cache
    for (const [key, record] of frontendCache.entries()) {
      if (now > record.expiresAt) {
        frontendCache.delete(key)
      }
    }
  }, 5 * 60 * 1000).unref?.()
}

function checkRateLimit(ip: string): {
  allowed: boolean
  message?: string
  retryAfter?: number
} {
  const now = Date.now()
  let record = ipRateLimits.get(ip)
  if (!record) {
    record = { timestamps: [] }
    ipRateLimits.set(ip, record)
  }

  // Keep only timestamps from the last 24 hours
  record.timestamps = record.timestamps.filter(
    t => now - t < 24 * 60 * 60 * 1000
  )

  // 1-minute limit: max 10 requests
  const recentOneMinute = record.timestamps.filter(t => now - t < 60 * 1000)
  if (recentOneMinute.length >= 10) {
    return {
      allowed: false,
      message: 'Rate limit reached: Maximum 10 checks per minute. Please slow down.',
      retryAfter: 60,
    }
  }

  // 24-hour limit: max 60 requests
  if (record.timestamps.length >= 60) {
    return {
      allowed: false,
      message:
        'Daily verification limit reached (60 checks/day). Please try again tomorrow.',
      retryAfter: 3600,
    }
  }

  record.timestamps.push(now)
  return { allowed: true }
}

export async function POST(req: NextRequest) {
  try {
    // 1. Resolve Client IP
    const forwarded = req.headers.get('x-forwarded-for')
    const realIp = req.headers.get('x-real-ip')
    const clientIp = forwarded
      ? forwarded.split(',')[0].trim()
      : realIp || '127.0.0.1'

    // 2. Enforce Rate Limiting
    const rateCheck = checkRateLimit(clientIp)
    if (!rateCheck.allowed) {
      return NextResponse.json(
        {
          error: 'Rate limit exceeded',
          message: rateCheck.message,
        },
        {
          status: 429,
          headers: {
            'Retry-After': String(rateCheck.retryAfter || 60),
          },
        }
      )
    }

    // 3. Validate Request Body
    const body = await req.json()
    const { email } = body

    if (!email || typeof email !== 'string') {
      return NextResponse.json({ error: 'Email is required' }, { status: 400 })
    }

    const trimmedEmail = email.trim().toLowerCase()

    // 4. Check Frontend In-Memory Cache (5-minute TTL)
    const cached = frontendCache.get(trimmedEmail)
    if (cached && Date.now() < cached.expiresAt) {
      return NextResponse.json(cached.data)
    }

    // 5. Proxy to Backend Verification Service
    const apiUrl = process.env.EMAIL_CHECKER_API_URL || 'http://localhost:4000'
    const apiKey = process.env.EMAIL_CHECKER_API_KEY

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    }

    if (apiKey) {
      headers['x-api-key'] = apiKey
      headers['Authorization'] = `Bearer ${apiKey}`
    }

    const backendRes = await fetch(`${apiUrl}/verify`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ email: trimmedEmail }),
      signal: AbortSignal.timeout(12000),
    })

    const data = await backendRes.json()

    // Cache successful verification results
    if (
      backendRes.ok &&
      (data.verdict === 'deliverable' || data.verdict === 'undeliverable')
    ) {
      frontendCache.set(trimmedEmail, {
        data,
        expiresAt: Date.now() + 5 * 60 * 1000,
      })
    }

    return NextResponse.json(data, { status: backendRes.status })
  } catch (error: any) {
    console.error('Error in /api/check proxy:', error)

    return NextResponse.json(
      {
        error: 'Service Unavailable',
        message:
          error.name === 'TimeoutError'
            ? 'The verification request timed out. Please try again.'
            : 'Could not connect to the verification service. Please try again in a moment.',
        verdict: 'unreachable',
      },
      { status: 502 }
    )
  }
}
