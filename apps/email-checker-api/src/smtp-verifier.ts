import * as dns from 'node:dns/promises'
import type { MxRecord } from 'node:dns'
import * as net from 'node:net'

export interface SmtpLogEntry {
  step: string
  command?: string
  response?: string
  timestamp: string
}

export type DeliverableStatus =
  | 'deliverable'
  | 'undeliverable'
  | 'greylisted'
  | 'ip-blocked'
  | 'unreachable'
  | 'no-mx'
  | 'invalid-syntax'

export interface VerificationResult {
  email: string
  user: string
  domain: string
  validSyntax: boolean
  mxRecords: string[]
  selectedMx?: string
  responseCode?: number
  responseMessage?: string
  verdict: DeliverableStatus
  details: string
  durationMs: number
  logs: SmtpLogEntry[]
}

export interface VerifyOptions {
  senderDomain?: string
  senderAddress?: string
  timeoutMs?: number
}

const EMAIL_REGEX =
  /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$/

export async function verifyEmailReachability(
  email: string,
  options: VerifyOptions = {}
): Promise<VerificationResult> {
  const startTime = Date.now()
  const logs: SmtpLogEntry[] = []

  const addLog = (step: string, command?: string, response?: string) => {
    logs.push({
      step,
      command,
      response,
      timestamp: new Date().toISOString(),
    })
  }

  const senderDomain = options.senderDomain || 'localhost'
  const senderAddress =
    options.senderAddress !== undefined
      ? options.senderAddress
      : (options.senderDomain ? `check@${options.senderDomain}` : '<>')
  const timeoutMs = options.timeoutMs ?? 7000

  // 1. Syntax check
  const trimmed = email.trim()
  const validSyntax = EMAIL_REGEX.test(trimmed)

  if (!validSyntax || !trimmed.includes('@')) {
    addLog('Syntax Validation', undefined, 'Invalid email format')
    return {
      email: trimmed,
      user: '',
      domain: '',
      validSyntax: false,
      mxRecords: [],
      verdict: 'invalid-syntax',
      details:
        'The provided email string does not adhere to valid email address syntax.',
      durationMs: Date.now() - startTime,
      logs,
    }
  }

  const [user, domain] = trimmed.split('@')
  addLog('Syntax Validation', undefined, `Valid syntax for ${trimmed}`)

  // 2. Resolve MX Records
  let mxRecords: MxRecord[] = []
  try {
    mxRecords = await dns.resolveMx(domain)
  } catch (err: any) {
    addLog('DNS MX Lookup', undefined, `Failed: ${err.message || err.code}`)
    return {
      email: trimmed,
      user,
      domain,
      validSyntax: true,
      mxRecords: [],
      verdict: 'no-mx',
      details: `No MX records found for domain '${domain}'. The domain does not have active mail servers configured.`,
      durationMs: Date.now() - startTime,
      logs,
    }
  }

  if (!mxRecords || mxRecords.length === 0) {
    addLog('DNS MX Lookup', undefined, 'No MX records returned')
    return {
      email: trimmed,
      user,
      domain,
      validSyntax: true,
      mxRecords: [],
      verdict: 'no-mx',
      details: `Domain '${domain}' has no MX records configured.`,
      durationMs: Date.now() - startTime,
      logs,
    }
  }

  mxRecords.sort((a, b) => a.priority - b.priority)
  const sortedHostnames = mxRecords.map(r => r.exchange)
  const primaryMx = sortedHostnames[0]

  addLog(
    'DNS MX Lookup',
    undefined,
    `Found ${sortedHostnames.length} servers. Selected: ${primaryMx}`
  )

  // 3. SSRF & Private IP Guard
  try {
    const resolvedIps = await dns.lookup(primaryMx, { all: true })
    const restrictedEntry = resolvedIps.find(entry => isRestrictedIp(entry.address))
    if (restrictedEntry) {
      addLog(
        'Security Guard',
        undefined,
        `Blocked: MX host '${primaryMx}' resolves to restricted network address (${restrictedEntry.address})`
      )
      return {
        email: trimmed,
        user,
        domain,
        validSyntax: true,
        mxRecords: sortedHostnames,
        selectedMx: primaryMx,
        verdict: 'unreachable',
        details: 'Mail server host resolved to an invalid or restricted network address.',
        durationMs: Date.now() - startTime,
        logs,
      }
    }
  } catch (err: any) {
    addLog(
      'DNS Host Lookup',
      undefined,
      `Failed to resolve MX host '${primaryMx}': ${err.message || err.code}`
    )
    return {
      email: trimmed,
      user,
      domain,
      validSyntax: true,
      mxRecords: sortedHostnames,
      selectedMx: primaryMx,
      verdict: 'unreachable',
      details: `Could not resolve mail server host '${primaryMx}'.`,
      durationMs: Date.now() - startTime,
      logs,
    }
  }

  // 4. Perform Direct SMTP Handshake
  try {
    const smtpResult = await executeSmtpProbe({
      host: primaryMx,
      port: 25,
      senderDomain,
      senderAddress,
      targetEmail: trimmed,
      timeoutMs,
      addLog,
    })

    let verdict: DeliverableStatus
    let details: string

    const responseLower = (smtpResult.responseMessage || '').toLowerCase()
    const isIpBlocked =
      responseLower.includes('spamhaus') ||
      responseLower.includes('blocked using') ||
      responseLower.includes('blacklist') ||
      responseLower.includes('barracuda') ||
      responseLower.includes('sorbs') ||
      responseLower.includes('tss09') ||
      responseLower.includes('5.7.1 service unavailable')

    if (isIpBlocked) {
      verdict = 'ip-blocked'
      details = `The mail server (${primaryMx}) rejected the connection due to an IP reputation or policy filter (${smtpResult.responseCode || 'blocked'}).`
    } else if (smtpResult.accepted) {
      verdict = 'deliverable'
      details = `The mail server '${primaryMx}' confirmed that mailbox '${trimmed}' exists and accepts messages (${smtpResult.responseCode}).`
    } else if (
      smtpResult.responseCode &&
      smtpResult.responseCode >= 500 &&
      smtpResult.responseCode < 600
    ) {
      verdict = 'undeliverable'
      details = `The mail server '${primaryMx}' rejected recipient '${trimmed}' (${smtpResult.responseCode}): ${smtpResult.responseMessage || 'User unknown'}.`
    } else if (
      smtpResult.responseCode &&
      smtpResult.responseCode >= 400 &&
      smtpResult.responseCode < 500
    ) {
      verdict = 'greylisted'
      details = `The mail server '${primaryMx}' returned a temporary deferral (${smtpResult.responseCode}). It may be greylisting or rate-limiting.`
    } else {
      verdict = 'unreachable'
      details =
        smtpResult.responseMessage || 'Could not complete SMTP verification.'
    }

    return {
      email: trimmed,
      user,
      domain,
      validSyntax: true,
      mxRecords: sortedHostnames,
      selectedMx: primaryMx,
      responseCode: smtpResult.responseCode,
      responseMessage: smtpResult.responseMessage,
      verdict,
      details,
      durationMs: Date.now() - startTime,
      logs,
    }
  } catch (err: any) {
    addLog('SMTP Connection', undefined, `Connection failure: ${err.message}`)
    return {
      email: trimmed,
      user,
      domain,
      validSyntax: true,
      mxRecords: sortedHostnames,
      selectedMx: primaryMx,
      responseMessage: err.message,
      verdict: 'unreachable',
      details: `Failed to connect to mail server '${primaryMx}' on port 25. Reason: ${err.message}.`,
      durationMs: Date.now() - startTime,
      logs,
    }
  }
}

