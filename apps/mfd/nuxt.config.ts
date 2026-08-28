export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  css: ['~/assets/css/main.css'],
  app: {
    head: {
      title: 'MFD',
      script: [
        {
          src: 'https://accounts.google.com/gsi/client',
          async: true,
          defer: true,
        },
      ],
    },
  },
  runtimeConfig: {
    adminEmail: process.env.ADMIN_EMAIL || '',
    sessionSecret: process.env.SESSION_SECRET || '',
    smessentialApiUrl: process.env.SMESSENTIAL_API_URL || '',
    smessentialApiSecret: process.env.SMESSENTIAL_API_SECRET || '',
    public: {
      googleClientId:
        process.env.NUXT_PUBLIC_GOOGLE_CLIENT_ID ||
        process.env.GOOGLE_CLIENT_ID ||
        '',
    },
  },
  postcss: {
    plugins: {
      tailwindcss: {},
      autoprefixer: {},
    },
  },
  modules: ['@pinia/nuxt'],
})
