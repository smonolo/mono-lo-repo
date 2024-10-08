import type { Metadata, Viewport } from 'next'
import type { PropsWithChildren } from 'react'
import '@/app/globals.css'
import Navbar from '@/components/layout/navbar'
import Footer from '@/components/layout/footer'
import { GoogleAnalytics } from '@next/third-parties/google'

export const metadata: Metadata = {
  title: {
    template: '%s | Stefano Monolo',
    default: 'Stefano Monolo',
  },
  description:
    'I am a software engineer deeply passionate about the web and its technologies.',
  alternates: {
    canonical: '/',
  },
  applicationName: 'Stefano Monolo',
  authors: [
    {
      name: 'Stefano Monolo <stefano@smnl.dev>',
      url: 'https://smnl.dev',
    },
  ],
  creator: 'Stefano Monolo <stefano@smnl.dev>',
  metadataBase: new URL('https://smnl.dev'),
  openGraph: {
    title: 'Stefano Monolo',
    description:
      'I am a software engineer deeply passionate about the web and its technologies.',
    url: 'https://smnl.dev',
    siteName: 'Stefano',
    type: 'website',
  },
  publisher: 'Stefano Monolo <stefano@smnl.dev>',
  robots: {
    index: true,
    follow: true,
  },
  twitter: {
    card: 'summary',
    site: '@stmonolo',
    creator: '@stmonolo',
    title: 'Stefano Monolo',
    description:
      'I am a software engineer deeply passionate about the web and its technologies.',
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  themeColor: '#121215',
}

export default function RootLayout({ children }: PropsWithChildren) {
  return (
    <html lang="en">
      <body className="bg-sm-black text-sm-white font-body relative m-0 flex min-h-screen w-full flex-col justify-between p-0">
        <GoogleAnalytics gaId="G-7BK0LLG38Q" />
        <Navbar />
        <main className="mx-auto w-full max-w-[1000px] px-5 py-20 md:py-32 lg:px-0">
          {children}
        </main>
        <Footer />
      </body>
    </html>
  )
}
