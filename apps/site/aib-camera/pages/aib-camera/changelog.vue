<script setup lang="ts">
import { aibCopy } from '#aib/utils/aib-camera-copy'
import { aibChangelogBody } from '#aib/utils/aib-changelog-bodies'

definePageMeta({
  layout: 'aib-camera',
})

const { locale } = useAibLocale()
const copy = computed(() => aibCopy(locale.value))
const changelogHtml = computed(() => aibChangelogBody(locale.value))
const canonical = 'https://smnl.dev/aib-camera/changelog'

useSeoMeta({
  title: () => copy.value.changelog.title,
  description: () => copy.value.changelog.description,
  ogTitle: () => copy.value.changelog.title,
  ogDescription: () => copy.value.changelog.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: () => copy.value.changelog.title,
  twitterDescription: () => copy.value.changelog.description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})
</script>

<template>
  <div class="space-y-8">
    <header class="space-y-4">
      <h1
        class="text-aib-fg dark:text-aib-fg-inverse text-[26px] font-bold leading-tight tracking-[-0.4px] sm:text-[28px]"
      >
        {{ copy.changelog.heading }}
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse text-[16px] leading-normal sm:text-[17px] [&_strong]:font-semibold"
        v-html="copy.changelog.leadHtml"
      />
    </header>

    <article
      class="aib-prose rounded-aib bg-aib-surface dark:bg-aib-surface-dark overflow-hidden px-4 py-6 sm:px-7 sm:py-8"
      v-html="changelogHtml"
    />
  </div>
</template>
