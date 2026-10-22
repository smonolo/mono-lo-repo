<!-- Regenera PDF + HTML: npm run docs:aib-camera-guida (da apps/site) → public/docs/ -->

# AIB Camera — Guida operativa

<table class="doc-meta">
  <tbody>
    <tr><td><strong>Versione documento</strong></td><td>22 ottobre 2026</td></tr>
    <tr><td><strong>Versione app di riferimento</strong></td><td>1.0.0</td></tr>
    <tr><td><strong>Lingua</strong></td><td>Italiano</td></tr>
    <tr><td><strong>Sito</strong></td><td><a href="https://smnl.dev/aib-camera">smnl.dev/aib-camera</a></td></tr>
    <tr><td><strong>Privacy</strong></td><td><a href="https://smnl.dev/aib-camera/privacy">smnl.dev/aib-camera/privacy</a></td></tr>
    <tr><td><strong>PDF</strong></td><td><a href="https://smnl.dev/docs/aib-camera-guida-operativa.pdf">smnl.dev/docs/aib-camera-guida-operativa.pdf</a></td></tr>
  </tbody>
</table>

## Indice

1. Scopo della guida
2. Cos’è AIB Camera
3. Cosa fa e cosa non fa
4. Requisiti e installazione
5. Permessi
6. Schermate principali
7. Fotocamera
8. Aspetto del timbro
9. Galleria (scheda Foto)
10. Registro scatti
11. Metadati EXIF
12. Uso offline e luogo
13. Condivisione
14. Limiti e responsabilità
15. Checklist operativa
16. Contatti

---

## 1. Scopo della guida

Questa guida spiega, in italiano, come funziona AIB Camera sul campo: permessi, scatto, timbro sulle foto, galleria, registro scatti e uso senza rete.

È pensata per chi usa l’app in attività di antincendio boschivo (AIB) e per chi deve valutarne l’adozione operativa (squadre, coordinamenti, enti). Descrive il comportamento reale della versione **1.0.0**.

---

## 2. Cos’è AIB Camera

AIB Camera è un’applicazione mobile **indipendente e non ufficiale** per la fotografia di campo nell’antincendio boschivo. Consente di:

- scattare foto con la fotocamera posteriore;
- stampare sul JPEG (timbro visivo) data, ora, posizione e — se disponibili — luogo e altri dati di contesto;
- conservare le foto in una **galleria interna all’app**;
- condividerle con il foglio di condivisione del sistema operativo;
- tenere un **registro scatti** locale con un identificativo per ogni scatto.

È progettata per restare utilizzabile **anche senza rete, senza GPS e senza accesso alla libreria Foto**: in emergenza deve sempre essere possibile scattare e condividere.

<aside class="callout callout-warn">

**Avviso — natura non istituzionale**

L’app **non** è realizzata, approvata né affiliata a Protezione Civile, Vigili del Fuoco, Regione Lombardia o altri enti pubblici. Non utilizza loghi, stemmi o sigle istituzionali. È uno strumento volontario; l’eventuale uso in contesti ufficiali resta sotto la responsabilità dell’organizzazione o dell’operatore che la adotta.

</aside>

---

## 3. Cosa fa e cosa non fa

### Cosa fa

| Funzione         | Dettaglio                                                                                                                                     |
| ---------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| Timbro sul JPEG  | Data/ora (fuso **Europe/Rome**), coordinate, precisione, altitudine, direzione, luogo e testo personalizzato, secondo le impostazioni Aspetto |
| Anteprima live   | Sul mirino compare lo stesso blocco che verrà bruciato sulla foto                                                                             |
| Scatto rapido    | Si possono scattare più foto di seguito; il timbro viene elaborato in coda, una foto alla volta                                               |
| Galleria in-app  | Fonte di verità: le foto restano nell’app anche senza permesso alla libreria Foto                                                             |
| Album di sistema | Copia nell’album «AIB Camera» se il permesso Foto è concesso (non garantita)                                                                  |
| Registro scatti  | Elenco locale solo in aggiunta: ID scatto, data/ora, coordinate; esportabile in CSV con `install_id`                                          |
| ID installazione | UUID stabile per installazione (Impostazioni → Informazioni, tocca per copiare); coincide con la colonna CSV                                  |
| EXIF             | Tentativo di scrittura di identificativo, GPS e commento macchina sul JPEG (non sempre disponibile)                                           |
| Offline          | Scatto, timbro, galleria, registro e condivisione non richiedono rete                                                                         |

