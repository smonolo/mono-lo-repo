import type { AibLocale } from '~/composables/useAibLocale'

type Feature = { title: string; body: string }
type DataRow = { data: string; where: string; why: string }

export type AibCopy = {
  nav: {
    homeAria: string
    guide: string
    privacy: string
    terms: string
    langIt: string
    langEn: string
    langSwitchAria: string
  }
  footer: {
    brand: string
  }
  home: {
    title: string
    description: string
    lead: string
    intro1: string
    intro2Html: string
    featuresLabel: string
    features: Feature[]
    guideTitle: string
    guideBodyHtml: string
    guideLink: string
    guideOr: string
    guidePdf: string
    privacyTitle: string
    privacyBodyHtml: string
    contactsTitle: string
    contactsBodyHtml: string
  }
  guide: {
    title: string
    description: string
    heading: string
    leadHtml: string
    downloadPdf: string
    openPdf: string
    pdfHref: string
    pdfFilename: string
  }
  privacy: {
    title: string
    description: string
    heading: string
    updated: string
    intro1Html: string
    intro2: string
    summaryTitle: string
    summary: string[]
    dataTitle: string
    colData: string
    colWhere: string
    colWhy: string
    dataRows: DataRow[]
    logTitle: string
    logBodyHtml: string
    exifTitle: string
    exifBodyHtml: string
    noBackendHtml: string
    permissionsTitle: string
    permissions: string[]
    shareTitle: string
    shareBody: string
    minorsTitle: string
    minorsBody: string
    changesTitle: string
    changesBody: string
    contactsTitle: string
    contactsBodyHtml: string
    contactsMailHtml: string
  }
  terms: {
    title: string
    description: string
    heading: string
    updated: string
    intro1Html: string
    intro2Html: string
    whoTitle: string
    whoBodyHtml: string
    purposeTitle: string
    purposeBodyHtml: string
    allowedTitle: string
    allowed: string[]
    aimTitle: string
    aimBodyHtml: string
    asIsTitle: string
    asIsBody1Html: string
    asIsBody2Html: string
    liabilityTitle: string
    liabilityBody: string
    privacyTitle: string
    privacyBodyHtml: string
    ipTitle: string
    ipBody: string
    updatesTitle: string
    updatesBody: string
    contactsTitle: string
    contactsBodyHtml: string
    contactsMailHtml: string
  }
}

