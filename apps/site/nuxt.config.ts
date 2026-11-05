import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  modules: ['@nuxt/fonts', '@nuxt/icon', 'nuxt-gtag'],
  // Avoid Vite cold-start "Failed to resolve import #app-manifest" race
  // (nuxt/nuxt#30461 / #33606). This site does not need the app manifest.
  experimental: {
    appManifest: false,
  },
  routeRules: {
    '/aib-camera/guida': {
      redirect: { to: 'https://aib.smnl.dev/camera/guide', statusCode: 301 },
    },
    '/aib-camera/novita': {
      redirect: {
        to: 'https://aib.smnl.dev/camera/changelog',
        statusCode: 301,
      },
    },
    '/aib-camera': {
      redirect: { to: 'https://aib.smnl.dev/camera', statusCode: 301 },
    },
    '/aib-camera/**': {
      redirect: { to: 'https://aib.smnl.dev/camera/**', statusCode: 301 },
    },
    '/aib-sitac': {
      redirect: { to: 'https://aib.smnl.dev/sitac', statusCode: 301 },
    },
    '/aib-sitac/**': {
      redirect: { to: 'https://aib.smnl.dev/sitac/**', statusCode: 301 },
    },
    '/aib-assets/**': {
      redirect: { to: 'https://aib.smnl.dev/camera/**', statusCode: 301 },
    },
    '/docs/aib-camera-guida-operativa.pdf': {
      redirect: {
        to: 'https://aib.smnl.dev/docs/aib-camera-guida-operativa.pdf',
        statusCode: 301,
      },
    },
    '/docs/aib-camera-field-guide.pdf': {
      redirect: {
        to: 'https://aib.smnl.dev/docs/aib-camera-field-guide.pdf',
        statusCode: 301,
      },
    },
  },

  gtag: {
    id: 'G-F1H9HVR3BL',
  },
  css: ['~/assets/css/main.css'],
  vite: {
    plugins: [tailwindcss()],
  },
  fonts: {
    defaults: {
      weights: [400, 500, 600, 700, 800],
    },
    families: [
      // Self-hosted (not on Google Fonts yet). Keep filename as GeistSerif —
      // `geist-serif.*` also registers as family `Geist` via @nuxt/fonts local.
      { name: 'Geist Serif', src: '/fonts/GeistSerif.woff2', weight: 400 },
    ],
  },
  app: {
    head: {
      title: 'Stefano Monolo',
      htmlAttrs: {
        lang: 'en',
      },
      bodyAttrs: {
        class: 'font-display bg-sm-black text-white',
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
