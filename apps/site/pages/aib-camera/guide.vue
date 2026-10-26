<script setup lang="ts">
import { aibCopy } from '~/utils/aib-camera-copy'
import { aibGuideBody } from '~/utils/aib-guide-bodies'

definePageMeta({
  layout: 'aib-camera',
})

const { locale } = useAibLocale()
const copy = computed(() => aibCopy(locale.value))
const guideHtml = computed(() => aibGuideBody(locale.value))
const canonical = 'https://smnl.dev/aib-camera/guide'

useSeoMeta({
  title: () => copy.value.guide.title,
  description: () => copy.value.guide.description,
  ogTitle: () => copy.value.guide.title,
  ogDescription: () => copy.value.guide.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: () => copy.value.guide.title,
  twitterDescription: () => copy.value.guide.description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})

const btnClass =
  'inline-flex min-h-11 w-full items-center justify-center rounded-aib bg-aib-surface-muted px-4 text-[14px] font-semibold text-aib-fg no-underline hover:opacity-85 sm:w-auto dark:bg-aib-surface-muted-dark dark:text-aib-fg-inverse'
const btnGhostClass =
  'inline-flex min-h-11 w-full items-center justify-center rounded-aib border border-aib-border-strong bg-transparent px-4 text-[14px] font-semibold text-aib-fg no-underline hover:opacity-85 sm:w-auto dark:border-aib-border-strong-dark dark:text-aib-fg-inverse'
</script>

<template>
  <div class="space-y-8">
    <header class="space-y-4">
      <h1
        class="text-aib-fg dark:text-aib-fg-inverse text-[26px] font-bold leading-tight tracking-[-0.4px] sm:text-[28px]"
      >
        {{ copy.guide.heading }}
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse text-[16px] leading-normal sm:text-[17px] [&_strong]:font-semibold"
        v-html="copy.guide.leadHtml"
      />
      <div class="flex flex-col gap-2 pt-1 sm:flex-row sm:flex-wrap sm:gap-3">
        <a
          :href="copy.guide.pdfHref"
          :download="copy.guide.pdfFilename"
          :class="btnClass"
          >{{ copy.guide.downloadPdf }}</a
        >
        <a
          :href="copy.guide.pdfHref"
          target="_blank"
          rel="noopener"
          :class="btnGhostClass"
        >
          {{ copy.guide.openPdf }}
        </a>
      </div>
    </header>

    <article
      class="aib-prose rounded-aib bg-aib-surface dark:bg-aib-surface-dark overflow-hidden px-4 py-6 sm:px-7 sm:py-8"
      v-html="guideHtml"
    />
  </div>
</template>
