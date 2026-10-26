<script setup lang="ts">
import { aibCopy } from '#aib/utils/aib-camera-copy'

const { locale, setLocale, localePath } = useAibLocale()
const copy = computed(() => aibCopy(locale.value))
const route = useRoute()

const linkBase =
  'inline-flex min-h-10 items-center text-[12px] font-medium no-underline transition-opacity hover:opacity-70 sm:min-h-0 sm:text-[13px]'
const linkIdle = 'text-aib-muted dark:text-aib-muted-dark'
const linkActive = 'text-aib-fg dark:text-aib-fg-inverse'

function navClass(path: string) {
  const active =
    path === '/aib-camera'
      ? route.path === '/aib-camera'
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
        :to="localePath('/aib-camera')"
        class="shrink-0"
        :aria-label="copy.nav.homeAria"
      >
        <img
          src="/aib-assets/logo.png"
          alt="AIB Camera"
          width="350"
          height="180"
          class="h-6 w-auto brightness-0 sm:h-7 dark:brightness-100"
        />
      </NuxtLink>

      <nav
        class="flex min-w-0 flex-1 items-center justify-end gap-2.5 sm:gap-4"
        :aria-label="copy.footer.brand"
      >
        <NuxtLink
          :to="localePath('/aib-camera/guide')"
          :class="navClass('/aib-camera/guide')"
        >
          {{ copy.nav.guide }}
        </NuxtLink>
        <NuxtLink
          :to="localePath('/aib-camera/privacy')"
          :class="navClass('/aib-camera/privacy')"
        >
          {{ copy.nav.privacy }}
        </NuxtLink>
        <NuxtLink
          :to="localePath('/aib-camera/terms')"
          :class="navClass('/aib-camera/terms')"
        >
          {{ copy.nav.terms }}
        </NuxtLink>

        <div
          class="bg-aib-track dark:bg-aib-track-dark ml-0.5 flex shrink-0 items-center gap-0.5 rounded-lg p-0.5 sm:ml-1"
          role="group"
          :aria-label="copy.nav.langSwitchAria"
        >
          <button
            type="button"
            class="min-h-7 min-w-7 rounded-md px-1.5 text-[11px] font-medium transition-opacity sm:px-2 sm:text-[12px]"
            :class="
              locale === 'it'
                ? 'bg-aib-fg text-aib-fg-inverse dark:bg-aib-fg-inverse dark:text-aib-fg'
                : 'text-aib-track-idle dark:text-aib-track-idle-dark hover:opacity-70'
            "
            :aria-pressed="locale === 'it'"
            @click="setLocale('it')"
          >
            {{ copy.nav.langIt }}
          </button>
          <button
            type="button"
            class="min-h-7 min-w-7 rounded-md px-1.5 text-[11px] font-medium transition-opacity sm:px-2 sm:text-[12px]"
            :class="
              locale === 'en'
                ? 'bg-aib-fg text-aib-fg-inverse dark:bg-aib-fg-inverse dark:text-aib-fg'
                : 'text-aib-track-idle dark:text-aib-track-idle-dark hover:opacity-70'
            "
            :aria-pressed="locale === 'en'"
            @click="setLocale('en')"
          >
            {{ copy.nav.langEn }}
          </button>
        </div>
      </nav>
    </div>
  </header>
</template>
