import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  css: ['~/assets/css/main.css'],
  vite: {
    plugins: [tailwindcss()],
  },
  app: {
    head: {
      title: 'Stefano Monolo',
      htmlAttrs: {
        lang: 'en',
      },
      meta: [
        { name: 'theme-color', content: '#121215' },
        {
          name: 'description',
          content:
            'I am a software engineer passionate about the web and its technologies.',
        },
        { name: 'application-name', content: 'smnl.dev' },
        { name: 'author', content: 'Stefano Monolo <stefano@smnl.dev>' },
        { name: 'creator', content: 'Stefano Monolo <stefano@smnl.dev>' },
        { name: 'publisher', content: 'Stefano Monolo <stefano@smnl.dev>' },
        { name: 'robots', content: 'index, follow' },
        { property: 'og:title', content: 'Stefano Monolo' },
        {
          property: 'og:description',
          content:
            'I am a software engineer passionate about the web and its technologies.',
        },
        { property: 'og:url', content: 'https://smnl.dev' },
        { property: 'og:type', content: 'website' },
        { property: 'og:site_name', content: 'Stefano Monolo' },
        { name: 'twitter:card', content: 'summary' },
        { name: 'twitter:site', content: '@stmonolo' },
        { name: 'twitter:creator', content: '@stmonolo' },
        { name: 'twitter:title', content: 'Stefano Monolo' },
        {
          name: 'twitter:description',
          content:
            'I am a software engineer passionate about the web and its technologies.',
        },
      ],
      link: [
        { rel: 'author', href: 'https://smnl.dev' },
        { rel: 'canonical', href: 'https://smnl.dev' },
      ],
    },
  },
})
