<script setup lang="ts">
definePageMeta({
  layout: 'aib-camera',
})

const title = 'Termini di utilizzo — AIB Camera'
const description =
  'Termini di utilizzo di AIB Camera: app indipendente non ufficiale, aperta a chiunque, per fotografia di campo AIB. Uso a proprio rischio, senza garanzia e senza affiliazione istituzionale.'
const canonical = 'https://smnl.dev/aib-camera/terms'

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

const linkClass =
  'text-aib-fg underline underline-offset-2 hover:opacity-70 dark:text-aib-fg-inverse'
const bodyClass =
  'text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const h2Class = 'text-[17px] font-semibold text-aib-fg dark:text-aib-fg-inverse'
const listClass =
  'list-disc space-y-2 pl-5 text-[15px] leading-relaxed text-aib-muted dark:text-aib-muted-dark [&_strong]:font-semibold [&_strong]:text-aib-fg dark:[&_strong]:text-aib-fg-inverse'
const crumbClass =
  'text-[13px] font-medium text-aib-muted dark:text-aib-muted-dark [&_a]:text-inherit [&_a]:no-underline [&_a:hover]:text-aib-fg [&_a:hover]:underline [&_a:hover]:underline-offset-2 dark:[&_a:hover]:text-aib-fg-inverse'
</script>

