<script setup lang="ts">
import { aibSitacCopy } from '#aibSitac/utils/aib-sitac-copy'

definePageMeta({ layout: 'aib-sitac' })

const copy = aibSitacCopy
const canonical = 'https://smnl.dev/aib-sitac/privacy'

useSeoMeta({
  title: copy.privacy.title,
  description: copy.privacy.description,
  ogTitle: copy.privacy.title,
  ogDescription: copy.privacy.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: copy.privacy.title,
  twitterDescription: copy.privacy.description,
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
        {{ copy.privacy.heading }}
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark text-[13px] font-medium"
      >
        {{ copy.privacy.updated }}
      </p>
    </header>

    <p :class="bodyClass">{{ copy.privacy.intro }}</p>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.privacy.summaryTitle }}</h2>
      <ul :class="listClass">
        <li v-for="(item, i) in copy.privacy.summary" :key="i">{{ item }}</li>
      </ul>
    </section>

    <section class="space-y-4">
      <h2 :class="h2Class">{{ copy.privacy.dataTitle }}</h2>
      <div
        class="rounded-aib bg-aib-surface dark:bg-aib-surface-dark overflow-x-auto"
      >
        <table
          class="w-full min-w-[36rem] border-collapse text-left text-[13px]"
        >
          <thead>
            <tr>
              <th
                class="border-aib-border text-aib-fg dark:border-aib-border-dark dark:text-aib-fg-inverse border-b px-3.5 py-3 font-semibold"
              >
                {{ copy.privacy.colData }}
              </th>
              <th
                class="border-aib-border text-aib-fg dark:border-aib-border-dark dark:text-aib-fg-inverse border-b px-3.5 py-3 font-semibold"
              >
                {{ copy.privacy.colWhere }}
              </th>
              <th
                class="border-aib-border text-aib-fg dark:border-aib-border-dark dark:text-aib-fg-inverse border-b px-3.5 py-3 font-semibold"
              >
                {{ copy.privacy.colWhy }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in copy.privacy.dataRows"
              :key="row.data"
              class="border-aib-border dark:border-aib-border-dark border-b last:border-b-0"
            >
              <td
                class="text-aib-muted dark:text-aib-muted-dark px-3.5 py-3 align-top"
              >
                {{ row.data }}
              </td>
              <td
                class="text-aib-muted dark:text-aib-muted-dark px-3.5 py-3 align-top"
              >
                {{ row.where }}
              </td>
              <td
                class="text-aib-muted dark:text-aib-muted-dark px-3.5 py-3 align-top"
              >
                {{ row.why }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.privacy.accessTitle }}</h2>
      <ul :class="listClass">
        <li v-for="(item, i) in copy.privacy.access" :key="i">{{ item }}</li>
      </ul>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.privacy.shareTitle }}</h2>
      <p :class="bodyClass">{{ copy.privacy.shareBody }}</p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.privacy.contactTitle }}</h2>
      <p :class="bodyClass">
        <a href="mailto:stefano@smnl.dev" :class="linkClass"
          >stefano@smnl.dev</a
        >
        ·
        <NuxtLink to="/" :class="linkClass">smnl.dev</NuxtLink>
        ·
        <NuxtLink to="/aib-sitac/terms" :class="linkClass">{{
          copy.nav.terms
        }}</NuxtLink>
      </p>
    </section>
  </article>
</template>
