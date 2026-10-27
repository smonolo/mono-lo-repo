<script setup lang="ts">
import { aibSitacCopy } from '#aibSitac/utils/aib-sitac-copy'

definePageMeta({ layout: 'aib-sitac' })

const copy = aibSitacCopy
const canonical = 'https://smnl.dev/aib-sitac/terms'

useSeoMeta({
  title: copy.terms.title,
  description: copy.terms.description,
  ogTitle: copy.terms.title,
  ogDescription: copy.terms.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: copy.terms.title,
  twitterDescription: copy.terms.description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})

const bodyClass =
  'text-aib-muted dark:text-aib-muted-dark text-[15px] leading-relaxed'
const h2Class = 'text-aib-fg dark:text-aib-fg-inverse text-[17px] font-semibold'
const listClass =
  'text-aib-muted dark:text-aib-muted-dark list-disc space-y-2 pl-5 text-[15px] leading-relaxed'
const linkClass =
  'text-aib-fg dark:text-aib-fg-inverse underline underline-offset-2 transition-opacity hover:opacity-70'
</script>

<template>
  <article class="space-y-10">
    <header class="space-y-3">
      <h1
        class="text-aib-fg dark:text-aib-fg-inverse text-[26px] font-bold leading-tight tracking-[-0.4px] sm:text-[28px]"
      >
        {{ copy.terms.heading }}
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark text-[13px] font-medium"
      >
        {{ copy.terms.updated }}
      </p>
    </header>

    <p :class="bodyClass">{{ copy.terms.intro }}</p>

    <ul :class="listClass">
      <li v-for="(item, i) in copy.terms.items" :key="i">{{ item }}</li>
    </ul>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.terms.contactTitle }}</h2>
      <p :class="bodyClass">
        <a href="mailto:stefano@smnl.dev" :class="linkClass"
          >stefano@smnl.dev</a
        >
        ·
        <NuxtLink to="/" :class="linkClass">smnl.dev</NuxtLink>
        ·
        <NuxtLink to="/aib-sitac/privacy" :class="linkClass">{{
          copy.nav.privacy
        }}</NuxtLink>
      </p>
    </section>
  </article>
</template>
