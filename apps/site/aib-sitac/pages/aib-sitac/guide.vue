<script setup lang="ts">
import { aibSitacCopy } from '#aibSitac/utils/aib-sitac-copy'

definePageMeta({ layout: 'aib-sitac' })

const copy = aibSitacCopy
const appHref = useRuntimeConfig().public.aibSitacAppUrl as string
const canonical = 'https://smnl.dev/aib-sitac/guide'

useSeoMeta({
  title: copy.guide.title,
  description: copy.guide.description,
  ogTitle: copy.guide.title,
  ogDescription: copy.guide.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: copy.guide.title,
  twitterDescription: copy.guide.description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})

const bodyClass =
  'text-aib-muted dark:text-aib-muted-dark text-[15px] leading-relaxed'
const h2Class = 'text-aib-fg dark:text-aib-fg-inverse text-[17px] font-semibold'
const ctaClass =
  'inline-flex min-h-[52px] w-full items-center justify-center rounded-aib bg-aib-surface-muted px-4 text-[15px] font-semibold text-aib-fg no-underline transition-opacity hover:opacity-70 sm:w-auto dark:bg-aib-surface-muted-dark dark:text-aib-fg-inverse'
</script>

<template>
  <article class="space-y-10">
    <header class="space-y-3">
      <h1
        class="text-aib-fg dark:text-aib-fg-inverse text-[26px] font-bold leading-tight tracking-[-0.4px] sm:text-[28px]"
      >
        {{ copy.guide.heading }}
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark text-[16px] leading-normal sm:text-[17px]"
      >
        {{ copy.guide.lead }}
      </p>
      <div class="pt-1">
        <a
          :href="appHref"
          :class="ctaClass"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ copy.guide.openApp }}
        </a>
      </div>
    </header>

    <section
      v-for="section in copy.guide.sections"
      :key="section.title"
      class="space-y-3"
    >
      <h2 :class="h2Class">{{ section.title }}</h2>
      <div :class="['space-y-3', bodyClass]">
        <p v-for="(p, i) in section.paragraphs" :key="i">{{ p }}</p>
      </div>
    </section>

    <p :class="bodyClass">
      Vedi anche
      <NuxtLink
        to="/aib-sitac/privacy"
        class="text-aib-fg dark:text-aib-fg-inverse underline underline-offset-2 transition-opacity hover:opacity-70"
        >{{ copy.nav.privacy }}</NuxtLink
      >
      e
      <NuxtLink
        to="/aib-sitac/terms"
        class="text-aib-fg dark:text-aib-fg-inverse underline underline-offset-2 transition-opacity hover:opacity-70"
        >{{ copy.nav.terms }}</NuxtLink
      >.
    </p>
  </article>
</template>
