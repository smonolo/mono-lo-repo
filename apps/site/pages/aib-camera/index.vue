<script setup lang="ts">
import { aibCopy } from '~/utils/aib-camera-copy'
import { aibHtml } from '~/utils/aib-html'

definePageMeta({
  layout: 'aib-camera',
})

const { locale } = useAibLocale()
const copy = computed(() => aibCopy(locale.value))
const canonical = 'https://smnl.dev/aib-camera'

useSeoMeta({
  title: () => copy.value.home.title,
  description: () => copy.value.home.description,
  ogTitle: () => copy.value.home.title,
  ogDescription: () => copy.value.home.description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: () => copy.value.home.title,
  twitterDescription: () => copy.value.home.description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})

const linkClass =
  'text-aib-fg underline underline-offset-2 hover:opacity-70 dark:text-aib-fg-inverse'
const bodyClass =
  'text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const h2Class = 'text-[17px] font-semibold text-aib-fg dark:text-aib-fg-inverse'

const privacyBody = computed(() => aibHtml(copy.value.home.privacyBodyHtml))
const contactsBody = computed(() => aibHtml(copy.value.home.contactsBodyHtml))
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
    </header>

    <section :class="['space-y-4', bodyClass]">
      <p>{{ copy.home.intro1 }}</p>
      <p v-html="copy.home.intro2Html" />
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
          class="border-aib-border dark:border-aib-border-dark border-b px-4 py-3.5 last:border-b-0"
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
        <span v-html="copy.home.guideBodyHtml" />
        <NuxtLink to="/aib-camera/guide" :class="linkClass">{{
          copy.home.guideLink
        }}</NuxtLink>
        {{ copy.home.guideOr }}
        <a :href="copy.guide.pdfHref" :class="linkClass">{{
          copy.home.guidePdf
        }}</a
        >.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.home.privacyTitle }}</h2>
      <p
        :class="[
          bodyClass,
          '[&_a]:underline [&_a]:underline-offset-2 [&_a]:hover:opacity-70',
        ]"
        v-html="privacyBody"
      />
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">{{ copy.home.contactsTitle }}</h2>
      <p
        :class="[
          bodyClass,
          '[&_a]:underline [&_a]:underline-offset-2 [&_a]:hover:opacity-70',
        ]"
        v-html="contactsBody"
      />
    </section>
  </article>
</template>
