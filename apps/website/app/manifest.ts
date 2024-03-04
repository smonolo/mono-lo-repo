import type { MetadataRoute } from 'next'

export default function manifest(): MetadataRoute.Manifest {
  return {
    name: 'Stefano Monolo',
    short_name: 'Stefano',
    description:
      'I am a software engineer deeply passionate about the web and its technologies.',
    start_url: '/',
    display: 'standalone',
    background_color: '#121215',
    theme_color: '#121215',
    icons: [
      {
        src: '/favicon.ico',
        sizes: 'any',
        type: 'image/x-icon',
      },
    ],
  }
}
