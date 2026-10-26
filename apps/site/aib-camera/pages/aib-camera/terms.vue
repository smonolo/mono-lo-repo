<script setup lang="ts">
import { aibCopy } from '#aib/utils/aib-camera-copy'
import { aibHtml } from '#aib/utils/aib-html'

definePageMeta({
  layout: 'aib-camera',
})

const { locale } = useAibLocale()
const copy = computed(() => aibCopy(locale.value))
const canonical = 'https://smnl.dev/aib-camera/terms'

useSeoMeta({
  title: () => copy.value.terms.title,
  description: () => copy.value.terms.description,
  ogTitle: () => copy.value.terms.title,
  ogDescription: () => copy.value.terms.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: () => copy.value.terms.title,
  twitterDescription: () => copy.value.terms.description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})

const bodyClass =
  'text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const h2Class = 'text-[17px] font-semibold text-aib-fg dark:text-aib-fg-inverse'
const listClass =
  'list-disc space-y-2 pl-5 text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const richBody = [
  bodyClass,
  '[&_a]:text-aib-fg [&_a]:underline [&_a]:underline-offset-2 [&_a]:hover:opacity-70 dark:[&_a]:text-aib-fg-inverse',
].join(' ')

const privacyBody = computed(() =>
  aibHtml(copy.value.terms.privacyBodyHtml, { locale: locale.value })
)
const contactsBody = computed(() =>
  aibHtml(copy.value.terms.contactsBodyHtml, { locale: locale.value })
)
const contactsMail = computed(() =>
  aibHtml(copy.value.terms.contactsMailHtml, { locale: locale.value })
)
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

    <div :class="['space-y-4', bodyClass]">
      <p v-html="copy.terms.intro1Html" />
      <p v-html="copy.terms.intro2Html" />
    </div>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.whoTitle }}</h2>
      <p :class="bodyClass" v-html="copy.terms.whoBodyHtml" />
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.purposeTitle }}</h2>
      <p :class="bodyClass" v-html="copy.terms.purposeBodyHtml" />
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.allowedTitle }}</h2>
      <ul :class="listClass">
        <li v-for="(item, i) in copy.terms.allowed" :key="i">
          {{ item }}
        </li>
      </ul>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.aimTitle }}</h2>
      <p :class="bodyClass" v-html="copy.terms.aimBodyHtml" />
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.asIsTitle }}</h2>
      <p :class="bodyClass" v-html="copy.terms.asIsBody1Html" />
      <p :class="bodyClass" v-html="copy.terms.asIsBody2Html" />
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.liabilityTitle }}</h2>
      <p :class="bodyClass">{{ copy.terms.liabilityBody }}</p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.privacyTitle }}</h2>
      <p :class="richBody" v-html="privacyBody" />
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.ipTitle }}</h2>
      <p :class="bodyClass">{{ copy.terms.ipBody }}</p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.updatesTitle }}</h2>
      <p :class="bodyClass">{{ copy.terms.updatesBody }}</p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">{{ copy.terms.contactsTitle }}</h2>
      <p :class="richBody" v-html="contactsBody" />
      <p :class="richBody" v-html="contactsMail" />
    </section>
  </article>
</template>
