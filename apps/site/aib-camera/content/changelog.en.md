<!-- Regenerate body HTML: npm run docs:aib-camera-changelog (from apps/site) → assets/changelog.en.body.html -->

# Changelog — AIB Camera

## 1.2.0 — 5 November 2026

- **Map tab**: live map of the effective point (Me or Aimed), with stamp-parity location lines, copy/share, and open in the system Maps app.
- **iOS** uses Apple Maps; **Android** uses Leaflet with OpenStreetMap/CARTO tiles (no Google Maps).
- Aimed red pin is **draggable** on the map; on drop, meters and bearing update from live GPS.
- Same **Aim** sheet from Camera and Map.
- **Photo viewer** with swipe between shots and a bottom filmstrip (share/delete the current shot).

## 1.1.0 — 3 November 2026

- **Device rotation** support (portrait and landscape).
- Adaptive letterbox viewfinder: in landscape, controls move to the **side rails**.
- Compass heading on the stamp is **corrected** for screen orientation so it tracks the camera in landscape.
- Fix for the `expo-screen-orientation` dependency in native builds.

## 1.0.2 — 3 November 2026

- Shared floating **glass tab bar** on iOS and Android (`expo-blur`), replacing iOS-only native liquid glass.
- **Solar Linear** icons unified on both platforms.
- Updated Android adaptive icon.

## 1.0.1 — 29 October 2026

- **Aimed target**: distance (presets or custom meters) + GPS + compass to estimate coordinates of what you point at; stamp label “Aimed target (estimated)”.
- **English** UI selectable in Settings (Italian remains the default).
- **Terms of use** published on the site.
- Share **multiple images** from the gallery.
- GPS/compass/place improvements: heading accuracy gate, circular smoothing, stickier place labels.
- PolyForm Noncommercial license, privacy/store URLs, and local build/submit flow.

## 1.0.0 — 23 October 2026

First public release.

- Field camera with letterboxed viewfinder and **live stamp** (date/time Europe/Rome, GPS, altitude, heading, place, custom text).
- Stamp **burned into** the JPEG (Skia) and best-effort EXIF (capture ID, GPS, JSON UserComment).
- **In-app gallery** as source of truth; optional Photos album “AIB Camera”.
- On-device **capture log** (UUID) with CSV export; install ID in Settings.
- Stamp field toggles in Settings; pinch-to-zoom on the viewfinder.
- Offline-first, no account or cloud sync; shutter works without GPS.
- Expo SDK 58, brand icon and splash.