interface SmtpProbeParams {
  host: string
  port: number
  senderDomain: string
  senderAddress: string
  targetEmail: string
  timeoutMs: number
  addLog: (step: string, command?: string, response?: string) => void
}

interface SmtpProbeResult {
  connected: boolean
  accepted: boolean
  responseCode?: number
  responseMessage?: string
}

function executeSmtpProbe(params: SmtpProbeParams): Promise<SmtpProbeResult> {
  const {
    host,
    port,
    senderDomain,
    senderAddress,
    targetEmail,
    timeoutMs,
    addLog,
  } = params

  return new Promise((resolve, reject) => {
    let socket: net.Socket | null = null
    let timer: NodeJS.Timeout | null = null
    let isFinished = false

    let buffer = ''
    let step:
      | 'GREETING'
      | 'EHLO'
      | 'HELO'
      | 'MAIL_FROM'
      | 'RCPT_TARGET'
      | 'QUIT' = 'GREETING'

    let targetAccepted = false
    let finalCode: number | undefined
    let finalMessage: string | undefined
    let lastCode: number | undefined
    let lastMessage: string | undefined

    const cleanup = () => {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      if (socket) {
        try {
          socket.removeAllListeners()
          if (!socket.destroyed) {
            socket.destroy()
          }
        } catch {
          // ignore
        }
        socket = null
      }
    }

    const finish = (result: SmtpProbeResult) => {
      if (isFinished) return
      isFinished = true
      cleanup()
      resolve(result)
    }

    const fail = (err: Error) => {
      if (isFinished) return
      isFinished = true
      cleanup()
      reject(err)
    }

    timer = setTimeout(() => {
      fail(new Error(`SMTP connection timed out after ${timeoutMs}ms`))
    }, timeoutMs)

    socket = net.createConnection({ host, port })

    socket.on('connect', () => {
      addLog('TCP Connection', undefined, `Connected to ${host}:${port}`)
    })

    socket.on('error', err => {
      fail(err)
    })

    const sendCmd = (cmd: string, stepName: string) => {
      if (!socket || socket.destroyed) return
      addLog(stepName, cmd)
      socket.write(`${cmd}\r\n`)
    }

    const parseSmtpResponse = (
      raw: string
    ): { complete: boolean; code?: number; lines: string[] } => {
      const lines = raw.split(/\r?\n/)
      const lastLine = lines[lines.length - 1]
      if (lastLine !== '') {
        return { complete: false, lines: [] }
      }

      const validLines = lines.slice(0, lines.length - 1)
      if (validLines.length === 0) return { complete: false, lines: [] }

      const finalEntry = validLines[validLines.length - 1]
      const match = finalEntry.match(/^(\d{3})(?: (.*))?$/)
      if (match) {
        return {
          complete: true,
          code: parseInt(match[1], 10),
          lines: validLines,
        }
      }

      return { complete: false, lines: [] }
    }

    socket.on('data', chunk => {
      buffer += chunk.toString('utf-8')

      const parsed = parseSmtpResponse(buffer)
      if (!parsed.complete || parsed.code === undefined) {
        return
      }

      const responseCode = parsed.code
      const fullResponse = parsed.lines.join('\n')
      buffer = ''

      lastCode = responseCode
      lastMessage = fullResponse

      switch (step) {
        case 'GREETING': {
          addLog('Greeting Response', undefined, fullResponse)
          if (responseCode >= 200 && responseCode < 300) {
            step = 'EHLO'
            sendCmd(`EHLO ${senderDomain}`, 'EHLO Command')
          } else {
            finish({
              connected: true,
              accepted: false,
              responseCode,
              responseMessage: fullResponse,
            })
          }
          break
        }

        case 'EHLO': {
          addLog('EHLO Response', undefined, fullResponse)
          if (responseCode >= 200 && responseCode < 300) {
            step = 'MAIL_FROM'
            sendCmd(formatMailFrom(senderAddress), 'MAIL FROM Command')
          } else {
            step = 'HELO'
            sendCmd(`HELO ${senderDomain}`, 'HELO Fallback Command')
          }
          break
        }

        case 'HELO': {
          addLog('HELO Response', undefined, fullResponse)
          if (responseCode >= 200 && responseCode < 300) {
            step = 'MAIL_FROM'
            sendCmd(formatMailFrom(senderAddress), 'MAIL FROM Command')
          } else {
            finish({
              connected: true,
              accepted: false,
              responseCode,
              responseMessage: fullResponse,
            })
          }
          break
        }

        case 'MAIL_FROM': {
          addLog('MAIL FROM Response', undefined, fullResponse)
          if (responseCode >= 200 && responseCode < 300) {
            step = 'RCPT_TARGET'
            sendCmd(`RCPT TO:<${targetEmail}>`, 'RCPT TO Target Command')
          } else {
            finish({
              connected: true,
              accepted: false,
              responseCode,
              responseMessage: fullResponse,
            })
          }
          break
        }

        case 'RCPT_TARGET': {
          addLog('RCPT TO Target Response', undefined, fullResponse)
          finalCode = responseCode
          finalMessage = fullResponse

          if (responseCode >= 200 && responseCode < 300) {
            targetAccepted = true
          }

          step = 'QUIT'
          sendCmd('QUIT', 'QUIT Command')
          break
        }

        case 'QUIT': {
          addLog('QUIT Response', undefined, fullResponse)
          finish({
            connected: true,
            accepted: targetAccepted,
            responseCode: finalCode ?? responseCode,
            responseMessage: finalMessage ?? fullResponse,
          })
          break
        }
      }
    })

    socket.on('close', () => {
      if (!isFinished) {
        finish({
          connected: true,
          accepted: targetAccepted,
          responseCode: finalCode ?? lastCode,
          responseMessage:
            finalMessage ??
            lastMessage ??
            'Connection closed by remote host before handshake completed.',
        })
      }
    })
  })
}

