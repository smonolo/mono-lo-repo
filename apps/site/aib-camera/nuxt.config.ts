import { dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = dirname(fileURLToPath(import.meta.url))

/**
 * AIB Camera marketing site — Nuxt layer.
 * Extended by apps/site for now; extractable as its own Nuxt app later.
 *
 * CSS is imported from the host `assets/css/main.css` so Tailwind stays a
 * single entry while colocated. On extract, register
 * `css: ['~/assets/css/aib-camera.css']` here (with `@import 'tailwindcss'`).
 */
export default defineNuxtConfig({
  alias: {
    '#aib': currentDir,
  },
  // Avoid trailingSlash forcing /aib-camera/ — static assets live under
  // public/aib-assets/ so they do not collide with pages/aib-camera/.
  trailingSlash: false,
  routeRules: {
    '/aib-camera/guida': {
      redirect: { to: '/aib-camera/guide', statusCode: 301 },
    },
    // Old asset paths (pre aib-assets move)
    '/aib-camera/favicon.png': {
      redirect: { to: '/aib-assets/favicon.png', statusCode: 301 },
    },
    '/aib-camera/apple-touch-icon.png': {
      redirect: { to: '/aib-assets/apple-touch-icon.png', statusCode: 301 },
    },
    '/aib-camera/icon.png': {
      redirect: { to: '/aib-assets/icon.png', statusCode: 301 },
    },
    '/aib-camera/logo.png': {
      redirect: { to: '/aib-assets/logo.png', statusCode: 301 },
    },
  },
})
