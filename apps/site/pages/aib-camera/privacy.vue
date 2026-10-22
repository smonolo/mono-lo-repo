<script setup lang="ts">
definePageMeta({
  layout: 'aib-camera',
})

const title = 'Informativa privacy — AIB Camera'
const description =
  'Informativa privacy di AIB Camera: fotografia di campo sul dispositivo. Foto, impostazioni, registro scatti e posizione restano sul telefono.'
const canonical = 'https://smnl.dev/aib-camera/privacy'

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

const dataRows = [
  {
    data: 'Immagine dalla fotocamera',
    where: 'Sul dispositivo (archivio app; opzionalmente Libreria Foto)',
    why: 'Scattare e condividere foto di campo timbrate',
  },
  {
    data: 'Data e ora',
    where: 'Stampate sul JPEG (Europe/Rome); anche EXIF DateTime',
    why: 'Contesto operativo sulla foto',
  },
  {
    data: 'Coordinate GPS, precisione, altitudine, direzione',
    where: 'Sul dispositivo; sul timbro / EXIF se disponibili',
    why: 'Contesto di posizione sulla foto',
  },
  {
    data: 'Etichetta di luogo (geocodifica inversa)',
    where:
      'Sul dispositivo; lookup di rete opzionale; cache locale; può comparire in EXIF',
    why: 'Luogo leggibile sul timbro',
  },
  {
    data: 'ID scatto (UUID)',
    where:
      'Sul dispositivo (registro scatti); EXIF ImageUniqueID / UserComment JSON',
    why: 'Identificare le foto scattate con questa app',
  },
  {
    data: 'Testo personalizzato del timbro',
    where:
      'Sul dispositivo (impostazioni); può comparire in EXIF description / comment',
    why: 'Note di squadra / turno sulla foto',
  },
  {
    data: 'Impostazioni dell’app',
    where: 'Sul dispositivo (archivio locale)',
    why: 'Ricordare le preferenze del timbro',
  },
]

const linkClass =
  'text-aib-fg underline underline-offset-2 hover:opacity-70 dark:text-aib-fg-inverse'
const bodyClass =
  'text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const h2Class = 'text-[17px] font-semibold text-aib-fg dark:text-aib-fg-inverse'
const h3Class = 'text-[15px] font-semibold text-aib-fg dark:text-aib-fg-inverse'
const listClass =
  'list-disc space-y-2 pl-5 text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const crumbClass =
  'text-[13px] font-medium text-aib-muted dark:text-aib-muted-dark [&_a]:text-inherit [&_a]:no-underline [&_a:hover]:text-aib-fg [&_a:hover]:underline [&_a:hover]:underline-offset-2 dark:[&_a:hover]:text-aib-fg-inverse'
const codeClass =
  'rounded-md bg-aib-surface-muted px-1 py-0.5 font-mono text-[0.86em] text-aib-fg dark:bg-aib-surface-muted-dark dark:text-aib-fg-inverse'
</script>