const it: AibCopy = {
  nav: {
    homeAria: 'AIB Camera — home',
    guide: 'Guida',
    privacy: 'Privacy',
    terms: 'Termini',
    langIt: 'IT',
    langEn: 'EN',
    langSwitchAria: 'Seleziona lingua',
  },
  footer: {
    brand: 'AIB Camera',
  },
  home: {
    title: 'AIB Camera',
    description:
      'Fotocamera di campo indipendente per chiunque: foto geolocalizzate di incendi boschivi (AIB) con timbro data/ora/GPS/luogo, ID EXIF e registro scatti sul dispositivo. Aperta al pubblico, offline, senza account.',
    lead: 'Fotografia di campo indipendente per chiunque — anteprima e timbro di data, ora, GPS e luogo su ogni scatto, con ID EXIF per i controlli successivi. Non riservata a enti o professionisti.',
    intro1:
      'AIB Camera è una piccola app mobile per scattare foto geolocalizzate timbrate in contesto di antincendio boschivo (AIB) o situazioni simili: quando serve documentare un incendio, condividere lo scatto (WhatsApp, Telegram, Mail, …) oppure leggere coordinate e altri dettagli a chi risponde all’emergenza (ad es. 112) — anche senza rete affidabile.',
    intro2Html:
      'È <strong>aperta a chiunque</strong>: cittadini, volontari o chiunque voglia usarla. <strong>Non</strong> è limitata a una specifica organizzazione né solo a operatori di Protezione Civile, Vigili del Fuoco o altri corpi. <strong>Non</strong> è affiliata, approvata né realizzata da Protezione Civile, Vigili del Fuoco, Regione Lombardia o altri enti pubblici. Creata da un volontario; disponibile al pubblico.',
    featuresLabel: 'Cosa fa',
    features: [
      {
        title: 'Mirino 4:3 e timbro live',
        body: 'Mirino con bande nere: flash, punta e configurazione timbro in alto, zoom e scatto in basso. Sul mirino compare lo stesso timbro che verrà bruciato sulla foto.',
      },
      {
        title: 'Punto mirato',
        body: 'Tieni premuto il mirino (o tocca Punta): si apre un foglio a tutta larghezza. Indica la distanza (es. 200 m) e blocca le coordinate stimate da GPS + bussola. Alterna Qui ↔ Mirato senza perdere il punto; sul timbro compare l’etichetta «Punto mirato (stimato)».',
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
    ],
    guideTitle: 'Guida operativa',
    guideBodyHtml:
      'Guida per l’uso sul campo: permessi, fotocamera, timbro, galleria, registro scatti, limiti e checklist. Leggibile sul web e stampabile in PDF.',
    guideLink: 'Apri la guida',
    guideOr: 'oppure scarica',
    guidePdf: 'guida-operativa.pdf',
    privacyTitle: 'Privacy e termini',
    privacyBodyHtml:
      'Foto, impostazioni, registro scatti e posizione restano sul dispositivo. Leggi l’ <a data-aib="privacy">informativa privacy</a> e i <a data-aib="terms">termini di utilizzo</a>.',
    contactsTitle: 'Contatti',
    contactsBodyHtml:
      'Domande sull’app: <a href="mailto:stefano@smnl.dev">stefano@smnl.dev</a>.',
  },
  guide: {
    title: 'Guida operativa — AIB Camera',
    description:
      'Guida operativa in italiano di AIB Camera: permessi, fotocamera, timbro, galleria, registro scatti, limiti e checklist sul campo. Leggibile nel browser e scaricabile in PDF.',
    heading: 'Guida operativa',
    leadHtml:
      'Come funziona AIB Camera sul campo: permessi, scatto, timbro, galleria e registro scatti. App <strong>non ufficiale</strong>, indipendente da enti pubblici. Versione di riferimento <strong>1.0.0</strong>.',
    downloadPdf: 'Scarica PDF',
    openPdf: 'Apri PDF',
    pdfHref: '/docs/aib-camera-guida-operativa.pdf',
    pdfFilename: 'aib-camera-guida-operativa.pdf',
  },
  privacy: {
    title: 'Informativa privacy — AIB Camera',
    description:
      'Informativa privacy di AIB Camera: app aperta a chiunque. Fotografia di campo sul dispositivo; foto, impostazioni, registro scatti e posizione restano sul telefono.',
    heading: 'Informativa privacy — AIB Camera',
    updated: 'Ultimo aggiornamento: 26 ottobre 2026',
    intro1Html:
      'AIB Camera è un’app mobile indipendente e non ufficiale per la fotografia di campo nell’antincendio boschivo (AIB) e situazioni simili. È <strong>aperta a chiunque</strong> — non riservata a una specifica organizzazione, squadra o insieme di professionisti. Chiunque può usarla per scattare foto geolocalizzate timbrate (ad esempio per condividerle con la Protezione Civile locale via WhatsApp o Telegram, o per leggere coordinate e altri dettagli a chi risponde all’emergenza, come il 112). <strong>Non</strong> è affiliata, approvata né realizzata da Protezione Civile, Vigili del Fuoco, Regione Lombardia o altri enti pubblici.',
    intro2:
      'Questa informativa descrive come l’app tratta i dati sul dispositivo.',
    summaryTitle: 'In sintesi',
    summary: [
      'L’app è <strong>offline-first</strong>. Foto, impostazioni, galleria in-app e registro scatti restano sul dispositivo.',
      'Non c’è <strong>account</strong>, <strong>SDK di analytics</strong> né <strong>sincronizzazione cloud</strong> gestita dallo sviluppatore.',
      'La <strong>fotocamera</strong> è necessaria per scattare.',
      'La <strong>posizione</strong> è opzionale e serve a stampare coordinate, altitudine, direzione e luogo sulla foto, e a scrivere i campi corrispondenti nell’EXIF JPEG quando possibile. Puoi anche bloccare un <strong>punto mirato</strong> (GPS + bussola + distanza inserita): le coordinate stimate possono sostituire la tua posizione sul timbro finché non le cancelli.',
      'L’accesso alla <strong>libreria Foto</strong> è opzionale e serve a salvare le foto timbrate nell’album «AIB Camera» e a rimuovere quelle copie quando elimini le foto dall’app.',
      'La rete serve solo per la <strong>geocodifica inversa opzionale</strong> (da GPS a etichetta di luogo) quando c’è connessione. Le etichette vicine possono restare in cache sul dispositivo per riuso offline.',
    ],
    dataTitle: 'Dati trattati dall’app',
    colData: 'Dato',
    colWhere: 'Dove resta',
    colWhy: 'Perché',
    dataRows: [
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
        data: 'Stima punto mirato (distanza + direzione)',
        where:
          'Sul dispositivo (punto sticky di sessione); può comparire su timbro / registro / EXIF GPS',
        why: 'Coordinate di ciò a cui stai puntando, non solo della tua posizione',
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
        data: 'ID installazione (UUID)',
        where:
          'Sul dispositivo (archivio locale); Impostazioni (tocca per copiare); esportazione CSV',
        why: 'Collegare i CSV del registro scatti a una specifica installazione',
      },
      {
        data: 'Testo personalizzato del timbro',
        where:
          'Sul dispositivo (impostazioni); può comparire in EXIF description / comment',
        why: 'Note opzionali sulla foto',
      },
      {
        data: 'Impostazioni dell’app',
        where: 'Sul dispositivo (archivio locale)',
        why: 'Ricordare le preferenze del timbro',
      },
    ],
    logTitle: 'Registro scatti',
    logBodyHtml:
      'Ogni scatto riuscito viene registrato in un <strong>registro scatti</strong> locale (ID, data/ora e coordinate se disponibili). Il registro resta sul dispositivo anche se elimini la foto dalla galleria in-app o dall’album di sistema. Non viene caricato online. Puoi aprirlo da Impostazioni, copiare un ID per confrontarlo con l’EXIF di un JPEG condiviso, oppure esportare tutto in CSV tramite il foglio di condivisione di sistema. Ogni installazione dell’app ha anche un <strong>ID installazione</strong> (UUID) locale, visibile in Impostazioni. Lì puoi toccare <strong>Versione</strong> o <strong>ID installazione</strong> per copiare il valore. L’ID installazione è scritto nella colonna <code data-aib="code">install_id</code> dei CSV, così i registri di più dispositivi restano distinguibili. Reinstallare l’app genera un nuovo ID. La stringa di versione è solo l’etichetta di build dell’app (non un identificativo di tracciamento).',
    exifTitle: 'Metadati EXIF',
    exifBodyHtml:
      'Dopo il timbro visivo l’app prova a scrivere tag EXIF standard sul JPEG salvato (ad esempio <code data-aib="code">Software</code>, <code data-aib="code">Artist</code>, <code data-aib="code">ImageUniqueID</code>, campi GPS e un <code data-aib="code">UserComment</code> JSON). Se la build del dispositivo non può scrivere EXIF, la foto resta salvata con il timbro visivo e la voce nel registro scatti. Se anche il compositing del timbro fallisce, resta comunque il JPEG non timbrato nella galleria in-app.',
    noBackendHtml:
      'Lo sviluppatore <strong>non</strong> gestisce un backend che riceve foto, posizione, impostazioni, ID di scatto o ID installazione.',
    permissionsTitle: 'Permessi',
    permissions: [
      '<strong>Fotocamera</strong> — necessaria per scattare.',
      '<strong>Posizione (in uso)</strong> — opzionale. Se negata puoi comunque scattare; le righe geo sul timbro risultano non disponibili e i campi GPS EXIF sono omessi.',
      '<strong>Libreria Foto / media</strong> — opzionale: salvare le foto timbrate nell’album «AIB Camera» e rimuovere quelle copie quando le elimini dall’app. La galleria in-app resta la fonte di verità anche se questo permesso è negato.',
    ],
    shareTitle: 'Condivisione',
    shareBody:
      'Se usi il foglio di condivisione di sistema (WhatsApp, Mail, File, ecc.), la foto condivisa (compreso l’eventuale EXIF scritto nel file) o un CSV del registro scatti lascia l’app secondo le regole dell’app o del servizio che scegli. Ciò è fuori dal controllo di AIB Camera.',
    minorsTitle: 'Privacy dei minori',
    minorsBody:
      'L’app non è rivolta ai minori e non raccoglie consapevolmente dati personali di minori.',
    changesTitle: 'Modifiche',
    changesBody:
      'Se questa informativa cambia in modo sostanziale, la data di «Ultimo aggiornamento» sopra verrà aggiornata. Continuare a usare l’app dopo un aggiornamento implica l’accettazione dell’informativa rivista.',
    contactsTitle: 'Contatti',
    contactsBodyHtml:
      'Sito dello sviluppatore: <a href="https://smnl.dev/?ref=aib-camera-privacy">smnl.dev</a> · URL informativa: <a href="https://smnl.dev/aib-camera/privacy">smnl.dev/aib-camera/privacy</a> · Termini: <a data-aib="terms">smnl.dev/aib-camera/terms</a>',
    contactsMailHtml:
      'Per domande sulla privacy di AIB Camera: <a href="mailto:stefano@smnl.dev">stefano@smnl.dev</a>.',
  },
  terms: {
    title: 'Termini di utilizzo — AIB Camera',
    description:
      'Termini di utilizzo di AIB Camera: app indipendente non ufficiale, aperta a chiunque, per fotografia di campo AIB. Uso a proprio rischio, senza garanzia e senza affiliazione istituzionale.',
    heading: 'Termini di utilizzo — AIB Camera',
    updated: 'Ultimo aggiornamento: 26 ottobre 2026',
    intro1Html:
      'Questi termini regolano l’uso dell’app mobile <strong>AIB Camera</strong>. Scaricando, installando o usando l’app accetti questi termini. Se non li accetti, non usare l’app.',
    intro2Html:
      'AIB Camera è un progetto <strong>indipendente e non ufficiale</strong> realizzato da un volontario. <strong>Non</strong> è affiliata, approvata né realizzata da Protezione Civile, Vigili del Fuoco, Regione Lombardia o qualsiasi altra istituzione pubblica. Non usare loghi, sigilli o nomi istituzionali nell’app in modo che possano suggerire un’approvazione ufficiale.',
    whoTitle: 'Chi può usarla',
    whoBodyHtml:
      '<strong>Chiunque.</strong> L’app <strong>non</strong> è riservata a una specifica organizzazione, squadra o ruolo professionale (Protezione Civile, Vigili del Fuoco o altri). È disponibile al pubblico. Usi tipici: documentare un incendio boschivo o una situazione simile, condividere la foto timbrata (WhatsApp, Telegram, Mail, …), oppure leggere coordinate e altri dettagli a chi risponde all’emergenza (ad es. 112). Anche volontari e organizzazioni possono usarla se lo desiderano: ciò non la rende esclusiva per loro.',
    purposeTitle: 'A cosa serve l’app',
    purposeBodyHtml:
      'AIB Camera aiuta a scattare foto di campo con data/ora, posizione (se disponibile) e testo personalizzato stampati sul JPEG, più un registro scatti locale. È uno <strong>strumento di supporto</strong> per la documentazione personale e la condivisione: <strong>non</strong> sostituisce procedure ufficiali, moduli, sistemi di comando o archivi istituzionali.',
    allowedTitle: 'Uso consentito',
    allowed: [
      'Usare l’app per fotografia di campo legata a incendi boschivi (AIB) o situazioni simili, nel rispetto della legge applicabile.',
      'Non presentare l’app, le foto o i metadati come documentazione ufficiale o certificata da un’istituzione pubblica.',
      'Non usare l’app per attività illegali, molestie, o per alterare in modo ingannevole coordinate, orari o contesto sulla foto.',
      'Sei responsabile di ciò che scatti, condividi o esporti (foto, EXIF, CSV del registro), comprese destinazione e destinatari.',
    ],
    aimTitle: 'Posizione e punto mirato',
    aimBodyHtml:
      'GPS, bussola, altitudine e luogo dipendono dal dispositivo, dai permessi e dalle condizioni di segnale. La modalità <strong>punto mirato</strong> (distanza + direzione) produce una <strong>stima</strong> a terra lungo la bussola: non è un fix GNSS sul bersaglio e non è certificata. Non basare decisioni di sicurezza o resoconti ufficiali solo su questi valori.',
    asIsTitle: 'Disponibilità e «così com’è»',
    asIsBody1Html:
      'L’app è fornita <strong>«così com’è»</strong> e <strong>«come disponibile»</strong>, senza garanzie di alcun tipo, espresse o implicite, inclusa l’idoneità a uno scopo particolare. In particolare non si garantiscono: continuità del servizio, precisione GPS/bussola/punto mirato, completezza EXIF, salvataggio nell’album Foto, o comportamento identico su tutti i dispositivi.',
    asIsBody2Html:
      'In emergenza l’app punta a restare utilizzabile anche senza rete o GPS, ma <strong>non</strong> sostituisce comunicazioni radio, procedure di sicurezza o giudizi operativi sul campo.',
    liabilityTitle: 'Limitazione di responsabilità',
    liabilityBody:
      'Nei limiti consentiti dalla legge applicabile, lo sviluppatore non è responsabile per danni diretti o indiretti, perdita di dati, decisioni operative errate, o conseguenze derivanti dall’uso o dall’impossibilità di usare AIB Camera — comprese stime di posizione inaccurate o foto condivise verso terzi.',
    privacyTitle: 'Privacy e dati',
    privacyBodyHtml:
      'Il trattamento dei dati sull’app è descritto nell’ <a data-aib="privacy">informativa privacy</a>. In sintesi: elaborazione prevalentemente sul dispositivo, senza account né sincronizzazione cloud gestita dallo sviluppatore.',
    ipTitle: 'Proprietà intellettuale',
    ipBody:
      'L’app, il nome «AIB Camera» come usato qui e i materiali del sito correlati appartengono allo sviluppatore, fatti salvi componenti di terzi (librerie open source, font, ecc.) con le rispettive licenze. Non è concessa alcuna licenza a marchi o segni di istituzioni pubbliche.',
    updatesTitle: 'Aggiornamenti',
    updatesBody:
      'L’app e questi termini possono cambiare. La data di «Ultimo aggiornamento» verrà rivista in caso di modifiche sostanziali. Continuare a usare l’app dopo un aggiornamento implica l’accettazione dei termini rivisti.',
    contactsTitle: 'Contatti',
    contactsBodyHtml:
      'Sito dello sviluppatore: <a href="https://smnl.dev/?ref=aib-camera-terms">smnl.dev</a> · URL termini: <a href="https://smnl.dev/aib-camera/terms">smnl.dev/aib-camera/terms</a> · Privacy: <a data-aib="privacy">smnl.dev/aib-camera/privacy</a>',
    contactsMailHtml:
      'Domande sui termini o sull’app: <a href="mailto:stefano@smnl.dev">stefano@smnl.dev</a>.',
  },
}

