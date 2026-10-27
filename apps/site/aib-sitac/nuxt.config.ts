import { dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = dirname(fileURLToPath(import.meta.url))

export default defineNuxtConfig({
  alias: {
    '#aibSitac': currentDir,
  },
  trailingSlash: false,
  runtimeConfig: {
    public: {
      aibSitacAppUrl:
        process.env.NUXT_PUBLIC_AIB_SITAC_APP_URL ||
        'https://aib-sitac.smnl.dev',
    },
  },
})
