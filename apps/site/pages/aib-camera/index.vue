<script setup lang="ts">
definePageMeta({
  layout: 'aib-camera',
})

const title = 'AIB Camera'
const description =
  'Fotocamera di campo indipendente per volontari AIB: anteprima live del timbro, JPEG con data/ora/GPS/luogo, ID EXIF e registro scatti sul dispositivo. Funziona offline, senza account.'
const canonical = 'https://smnl.dev/aib-camera'

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

const features = [
  {
    title: 'Anteprima live del timbro',
    body: 'Sul mirino compare lo stesso timbro che verrà bruciato sulla foto — campi, dimensione testo e opacità dalle impostazioni Aspetto.',
  },
  {
    title: 'Timbro sul JPEG',
    body: 'Data, ora (Europe/Rome), coordinate, altitudine, direzione, luogo e testo personalizzato opzionale stampati sulla foto.',
  },
  {
    title: 'Scatto rapido',
    body: 'Puoi scattare in sequenza senza aspettare l’elaborazione del timbro. Per la posizione si usa il fix live quando disponibile, così lo scatto resta snello.',
  },
  {
    title: 'EXIF per le macchine',
    body: 'Dopo il timbro l’app scrive Software, ImageUniqueID (UUID), tag GPS e un UserComment JSON quando la build nativa lo supporta.',
  },
  {
    title: 'Galleria in-app',
    body: 'Le foto restano nell’app come fonte di verità. Album Foto «AIB Camera» opzionale; eliminando dall’app si rimuove anche quella copia quando possibile.',
  },
  {
    title: 'Registro scatti',
    body: 'Ogni scatto ha un ID locale, data e coordinate, consultabili da Impostazioni — anche dopo aver eliminato la foto dalla galleria.',
  },
  {
    title: 'Funziona offline',
    body: 'Scatto, timbro, galleria, registro scatti e condivisione senza rete. GPS e etichette di luogo sono opzionali.',
  },
  {
    title: 'Tutto sul dispositivo',
    body: 'Nessun account, nessun SDK di analytics, nessuna sincronizzazione cloud. Foto, impostazioni e registro restano sul dispositivo.',
  },
  {
    title: 'Pronto per l’emergenza',
    body: 'La fotocamera è obbligatoria; la posizione no. Lo scatto resta utilizzabile anche senza fix GPS.',
  },
]

const linkClass =
  'text-aib-fg underline underline-offset-2 hover:opacity-70 dark:text-aib-fg-inverse'
const bodyClass =
  'text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const h2Class = 'text-[17px] font-semibold text-aib-fg dark:text-aib-fg-inverse'
</script>

<template>
  <article class="space-y-10">
    <header class="space-y-3">
      <p
        class="text-aib-muted dark:text-aib-muted-dark [&_a:hover]:text-aib-fg dark:[&_a:hover]:text-aib-fg-inverse text-[13px] font-medium [&_a:hover]:underline [&_a:hover]:underline-offset-2 [&_a]:text-inherit [&_a]:no-underline"
      >
        <NuxtLink to="/">smnl.dev</NuxtLink>
        <span aria-hidden="true"> / </span>
        <span>AIB Camera</span>
      </p>
      <h1
        class="text-aib-fg dark:text-aib-fg-inverse text-[28px] font-bold leading-tight tracking-[-0.4px]"
      >
        AIB Camera
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark text-[17px] leading-normal"
      >
        Fotografia di campo indipendente per volontari di antincendio boschivo —
        anteprima e timbro di data, ora, GPS e luogo su ogni scatto, con ID EXIF
        per i controlli successivi.
      </p>
    </header>

    <section :class="['space-y-4', bodyClass]">
      <p>
        AIB Camera è una piccola app mobile per scattare foto di campo timbrate
        quando serve un resoconto chiaro — su una linea di fuoco, in un
        intervento alluvionale o in qualsiasi turno di volontariato in cui rete
        e permessi sono inaffidabili.
      </p>
      <p>
        <strong>Non</strong> è affiliata, approvata né realizzata da Protezione
        Civile, Vigili del Fuoco, Regione Lombardia o altri enti pubblici.
        Creata da un volontario per uso volontario.
      </p>
    </section>

    <section>
      <p
        class="text-aib-muted dark:text-aib-muted-dark mb-2 text-[13px] font-medium"
      >
        Cosa fa
      </p>
      <ul
        class="rounded-aib bg-aib-surface dark:bg-aib-surface-dark overflow-hidden"
      >
        <li
          v-for="feature in features"
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
      <h2 :class="h2Class">Guida operativa</h2>
      <p :class="bodyClass">
        Guida in italiano per l’uso sul campo e per chi valuta l’adozione:
        permessi, fotocamera, timbro, galleria, registro scatti, limiti e
        checklist pre-turno. Leggibile sul web e stampabile in PDF.
        <NuxtLink to="/aib-camera/guida" :class="linkClass"
          >Apri la guida</NuxtLink
        >
        oppure scarica
        <a href="/docs/aib-camera-guida-operativa.pdf" :class="linkClass"
          >guida-operativa.pdf</a
        >.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">Privacy</h2>
      <p :class="bodyClass">
        Foto, impostazioni, registro scatti e posizione restano sul dispositivo.
        Leggi l’intera
        <NuxtLink to="/aib-camera/privacy" :class="linkClass"
          >informativa privacy</NuxtLink
        >.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">Contatti</h2>
      <p :class="bodyClass">
        Domande sull’app:
        <a href="mailto:stefano@smnl.dev" :class="linkClass">stefano@smnl.dev</a
        >.
      </p>
    </section>
  </article>
</template>
