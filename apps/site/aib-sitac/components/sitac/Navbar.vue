<script setup lang="ts">
import { aibSitacCopy } from '#aibSitac/utils/aib-sitac-copy'

const copy = aibSitacCopy
const route = useRoute()
const appHref = useRuntimeConfig().public.aibSitacAppUrl as string

const linkBase =
  'inline-flex min-h-10 items-center text-[12px] font-medium no-underline transition-opacity hover:opacity-70 sm:min-h-0 sm:text-[13px]'
const linkIdle = 'text-aib-muted dark:text-aib-muted-dark'
const linkActive = 'text-aib-fg dark:text-aib-fg-inverse'

function navClass(path: string) {
  const active =
    path === '/aib-sitac'
      ? route.path === '/aib-sitac'
      : route.path === path || route.path.startsWith(`${path}/`)
  return [linkBase, active ? linkActive : linkIdle]
}
</script>

<template>
  <header
    class="border-aib-border dark:border-aib-border-dark bg-aib-canvas/90 dark:bg-aib-canvas-inverse/90 sticky top-0 z-20 border-b backdrop-blur-md"
    style="padding-top: env(safe-area-inset-top)"
  >
    <div
      class="mx-auto flex h-12 w-full max-w-[700px] items-center justify-between gap-2 px-4 sm:h-14 sm:gap-3 sm:px-5"
    >
      <NuxtLink
        to="/aib-sitac"
        class="flex shrink-0 items-center gap-2 no-underline transition-opacity hover:opacity-70"
        :aria-label="copy.nav.homeAria"
      >
        <img
          src="/aib-assets/logo.png"
          alt=""
          width="350"
          height="180"
          class="h-4.5 w-auto brightness-0 sm:h-5 dark:brightness-100"
        />
        <span
          class="text-aib-fg dark:text-aib-fg-inverse text-[18px] font-bold tracking-[-0.4px] sm:text-[20px]"
        >
          SITAC
        </span>
      </NuxtLink>

      <nav
        class="flex min-w-0 flex-1 items-center justify-end gap-2.5 sm:gap-4"
        :aria-label="copy.footer.brand"
      >
        <NuxtLink
          to="/aib-sitac/guide"
          :class="navClass('/aib-sitac/guide')"
          class="hidden sm:inline-flex"
        >
          {{ copy.nav.guide }}
        </NuxtLink>
        <NuxtLink
          to="/aib-sitac/privacy"
          :class="navClass('/aib-sitac/privacy')"
          class="hidden sm:inline-flex"
        >
          {{ copy.nav.privacy }}
        </NuxtLink>
        <NuxtLink
          to="/aib-sitac/terms"
          :class="navClass('/aib-sitac/terms')"
          class="hidden sm:inline-flex"
        >
          {{ copy.nav.terms }}
        </NuxtLink>
        <a
          :href="appHref"
          :class="[linkBase, linkActive]"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ copy.nav.openApp }}
        </a>
      </nav>
    </div>
  </header>
</template>
