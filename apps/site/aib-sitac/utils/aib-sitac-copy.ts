export const aibSitacCopy = {
  nav: {
    home: 'AIB SITAC',
    homeAria: 'AIB SITAC — home',
    guide: 'Guida',
    privacy: 'Privacy',
    terms: 'Termini',
    openApp: 'Apri l’app',
  },
  footer: {
    brand: 'AIB SITAC',
  },
  home: {
    title: 'AIB SITAC',
    description:
      'Mappa di situazione tattica non ufficiale per incendi boschivi — segni con timestamp, stanze condivise, timeline.',
    lead: 'Mappa di situazione tattica per incendi boschivi.',
    intro1:
      'Disegna simboli, aree e frecce su una mappa condivisa. Ogni segno ha un timestamp così puoi ripercorrere come si è evoluto lo scenario.',
    intro2:
      'Indipendente e non ufficiale — non affiliato a Protezione Civile, Vigili del Fuoco o altre istituzioni. Pensato per operatori; aperto a chiunque.',
    featuresLabel: 'Cosa fa',
    features: [
      {
        title: 'Interventi condivisi',
        body: 'Crea una mappa, password opzionale (modificabile dalla sidebar); elenco in home con ricerca, filtro date e paginazione; link da condividere.',
      },
      {
        title: 'Segni con timestamp',
        body: 'Frecce, forme, testo e simboli tattici — storico append-only, con il nome del profilo su ogni add/remove.',
      },
      {
        title: 'Scrubber della timeline',
        body: 'Ogni add/remove è uno step (con chi l’ha fatto); ?step= nell’URL e in «Copia link»; export GeoJSON o PNG.',
      },
      {
        title: 'Aggiornamenti in tempo reale',
        body: 'Stanze aperte via SSE; stanze con password aggiornate con polling. Solo dark mode in v1.',
      },
    ],
    appCta: 'Apri l’app',
    guideTitle: 'Guida',
    guideBody:
      'Come creare un intervento, usare cursore e simboli, navigare la timeline e condividere lo step.',
    guideLink: 'Leggi la guida',
    privacyTitle: 'Privacy',
    privacyBody:
      'Metadati, elenco pubblico in home (titolo / data / protetto) e storico eventi nel database del prodotto. Niente account in v1: accesso = link + password opzionale (hash scrypt lato API). Nessun SDK analytics di default.',
    privacyLink: 'Informativa privacy',
    termsTitle: 'Termini',
    termsBody:
      'Strumento di volontariato non ufficiale: non sostituisce i canali di emergenza né i sistemi istituzionali.',
    termsLink: 'Termini di utilizzo',
    contactsTitle: 'Contatti',
  },
  guide: {
    title: 'AIB SITAC — Guida',
    description:
      'Come usare AIB SITAC: interventi, profilo, simboli, timeline, password ed export.',
    heading: 'Guida',
    lead: 'Guida operativa breve per sala, capisquadra e chiunque usi la mappa. L’interfaccia è in italiano; solo dark mode per ora.',
    sections: [
      {
        title: '1. Crea o apri un intervento',
        paragraphs: [
          'Dalla home dell’app puoi creare un nuovo intervento (titolo e password opzionale, minimo 4 caratteri se la imposti) o cercare tra quelli esistenti (filtro per titolo, intervallo di date Europe/Rome, paginazione).',
          'In creazione, se il browser lo consente, la mappa può centrarsi sulla tua posizione; altrimenti usa il centro predefinito.',
          'Apri la stanza dal link o dall’elenco. Se è protetta, inserisci la password: resta in sessionStorage del browser per quella stanza (si chiude con la sessione).',
        ],
      },
      {
        title: '2. Profilo (nome visualizzato)',
        paragraphs: [
          'Icona utente in navbar: imposta un nome (es. Sala / Caposquadra). È obbligatorio prima di aggiungere o rimuovere segni.',
          'Il nome resta in localStorage sul dispositivo; quando disegni o elimini un segno viene copiato sull’evento e compare nella timeline.',
        ],
      },
      {
        title: '3. Sidebar sinistra — cursore e simboli',
        paragraphs: [
          '«Cursore»: seleziona un segno sulla mappa (clic sul vuoto non fa nulla). Con un segno selezionato puoi rimuoverlo con «Elimina segno» in fondo alla sidebar (solo in «Ora», non mentre scrubbi la timeline).',
          'Annotazioni in alto: Freccia e Forma (più clic, poi «Termina» o doppio clic); Testo (un clic e la finestra per l’etichetta).',
          'Sotto, simboli tattici non ufficiali: Hotspot, Fronte attivo, Area bruciata, Punto acqua, Osservatorio, PC/ROS, Mezzo, Squadra, Strada chiusa, Vento. Non sono simbologia NATO/PC istituzionale.',
        ],
      },
      {
        title: '4. Sidebar destra — timeline e azioni',
        paragraphs: [
          'Ogni add/remove è uno step automatico. Usa lo slider o ← →; «Ora» torna al live. Lo step corrente va in ?step= nell’URL; «Copia link» include lo step se stai scrubbando.',
          'Impostazioni intervento (rinomina, password) e export GeoJSON/PNG: disponibili in «Ora»; rinomina/password non sono modificabili mentre scrubbi la timeline.',
        ],
      },
      {
        title: '5. Collaborazione e password',
        paragraphs: [
          'Stanze senza password: chiunque col link può vedere e modificare; aggiornamenti live via SSE.',
          'Stanze con password: serve la password per aprire e modificare; gli aggiornamenti arrivano con polling. Non mettere la password nell’URL.',
        ],
      },
      {
        title: '6. Limiti e buon uso',
        paragraphs: [
          'I segni possono essere stimati o incompleti: usali con giudizio operativo. Non presentare la mappa come prodotto ufficiale.',
          'Per dettagli su dati e condizioni d’uso vedi Privacy e Termini.',
        ],
      },
    ],
    openApp: 'Apri l’app',
  },
  privacy: {
    title: 'Privacy — AIB SITAC',
    description:
      'Informativa privacy di AIB SITAC: dati in database, niente account, password opzionale.',
    heading: 'Privacy',
    updated: 'Ultimo aggiornamento: 27 ottobre 2026',
    intro:
      'AIB SITAC è uno strumento indipendente e non ufficiale per mappe di situazione tattica su incendi boschivi. Non è affiliato a Protezione Civile, Vigili del Fuoco, Regione Lombardia o altre istituzioni pubbliche.',
    summaryTitle: 'In sintesi',
    summary: [
      'Metadati dell’intervento e lo storico eventi (segni) sono salvati nel database PostgreSQL del prodotto quando crei o apri una stanza.',
      'L’elenco interventi in home (e l’API di elenco) è pubblico: per ogni stanza compaiono titolo, data di creazione (Europe/Rome) e se è protetta da password; aprire o vedere i segni resta vincolato al link e, se impostata, alla password.',
      'Niente account in v1: l’accesso è il link dell’intervento e, se impostata, la password.',
      'Nessun SDK analytics incluso di default.',
      'Il nome visualizzato (icona utente) resta in localStorage del browser; quando aggiungi o rimuovi un segno viene copiato sull’evento come actorLabel.',
    ],
    dataTitle: 'Dati trattati',
    colData: 'Dato',
    colWhere: 'Dove',
    colWhy: 'Perché',
    dataRows: [
      {
        data: 'Titolo, data creazione, indicatore «protetto» (elenco home / API)',
        where: 'Database (+ risposta API pubblica)',
        why: 'Ricerca e apertura stanze dalla home',
      },
      {
        data: 'Centro mappa e zoom iniziale',
        where: 'Database',
        why: 'Aprire la stanza al centro giusto',
      },
      {
        data: 'Hash della password (opzionale)',
        where: 'Database',
        why: 'Proteggere lettura/modifica',
      },
      {
        data: 'Eventi mappa (geometrie, simboli, note, timestamp, nome attore)',
        where: 'Database',
        why: 'SITAC collaborativa e timeline',
      },
      {
        data: 'Nome visualizzato (profilo locale)',
        where: 'localStorage del browser',
        why: 'Etichetta sugli eventi',
      },
      {
        data: 'Password stanza (dopo lo sblocco)',
        where: 'sessionStorage del browser',
        why: 'Riaprire la stanza nella stessa sessione',
      },
    ],
    accessTitle: 'Accesso e password',
    access: [
      'Stanze aperte (senza password): chiunque col link può vedere e modificare; aggiornamenti live via Server-Sent Events.',
      'Stanze protette: la password è inviata solo come header HTTP X-Passphrase (mai nell’URL). L’API conserva un hash scrypt salato e limita i tentativi errati.',
      'Dopo lo sblocco, la password resta in sessionStorage del browser per quella stanza (si chiude con la sessione); non viene messa nell’URL.',
      'Il nome profilo resta in localStorage; sugli eventi compare come actorLabel.',
    ],
    shareTitle: 'Condivisioni ed export',
    shareBody:
      'Export GeoJSON / PNG e link condivisi escono dall’app secondo le regole degli strumenti in cui li salvi o inoltri (chat, email, file).',
    contactTitle: 'Contatto',
  },
  terms: {
    title: 'Termini — AIB SITAC',
    description:
      'Termini di utilizzo di AIB SITAC: strumento non ufficiale, licenza, uso responsabile.',
    heading: 'Termini di utilizzo',
    updated: 'Ultimo aggiornamento: 27 ottobre 2026',
    intro:
      'AIB SITAC è uno strumento di volontariato non ufficiale per la mappa tattica di incendi boschivi. Non sostituisce i numeri di emergenza, i canali ufficiali né i sistemi istituzionali.',
    items: [
      'Lo usi con giudizio operativo: i segni possono essere stimati, incompleti o non aggiornati.',
      'Non presentarlo (né le mappe) come prodotto ufficiale di autorità pubbliche.',
      'Il codice è sotto PolyForm Noncommercial 1.0.0; nome e asset di brand non sono concessi per riuso in altri prodotti.',
      'Il servizio può cambiare, interrompersi o essere ritirato senza preavviso.',
      'Non abusare dell’API (scraping automatico, spam di stanze, tentativi di forzare la password).',
    ],
    contactTitle: 'Contatto',
  },
} as const
