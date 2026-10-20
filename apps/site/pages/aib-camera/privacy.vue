<script setup lang="ts">
const title = 'Privacy Policy — AIB Camera'
const description =
  'Privacy policy for AIB Camera: local-first field photography. Photos, settings, and location stay on your device.'
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
    data: 'Camera image',
    where: 'On device (app storage; optionally Photos/Gallery)',
    why: 'Capture and share stamped field photos',
  },
  {
    data: 'Date & time',
    where: 'Burned into the JPEG stamp (Europe/Rome)',
    why: 'Operational context on the photo',
  },
  {
    data: 'GPS coordinates, accuracy, altitude, heading',
    where: 'On device; burned into stamp / EXIF when available',
    why: 'Location context on the photo',
  },
  {
    data: 'Place label (reverse geocode)',
    where: 'On device; optional network lookup; local cache',
    why: 'Human-readable place on the stamp',
  },
  {
    data: 'Custom stamp text you type',
    where: 'On device (settings)',
    why: 'Squad / shift notes on the photo',
  },
  {
    data: 'App settings',
    where: 'On device (local storage)',
    why: 'Remember stamp preferences',
  },
]
</script>

<template>
  <article class="space-y-10 pb-8 md:px-[50px]">
    <header class="space-y-3">
      <p class="text-sm text-gray-400">
        <NuxtLink
          to="/"
          class="hover:text-sm-blue text-gray-300 hover:underline focus-visible:underline"
        >
          smnl.dev
        </NuxtLink>
        <span aria-hidden="true"> / </span>
        <span>AIB Camera</span>
        <span aria-hidden="true"> / </span>
        <span>Privacy</span>
      </p>
      <h1 class="font-heading text-3xl font-semibold">
        Privacy Policy — AIB Camera
      </h1>
      <p class="text-sm text-gray-400">Last updated: 20 October 2026</p>
    </header>

    <div class="space-y-4 text-gray-300">
      <p>
        AIB Camera is an independent, unofficial mobile app for Antincendio
        Boschivo (AIB) field photography. It is
        <strong class="font-medium text-white">not</strong> affiliated with,
        endorsed by, or built by Protezione Civile, Vigili del Fuoco, Regione
        Lombardia, or any other public institution.
      </p>
      <p>This policy describes how the app handles data on your device.</p>
    </div>

    <section class="space-y-4">
      <h2 class="font-heading text-xl font-semibold text-white">Summary</h2>
      <ul class="list-disc space-y-2 pl-5 text-gray-300">
        <li>
          The app works
          <strong class="font-medium text-white">offline-first</strong>. Photos,
          settings, and the in-app gallery stay on your device.
        </li>
        <li>
          There is
          <strong class="font-medium text-white">no account</strong>,
          <strong class="font-medium text-white">no analytics SDK</strong>, and
          <strong class="font-medium text-white">no cloud sync</strong> operated
          by the developer.
        </li>
        <li>
          <strong class="font-medium text-white">Camera</strong> is required to
          take photos.
        </li>
        <li>
          <strong class="font-medium text-white">Location</strong> is optional
          and used only to stamp coordinates, altitude, heading, and place on
          the photo (and in JPEG metadata when available).
        </li>
        <li>
          <strong class="font-medium text-white">Photos library</strong> access
          is optional and used only to save stamped photos to an album named
          «AIB Camera».
        </li>
        <li>
          Network is used only for
          <strong class="font-medium text-white"
            >optional reverse geocoding</strong
          >
          (turning GPS into a place label) when a connection is available.
          Nearby place labels may be cached on device for offline reuse.
        </li>
      </ul>
    </section>

    <section class="space-y-4">
      <h2 class="font-heading text-xl font-semibold text-white">
        Data the app processes
      </h2>
      <div class="overflow-x-auto rounded-xl border border-gray-800">
        <table class="w-full min-w-[36rem] border-collapse text-left text-sm">
          <thead class="bg-white/5 text-white">
            <tr>
              <th class="px-3 py-2.5 font-medium">Data</th>
              <th class="px-3 py-2.5 font-medium">Where it stays</th>
              <th class="px-3 py-2.5 font-medium">Why</th>
            </tr>
          </thead>
          <tbody class="text-gray-300">
            <tr
              v-for="row in dataRows"
              :key="row.data"
              class="border-t border-gray-800"
            >
              <td class="px-3 py-2.5 align-top text-white">{{ row.data }}</td>
              <td class="px-3 py-2.5 align-top">{{ row.where }}</td>
              <td class="px-3 py-2.5 align-top">{{ row.why }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="text-gray-300">
        The developer does
        <strong class="font-medium text-white">not</strong> operate a backend
        that receives your photos, location, or settings.
      </p>
    </section>

    <section class="space-y-4">
      <h2 class="font-heading text-xl font-semibold text-white">Permissions</h2>
      <ul class="list-disc space-y-2 pl-5 text-gray-300">
        <li>
          <strong class="font-medium text-white">Camera</strong> — required to
          take photos.
        </li>
        <li>
          <strong class="font-medium text-white">Location (when in use)</strong>
          — optional. If denied, you can still shoot; geo lines on the stamp
          show as unavailable.
        </li>
        <li>
          <strong class="font-medium text-white">Photo library / media</strong>
          — optional, write-oriented: save stamped photos to the «AIB Camera»
          album. The in-app gallery remains the source of truth even if this
          permission is denied.
        </li>
      </ul>
    </section>

    <section class="space-y-4">
      <h2 class="font-heading text-xl font-semibold text-white">Sharing</h2>
      <p class="text-gray-300">
        If you use the system share sheet (WhatsApp, Mail, Files, etc.), the
        shared photo leaves the app under the rules of the app or service you
        choose. That is outside this app’s control.
      </p>
    </section>

    <section class="space-y-4">
      <h2 class="font-heading text-xl font-semibold text-white">
        Children’s privacy
      </h2>
      <p class="text-gray-300">
        The app is not directed at children and does not knowingly collect
        personal data from children.
      </p>
    </section>

    <section class="space-y-4">
      <h2 class="font-heading text-xl font-semibold text-white">Changes</h2>
      <p class="text-gray-300">
        If this policy changes in a material way, the “Last updated” date above
        will be revised. Continued use of the app after an update means you
        accept the revised policy.
      </p>
    </section>

    <section class="space-y-4">
      <h2 class="font-heading text-xl font-semibold text-white">Contact</h2>
      <p class="text-gray-300">
        Developer site:
        <a
          href="https://smnl.dev/?ref=aib-camera-privacy"
          class="hover:text-sm-blue text-white hover:underline focus-visible:underline"
        >
          smnl.dev
        </a>
      </p>
      <p class="text-gray-300">
        For privacy questions about AIB Camera, email
        <a
          href="mailto:stefano@smnl.dev"
          class="hover:text-sm-blue text-white hover:underline focus-visible:underline"
        >
          stefano@smnl.dev
        </a>.
      </p>
    </section>
  </article>
</template>
