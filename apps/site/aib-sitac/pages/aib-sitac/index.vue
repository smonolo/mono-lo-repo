<script setup lang="ts">
import { aibSitacCopy } from '#aibSitac/utils/aib-sitac-copy'

definePageMeta({ layout: 'aib-sitac' })

const copy = aibSitacCopy
const appHref = useRuntimeConfig().public.aibSitacAppUrl as string
const canonical = 'https://smnl.dev/aib-sitac'

useSeoMeta({
  title: copy.home.title,
  description: copy.home.description,
  ogTitle: copy.home.title,
  ogDescription: copy.home.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: copy.home.title,
  twitterDescription: copy.home.description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})

const linkClass =
  'text-aib-fg dark:text-aib-fg-inverse underline underline-offset-2 transition-opacity hover:opacity-70'
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
        {{ copy.home.title }}
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark text-[16px] leading-normal sm:text-[17px]"
      >
        {{ copy.home.lead }}
      </p>
      <div class="pt-1">
        <a
          :href="appHref"
          :class="ctaClass"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ copy.home.appCta }}
        </a>
      </div>
    </header>

    <section :class="['space-y-4', bodyClass]">
      <p>{{ copy.home.intro1 }}</p>
      <p>{{ copy.home.intro2 }}</p>
    </section>

    <section>
      <p
        class="text-aib-muted dark:text-aib-muted-dark mb-2 text-[13px] font-medium"
      >
        {{ copy.home.featuresLabel }}
      </p>
      <ul
        class="rounded-aib bg-aib-surface dark:bg-aib-surface-dark overflow-hidden"
      >
        <li
          v-for="feature in copy.home.features"
          :key="feature.title"
          class="border-aib-border dark:border-aib-border-dark border-b px-3.5 py-3.5 last:border-b-0"
        >
          <p
            class="text-aib-fg dark:text-aib-fg-inverse text-[15px] font-semibold"
          >
            {{ feature.title }}
          </p>
          <p
            class="text-aib-muted dark:text-aib-muted-dark mt-1 text-[13px] leading-snug"
          >
            {{ feature.body }}
          </p>
        </li>
      </ul>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.home.guideTitle }}</h2>
      <p :class="bodyClass">
        {{ copy.home.guideBody }}
        <NuxtLink to="/aib-sitac/guide" :class="linkClass">{{
          copy.home.guideLink
        }}</NuxtLink
        >.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.home.privacyTitle }}</h2>
      <p :class="bodyClass">
        {{ copy.home.privacyBody }}
        <NuxtLink to="/aib-sitac/privacy" :class="linkClass">{{
          copy.home.privacyLink
        }}</NuxtLink
        >.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.home.termsTitle }}</h2>
      <p :class="bodyClass">
        {{ copy.home.termsBody }}
        <NuxtLink to="/aib-sitac/terms" :class="linkClass">{{
          copy.home.termsLink
        }}</NuxtLink
        >.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.home.contactsTitle }}</h2>
      <p :class="bodyClass">
        <a href="mailto:stefano@smnl.dev" :class="linkClass"
          >stefano@smnl.dev</a
        >
        ·
        <NuxtLink to="/" :class="linkClass">smnl.dev</NuxtLink>
      </p>
    </section>
  </article>
</template>