<template>
  <article class="space-y-10">
    <header class="space-y-3">
      <p :class="crumbClass">
        <NuxtLink to="/">smnl.dev</NuxtLink>
        <span aria-hidden="true"> / </span>
        <NuxtLink to="/aib-camera">AIB Camera</NuxtLink>
        <span aria-hidden="true"> / </span>
        <span>Informativa</span>
      </p>
      <h1
        class="text-aib-fg dark:text-aib-fg-inverse text-[28px] font-bold leading-tight tracking-[-0.4px]"
      >
        Informativa privacy — AIB Camera
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark text-[13px] font-medium"
      >
        Ultimo aggiornamento: 22 ottobre 2026
      </p>
    </header>

    <div :class="['space-y-4', bodyClass]">
      <p>
        AIB Camera è un’app mobile indipendente e non ufficiale per la
        fotografia di campo nell’antincendio boschivo (AIB).
        <strong>Non</strong> è affiliata, approvata né realizzata da Protezione
        Civile, Vigili del Fuoco, Regione Lombardia o altri enti pubblici.
      </p>
      <p>
        Questa informativa descrive come l’app tratta i dati sul dispositivo.
      </p>
    </div>

    <section class="space-y-3">
      <h2 :class="h2Class">In sintesi</h2>
      <ul :class="listClass">
        <li>
          L’app è <strong>offline-first</strong>. Foto, impostazioni, galleria
          in-app e registro scatti restano sul dispositivo.
        </li>
        <li>
          Non c’è <strong>account</strong>, <strong>SDK di analytics</strong> né
          <strong>sincronizzazione cloud</strong> gestita dallo sviluppatore.
        </li>
        <li>La <strong>fotocamera</strong> è necessaria per scattare.</li>
        <li>
          La <strong>posizione</strong> è opzionale e serve solo a stampare
          coordinate, altitudine, direzione e luogo sulla foto, e a scrivere i
          campi corrispondenti nell’EXIF JPEG quando possibile.
        </li>
        <li>
          L’accesso alla <strong>libreria Foto</strong> è opzionale e serve a
          salvare le foto timbrate nell’album «AIB Camera» e a rimuovere quelle
          copie quando elimini le foto dall’app.
        </li>
        <li>
          La rete serve solo per la
          <strong>geocodifica inversa opzionale</strong> (da GPS a etichetta di
          luogo) quando c’è connessione. Le etichette vicine possono restare in
          cache sul dispositivo per riuso offline.
        </li>
      </ul>
    </section>

    <section class="space-y-4">
      <h2 :class="h2Class">Dati trattati dall’app</h2>
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
                Dato
              </th>
              <th
                class="border-aib-border text-aib-fg dark:border-aib-border-dark dark:text-aib-fg-inverse border-b px-3.5 py-3 font-semibold"
              >
                Dove resta
              </th>
              <th
                class="border-aib-border text-aib-fg dark:border-aib-border-dark dark:text-aib-fg-inverse border-b px-3.5 py-3 font-semibold"
              >
                Perché
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in dataRows" :key="row.data">
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

      <h3 :class="h3Class">Registro scatti</h3>
      <p :class="bodyClass">
        Ogni scatto riuscito viene registrato in un
        <strong>registro scatti</strong> locale (ID, data/ora e coordinate se
        disponibili). Il registro resta sul dispositivo anche se elimini la foto
        dalla galleria in-app o dall’album di sistema. Non viene caricato
        online. Puoi aprirlo da Impostazioni e copiare un ID per confrontarlo
        con l’EXIF di un JPEG condiviso.
      </p>

      <h3 :class="h3Class">Metadati EXIF</h3>
      <p :class="bodyClass">
        Dopo il timbro visivo l’app prova a scrivere tag EXIF standard sul JPEG
        salvato (ad esempio <code :class="codeClass">Software</code>,
        <code :class="codeClass">Artist</code>,
        <code :class="codeClass">ImageUniqueID</code>, campi GPS e un
        <code :class="codeClass">UserComment</code> JSON). Se la build del
        dispositivo non può scrivere EXIF, la foto resta salvata con il timbro
        visivo e la voce nel registro scatti.
      </p>

      <p :class="bodyClass">
        Lo sviluppatore <strong>non</strong> gestisce un backend che riceve
        foto, posizione, impostazioni o ID di scatto.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Permessi</h2>
      <ul :class="listClass">
        <li><strong>Fotocamera</strong> — necessaria per scattare.</li>
        <li>
          <strong>Posizione (in uso)</strong> — opzionale. Se negata puoi
          comunque scattare; le righe geo sul timbro risultano non disponibili e
          i campi GPS EXIF sono omessi.
        </li>
        <li>
          <strong>Libreria Foto / media</strong> — opzionale: salvare le foto
          timbrate nell’album «AIB Camera» e rimuovere quelle copie quando le
          elimini dall’app. La galleria in-app resta la fonte di verità anche se
          questo permesso è negato.
        </li>
      </ul>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">Condivisione</h2>
      <p :class="bodyClass">
        Se usi il foglio di condivisione di sistema (WhatsApp, Mail, File,
        ecc.), la foto condivisa (compreso l’eventuale EXIF scritto nel file)
        lascia l’app secondo le regole dell’app o del servizio che scegli. Ciò è
        fuori dal controllo di AIB Camera.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">Privacy dei minori</h2>
      <p :class="bodyClass">
        L’app non è rivolta ai minori e non raccoglie consapevolmente dati
        personali di minori.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">Modifiche</h2>
      <p :class="bodyClass">
        Se questa informativa cambia in modo sostanziale, la data di «Ultimo
        aggiornamento» sopra verrà aggiornata. Continuare a usare l’app dopo un
        aggiornamento implica l’accettazione dell’informativa rivista.
      </p>
    </section>

    <section class="space-y-2">
      <h2 :class="h2Class">Contatti</h2>
      <p :class="bodyClass">
        Sito dello sviluppatore:
        <a href="https://smnl.dev/?ref=aib-camera-privacy" :class="linkClass"
          >smnl.dev</a
        >
        · URL informativa:
        <a href="https://smnl.dev/aib-camera/privacy" :class="linkClass"
          >smnl.dev/aib-camera/privacy</a
        >
      </p>
      <p :class="bodyClass">
        Per domande sulla privacy di AIB Camera:
        <a href="mailto:stefano@smnl.dev" :class="linkClass">stefano@smnl.dev</a
        >.
      </p>
    </section>
  </article>
</template>
