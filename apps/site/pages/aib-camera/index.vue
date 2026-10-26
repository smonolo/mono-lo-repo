<script setup lang="ts">
definePageMeta({
  layout: 'aib-camera',
})

const title = 'AIB Camera'
const description =
  'Fotocamera di campo indipendente per chiunque: foto geolocalizzate di incendi boschivi (AIB) con timbro data/ora/GPS/luogo, ID EXIF e registro scatti sul dispositivo. Aperta al pubblico, offline, senza account.'
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
    title: 'Mirino 4:3 e timbro live',
    body: 'Mirino con bande nere: flash, punta e configurazione timbro in alto, zoom e scatto in basso. Sul mirino compare lo stesso timbro che verrà bruciato sulla foto.',
  },
  {
    title: 'Punto mirato',
    body: 'Punta la fotocamera, indica la distanza (es. 200 m) e blocca le coordinate stimate da GPS + bussola. Alterna Qui ↔ Mirato senza perdere il punto; sul timbro compare l’etichetta «Punto mirato (stimato)».',
  },
  {
    title: 'Timbro sul JPEG',
    body: 'Data, ora (Europe/Rome), coordinate, altitudine, direzione, luogo e testo personalizzato opzionale stampati sulla foto.',
  },
  {
    title: 'Scatto rapido',
    body: 'Puoi scattare in sequenza senza aspettare l’elaborazione del timbro. Per la posizione si usa il fix live (o il punto mirato bloccato) quando disponibile, così lo scatto resta snello.',
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
    body: 'Ogni scatto ha un ID locale, data e coordinate. Da Impostazioni vedi versione e ID installazione (tocca per copiare) ed esporti il registro in CSV con install_id — anche dopo aver eliminato la foto dalla galleria.',
  },
  {
    title: 'Funziona offline',
    body: 'Scatto, timbro, galleria, registro scatti e condivisione senza rete. GPS e etichette di luogo sono opzionali. Su dispositivi limitati (niente EXIF, album Foto, font o vibrazione) l’app continua comunque a scattare e salvare.',
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
        Fotografia di campo indipendente per chiunque — anteprima e timbro di
        data, ora, GPS e luogo su ogni scatto, con ID EXIF per i controlli
        successivi. Non riservata a enti o professionisti.
      </p>
    </header>

    <section :class="['space-y-4', bodyClass]">
      <p>
        AIB Camera è una piccola app mobile per scattare foto geolocalizzate
        timbrate in contesto di antincendio boschivo (AIB) o situazioni simili:
        quando serve documentare un incendio, condividere lo scatto (WhatsApp,
        Telegram, Mail, …) oppure leggere coordinate e altri dettagli a chi
        risponde all’emergenza (ad es. 112) — anche senza rete affidabile.
      </p>
      <p>
        È <strong>aperta a chiunque</strong>: cittadini, volontari o chiunque
        voglia usarla. <strong>Non</strong> è limitata a una specifica
        organizzazione né solo a operatori di Protezione Civile, Vigili del
        Fuoco o altri corpi. <strong>Non</strong> è affiliata, approvata né
        realizzata da Protezione Civile, Vigili del Fuoco, Regione Lombardia o
        altri enti pubblici. Creata da un volontario; disponibile al pubblico.
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
        Guida in italiano per l’uso sul campo: permessi, fotocamera, timbro,
        galleria, registro scatti, limiti e checklist. Leggibile sul web e
        stampabile in PDF.
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
      <h2 :class="h2Class">Privacy e termini</h2>
      <p :class="bodyClass">
        Foto, impostazioni, registro scatti e posizione restano sul dispositivo.
        Leggi l’
        <NuxtLink to="/aib-camera/privacy" :class="linkClass"
          >informativa privacy</NuxtLink
        >
        e i
        <NuxtLink to="/aib-camera/terms" :class="linkClass"
          >termini di utilizzo</NuxtLink
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
