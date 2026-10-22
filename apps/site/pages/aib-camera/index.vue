<script setup lang="ts">
const title = 'AIB Camera'
const description =
  'Independent field camera for AIB volunteers: stamped photos with date, time, GPS, and place — plus EXIF IDs and an on-device capture log. Offline-first, no account.'
const canonical = 'https://smnl.dev/aib-camera'

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

const features = [
  {
    title: 'Stamp on the JPEG',
    body: 'Date, time (Europe/Rome), coordinates, altitude, heading, and place burned into the photo.',
  },
  {
    title: 'EXIF for machines',
    body: 'After stamping, the app writes Software, ImageUniqueID (UUID), GPS tags, and a JSON UserComment when the native build supports it.',
  },
  {
    title: 'Capture log',
    body: 'Each shot gets a local ID, date, and coords you can open from Settings — even after deleting the photo from the gallery.',
  },
  {
    title: 'Works offline',
    body: 'Capture, stamp, in-app gallery, capture log, and share without network. GPS and place labels are optional.',
  },
  {
    title: 'Local-first',
    body: 'No account, no analytics SDK, no cloud sync. Photos, settings, and the capture log stay on your device.',
  },
  {
    title: 'Emergency-ready',
    body: 'Camera is required; location is not. The shutter stays usable even without a GPS fix.',
  },
]
</script>

<template>
  <article class="space-y-10 pb-8 md:px-[50px]">
    <header class="space-y-4">
      <p class="text-sm text-gray-400">
        <NuxtLink
          to="/"
          class="hover:text-sm-blue text-gray-300 hover:underline focus-visible:underline"
          >smnl.dev</NuxtLink
        >
        <span aria-hidden="true"> / </span>
        <span>AIB Camera</span>
      </p>
      <h1 class="font-heading text-3xl font-semibold">AIB Camera</h1>
      <p class="text-lg text-gray-300">
        Independent field photography for Antincendio Boschivo volunteers —
        stamp date, time, GPS, and place on every shot, with EXIF IDs for later
        checks.
      </p>
    </header>

    <section class="space-y-4 text-gray-300">
      <p>
        AIB Camera is a small mobile app for capturing stamped field photos when
        you need a clear record — on a fire line, a flood response, or any
        volunteer shift where connectivity and permissions are unreliable.
      </p>
      <p>
        It is
        <strong class="font-medium text-white">not</strong> affiliated with,
        endorsed by, or built by Protezione Civile, Vigili del Fuoco, Regione
        Lombardia, or any other public institution. Built by a volunteer for
        volunteer use.
      </p>
    </section>

    <section class="space-y-4">
      <h2 class="font-heading text-xl font-semibold text-white">
        What it does
      </h2>
      <ul class="divide-y divide-gray-800 border-y border-gray-800">
        <li
          v-for="feature in features"
          :key="feature.title"
          class="space-y-1 py-4"
        >
          <p class="font-medium text-white">{{ feature.title }}</p>
          <p class="text-gray-300">{{ feature.body }}</p>
        </li>
      </ul>
    </section>

    <section class="space-y-3 text-gray-300">
      <h2 class="font-heading text-xl font-semibold text-white">Privacy</h2>
      <p>
        Photos, settings, the capture log, and location stay on your device.
        Read the full
        <NuxtLink
          to="/aib-camera/privacy"
          class="hover:text-sm-blue text-white hover:underline focus-visible:underline"
          >privacy policy</NuxtLink
        >.
      </p>
    </section>

    <section class="space-y-3 text-gray-300">
      <h2 class="font-heading text-xl font-semibold text-white">Contact</h2>
      <p>
        Questions about the app:
        <a
          href="mailto:stefano@smnl.dev"
          class="hover:text-sm-blue text-white hover:underline focus-visible:underline"
          >stefano@smnl.dev</a
        >.
      </p>
    </section>
  </article>
</template>