const en: AibCopy = {
  nav: {
    homeAria: 'AIB Camera — home',
    guide: 'Guide',
    privacy: 'Privacy',
    terms: 'Terms',
    langIt: 'IT',
    langEn: 'EN',
    langSwitchAria: 'Select language',
  },
  footer: {
    brand: 'AIB Camera',
  },
  home: {
    title: 'AIB Camera',
    description:
      'Independent field camera for anyone: geotagged wildfire (AIB) photos with date/time/GPS/place stamp, EXIF ID and on-device capture log. Public, offline, no account.',
    lead: 'Independent field photography for anyone — live preview and a date, time, GPS and place stamp on every shot, with an EXIF ID for later checks. Not limited to agencies or professionals.',
    intro1:
      'AIB Camera is a small mobile app for stamped, geotagged photos in wildfire (AIB) and similar situations: when you need to document a fire, share the shot (WhatsApp, Telegram, Mail, …), or read coordinates and other details to emergency responders (e.g. 112) — even without a reliable network.',
    intro2Html:
      'It is <strong>open to anyone</strong>: citizens, volunteers, or whoever wants to use it. It is <strong>not</strong> limited to a specific organization or only to Civil Protection, Fire Service, or other agency operators. It is <strong>not</strong> affiliated with, endorsed by, or built by Civil Protection, the Fire Service, Regione Lombardia, or any other public institution. Made by a volunteer; available to the public.',
    featuresLabel: 'What it does',
    features: [
      {
        title: '4:3 viewfinder and live stamp',
        body: 'Viewfinder with black letterbox bands: flash, aim, and stamp settings on top; zoom and shutter below. The viewfinder shows the same stamp that will be burned into the photo.',
      },
      {
        title: 'Aimed target',
        body: 'Press and hold the viewfinder (or tap Aim) to open a full-width sheet. Enter a distance (e.g. 200 m) and lock estimated coordinates from GPS + compass. Toggle Me ↔ Target without losing the lock; the stamp shows “Aimed target (estimated)”.',
      },
      {
        title: 'Stamp on the JPEG',
        body: 'Date, time (Europe/Rome), coordinates, altitude, heading, place, and optional custom text printed on the photo.',
      },
      {
        title: 'Fast shutter',
        body: 'Shoot in sequence without waiting for stamp processing. Position uses the live fix (or locked aimed point) when available, so the shutter stays snappy.',
      },
      {
        title: 'EXIF for machines',
        body: 'After stamping, the app writes Software, ImageUniqueID (UUID), GPS tags, and a JSON UserComment when the native build supports it.',
      },
      {
        title: 'In-app gallery',
        body: 'Photos stay in the app as the source of truth. Optional Photos album “AIB Camera”; deleting in-app also removes that copy when possible.',
      },
      {
        title: 'Capture log',
        body: 'Each shot gets a local ID, date, and coordinates. In Settings you see version and install ID (tap to copy) and export the log as CSV with install_id — even after deleting the photo from the gallery.',
      },
      {
        title: 'Works offline',
        body: 'Capture, stamp, gallery, capture log, and share without a network. GPS and place labels are optional. On constrained devices (no EXIF, Photos album, fonts, or haptics) the app still captures and saves.',
      },
      {
        title: 'All on-device',
        body: 'No account, no analytics SDK, no cloud sync. Photos, settings, and the log stay on the device.',
      },
      {
        title: 'Emergency-ready',
        body: 'Camera is required; location is not. You can still shoot without a GPS fix.',
      },
    ],
    guideTitle: 'Field guide',
    guideBodyHtml:
      'Field guide covering permissions, camera, stamp, gallery, capture log, limits, and checklist. Readable on the web and printable as PDF.',
    guideLink: 'Open the guide',
    guideOr: 'or download',
    guidePdf: 'field-guide.pdf',
    privacyTitle: 'Privacy and terms',
    privacyBodyHtml:
      'Photos, settings, capture log, and location stay on the device. Read the <a data-aib="privacy">privacy policy</a> and the <a data-aib="terms">terms of use</a>.',
    contactsTitle: 'Contact',
    contactsBodyHtml:
      'Questions about the app: <a href="mailto:stefano@smnl.dev">stefano@smnl.dev</a>.',
  },
  guide: {
    title: 'Field guide — AIB Camera',
    description:
      'AIB Camera field guide in English: permissions, camera, stamp, gallery, capture log, limits, and field checklist. Readable in the browser and downloadable as PDF.',
    heading: 'Field guide',
    leadHtml:
      'How AIB Camera works in the field: permissions, capture, stamp, gallery, and capture log. <strong>Unofficial</strong> app, independent of public agencies. Reference version <strong>1.0.0</strong>.',
    downloadPdf: 'Download PDF',
    openPdf: 'Open PDF',
    pdfHref: '/docs/aib-camera-field-guide.pdf',
    pdfFilename: 'aib-camera-field-guide.pdf',
  },
  privacy: {
    title: 'Privacy policy — AIB Camera',
    description:
      'AIB Camera privacy policy: app open to anyone. On-device field photography; photos, settings, capture log, and location stay on the phone.',
    heading: 'Privacy policy — AIB Camera',
    updated: 'Last updated: 26 October 2026',
    intro1Html:
      'AIB Camera is an independent, unofficial mobile app for field photography in wildfire response (AIB) and similar situations. It is <strong>open to anyone</strong> — not limited to a specific organization, crew, or set of professionals. Anyone can use it to take stamped, geotagged photos (for example to share with local Civil Protection via WhatsApp or Telegram, or to read coordinates and other details to emergency responders such as 112). It is <strong>not</strong> affiliated with, endorsed by, or built by Civil Protection, the Fire Service, Regione Lombardia, or other public institutions.',
    intro2: 'This policy describes how the app handles data on the device.',
    summaryTitle: 'In short',
    summary: [
      'The app is <strong>offline-first</strong>. Photos, settings, in-app gallery, and capture log stay on the device.',
      'There is no <strong>account</strong>, <strong>analytics SDK</strong>, or developer-managed <strong>cloud sync</strong>.',
      'The <strong>camera</strong> is required to take photos.',
      '<strong>Location</strong> is optional and is used to print coordinates, altitude, heading, and place on the photo, and to write matching EXIF JPEG fields when possible. You can also lock an <strong>aimed target</strong> (GPS + compass + entered distance): estimated coordinates can replace your position on the stamp until you clear them.',
      '<strong>Photos library</strong> access is optional and is used to save stamped photos into the “AIB Camera” album and remove those copies when you delete photos in the app.',
      'Network is used only for optional <strong>reverse geocoding</strong> (GPS → place label) when online. Nearby labels may stay cached on the device for offline reuse.',
    ],
    dataTitle: 'Data processed by the app',
    colData: 'Data',
    colWhere: 'Where it stays',
    colWhy: 'Why',
    dataRows: [
      {
        data: 'Camera image',
        where: 'On device (app storage; optionally Photos library)',
        why: 'Capture and share stamped field photos',
      },
      {
        data: 'Date and time',
        where: 'Printed on the JPEG (Europe/Rome); also EXIF DateTime',
        why: 'Operational context on the photo',
      },
      {
        data: 'GPS coordinates, accuracy, altitude, heading',
        where: 'On device; on stamp / EXIF when available',
        why: 'Location context on the photo',
      },
      {
        data: 'Aimed-target estimate (distance + heading)',
        where:
          'On device (session sticky point); may appear on stamp / log / EXIF GPS',
        why: 'Coordinates of what you are pointing at, not only your position',
      },
      {
        data: 'Place label (reverse geocode)',
        where:
          'On device; optional network lookup; local cache; may appear in EXIF',
        why: 'Human-readable place on the stamp',
      },
      {
        data: 'Capture ID (UUID)',
        where: 'On device (capture log); EXIF ImageUniqueID / UserComment JSON',
        why: 'Identify photos taken with this app',
      },
      {
        data: 'Install ID (UUID)',
        where: 'On device (local storage); Settings (tap to copy); CSV export',
        why: 'Tie capture-log CSVs to a specific installation',
      },
      {
        data: 'Custom stamp text',
        where: 'On device (settings); may appear in EXIF description / comment',
        why: 'Optional notes on the photo',
      },
      {
        data: 'App settings',
        where: 'On device (local storage)',
        why: 'Remember stamp preferences',
      },
    ],
    logTitle: 'Capture log',
    logBodyHtml:
      'Each successful shot is recorded in a local <strong>capture log</strong> (ID, date/time, and coordinates when available). The log stays on the device even if you delete the photo from the in-app gallery or system album. It is not uploaded. You can open it from Settings, copy an ID to compare with EXIF on a shared JPEG, or export everything as CSV via the system share sheet. Each app install also has a local <strong>install ID</strong> (UUID), shown in Settings. There you can tap <strong>Version</strong> or <strong>Install ID</strong> to copy the value. The install ID is written in the <code data-aib="code">install_id</code> CSV column so logs from multiple devices stay distinguishable. Reinstalling the app creates a new ID. The version string is only the app build label (not a tracking identifier).',
    exifTitle: 'EXIF metadata',
    exifBodyHtml:
      'After the visual stamp, the app tries to write standard EXIF tags on the saved JPEG (for example <code data-aib="code">Software</code>, <code data-aib="code">Artist</code>, <code data-aib="code">ImageUniqueID</code>, GPS fields, and a JSON <code data-aib="code">UserComment</code>). If the device build cannot write EXIF, the photo remains saved with the visual stamp and the capture-log entry. If stamp compositing also fails, the unstamped JPEG still remains in the in-app gallery.',
    noBackendHtml:
      'The developer does <strong>not</strong> run a backend that receives photos, location, settings, capture IDs, or install IDs.',
    permissionsTitle: 'Permissions',
    permissions: [
      '<strong>Camera</strong> — required to capture.',
      '<strong>Location (while using)</strong> — optional. If denied you can still shoot; geo lines on the stamp show unavailable and GPS EXIF fields are omitted.',
      '<strong>Photos / media library</strong> — optional: save stamped photos to the “AIB Camera” album and remove those copies when you delete them in the app. The in-app gallery remains the source of truth even if this permission is denied.',
    ],
    shareTitle: 'Sharing',
    shareBody:
      'If you use the system share sheet (WhatsApp, Mail, Files, etc.), the shared photo (including any EXIF written into the file) or a capture-log CSV leaves the app under the rules of the app or service you choose. That is outside AIB Camera’s control.',
    minorsTitle: 'Children’s privacy',
    minorsBody:
      'The app is not directed at children and does not knowingly collect personal data from minors.',
    changesTitle: 'Changes',
    changesBody:
      'If this policy changes in a material way, the “Last updated” date above will be revised. Continuing to use the app after an update means you accept the revised policy.',
    contactsTitle: 'Contact',
    contactsBodyHtml:
      'Developer site: <a href="https://smnl.dev/?ref=aib-camera-privacy">smnl.dev</a> · Policy URL: <a href="https://smnl.dev/aib-camera/privacy">smnl.dev/aib-camera/privacy</a> · Terms: <a data-aib="terms">smnl.dev/aib-camera/terms</a>',
    contactsMailHtml:
      'Privacy questions about AIB Camera: <a href="mailto:stefano@smnl.dev">stefano@smnl.dev</a>.',
  },
  terms: {
    title: 'Terms of use — AIB Camera',
    description:
      'AIB Camera terms of use: independent unofficial app, open to anyone, for AIB field photography. Use at your own risk, as-is, with no institutional affiliation.',
    heading: 'Terms of use — AIB Camera',
    updated: 'Last updated: 26 October 2026',
    intro1Html:
      'These terms govern use of the <strong>AIB Camera</strong> mobile app. By downloading, installing, or using the app you accept these terms. If you do not accept them, do not use the app.',
    intro2Html:
      'AIB Camera is an <strong>independent, unofficial</strong> project made by a volunteer. It is <strong>not</strong> affiliated with, endorsed by, or built by Civil Protection, the Fire Service, Regione Lombardia, or any other public institution. Do not use institutional logos, seals, or names in the app in a way that could suggest official endorsement.',
    whoTitle: 'Who may use it',
    whoBodyHtml:
      '<strong>Anyone.</strong> The app is <strong>not</strong> limited to a specific organization, crew, or professional role (Civil Protection, Fire Service, or others). It is available to the public. Typical uses: document a wildfire or similar situation, share the stamped photo (WhatsApp, Telegram, Mail, …), or read coordinates and other details to emergency responders (e.g. 112). Volunteers and organizations may also use it if they wish: that does not make it exclusive to them.',
    purposeTitle: 'What the app is for',
    purposeBodyHtml:
      'AIB Camera helps you take field photos with date/time, location (when available), and custom text printed on the JPEG, plus a local capture log. It is a <strong>support tool</strong> for personal documentation and sharing: it does <strong>not</strong> replace official procedures, forms, command systems, or institutional archives.',
    allowedTitle: 'Allowed use',
    allowed: [
      'Use the app for field photography related to wildfires (AIB) or similar situations, in compliance with applicable law.',
      'Do not present the app, photos, or metadata as official or institutionally certified documentation.',
      'Do not use the app for illegal activity, harassment, or to deceptively alter coordinates, times, or context on the photo.',
      'You are responsible for what you capture, share, or export (photos, EXIF, log CSV), including destination and recipients.',
    ],
    aimTitle: 'Location and aimed target',
    aimBodyHtml:
      'GPS, compass, altitude, and place depend on the device, permissions, and signal conditions. <strong>Aimed target</strong> mode (distance + heading) produces a ground <strong>estimate</strong> along the compass: it is not a GNSS fix on the target and is not certified. Do not base safety decisions or official reports solely on these values.',
    asIsTitle: 'Availability and “as is”',
    asIsBody1Html:
      'The app is provided <strong>“as is”</strong> and <strong>“as available”</strong>, without warranties of any kind, express or implied, including fitness for a particular purpose. In particular there is no guarantee of: continuity of service, GPS/compass/aimed-target accuracy, EXIF completeness, Photos album save, or identical behavior on all devices.',
    asIsBody2Html:
      'In emergencies the app aims to remain usable without network or GPS, but it does <strong>not</strong> replace radio communications, safety procedures, or operational judgment in the field.',
    liabilityTitle: 'Limitation of liability',
    liabilityBody:
      'To the extent permitted by applicable law, the developer is not liable for direct or indirect damages, data loss, wrong operational decisions, or consequences arising from use of or inability to use AIB Camera — including inaccurate position estimates or photos shared with third parties.',
    privacyTitle: 'Privacy and data',
    privacyBodyHtml:
      'How the app handles data is described in the <a data-aib="privacy">privacy policy</a>. In short: mostly on-device processing, with no account or developer-managed cloud sync.',
    ipTitle: 'Intellectual property',
    ipBody:
      'The app, the name “AIB Camera” as used here, and related site materials belong to the developer, subject to third-party components (open-source libraries, fonts, etc.) under their licenses. No license is granted to trademarks or marks of public institutions.',
    updatesTitle: 'Updates',
    updatesBody:
      'The app and these terms may change. The “Last updated” date will be revised for material changes. Continuing to use the app after an update means you accept the revised terms.',
    contactsTitle: 'Contact',
    contactsBodyHtml:
      'Developer site: <a href="https://smnl.dev/?ref=aib-camera-terms">smnl.dev</a> · Terms URL: <a href="https://smnl.dev/aib-camera/terms">smnl.dev/aib-camera/terms</a> · Privacy: <a data-aib="privacy">smnl.dev/aib-camera/privacy</a>',
    contactsMailHtml:
      'Questions about the terms or the app: <a href="mailto:stefano@smnl.dev">stefano@smnl.dev</a>.',
  },
}

const catalogs: Record<AibLocale, AibCopy> = { it, en }

export function aibCopy(locale: AibLocale): AibCopy {
  return catalogs[locale] ?? catalogs.it
}
