<script setup lang="ts">
useHead({
  htmlAttrs: { lang: 'it' },
  link: [
    { rel: 'icon', type: 'image/png', href: '/aib-camera/favicon.png' },
    { rel: 'apple-touch-icon', href: '/aib-camera/apple-touch-icon.png' },
  ],
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
      content: 'https://smnl.dev/aib-camera/icon.png',
    },
    {
      name: 'twitter:image',
      content: 'https://smnl.dev/aib-camera/icon.png',
    },
  ],
})
</script>

<template>
  <div
    class="aib bg-aib-canvas font-display text-aib-fg dark:bg-aib-canvas-inverse dark:text-aib-fg-inverse flex min-h-screen flex-col antialiased"
  >
    <div class="mx-auto w-full max-w-[700px] flex-1 px-5 pb-10 pt-12">
      <main>
        <slot />
      </main>
    </div>
    <footer
      class="text-aib-muted dark:text-aib-muted-dark mx-auto flex w-full max-w-[700px] items-center justify-between gap-4 px-5 py-8 text-[13px] font-medium"
    >
      <p>
        AIB Camera
        <span aria-hidden="true"> · </span>
        <NuxtLink
          to="/"
          class="text-aib-fg dark:text-aib-fg-inverse underline underline-offset-2 hover:opacity-70"
          >smnl.dev</NuxtLink
        >
      </p>
      <a
        href="mailto:stefano@smnl.dev"
        class="text-aib-fg dark:text-aib-fg-inverse underline underline-offset-2 hover:opacity-70"
        >stefano@smnl.dev</a
      >
    </footer>
  </div>
</template>