function formatMailFrom(senderAddress: string): string {
  const clean = (senderAddress || '').trim().replace(/^<|>$/g, '')
  if (!clean || clean === '<>') {
    return 'MAIL FROM:<>'
  }
  return `MAIL FROM:<${clean}>`
}

function isRestrictedIp(ip: string): boolean {
  if (!ip) return true

  // IPv4 mapped IPv6 (e.g. ::ffff:127.0.0.1)
  const ipv4Mapped = ip.match(/^::ffff:(\d+\.\d+\.\d+\.\d+)$/i)
  const targetIp = ipv4Mapped ? ipv4Mapped[1] : ip

  if (net.isIPv4(targetIp)) {
    const parts = targetIp.split('.').map(Number)
    const [b0, b1] = parts

    // 0.0.0.0/8 (Current network)
    if (b0 === 0) return true
    // 127.0.0.0/8 (Loopback)
    if (b0 === 127) return true
    // 10.0.0.0/8 (Private)
    if (b0 === 10) return true
    // 172.16.0.0/12 (Private)
    if (b0 === 172 && b1 >= 16 && b1 <= 31) return true
    // 192.168.0.0/16 (Private)
    if (b0 === 192 && b1 === 168) return true
    // 169.254.0.0/16 (Link-local / Cloud metadata service)
    if (b0 === 169 && b1 === 254) return true
    // 224.0.0.0/4 (Multicast) or >= 240 (Reserved/Broadcast)
    if (b0 >= 224) return true

    return false
  }

  if (net.isIPv6(targetIp)) {
    const norm = targetIp.toLowerCase()
    // Loopback
    if (norm === '::1' || norm === '0000:0000:0000:0000:0000:0000:0000:0001') return true
    // Unspecified
    if (norm === '::' || norm === '0000:0000:0000:0000:0000:0000:0000:0000') return true
    // Link-local (fe80::/10)
    if (norm.startsWith('fe8') || norm.startsWith('fe9') || norm.startsWith('fea') || norm.startsWith('feb')) return true
    // Unique local (fc00::/7 - fc00:: and fd00::)
    if (norm.startsWith('fc') || norm.startsWith('fd')) return true

    return false
  }

  return true
}

