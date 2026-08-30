'use client'

import { useState } from 'react'

type Props = {
  ip?: string
  onlineCount?: number
  isOnline?: boolean
}

export default function CopyServerIp({
  ip = 'mc.smnl.dev',
  onlineCount = 0,
  isOnline = true,
}: Props) {
  const [copied, setCopied] = useState(false)

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(ip)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {}
  }

  return (
    <button
      type="button"
      onClick={handleCopy}
      className="group inline-flex items-center gap-x-2.5 rounded-full border border-neutral-800 bg-white/[0.02] px-3.5 py-1.5 text-xs text-neutral-300 transition-all hover:border-neutral-700 hover:bg-white/[0.05] hover:text-white focus:outline-none"
      title="Click to copy server IP"
    >
      <span
        className={`h-2 w-2 rounded-full ${
          isOnline && onlineCount > 0
            ? 'bg-emerald-400 shadow-[0_0_8px_rgba(52,211,153,0.6)]'
            : isOnline
              ? 'bg-emerald-500/80'
              : 'bg-rose-500'
        }`}
      />
      <span className="font-mono text-neutral-200">{ip}</span>
      <span className="text-neutral-500">·</span>
      <span className="text-[11px] text-neutral-400 group-hover:text-neutral-300">
        {copied ? (
          <span className="font-medium text-emerald-400">Copied!</span>
        ) : (
          <span>{onlineCount} online</span>
        )}
      </span>
    </button>
  )
}
