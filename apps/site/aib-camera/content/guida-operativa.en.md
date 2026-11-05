<!-- Regenera PDF + body HTML (IT + EN): npm run docs:aib-camera-guida (da apps/site) → public/docs/ + assets/aib-camera/ -->

# AIB Camera — Field guide

<table class="doc-meta">
  <tbody>
    <tr><td><strong>Document version</strong></td><td>5 November 2026</td></tr>
    <tr><td><strong>Reference app version</strong></td><td>1.2.0</td></tr>
    <tr><td><strong>Language</strong></td><td>English</td></tr>
    <tr><td><strong>Site</strong></td><td><a href="https://smnl.dev/aib-camera">smnl.dev/aib-camera</a></td></tr>
    <tr><td><strong>Privacy</strong></td><td><a href="https://smnl.dev/aib-camera/privacy">smnl.dev/aib-camera/privacy</a></td></tr>
    <tr><td><strong>Terms</strong></td><td><a href="https://smnl.dev/aib-camera/terms">smnl.dev/aib-camera/terms</a></td></tr>
    <tr><td><strong>PDF</strong></td><td><a href="https://smnl.dev/docs/aib-camera-field-guide.pdf">smnl.dev/docs/aib-camera-field-guide.pdf</a></td></tr>
  </tbody>
</table>

## Contents

1. Purpose of this guide
2. What AIB Camera is
3. What it does and does not do
4. Requirements and install
5. Permissions
6. Main screens
7. Camera
8. Stamp settings
9. Map
10. Gallery (Photos tab)
11. Capture log
12. EXIF metadata
13. Offline use and place
14. Sharing
15. Limits and responsibility
16. Field checklist
17. Contact

---

## 1. Purpose of this guide

This guide explains, in English, how AIB Camera works in the field: permissions, capture, stamping photos, gallery, capture log, and use without a network.

It is for **anyone** who uses the app — citizens, volunteers, or others — to document a wildfire (AIB) or similar situation. It describes the real behavior of version **1.2.0**.

---

## 2. What AIB Camera is

AIB Camera is an **independent, unofficial** mobile app for field photography in wildfire response and similar situations. It is **open to anyone**: not limited to a specific organization or only to professional operators. It lets you:

- take photos with the rear camera;
- print on the JPEG (visual stamp) date, time, position, and — when available — place and other context;
- keep photos in an **in-app gallery**;
- share them with the system share sheet (WhatsApp, Telegram, Mail, …);
- keep a local **capture log** with an identifier for each shot.

Typical uses: document a fire, send the shot to whoever needs it, or read coordinates and other details to emergency responders (e.g. 112).

It is designed to stay usable **even without network, GPS, or Photos library access**: in an emergency it must always be possible to capture and share.

<aside class="callout callout-warn">

**Notice — not institutional**

The app is **not** built, endorsed, or affiliated with Civil Protection, the Fire Service, Regione Lombardia, or other public institutions. It does not use institutional logos, seals, or acronyms. It is an independent tool available to the public; any use in official contexts remains the responsibility of the person using it.

</aside>

---

## 3. What it does and does not do

### What it does

| Feature        | Detail                                                                                                                               |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| Stamp on JPEG  | Date/time (**Europe/Rome** timezone), coordinates, accuracy, altitude, heading, place, and custom text, per Stamp settings           |
| Aimed target   | Distance + compass heading from GPS: estimates coordinates of what you point at; Me ↔ Target toggle; “estimated” label on the stamp |
| Live preview   | The viewfinder shows the same block that will be burned into the photo                                                               |
| Fast capture   | You can take several photos in a row; the stamp is processed in a queue, one photo at a time                                         |
| Map tab        | Me/Aimed point on the map; same stamp info; copy, share text + maps link, open in Maps                                               |
| In-app gallery | Source of truth: photos stay in the app even without Photos library permission                                                       |
| System album   | Copy into the “AIB Camera” album if Photos permission is granted (not guaranteed)                                                    |
| Capture log    | Local append-only list: capture ID, date/time, coordinates; exportable as CSV with `install_id`                                      |
| Install ID     | Stable UUID per install (Settings, tap to copy); matches the CSV column                                                              |
| EXIF           | Best-effort write of identifier, GPS, and machine comment on the JPEG (not always available)                                         |
| Offline        | Capture, stamp, gallery, log, and share do not require a network                                                                     |

### What it does not do

- Does not require an account, login, or cloud sync.
- Does not send photos, location, or the log to developer servers.
- Does not replace institutional archival procedures, reports, or command-and-control systems.
- Does not guarantee GPS accuracy or EXIF completeness: those depend on device, permission, signal, and app build.
- Does not offer a front camera or flash “auto” mode. UI language (Italian by default, or English) is switched in Settings.

