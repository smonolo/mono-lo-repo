'use client'

import { useState } from 'react'

interface SmtpLogEntry {
  step: string
  command?: string
  response?: string
  timestamp: string
}

interface VerificationResult {
  email: string
  user: string
  domain: string
  validSyntax: boolean
  mxRecords: string[]
  selectedMx?: string
  responseCode?: number
  responseMessage?: string
  verdict:
    | 'deliverable'
    | 'undeliverable'
    | 'greylisted'
    | 'ip-blocked'
    | 'unreachable'
    | 'no-mx'
    | 'invalid-syntax'
  details: string
  durationMs: number
  logs: SmtpLogEntry[]
  error?: string
  message?: string
}

export default function EmailCheckerPage() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<VerificationResult | null>(null)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [showLogs, setShowLogs] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const targetEmail = email.trim()

    if (!targetEmail) {
      setErrorMsg('Please enter an email address to check.')
      return
    }

    setLoading(true)
    setErrorMsg(null)
    setResult(null)

    try {
      const res = await fetch('/api/check', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: targetEmail }),
      })

      const data = await res.json()
      if (!res.ok && !data.verdict) {
        setErrorMsg(data.message || data.error || 'Failed to verify email')
      } else {
        setResult(data)
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'An unexpected error occurred')
    } finally {
      setLoading(false)
    }
  }

  const getVerdictBadge = (verdict: VerificationResult['verdict']) => {
    switch (verdict) {
      case 'deliverable':
        return {
          badgeClass:
            'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20',
          dot: 'bg-emerald-400 shadow-[0_0_8px_rgba(52,211,153,0.5)]',
          title: 'Deliverable',
          desc: 'Mailbox exists and accepts incoming messages.',
        }
      case 'undeliverable':
        return {
          badgeClass: 'bg-rose-500/10 text-rose-400 border border-rose-500/20',
          dot: 'bg-rose-500',
          title: 'Undeliverable',
          desc: 'Mail server rejected recipient. Mailbox does not exist.',
        }
      case 'greylisted':
        return {
          badgeClass:
            'bg-yellow-500/10 text-yellow-400 border border-yellow-500/20',
          dot: 'bg-yellow-500',
          title: 'Greylisted / Rate-Limited',
          desc: 'The server temporarily deferred the check (anti-spam protection).',
        }
      case 'ip-blocked':
        return {
          badgeClass:
            'bg-purple-500/10 text-purple-300 border border-purple-500/20',
          dot: 'bg-purple-400',
          title: 'Connection Blocked by Server',
          desc: 'The target mail server rejected the connection due to an IP reputation or policy filter.',
        }
      case 'no-mx':
        return {
          badgeClass:
            'bg-neutral-500/10 text-neutral-400 border border-neutral-500/20',
          dot: 'bg-neutral-500',
          title: 'No Mail Server',
          desc: 'No active DNS MX records configured for this domain.',
        }
      case 'invalid-syntax':
        return {
          badgeClass: 'bg-rose-500/10 text-rose-400 border border-rose-500/20',
          dot: 'bg-rose-500',
          title: 'Invalid Syntax',
          desc: 'The email address does not follow standard RFC formatting.',
        }
      case 'unreachable':
      default:
        return {
          badgeClass:
            'bg-orange-500/10 text-orange-400 border border-orange-500/20',
          dot: 'bg-orange-500',
          title: 'Server Unreachable',
          desc: 'Could not connect on port 25 or remote server timed out.',
        }
    }
  }

  return (
    <div className="mx-auto flex w-full max-w-xl flex-col items-center justify-center space-y-6 text-center">
      {/* Header */}
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold tracking-tight text-white sm:text-4xl">
          Email Checker
        </h1>
        <p className="text-sm text-neutral-400">
          Test if an email address exists and accepts messages without sending
          any email.
        </p>
      </div>

      {/* Input Card */}
      <div className="w-full space-y-4 rounded-xl border border-neutral-800 bg-white/[0.02] p-5 text-left sm:p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <label
              htmlFor="email"
              className="block text-xs font-medium uppercase tracking-wider text-neutral-400"
            >
              Email Address
            </label>
            <input
              id="email"
              name="email"
              type="text"
              value={email}
              onChange={e => {
                setEmail(e.target.value)
                if (errorMsg) setErrorMsg(null)
              }}
              onInput={e => {
                setEmail((e.target as HTMLInputElement).value)
                if (errorMsg) setErrorMsg(null)
              }}
              placeholder="name@example.com"
              autoComplete="email"
              autoFocus
              className="w-full rounded-lg border border-neutral-800 bg-white/[0.02] px-4 py-3 text-sm text-white placeholder-neutral-500 transition-colors focus:border-neutral-600 focus:outline-none focus:ring-0 focus:ring-offset-0"
              disabled={loading}
            />
          </div>

          <div className="flex items-center justify-between pt-1">
            <div className="flex flex-wrap items-center gap-1.5 text-xs text-neutral-500">
              <span>Try:</span>
              {[
                'support@github.com',
                'test@gmail.com',
                'nonexistent98234@microsoft.com',
              ].map(sample => (
                <button
                  key={sample}
                  type="button"
                  onClick={() => {
                    setEmail(sample)
                    if (errorMsg) setErrorMsg(null)
                  }}
                  className="rounded-md border border-neutral-800 bg-white/[0.02] px-2 py-0.5 text-[11px] text-neutral-400 transition-all hover:border-neutral-700 hover:bg-white/[0.05] hover:text-white"
                >
                  {sample}
                </button>
              ))}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="bg-sm-blue hover:bg-sm-blue/90 inline-flex items-center justify-center gap-x-2 rounded-lg px-4 py-2 text-sm font-medium text-white transition-all active:scale-95 disabled:pointer-events-none disabled:opacity-50"
            >
              {loading ? (
                <>
                  <svg
                    className="h-4 w-4 animate-spin text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    ></path>
                  </svg>
                  <span>Checking...</span>
                </>
              ) : (
                'Check'
              )}
            </button>
          </div>
        </form>
      </div>

      {/* Error Notice */}
      {errorMsg && (
        <div className="w-full rounded-xl border border-rose-500/20 bg-rose-500/10 p-4 text-left text-xs text-rose-300">
          {errorMsg}
        </div>
      )}

      {/* Results Card */}
      {result && (
        <div className="animate-in fade-in w-full space-y-5 rounded-xl border border-neutral-800 bg-white/[0.02] p-5 text-left duration-200 sm:p-6">
          {/* Verdict Banner */}
          {(() => {
            const badge = getVerdictBadge(result.verdict)
            return (
              <div
                className={`flex items-start gap-x-3 rounded-lg p-4 ${badge.badgeClass}`}
              >
                <span
                  className={`mt-1 h-2.5 w-2.5 shrink-0 rounded-full ${badge.dot}`}
                />
                <div className="space-y-0.5">
                  <div className="text-sm font-semibold leading-tight">
                    {badge.title}
                  </div>
                  <div className="text-xs leading-relaxed opacity-90">
                    {result.details}
                  </div>
                </div>
              </div>
            )
          })()}

          {/* Details Grid */}
          <div className="grid grid-cols-3 gap-3 text-xs">
            <div className="rounded-lg border border-neutral-800/80 bg-white/[0.01] p-3">
              <span className="mb-1 block text-[11px] font-medium uppercase tracking-wider text-neutral-500">
                Target MX
              </span>
              <span className="block truncate font-mono text-neutral-200">
                {result.selectedMx || 'None'}
              </span>
            </div>
            <div className="rounded-lg border border-neutral-800/80 bg-white/[0.01] p-3">
              <span className="mb-1 block text-[11px] font-medium uppercase tracking-wider text-neutral-500">
                Status Code
              </span>
              <span className="font-mono text-neutral-200">
                {result.responseCode ?? 'N/A'}
              </span>
            </div>
            <div className="rounded-lg border border-neutral-800/80 bg-white/[0.01] p-3">
              <span className="mb-1 block text-[11px] font-medium uppercase tracking-wider text-neutral-500">
                Duration
              </span>
              <span className="font-mono text-neutral-200">
                {result.durationMs}ms
              </span>
            </div>
          </div>

          {/* Technical Trace Accordion */}
          {result.logs && result.logs.length > 0 && (
            <div className="space-y-2 border-t border-neutral-800/60 pt-2">
              <button
                type="button"
                onClick={() => setShowLogs(!showLogs)}
                className="flex w-full items-center justify-between text-xs text-neutral-400 transition-colors hover:text-neutral-200"
              >
                <span className="font-mono">
                  Handshake Trace ({result.logs.length} steps)
                </span>
                <span>{showLogs ? '▲ Hide' : '▼ Inspect'}</span>
              </button>

              {showLogs && (
                <div className="max-h-72 space-y-2 overflow-y-auto rounded-lg border border-neutral-800 bg-[#0c0c0e] p-3 font-mono text-[11px]">
                  {result.logs.map((log, idx) => (
                    <div
                      key={idx}
                      className="space-y-0.5 border-b border-neutral-900 pb-1.5 last:border-none last:pb-0"
                    >
                      <div className="text-[10px] text-neutral-500">
                        [{log.timestamp.split('T')[1].replace('Z', '')}]{' '}
                        <span className="font-semibold text-neutral-400">
                          {log.step}
                        </span>
                      </div>
                      {log.command && (
                        <div className="text-sm-blue pl-2">
                          &gt; {log.command}
                        </div>
                      )}
                      {log.response && (
                        <div className="whitespace-pre-wrap pl-2 text-emerald-400/90">
                          &lt; {log.response}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
