<!-- Regenera body HTML: npm run docs:aib-camera-changelog (da apps/site) → assets/changelog.body.html -->

# Novità — AIB Camera

## 1.2.0 — 5 novembre 2026

- **Scheda Mappa**: mappa del punto effettivo (Qui o Mirato), con linee di posizione allineate al timbro, copia/condivisione e apertura nell’app Mappe di sistema.
- **iOS** usa Apple Maps; **Android** usa Leaflet con tile OpenStreetMap/CARTO (senza Google Maps).
- Pin rosso del punto mirato **trascinabile** sulla mappa: al rilascio aggiorna metri e direzione dal GPS live.
- Stesso foglio **Punta** da Fotocamera e da Mappa.
- **Visualizzatore foto** con scorrimento tra gli scatti e filmstrip in basso (condivisione ed eliminazione dello scatto corrente).

## 1.1.0 — 3 novembre 2026

- Supporto **rotazione** del dispositivo (portrait e landscape).
- Mirino con bande nere adattive: in orizzontale i controlli passano alle **rail laterali**.
- Direzione bussola sul timbro **corretta** in base all’orientamento dello schermo, così segue la fotocamera anche in landscape.
- Fix della dipendenza `expo-screen-orientation` per le build native.

## 1.0.2 — 3 novembre 2026

- Tab bar flottante **condivisa** su iOS e Android (vetro con `expo-blur`), al posto del liquid glass nativo solo iOS.
- Icone **Solar Linear** unificate su entrambe le piattaforme.
- Icona adaptive Android aggiornata.

## 1.0.1 — 29 ottobre 2026

- **Punto mirato**: distanza (preset o metri liberi) + GPS + bussola per stimare le coordinate di ciò a cui punti; etichetta «Punto mirato (stimato)» sul timbro.
- Interfaccia in **inglese** selezionabile da Impostazioni (default italiano).
- **Termini di utilizzo** pubblicati sul sito.
- Condivisione di **più immagini** dalla galleria.
- Migliorie a GPS/bussola/luogo: filtro accuratezza heading, smoothing circolare, etichetta di luogo più stabile.
- Licenza PolyForm Noncommercial, URL privacy/store e flusso di build/submit locali.

## 1.0.0 — 23 ottobre 2026

Prima versione pubblica.

- Fotocamera di campo con mirino letterbox e **timbro live** (data/ora Europe/Rome, GPS, altitudine, direzione, luogo, testo personalizzato).
- Timbro **bruciato** sul JPEG (Skia) e scrittura EXIF best-effort (ID scatto, GPS, UserComment JSON).
- **Galleria in-app** come fonte di verità; album Foto «AIB Camera» opzionale.
- **Registro scatti** locale (UUID) con esportazione CSV; ID installazione in Impostazioni.
- Impostazioni per i campi del timbro; pinch-to-zoom sul mirino.
- Offline-first, senza account né sync cloud; scatto utilizzabile anche senza GPS.
- Expo SDK 58, icona e splash di brand.
