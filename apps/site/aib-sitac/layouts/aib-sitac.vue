<script setup lang="ts">
import { aibSitacCopy } from '#aibSitac/utils/aib-sitac-copy'

const copy = aibSitacCopy
const route = useRoute()

const linkBase =
  'inline-flex min-h-8 items-center text-[12px] font-medium no-underline transition-opacity hover:opacity-70 sm:min-h-0 sm:text-[13px]'
const linkIdle = 'text-aib-muted dark:text-aib-muted-dark'
const linkActive = 'text-aib-fg dark:text-aib-fg-inverse'

function navClass(path: string) {
  const active =
    path === '/aib-sitac'
      ? route.path === '/aib-sitac'
      : route.path === path || route.path.startsWith(`${path}/`)
  return [linkBase, active ? linkActive : linkIdle]
}

useHead({
  meta: [
    {
      name: 'theme-color',
      content: '#F5F5F5',
      media: '(prefers-color-scheme: light)',
    },
    {
      name: 'theme-color',
      content: '#111111',
      media: '(prefers-color-scheme: dark)',
    },
    {
      property: 'og:image',
      content: 'https://smnl.dev/aib-assets/icon.png',
    },
    {
      name: 'twitter:image',
      content: 'https://smnl.dev/aib-assets/icon.png',
    },
  ],
  link: [
    { rel: 'icon', type: 'image/png', href: '/aib-assets/favicon.png' },
    { rel: 'apple-touch-icon', href: '/aib-assets/apple-touch-icon.png' },
  ],
})
</script>

<template>
  <div
    class="aib bg-aib-canvas font-display text-aib-fg dark:bg-aib-canvas-inverse dark:text-aib-fg-inverse flex min-h-dvh flex-col overflow-x-hidden antialiased"
  >
    <SitacNavbar />
    <div
      class="mx-auto w-full max-w-[700px] flex-1 px-4 pb-10 pt-6 sm:px-5 sm:pt-8"
    >
      <main>
        <slot />
      </main>
    </div>
    <footer
      class="border-aib-border dark:border-aib-border-dark w-full border-t"
    >
      <div
        class="text-aib-muted dark:text-aib-muted-dark flex w-full flex-col gap-2 px-4 py-3 text-[13px] font-medium sm:flex-row sm:items-center sm:justify-between sm:gap-3 sm:px-5"
        style="padding-bottom: max(0.75rem, env(safe-area-inset-bottom))"
      >
        <nav
          class="flex min-w-0 flex-wrap items-center gap-x-3 gap-y-1"
          :aria-label="copy.nav.home"
        >
          <NuxtLink to="/aib-sitac/guide" :class="navClass('/aib-sitac/guide')">
            {{ copy.nav.guide }}
          </NuxtLink>
          <NuxtLink
            to="/aib-sitac/privacy"
            :class="navClass('/aib-sitac/privacy')"
          >
            {{ copy.nav.privacy }}
          </NuxtLink>
          <NuxtLink to="/aib-sitac/terms" :class="navClass('/aib-sitac/terms')">
            {{ copy.nav.terms }}
          </NuxtLink>
        </nav>

        <div
          class="flex min-w-0 flex-wrap items-center gap-x-3 gap-y-1 sm:justify-end"
        >
          <NuxtLink
            to="/"
            class="text-aib-fg dark:text-aib-fg-inverse underline underline-offset-2 transition-opacity hover:opacity-70"
          >
            smnl.dev
          </NuxtLink>
          <a
            href="mailto:stefano@smnl.dev"
            class="text-aib-fg dark:text-aib-fg-inverse break-all underline underline-offset-2 transition-opacity hover:opacity-70 sm:break-normal"
          >
            stefano@smnl.dev
          </a>
        </div>
      </div>
    </footer>
  </div>
</template>
