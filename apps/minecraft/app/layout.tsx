import type { Metadata, Viewport } from 'next'
import { GeistSans } from 'geist/font/sans'
import { GeistMono } from 'geist/font/mono'
import type { PropsWithChildren } from 'react'
import '@/app/globals.css'

import Navbar from '@/components/Navbar'
import Footer from '@/components/Footer'

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 5,
  themeColor: '#0a0a0c',
}

export const metadata: Metadata = {
  title: {
    default: 'Minecraft',
    template: '%s - Minecraft',
  },
  description: 'Minecraft Server Platform & Telemetry',
  icons: {
    icon: '/favicon.ico',
  },
  robots: {
    index: false,
    follow: false,
    nocache: true,
    googleBot: {
      index: false,
      follow: false,
      noimageindex: true,
    },
  },
}

export default function RootLayout({ children }: PropsWithChildren) {
  return (
    <html
      lang="en"
      className={`${GeistSans.variable} ${GeistMono.variable} h-full`}
    >
      <body className="bg-sm-black flex min-h-screen flex-col font-sans text-white antialiased">
        <Navbar />
        <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:px-6 sm:py-10 lg:px-8">
          {children}
        </main>
        <Footer />
      </body>
    </html>
  )
}
