<template>
  <section id="details" class="w-full">
    <h1 class="text-2xl font-semibold md:text-3xl">Stefano Monolo</h1>
    <p class="text-lg font-medium md:text-xl">
      I am a software engineer passionate about the web and its technologies.
    </p>
    <ul class="mt-4 flex flex-wrap items-center gap-x-6 gap-y-2">
      <li v-for="(link, key) in links" :key>
        <a
          :href="link.url"
          :target="link.url.startsWith('https') ? '_blank' : '_self'"
          class="flex items-center gap-x-2 text-lg font-medium underline-offset-2 hover:underline md:text-xl"
        >
          <i :class="`bi bi-${link.icon}`" />
          <span>{{ link.text }}</span>
        </a>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
type Link = {
  icon: string
  url: string
  text: string
}

defineComponent({ name: 'Details' })

const links: Link[] = [
  {
    icon: 'envelope',
    url: 'mailto:stmonolo@gmail.com',
    text: 'stmonolo@gmail.com',
  },
  {
    icon: 'github',
    url: 'https://github.com/smonolo',
    text: 'smonolo',
  },
  {
    icon: 'linkedin',
    url: 'https://www.linkedin.com/in/stemon/',
    text: 'stemon',
  },
  {
    icon: 'link-45deg',
    url: 'https://smnl.dev',
    text: 'smnl.dev',
  },
  {
    icon: 'telephone',
    url: 'tel:+393917422284',
    text: '+39 391 742 2284',
  },
  {
    icon: 'pin-map',
    url: 'https://maps.app.goo.gl/ocGf5twwchq9JD2f8',
    text: 'Milan, Italy',
  },
]
</script>