<template>
  <article class="space-y-10">
    <header class="space-y-3">
      <p :class="crumbClass">
        <NuxtLink to="/">smnl.dev</NuxtLink>
        <span aria-hidden="true"> / </span>
        <NuxtLink to="/aib-camera">AIB Camera</NuxtLink>
        <span aria-hidden="true"> / </span>
        <span>Termini</span>
      </p>
      <h1
        class="text-aib-fg dark:text-aib-fg-inverse text-[28px] font-bold leading-tight tracking-[-0.4px]"
      >
        Termini di utilizzo — AIB Camera
      </h1>
      <p
        class="text-aib-muted dark:text-aib-muted-dark text-[13px] font-medium"
      >
        Ultimo aggiornamento: 26 ottobre 2026
      </p>
    </header>

    <div :class="['space-y-4', bodyClass]">
      <p>
        Questi termini regolano l’uso dell’app mobile
        <strong>AIB Camera</strong>. Scaricando, installando o usando l’app
        accetti questi termini. Se non li accetti, non usare l’app.
      </p>
      <p>
        AIB Camera è un progetto
        <strong>indipendente e non ufficiale</strong> realizzato da un
        volontario. <strong>Non</strong> è affiliata, approvata né realizzata da
        Protezione Civile, Vigili del Fuoco, Regione Lombardia o qualsiasi altra
        istituzione pubblica. Non usare loghi, sigilli o nomi istituzionali
        nell’app in modo che possano suggerire un’approvazione ufficiale.
      </p>
    </div>

    <section class="space-y-3">
      <h2 :class="h2Class">Chi può usarla</h2>
      <p :class="bodyClass">
        <strong>Chiunque.</strong> L’app <strong>non</strong> è riservata a una
        specifica organizzazione, squadra o ruolo professionale (Protezione
        Civile, Vigili del Fuoco o altri). È disponibile al pubblico. Usi
        tipici: documentare un incendio boschivo o una situazione simile,
        condividere la foto timbrata (WhatsApp, Telegram, Mail, …), oppure
        leggere coordinate e altri dettagli a chi risponde all’emergenza (ad es.
        112). Anche volontari e organizzazioni possono usarla se lo desiderano:
        ciò non la rende esclusiva per loro.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">A cosa serve l’app</h2>
      <p :class="bodyClass">
        AIB Camera aiuta a scattare foto di campo con data/ora, posizione (se
        disponibile) e testo personalizzato stampati sul JPEG, più un registro
        scatti locale. È uno
        <strong>strumento di supporto</strong> per la documentazione personale e
        la condivisione: <strong>non</strong> sostituisce procedure ufficiali,
        moduli, sistemi di comando o archivi istituzionali.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Uso consentito</h2>
      <ul :class="listClass">
        <li>
          Usare l’app per fotografia di campo legata a incendi boschivi (AIB) o
          situazioni simili, nel rispetto della legge applicabile.
        </li>
        <li>
          Non presentare l’app, le foto o i metadati come documentazione
          ufficiale o certificata da un’istituzione pubblica.
        </li>
        <li>
          Non usare l’app per attività illegali, molestie, o per alterare in
          modo ingannevole coordinate, orari o contesto sulla foto.
        </li>
        <li>
          Sei responsabile di ciò che scatti, condividi o esporti (foto, EXIF,
          CSV del registro), comprese destinazione e destinatari.
        </li>
      </ul>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Posizione e punto mirato</h2>
      <p :class="bodyClass">
        GPS, bussola, altitudine e luogo dipendono dal dispositivo, dai permessi
        e dalle condizioni di segnale. La modalità
        <strong>punto mirato</strong> (distanza + direzione) produce una
        <strong>stima</strong> a terra lungo la bussola: non è un fix GNSS sul
        bersaglio e non è certificata. Non basare decisioni di sicurezza o
        resoconti ufficiali solo su questi valori.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Disponibilità e «così com’è»</h2>
      <p :class="bodyClass">
        L’app è fornita <strong>«così com’è»</strong> e
        <strong>«come disponibile»</strong>, senza garanzie di alcun tipo,
        espresse o implicite, inclusa l’idoneità a uno scopo particolare. In
        particolare non si garantiscono: continuità del servizio, precisione
        GPS/bussola/punto mirato, completezza EXIF, salvataggio nell’album Foto,
        o comportamento identico su tutti i dispositivi.
      </p>
      <p :class="bodyClass">
        In emergenza l’app punta a restare utilizzabile anche senza rete o GPS,
        ma <strong>non</strong> sostituisce comunicazioni radio, procedure di
        sicurezza o giudizi operativi sul campo.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Limitazione di responsabilità</h2>
      <p :class="bodyClass">
        Nei limiti consentiti dalla legge applicabile, lo sviluppatore non è
        responsabile per danni diretti o indiretti, perdita di dati, decisioni
        operative errate, o conseguenze derivanti dall’uso o dall’impossibilità
        di usare AIB Camera — comprese stime di posizione inaccurate o foto
        condivise verso terzi.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Privacy e dati</h2>
      <p :class="bodyClass">
        Il trattamento dei dati sull’app è descritto nell’
        <NuxtLink to="/aib-camera/privacy" :class="linkClass"
          >informativa privacy</NuxtLink
        >. In sintesi: elaborazione prevalentemente sul dispositivo, senza
        account né sincronizzazione cloud gestita dallo sviluppatore.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Proprietà intellettuale</h2>
      <p :class="bodyClass">
        L’app, il nome «AIB Camera» come usato qui e i materiali del sito
        correlati appartengono allo sviluppatore, fatti salvi componenti di
        terzi (librerie open source, font, ecc.) con le rispettive licenze. Non
        è concessa alcuna licenza a marchi o segni di istituzioni pubbliche.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Aggiornamenti</h2>
      <p :class="bodyClass">
        L’app e questi termini possono cambiare. La data di «Ultimo
        aggiornamento» verrà rivista in caso di modifiche sostanziali.
        Continuare a usare l’app dopo un aggiornamento implica l’accettazione
        dei termini rivisti.
      </p>
    </section>

    <section class="space-y-3">
      <h2 :class="h2Class">Contatti</h2>
      <p :class="bodyClass">
        Sito dello sviluppatore:
        <a href="https://smnl.dev/?ref=aib-camera-terms" :class="linkClass"
          >smnl.dev</a
        >
        · URL termini:
        <a href="https://smnl.dev/aib-camera/terms" :class="linkClass"
          >smnl.dev/aib-camera/terms</a
        >
        · Privacy:
        <NuxtLink to="/aib-camera/privacy" :class="linkClass"
          >smnl.dev/aib-camera/privacy</NuxtLink
        >
      </p>
      <p :class="bodyClass">
        Domande sui termini o sull’app:
        <a href="mailto:stefano@smnl.dev" :class="linkClass">stefano@smnl.dev</a
        >.
      </p>
    </section>
  </article>
</template>
