<script setup lang="ts">
import { aibCopy } from '~/utils/aib-camera-copy'

const { locale } = useAibLocale()
const copy = computed(() => aibCopy(locale.value))

useHead({
  meta: [
    {
      name: 'viewport',
      content: 'width=device-width, initial-scale=1, viewport-fit=cover',
    },
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
    <AibNavbar />
    <div
      class="mx-auto w-full max-w-[700px] flex-1 px-4 pb-10 pt-6 sm:px-5 sm:pt-8"
    >
      <main>
        <slot />
      </main>
    </div>
    <footer
      class="text-aib-muted dark:text-aib-muted-dark mx-auto flex w-full max-w-[700px] flex-col gap-2 px-4 py-8 text-[13px] font-medium sm:flex-row sm:items-center sm:justify-between sm:gap-4 sm:px-5"
      style="padding-bottom: max(2rem, env(safe-area-inset-bottom))"
    >
      <p class="min-w-0">
        {{ copy.footer.brand }}
        <span aria-hidden="true"> · </span>
        <NuxtLink
          to="/"
          class="text-aib-fg dark:text-aib-fg-inverse underline underline-offset-2 hover:opacity-70"
          >smnl.dev</NuxtLink
        >
      </p>
      <a
        href="mailto:stefano@smnl.dev"
        class="text-aib-fg dark:text-aib-fg-inverse break-all underline underline-offset-2 hover:opacity-70 sm:break-normal"
        >stefano@smnl.dev</a
      >
    </footer>
  </div>
</template>
