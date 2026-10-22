<script setup lang="ts">
definePageMeta({
  layout: 'aib-camera',
})

const title = 'Guida operativa — AIB Camera'
const description =
  'Guida operativa in italiano di AIB Camera: permessi, fotocamera, timbro, galleria, registro scatti, limiti e checklist sul campo. Leggibile nel browser e scaricabile in PDF.'
const canonical = 'https://smnl.dev/aib-camera/guida'
const pdfHref = '/docs/aib-camera-guida-operativa.pdf'
const bodyHref = '/docs/aib-camera-guida-operativa.body.html'

useSeoMeta({
  title,
  description,
  ogTitle: title,
  ogDescription: description,
  ogUrl: canonical,
  ogType: 'website',
  twitterTitle: title,
  twitterDescription: description,
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: canonical }],
})

const { data: guideHtml, error: guideError } = await useAsyncData(
  'aib-camera-guida-body',
  () => $fetch<string>(bodyHref, { responseType: 'text' })
)

const linkClass =
  'text-aib-fg underline underline-offset-2 hover:opacity-70 dark:text-aib-fg-inverse'
const crumbClass =
  'text-[13px] font-medium text-aib-muted dark:text-aib-muted-dark [&_a]:text-inherit [&_a]:no-underline [&_a:hover]:text-aib-fg [&_a:hover]:underline [&_a:hover]:underline-offset-2 dark:[&_a:hover]:text-aib-fg-inverse'
const btnClass =
  'inline-flex min-h-11 items-center justify-center rounded-aib bg-aib-surface-muted px-4 text-[14px] font-semibold text-aib-fg no-underline hover:opacity-85 dark:bg-aib-surface-muted-dark dark:text-aib-fg-inverse'
const btnGhostClass =
  'inline-flex min-h-11 items-center justify-center rounded-aib border border-aib-border-strong bg-transparent px-4 text-[14px] font-semibold text-aib-fg no-underline hover:opacity-85 dark:border-aib-border-strong-dark dark:text-aib-fg-inverse'
</script>

<template>
  <div class="space-y-8">
    <header class="space-y-4">
      <p :class="crumbClass">
        <NuxtLink to="/">smnl.dev</NuxtLink>
        <span aria-hidden="true"> / </span>
        <NuxtLink to="/aib-camera">AIB Camera</NuxtLink>
        <span aria-hidden="true"> / </span>
        <span>Guida</span>
      </p>
      <h1
        class="text-aib-fg dark:text-aib-fg-inverse text-[28px] font-bold leading-tight tracking-[-0.4px]"
      >
        Guida operativa
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse text-[17px] leading-normal [&_strong]:font-semibold"
      >
        Come funziona AIB Camera sul campo: permessi, scatto, timbro, galleria e
        registro scatti. App <strong>non ufficiale</strong>, indipendente da
        enti pubblici. Versione di riferimento <strong>1.0.0</strong>.
      </p>
      <div class="flex flex-wrap gap-3 pt-1">
        <a :href="pdfHref" :class="btnClass">Scarica PDF</a>
        <a
          :href="pdfHref"
          target="_blank"
          rel="noopener"
          :class="btnGhostClass"
        >
          Apri PDF
        </a>
      </div>
    </header>

    <div
      v-if="guideError"
      class="rounded-aib bg-aib-surface text-aib-muted dark:bg-aib-surface-dark dark:text-aib-muted-dark px-4 py-3 text-[13px]"
    >
      Contenuto non disponibile. Apri il
      <a :href="pdfHref" :class="linkClass">PDF della guida</a>
      oppure rigenera i file con
      <code
        class="bg-aib-surface-muted text-aib-fg dark:bg-aib-surface-muted-dark dark:text-aib-fg-inverse rounded-md px-1 py-0.5 font-mono text-[0.86em]"
        >npm run docs:aib-camera-guida</code
      >.
    </div>

    <article
      v-else
      class="aib-prose rounded-aib bg-aib-surface dark:bg-aib-surface-dark overflow-hidden px-5 py-7 sm:px-7 sm:py-8"
      v-html="guideHtml"
    />
  </div>
</template>