---

## 4. Requirements and install

- **iOS** or **Android** phone with a rear camera.
- A **physical device** is recommended (not only a simulator or emulator).
- Orientation: **portrait and landscape**. In landscape the camera moves controls to the side rails (flash/aim/stamp on the left; shutter/zoom on the right).
- UI language: **Italian** (default) or **English**, selectable in **Settings** and stored on the device.
- Stamp timezone: always **Europe/Rome**, independent of the phone timezone.

Up-to-date availability and distribution: [smnl.dev/aib-camera](https://smnl.dev/aib-camera). For organized rollouts (many devices, dedicated builds), contact the developer (see § 17).

---

## 5. Permissions

| Permission                 | Required? | What it is for                                                                                            |
| -------------------------- | --------- | --------------------------------------------------------------------------------------------------------- |
| **Camera**                 | Yes       | Taking photos. Without it the app cannot capture images.                                                  |
| **Location (while using)** | No        | Coordinates, accuracy, altitude, heading, and place on the stamp and Map tab and, when possible, in EXIF. |
| **Photos / media library** | No        | Copy photos into the system “AIB Camera” album and remove them there when deleted in the app.             |

**Without GPS** you can still shoot. The stamp keeps date/time and any custom text; geo lines show _Coordinates unavailable_ / _Place unavailable_.

**Without Photos library** images stay in the **in-app gallery**, which is the source of truth.

Permissions can be reviewed from **Settings** and the phone’s system Settings.

---

## 6. Main screens

A floating glass tab bar (same on iOS and Android) with **icons only**:

1. **Camera** — capture and stamp preview
2. **Map** — effective point (Me or Aimed), the same **Aim** sheet as Camera, stamp-parity info, copy / share, and open in system Maps
3. **Photos** — in-app gallery
4. **Settings** — language (Italiano / English), permissions, version and install ID (tap to copy), capture log, links to this guide and the privacy policy. Stamp settings open from the icon at the top right of the camera.

Names stay available to VoiceOver / TalkBack via accessibility labels.

---

## 7. Camera

### 7.1 Viewfinder and letterbox bands

In **portrait**, the live preview has **black bands** above and below. Top: **flash** on the left, **Aim** next to flash, **stamp configuration** on the right. Bottom: shutter in the center, last-shot preview on the left, **zoom** on the right. In **landscape**, bands move to the sides: secondary controls on the left, shutter / preview / zoom on the right. Beyond 2× the tele button shows the live factor (3×, 4×, …).

### 7.2 Live stamp preview

The viewfinder shows the same block that will be **burned** into the JPEG:

- top: date/time, optional **Aimed target (estimated)** line, coordinates, meta (estimated distance / accuracy / altitude / heading), place — per Stamp settings toggles;
- bottom: custom text, if filled in.

Font, text size, and background opacity follow **Stamp settings** (top-right icon on the camera). The preview matches what will land on the photo (live and burned-in stamps scale from the short side of the frame). **Heading** stays aligned with what the camera is pointing at in landscape as well.

### 7.3 Controls

| Control               | Behavior                                                                                                                                                                   |
| --------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Shutter**           | White circle (bottom band in portrait; right rail in landscape): captures the photo.                                                                                       |
| **Last-shot preview** | Square near the shutter: opens the detail of the last ready photo. A loading indicator appears while processing is still running.                                          |
| **Flash**             | Flashlight icon (top left in portrait; left rail in landscape): **on / off** only (no auto mode).                                                                          |
| **Aim**               | Crosshair icon next to flash, or **press and hold** the viewfinder (~0.5 s): full-width content-height sheet; dismiss with ✕ or tap outside; distance and **Lock target**. |
| **Me / Target**       | With a sticky point active, selector at top center: switches between your GPS position and the aimed target without losing it.                                             |
| **Stamp settings**    | Sliders icon (top right in portrait; left rail in landscape): full-width bottom sheet (full height); dismiss with ✕ or tap outside.                                        |
| **Zoom**              | Buttons near the shutter (e.g. 0.5× / 1× / 2× when available) and **pinch**; beyond 2× the active button shows 3×, 4×, …. **Double-tap** the viewfinder resets zoom to 1×. |
| **Swipe**             | Swipe **left** from the viewfinder to open the gallery.                                                                                                                    |

Only the **rear** camera is used.

### 7.4 Aimed target (distance + heading)

To use the coordinates of what you are **pointing at** (e.g. a hotspot 200 m away), not your position. **Aim** is app-level: the **same sheet** from Camera and Map.

1. Make sure GPS and compass are available.
2. Face the point of interest (the estimate uses compass heading).
3. Open **Aim**: on Camera (chrome or press-and-hold the viewfinder) or on Map (crosshair in the top-right). The same full-width sheet opens (dismiss with ✕ or tap outside).
4. Pick a preset (**100 / 250 / 500 / 1000** m) or type meters, then **Lock target**.
5. Stamp and map show estimated coordinates and the label **Aimed target (estimated)**.
6. Use **Me ↔ Target** on Camera or Map without losing the lock; **Clear target** (in the Aim sheet) removes it.

It is a ground estimate along the compass heading (no altimetry / DEM): useful in the field, not a GNSS fix on the target. Without GPS or heading, lock fails; capture remains available in Me mode.

### 7.5 Capture and background processing

After the shutter:

1. The raw JPEG is acquired immediately; the shutter stays available for more shots.
2. In a queue, **one photo at a time**, the app: applies the stamp, writes EXIF if possible, saves to the in-app gallery, updates the capture log, and tries to copy into the “AIB Camera” album.

You can shoot in sequence without waiting for the stamp to finish: the indicator on the left preview shows that processing is still running.

For the stamp the app prefers the **position already available** on the viewfinder (live GPS or locked aimed target), so the shutter is not blocked waiting for a more precise fix.

---

## 8. Stamp settings

Path: sliders icon on the **camera** — full-width, full-height bottom sheet (**Stamp settings**); dismiss with ✕ or tap outside.

### 8.1 Visible fields (toggles)

- Date and time
- Coordinates
- Place (label from reverse geocode or local cache)
- GPS accuracy
- Altitude
- Heading (compass)

### 8.2 Custom text

Up to **4** free lines (e.g. crew name or shift). Empty by default: no bottom block appears. Suggested in-app placeholder: _Squadra Nord · Turno A_.

### 8.3 Date format

| Option    | Description                          |
| --------- | ------------------------------------ |
| **Full**  | Weekday and extended date            |
| **Short** | Compact date and time (**default**)  |
| **ISO**   | ISO format with Europe/Rome timezone |

### 8.4 Text size

Small / **Medium** (default) / Large.

### 8.5 Stamp background opacity

From **0%** (text only; with shadow for readability) to **100%**, in 25% steps. Default: **0%**.

Changes apply immediately to the live preview and later shots. Photos already taken are not modified.

---

## 9. Map

The **Map** tab shows the same effective geo point used by the stamp:

- map with the system blue user puck (and heading cone when the compass is available) for **Me**; a red pin on the **aimed target**, with the blue puck still on your position;
- **Aim** icon top-right: opens the same sheet as Camera (lock, change distance, clear);
- if an aimed point is active, you can toggle **Me ↔ Aimed** here too, and **drag the red pin** on the map: on release the locked point and distance in meters update from your position;
- the same stamp info lines (date/time, coordinates, meta, place — per Stamp settings toggles);
- **Copy** puts the text on the clipboard; **Share** opens the system sheet with text + a maps link; **Open in Maps** opens Apple Maps / Google Maps.

Without GPS the tab stays usable: a hint appears and there is no marker. Map tiles need a network: **iOS** Apple Maps, **Android** Leaflet with OpenStreetMap (no Google Maps key). Copy and share of the text still work when coordinates are available.

---

## 10. Gallery (Photos tab)

- Photos are grouped by day (Europe/Rome timezone), with headers like _Today_ / _Yesterday_.
- **5-column** grid of square thumbnails.
- Tap a thumbnail to open the **viewer**: browse shots (swipe or the filmstrip below), then **Share** / **Delete**.
- Selection mode: **Select** button or long-press a thumbnail; then multi share or delete.

### In-app gallery vs system album

| Comparison               | In-app gallery                                                    | “AIB Camera” album                               |
| ------------------------ | ----------------------------------------------------------------- | ------------------------------------------------ |
| Role                     | **Source of truth**                                               | Copy in the phone library (if permission allows) |
| Needs Photos permission? | No                                                                | Yes, if you want the copy                        |
| Delete in app            | Removes the local file and, when present, the Photos library copy | —                                                |

<aside class="callout">

**Capture log:** deleting a photo from the gallery does **not** remove the matching capture-log row.

</aside>

---

## 11. Capture log

Access: **Settings → Capture log**.

- Each successful shot adds a row with: **ID** (UUID), date/time, coordinates when available (otherwise _No GPS_).
- The log is **append-only** (entries are not deleted from the app): rows stay on the device after gallery deletions.
- Tap a row to **copy the ID** and compare it with EXIF on a shared JPEG (`ImageUniqueID` / JSON comment), when EXIF was written.
- **Export CSV** (top): builds a local file with columns `install_id`, `id`, `taken_at`, `latitude`, `longitude` and opens the system share sheet (Mail, Files, AirDrop, …). No automatic upload.
- In **Settings**, tap **Version** or **Install ID** to copy the value to the clipboard.
- **Install ID:** UUID created on first launch. Same value as the `install_id` CSV column; reinstalling creates a new one.

The log is **not** sent to any server; CSV export leaves the device only if you choose a share app or service.

---

## 12. EXIF metadata

After stamping the app tries to write on the JPEG, among other fields:

- `Software` / `Artist`
- `ImageUniqueID` (capture UUID)
- capture date/time (Europe/Rome)
- GPS tags, if position is available
- `ImageDescription` and `UserComment` (JSON for machine use)

<aside class="callout callout-warn">

**Caution — EXIF not guaranteed:** if the native module is unavailable on the installed build, **stamped photo, gallery, and log remain valid**. Only metadata write into the file is missing. Do not treat EXIF as automatic legal proof: always also check the visual stamp and the log.

</aside>

---

## 13. Offline use and place

- Capture, stamp, gallery, log, and share **do not require a network**.
- Network is only needed for **reverse geocoding** (coordinates → place label), if Place is enabled and there is connectivity.
- Recent labels stay in a **local cache** and can be reused offline nearby (about **2.5 km** radius).

Without network or GPS, the stamp shows fallback text (_Coordinates unavailable_, _Place unavailable_) without blocking the shutter.

On older or constrained devices, the app **degrades quietly**: if Skia stamp fails the unstamped JPEG stays in the gallery; without native EXIF, photo + gallery + log remain; without Photos permission only the in-app gallery remains; fonts and haptics use system fallbacks. The shutter must not block for these details.

---

## 14. Sharing

From detail or gallery selection the **system share sheet** opens (Messages, Mail, WhatsApp, Files, etc.).

The shared file is the already stamped JPEG, with EXIF if it was written. From the **Map** tab you can instead share or copy **location text** (with a maps link) without sending a photo. After sending, handling and retention depend on the app or service chosen by the sharer (outside AIB Camera’s control).

---

## 15. Limits and responsibility

Anyone using the app should keep these points in mind:

1. **Support tool**, not a substitute for official documentation or archival procedures.
2. **Position accuracy** depends on the device, Location permission, and signal; the app does not correct or certify GPS. **Aimed target** is an estimate (GPS + compass + entered distance), not a fix on the target.
3. **Date and time on the stamp** always use Europe/Rome; check that the phone clock is set correctly.
4. **“AIB Camera” album** is a convenience copy: the reliable copy on the device is the **in-app gallery**.
5. **Privacy:** photos, settings, and log stay on the device; there is no developer-managed cloud sync. Sharing with third parties is the user’s choice.
6. **Updates:** this guide refers to app version **1.2.0**; for later releases check for differences on [smnl.dev/aib-camera](https://smnl.dev/aib-camera).

---

## 16. Field checklist

1. Grant at least **Camera** permission; Location and Photos when useful.
2. Check **Stamp settings** (active fields, custom text, date format, size, opacity) before shooting.
3. Verify on the **live preview** what will land on the JPEG.
   3b. If you need to document a distant point (e.g. a hotspot), use **Aim** (tap or press-and-hold the viewfinder) with an estimated distance and check the **Aimed target (estimated)** label before shooting.
4. In a capture sequence, do not wait for the processing indicator: you can keep photographing.
5. If you need the Photos library copy, confirm the **AIB Camera** album updates (Photos permission granted).
6. To trace a shot after deleting the photo, use the **capture log** and ID; to link CSVs across devices, compare **Install ID** in Settings with the `install_id` column.
7. Before sharing externally, ensure content and destination comply with applicable law.

---

## 17. Contact

<table class="doc-meta">
  <tbody>
    <tr><td><strong>App questions</strong></td><td><a href="mailto:stefano@smnl.dev">stefano@smnl.dev</a></td></tr>
    <tr><td><strong>Privacy policy</strong></td><td><a href="https://smnl.dev/aib-camera/privacy">smnl.dev/aib-camera/privacy</a></td></tr>
    <tr><td><strong>Terms of use</strong></td><td><a href="https://smnl.dev/aib-camera/terms">smnl.dev/aib-camera/terms</a></td></tr>
    <tr><td><strong>This guide (web)</strong></td><td><a href="https://smnl.dev/aib-camera/guide">smnl.dev/aib-camera/guide</a></td></tr>
    <tr><td><strong>This guide (PDF)</strong></td><td><a href="https://smnl.dev/docs/aib-camera-field-guide.pdf">smnl.dev/docs/aib-camera-field-guide.pdf</a></td></tr>
  </tbody>
</table>

---

_Unofficial document · Reference app version 1.2.0 · Printable · © Stefano Monolo — smnl.dev_
