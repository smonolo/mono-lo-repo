import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import '@/app/globals.css'
import type { PropsWithChildren } from 'react'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'Hello',
}

export default function RootLayout({ children }: PropsWithChildren) {
  return (
    <html lang="en">
      <body className={inter.className}>{children}</body>
    </html>
  )
}
