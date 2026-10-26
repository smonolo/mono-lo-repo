<script setup lang="ts">
import { aibCopy } from '#aib/utils/aib-camera-copy'
import { aibHtml } from '#aib/utils/aib-html'

definePageMeta({
  layout: 'aib-camera',
})

const { locale } = useAibLocale()
const copy = computed(() => aibCopy(locale.value))
const canonical = 'https://smnl.dev/aib-camera/privacy'

useSeoMeta({
  title: () => copy.value.privacy.title,
  description: () => copy.value.privacy.description,
  ogTitle: () => copy.value.privacy.title,
  ogDescription: () => copy.value.privacy.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: () => copy.value.privacy.title,
  twitterDescription: () => copy.value.privacy.description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})

const bodyClass =
  'text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const h2Class = 'text-[17px] font-semibold text-aib-fg dark:text-aib-fg-inverse'
const h3Class = 'text-[15px] font-semibold text-aib-fg dark:text-aib-fg-inverse'
const listClass =
  'list-disc space-y-2 pl-5 text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const codeClass =
  'rounded-md bg-aib-surface-muted px-1 py-0.5 font-mono text-[0.86em] text-aib-fg dark:bg-aib-surface-muted-dark dark:text-aib-fg-inverse'
const richBody = [
  bodyClass,
  '[&_a]:text-aib-fg [&_a]:underline [&_a]:underline-offset-2 [&_a]:hover:opacity-70 dark:[&_a]:text-aib-fg-inverse',
].join(' ')

const logBody = computed(() =>
  aibHtml(copy.value.privacy.logBodyHtml, {
    codeClass,
    locale: locale.value,
  })
)
const exifBody = computed(() =>
  aibHtml(copy.value.privacy.exifBodyHtml, {
    codeClass,
    locale: locale.value,
  })
)
const contactsBody = computed(() =>
  aibHtml(copy.value.privacy.contactsBodyHtml, { locale: locale.value })
)
const contactsMail = computed(() =>
  aibHtml(copy.value.privacy.contactsMailHtml, { locale: locale.value })
)
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

    <div :class="['space-y-4', bodyClass]">
      <p v-html="copy.privacy.intro1Html" />
      <p>{{ copy.privacy.intro2 }}</p>
    </div>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.privacy.summaryTitle }}</h2>
      <ul :class="listClass">
        <li v-for="(item, i) in copy.privacy.summary" :key="i" v-html="item" />
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
            <tr v-for="row in copy.privacy.dataRows" :key="row.data">
              <td
                class="border-aib-border text-aib-fg dark:border-aib-border-dark dark:text-aib-fg-inverse border-t px-3.5 py-3 align-top font-medium"
              >
                {{ row.data }}
              </td>
              <td
                class="border-aib-border text-aib-muted dark:border-aib-border-dark dark:text-aib-muted-dark border-t px-3.5 py-3 align-top"
              >
                {{ row.where }}
              </td>
              <td
                class="border-aib-border text-aib-muted dark:border-aib-border-dark dark:text-aib-muted-dark border-t px-3.5 py-3 align-top"
              >
                {{ row.why }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <h3 :class="h3Class">{{ copy.privacy.logTitle }}</h3>
      <p :class="richBody" v-html="logBody" />

      <h3 :class="h3Class">{{ copy.privacy.exifTitle }}</h3>
      <p :class="richBody" v-html="exifBody" />

      <p :class="bodyClass" v-html="copy.privacy.noBackendHtml" />
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.privacy.permissionsTitle }}</h2>
      <ul :class="listClass">
        <li
          v-for="(item, i) in copy.privacy.permissions"
          :key="i"
          v-html="item"
        />
      </ul>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.privacy.shareTitle }}</h2>
      <p :class="bodyClass">{{ copy.privacy.shareBody }}</p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.privacy.minorsTitle }}</h2>
      <p :class="bodyClass">{{ copy.privacy.minorsBody }}</p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.privacy.changesTitle }}</h2>
      <p :class="bodyClass">{{ copy.privacy.changesBody }}</p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.privacy.contactsTitle }}</h2>
      <p :class="richBody" v-html="contactsBody" />
      <p :class="richBody" v-html="contactsMail" />
    </section>
  </article>
</template>
