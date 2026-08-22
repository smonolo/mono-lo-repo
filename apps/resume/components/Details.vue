<template>
  <section id="details" class="w-full">
    <h1 class="text-2xl font-semibold text-[#111827] md:text-3xl">
      Stefano Monolo
    </h1>
    <p class="mt-1 text-lg font-medium text-[#4b5563] md:text-xl">
      I am a software engineer passionate about the web and its technologies.
    </p>
    <ul class="mt-4 flex flex-wrap items-center gap-x-6 gap-y-2">
      <li v-for="(link, key) in links" :key>
        <a
          v-if="link.url"
          :href="link.url"
          :target="link.url.startsWith('https') ? '_blank' : '_self'"
          class="flex items-center gap-x-2 text-lg font-medium text-[#111827] underline-offset-2 hover:text-[#008cff] hover:underline md:text-xl"
        >
          <i :class="`bi bi-${link.icon}`" />
          <span>{{ link.text }}</span>
        </a>
        <div
          v-else
          class="flex items-center gap-x-2 text-lg font-medium text-[#111827] md:text-xl"
        >
          <i :class="`bi bi-${link.icon}`" />
          <span>{{ link.text }}</span>
        </div>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
type Link = {
  icon: string
  url?: string
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
    icon: 'pin-map',
    text: 'Milan, Italy',
  },
]
</script>
