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
        body: 'Crea una mappa, password opzionale (modificabile dalla sidebar); in home Cerca (ricerca + elenco) o Crea; link da condividere.',
      },
      {
        title: 'Segni con timestamp',
        body: 'Frecce, forme, testo e simboli tattici — storico append-only, con il nome del profilo su ogni aggiunta, spostamento o rimozione.',
      },
      {
        title: 'Scrubber della timeline',
        body: 'Ogni aggiunta, spostamento o rimozione è uno step (con chi l’ha fatto); ?step= nell’URL e in «Copia link»; export GeoJSON o PNG.',
      },
      {
        title: 'Aggiornamenti in tempo reale',
        body: 'Stanze aperte via SSE; stanze con password aggiornate con polling. Usabile su telefono e desktop; tema chiaro/scuro.',
      },
    ],
    appCta: 'Apri l’app',
    guideTitle: 'Guida',
    guideBody:
      'Come creare un intervento, usare cursore e simboli, navigare la timeline e condividere lo step.',
    guideLink: 'Leggi la guida',
    privacyTitle: 'Privacy',
    privacyBody:
      'Metadati, elenco pubblico in home (titolo / data / protetto; centro mappa redatti se protetta) e storico eventi nel database del prodotto. Niente account in v1: accesso = link + password opzionale (hash scrypt; header X-Passphrase). Tema e profilo in localStorage. Tile da HOT OSM. Guida/Privacy/Termini sul sito marketing.',
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
    lead: 'Guida operativa breve per sala, capisquadra e chiunque usi la mappa. L’interfaccia è in italiano; tema chiaro o scuro (predefinito: sistema).',
    sections: [
      {
        title: '1. Crea o apri un intervento',
        paragraphs: [
          'Dalla home (scheda centrata, senza navbar/footer) scegli **Cerca** per trovare e aprire un intervento esistente (ricerca per titolo, paginazione) oppure **Crea** per uno nuovo (titolo e password opzionale, minimo 4 caratteri se la imposti).',
          'In creazione, se il browser lo consente, la mappa può centrarsi sulla tua posizione; altrimenti usa il centro predefinito.',
          'Apri la stanza dal link o dall’elenco. Se è protetta, vedi la mappa (basemap) con un dialogo password obbligatorio: finché non sblocchi non partono richieste di dettaglio/eventi e gli strumenti restano disattivati. Dopo lo sblocco la password resta in sessionStorage del browser per quella stanza (si chiude con la sessione).',
        ],
      },
      {
        title: '2. Profilo (nome visualizzato)',
        paragraphs: [
          'In home: profilo in basso nella scheda (accanto a Informazioni e al controllo tema). Nella mappa: in fondo alla sidebar, profilo a sinistra e icona tema a destra.',
          'Imposta un nome (es. Sala / Caposquadra); è obbligatorio prima di aggiungere, spostare o rimuovere segni. Il nome resta in localStorage; quando disegni, sposti o elimini un segno viene copiato sull’evento e compare nella timeline.',
        ],
      },
      {
        title: '3. Sidebar — info, segni e azioni',
        paragraphs: [
          'Nella vista mappa non ci sono navbar/footer del sito. Su desktop: sidebar a sinistra e mappa a destra. Su telefono: mappa a tutto schermo e menu (☰) in alto a sinistra che apre la stessa sidebar a cassetto; scegliendo uno strumento il menu si chiude.',
          'Nella sidebar: logo (torna alla home), titolo intervento (lucchetto se protetto), poi «Cursore» e simboli (Freccia / Forma / Testo e tattici non ufficiali). In modalità cursore, tocca un segno per selezionarlo; il tocco sul vuoto deseleziona.',
          'Sotto: impostazioni, «Esporta», «Informazioni» (su smnl.dev/aib-sitac). In fondo: profilo e icona tema. Rinomina/password solo in «Ora».',
        ],
      },
      {
        title: '4. Controlli flottanti in basso',
        paragraphs: [
          'Al centro in basso sulla mappa: nome del tool o del segno selezionato, icona info (apre una finestra con a cosa serve e come si usa), «Termina» / «Annulla» mentre disegni una freccia o una forma, e «Sposta» / «Elimina» quando hai un segno selezionato («Sposta» = trascina, poi «Termina» per salvare o «Annulla»; «Elimina» con conferma; solo in «Ora», non mentre scrubbi la timeline).',
        ],
      },
      {
        title: '5. Timeline flottante in alto',
        paragraphs: [
          'In alto sulla mappa (su telefono può andare su due righe): ogni aggiunta, spostamento o rimozione è uno step automatico. Usa lo slider, ← →, o «« / »» per inizio/fine (live). Lo step corrente va in ?step= nell’URL; «Copia link» include lo step se stai scrubbando.',
        ],
      },
      {
        title: '6. Collaborazione e password',
        paragraphs: [
          'Stanze senza password: chiunque col link può vedere e modificare; aggiornamenti live via SSE. Se viene aggiunta una password, le connessioni live si chiudono e serve sbloccare.',
          'Stanze con password: la mappa si apre in sola lettura con dialogo password non chiudibile; serve la password per caricare i dati, leggere eventi e modificare; gli aggiornamenti arrivano con polling. Non mettere la password nell’URL.',
        ],
      },
      {
        title: '7. Limiti e buon uso',
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
    updated: 'Ultimo aggiornamento: 29 ottobre 2026',
    intro:
      'AIB SITAC è uno strumento indipendente e non ufficiale per mappe di situazione tattica su incendi boschivi. Non è affiliato a Protezione Civile, Vigili del Fuoco, Regione Lombardia o altre istituzioni pubbliche.',
    summaryTitle: 'In sintesi',
    summary: [
      'Metadati dell’intervento e lo storico eventi (segni) sono salvati nel database PostgreSQL del prodotto quando crei o apri una stanza.',
      'L’elenco interventi in home (e l’API di elenco) è pubblico: per ogni stanza compaiono titolo, data di creazione (Europe/Rome), eventuale creatore e se è protetta da password; centro mappa e zoom delle stanze protette non sono esposti nell’elenco. Aprire o vedere i segni resta vincolato al link e, se impostata, alla password.',
      'Niente account in v1: l’accesso è il link dell’intervento e, se impostata, la password.',
      'Nessun SDK analytics incluso di default.',
      'Il nome visualizzato (profilo in home o in mappa) resta in localStorage del browser; alla creazione di un intervento può essere salvato come creatore (createdBy), e quando aggiungi o rimuovi un segno viene copiato sull’evento come actorLabel.',
      'La preferenza di tema (sistema / chiaro / scuro) resta in localStorage se la cambi dal predefinito (sistema).',
      'Le tile della mappa arrivano da server HOT OSM / openstreetmap.fr, che possono vedere IP e coordinate delle tile richieste.',
      'Guida, Privacy e Termini dall’app si aprono sul sito smnl.dev/aib-sitac (niente copie in-app).',
    ],
    dataTitle: 'Dati trattati',
    colData: 'Dato',
    colWhere: 'Dove',
    colWhy: 'Perché',
    dataRows: [
      {
        data: 'Titolo, data creazione, eventuale creatore, indicatore «protetto» (elenco home / API)',
        where: 'Database (+ risposta API pubblica)',
        why: 'Ricerca e apertura stanze dalla home',
      },
      {
        data: 'Centro mappa e zoom iniziale',
        where: 'Database (solo dopo sblocco)',
        why: 'Aprire la stanza al centro giusto; redatti nell’elenco pubblico se protetta',
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
        why: 'Creatore su nuovi interventi + etichetta sugli eventi',
      },
      {
        data: 'Preferenza tema (sistema / chiaro / scuro)',
        where: 'localStorage del browser',
        why: 'Ricordare il tema UI',
      },
      {
        data: 'Richieste tile mappa (zoom/x/y)',
        where: 'Server tile HOT OSM',
        why: 'Disegnare lo sfondo mappa',
      },
      {
        data: 'Password stanza (dopo lo sblocco)',
        where: 'sessionStorage del browser',
        why: 'Riaprire la stanza nella stessa sessione',
      },
    ],
    accessTitle: 'Accesso e password',
    access: [
      'Stanze aperte (senza password): chiunque col link può vedere e modificare; aggiornamenti live via Server-Sent Events. Se viene aggiunta una password, le connessioni live aperte vengono chiuse e serve sbloccare di nuovo.',
      'Stanze protette: l’UI chiede solo se la stanza è protetta (stesso flag dell’elenco home), poi mostra la shell mappa con dialogo password obbligatorio e non fa altre richieste (dettaglio, eventi, SSE) finché non sblocchi; ogni lettura/scrittura richiede la password solo come header HTTP X-Passphrase (mai nell’URL). L’API conserva un hash scrypt salato e limita i tentativi errati. Cambiare o togliere la password disconnette gli ascoltatori live di quella stanza.',
      'Dopo lo sblocco, la password resta in sessionStorage del browser per quella stanza (si chiude con la sessione); non viene messa nell’URL.',
      'Il nome profilo resta in localStorage; alla creazione può diventare createdBy, sugli eventi compare come actorLabel. La preferenza tema resta in localStorage (chiave aib-sitac-theme) se impostata.',
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
    updated: 'Ultimo aggiornamento: 29 ottobre 2026',
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