### Cosa non fa

- Non richiede account, login o sincronizzazione cloud.
- Non invia foto, posizione o registro a server del sviluppatore.
- Non sostituisce procedure istituzionali di archiviazione, verbali o sistemi di comando e controllo.
- Non garantisce precisione GPS o completezza EXIF: dipendono da dispositivo, permesso, segnale e build dell’app.
- Non offre fotocamera anteriore, modalità flash «auto», né cambio lingua in-app (interfaccia predefinita in italiano).

---

## 4. Requisiti e installazione

- Telefono **iOS** o **Android** con fotocamera posteriore.
- Si consiglia un **dispositivo fisico** (non solo simulatore o emulatore).
- Orientamento supportato: **verticale** (portrait).
- Lingua dell’interfaccia: **italiano** (predefinita).
- Fuso orario sul timbro: sempre **Europe/Rome**, indipendente dal fuso impostato sul telefono.

Informazioni aggiornate su disponibilità e distribuzione: [smnl.dev/aib-camera](https://smnl.dev/aib-camera). Per adozioni organizzate (distribuzione a più dispositivi, build dedicate), contattare lo sviluppatore (vedi § 16).

---

## 5. Permessi

| Permesso                  | Obbligatorio? | A cosa serve                                                                                        |
| ------------------------- | ------------- | --------------------------------------------------------------------------------------------------- |
| **Fotocamera**            | Sì            | Scattare le foto. Senza questo permesso l’app non può acquisire immagini.                           |
| **Posizione (in uso)**    | No            | Coordinate, precisione, altitudine, direzione e luogo sul timbro e, se possibile, nell’EXIF.        |
| **Libreria Foto / Media** | No            | Copiare le foto nell’album di sistema «AIB Camera» e rimuoverle da lì quando si eliminano dall’app. |

**Senza GPS** è comunque possibile scattare. Sul timbro restano data/ora e l’eventuale testo personalizzato; le righe geo mostrano _Coordinate non disponibili_ / _Luogo non disponibile_.

**Senza libreria Foto** le immagini restano nella **galleria dell’app**, che è la fonte di verità.

I permessi si possono rivedere da **Impostazioni → Permessi** e dalle Impostazioni di sistema del telefono.

---

## 6. Schermate principali

Tre schede nella barra inferiore:

1. **Fotocamera** — scatto e anteprima del timbro
2. **Foto** — galleria in-app
3. **Impostazioni** — hub con **Permessi**, **Aspetto** e **Informazioni** (versione e ID installazione, entrambi copiabili al tocco; registro scatti; informativa privacy)

---

## 7. Fotocamera

### 7.1 Anteprima del timbro (live)

Sul mirino compare lo stesso blocco che verrà **bruciato** sul JPEG:

- in alto: data/ora, coordinate, meta (precisione / altitudine / direzione), luogo — secondo gli interruttori in Aspetto;
- in basso: testo personalizzato, se compilato.

Carattere, dimensione del testo e opacità dello sfondo seguono **Impostazioni → Aspetto**. L’anteprima coincide con ciò che finirà sulla foto.

### 7.2 Controlli

| Controllo                   | Comportamento                                                                                                                                                            |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Pulsante di scatto**      | Cerchio bianco al centro: acquisisce la foto.                                                                                                                            |
| **Anteprima ultimo scatto** | Quadratino a sinistra: apre il dettaglio dell’ultima foto pronta. Se l’elaborazione è ancora in corso compare un indicatore di caricamento.                              |
| **Flash**                   | Icona fulmine in basso a destra: solo **acceso / spento** (non c’è modalità automatica). È possibile anche **toccare due volte** il mirino per attivarlo o disattivarlo. |
| **Aspetto**                 | Icona ingranaggio a destra del flash: apre direttamente le impostazioni del timbro.                                                                                      |
| **Zoom**                    | Pulsanti sul mirino (es. 0,5× / 1× / 2× se disponibili) e gesto a **due dita**; durante il gesto compare il fattore di zoom.                                             |
| **Scorrimento**             | Scorrere verso **sinistra** dal mirino per aprire la galleria.                                                                                                           |

La fotocamera usata è solo quella **posteriore**.

### 7.3 Scatto e elaborazione in background

Dopo lo scatto:

1. Il JPEG grezzo viene acquisito subito; il pulsante di scatto resta disponibile per altri scatti.
2. In coda, **una foto alla volta**, l’app: applica il timbro, scrive l’EXIF se possibile, salva nella galleria in-app, aggiorna il registro scatti e prova a copiare nell’album «AIB Camera».

È possibile scattare in sequenza senza attendere la fine del timbro: l’indicatore sull’anteprima a sinistra segnala che l’elaborazione è ancora in corso.

Per il timbro l’app preferisce la **posizione GPS già disponibile** sul mirino, così lo scatto non resta bloccato in attesa di un segnale più preciso.

---

## 8. Aspetto del timbro

Percorso: **Impostazioni → Aspetto** (oppure l’ingranaggio sulla fotocamera).

### 8.1 Campi visibili (interruttori)

- Data e ora
- Coordinate
- Luogo (etichetta da geocodifica inversa o da cache locale)
- Precisione GPS
- Altitudine
- Direzione (bussola)

### 8.2 Testo personalizzato

Fino a **4 righe** libere (es. nome squadra o turno). Vuoto di default: non compare alcun blocco in basso. Segnaposto suggerito in app: _Squadra Nord · Turno A_.

### 8.3 Formato data

| Opzione      | Descrizione                           |
| ------------ | ------------------------------------- |
| **Completo** | Giorno della settimana e data estesa  |
| **Breve**    | Data e ora compatte (**predefinito**) |
| **ISO**      | Formato ISO con fuso Europe/Rome      |

### 8.4 Dimensione testo

Piccolo / **Medio** (predefinito) / Grande.

### 8.5 Opacità dello sfondo del timbro

Da **0%** (solo testo; con ombra per la leggibilità) a **100%**, a step del 25%. Predefinito: **0%**.

Le modifiche si applicano subito all’anteprima live e agli scatti successivi. Le foto già scattate non vengono modificate.

---

## 9. Galleria (scheda Foto)

- Le foto sono raggruppate per giorno (fuso Europe/Rome), con intestazioni tipo _Oggi_ / _Ieri_.
- Toccare una miniatura per il dettaglio (**Condividi** / **Elimina**).
- Modalità selezione: pulsante **Seleziona** oppure pressione prolungata su una miniatura; quindi condivisione o eliminazione multipla.

### Galleria in-app e album di sistema

| Confronto               | Galleria in-app                                                        | Album «AIB Camera»                                             |
| ----------------------- | ---------------------------------------------------------------------- | -------------------------------------------------------------- |
| Ruolo                   | **Fonte di verità**                                                    | Copia nella libreria del telefono (se il permesso lo consente) |
| Serve il permesso Foto? | No                                                                     | Sì, se si vuole la copia                                       |
| Eliminazione dall’app   | Rimuove il file locale e, se presente, anche la copia in Libreria Foto | —                                                              |

<aside class="callout">

**Registro scatti:** eliminare una foto dalla galleria **non** cancella la riga corrispondente nel registro scatti.

</aside>

---

## 10. Registro scatti

Accesso: **Impostazioni → Informazioni → Apri registro scatti**.

- Ogni scatto riuscito aggiunge una riga con: **ID** (UUID), data/ora, coordinate se disponibili (altrimenti _Nessun GPS_).
- Il registro è **solo in aggiunta** (non si cancellano voci dall’app): le righe restano sul dispositivo anche dopo le eliminazioni dalla galleria.
- Toccare una riga per **copiare l’ID** e confrontarlo con l’EXIF del JPEG condiviso (`ImageUniqueID` / commento JSON), quando l’EXIF è stato scritto.
- Pulsante **Esporta CSV** (in alto): genera un file locale con colonne `install_id`, `id`, `taken_at`, `latitude`, `longitude` e lo apre nel foglio di condivisione di sistema (Mail, File, AirDrop, …). Nessun upload automatico.
- In **Impostazioni → Informazioni**, tocca **Versione** o **ID installazione** per copiare il valore negli appunti.
- **ID installazione:** UUID creato alla prima apertura. È lo stesso valore della colonna `install_id` nei CSV; reinstallando l’app se ne genera uno nuovo.

Il registro **non** viene inviato a nessun server; l’esportazione CSV lascia il dispositivo solo se scegli tu un’app o un servizio di condivisione.

---

## 11. Metadati EXIF

Dopo il timbro l’app prova a scrivere sul JPEG, tra l’altro:

- `Software` / `Artist`
- `ImageUniqueID` (UUID dello scatto)
- date/ora di ripresa (Europe/Rome)
- tag GPS, se la posizione è disponibile
- `ImageDescription` e `UserComment` (JSON per uso macchina)

<aside class="callout callout-warn">

**Attenzione — EXIF non garantito:** se il modulo nativo non è disponibile sulla build installata, **foto timbrata, galleria e registro restano validi**. Manca solo la scrittura dei metadati nel file. Non trattare l’EXIF come prova legale automatica: verificare sempre anche il timbro visivo e il registro.

</aside>

---

## 12. Uso offline e luogo

- Scatto, timbro, galleria, registro e condivisione **non richiedono rete**.
- La rete serve solo per la **geocodifica inversa** (coordinate → etichetta di luogo), se il campo Luogo è attivo e c’è connessione.
- Le etichette recenti restano in **cache locale** e possono essere riusate offline nelle vicinanze (raggio circa **2,5 km**).

Senza rete o senza GPS, il timbro mostra i testi di fallback (_Coordinate non disponibili_, _Luogo non disponibile_) senza bloccare lo scatto.

---

## 13. Condivisione

Dal dettaglio o dalla selezione in galleria si apre il **foglio di condivisione di sistema** (Messaggi, Mail, WhatsApp, File, ecc.).

Il file condiviso è il JPEG già timbrato, con EXIF se è stato scritto. Dopo l’invio, trattamento e conservazione dipendono dall’app o dal servizio scelto dall’operatore (fuori dal controllo di AIB Camera).

---

## 14. Limiti e responsabilità

Chi adotta o usa l’app deve tenere presenti questi punti:

1. **Strumento di supporto**, non sostituto di procedure ufficiali di documentazione o archiviazione.
2. **Precisione della posizione** dipende dal dispositivo, dal permesso Posizione e dalle condizioni di segnale; l’app non corregge né certifica il GPS.
3. **Data e ora sul timbro** usano sempre Europe/Rome; verificare che l’orologio del telefono sia impostato correttamente.
4. **Album «AIB Camera»** è una copia di comodo: sul dispositivo la copia affidabile è la **galleria in-app**; a livello organizzativo vale ciò che viene esportato o condiviso secondo le procedure interne.
5. **Privacy:** foto, impostazioni e registro restano sul dispositivo; non c’è sincronizzazione cloud gestita dallo sviluppatore. La condivisione verso terzi è scelta dell’operatore.
6. **Aggiornamenti:** questa guida si riferisce alla versione app **1.0.0**; in caso di release successive verificare eventuali differenze su [smnl.dev/aib-camera](https://smnl.dev/aib-camera).

---

## 15. Checklist operativa

1. Concedere almeno il permesso **Fotocamera**; Posizione e Foto quando possibile e utile al turno.
2. Prima del turno, controllare **Aspetto** (campi attivi, testo personalizzato, formato data, dimensione, opacità).
3. Verificare sull’**anteprima live** cosa finirà sul JPEG.
4. In sequenza di scatti, non attendere l’indicatore di elaborazione: si può continuare a fotografare.
5. Se serve la copia in Libreria Foto, verificare che l’album **AIB Camera** si aggiorni (permesso Foto concesso).
6. Per rintracciare uno scatto dopo l’eliminazione della foto, usare il **registro scatti** e l’ID; per collegare CSV di più dispositivi, confrontare l’**ID installazione** in Informazioni con la colonna `install_id`.
7. Prima di condividere all’esterno, assicurarsi che contenuto e destinazione rispettino le regole della propria organizzazione.

---

## 16. Contatti

<table class="doc-meta">
  <tbody>
    <tr><td><strong>Domande sull’app</strong></td><td><a href="mailto:stefano@smnl.dev">stefano@smnl.dev</a></td></tr>
    <tr><td><strong>Informativa privacy</strong></td><td><a href="https://smnl.dev/aib-camera/privacy">smnl.dev/aib-camera/privacy</a></td></tr>
    <tr><td><strong>Questa guida (web)</strong></td><td><a href="https://smnl.dev/aib-camera/guida">smnl.dev/aib-camera/guida</a></td></tr>
    <tr><td><strong>Questa guida (PDF)</strong></td><td><a href="https://smnl.dev/docs/aib-camera-guida-operativa.pdf">smnl.dev/docs/aib-camera-guida-operativa.pdf</a></td></tr>
  </tbody>
</table>

---

_Documento non ufficiale · Versione app di riferimento 1.0.0 · Stampabile · © Stefano Monolo — smnl.dev_
