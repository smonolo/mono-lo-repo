<script setup lang="ts">
const socials = [
  { name: 'GitHub', icon: 'bi:github', url: 'https://github.com/smonolo' },
  {
    name: 'LinkedIn',
    icon: 'bi:linkedin',
    url: 'https://www.linkedin.com/in/stemon/',
  },
]
</script>

<template>
  <nav>
    <div
      class="flex max-w-full items-center justify-between px-4 py-8 md:mx-auto md:max-w-[600px] md:px-0"
    >
      <NuxtLink
        to="/"
        aria-label="Home"
        class="flex h-11 w-11 items-center justify-center"
      >
        <div class="bg-sm-blue h-7 w-7 rounded" />
      </NuxtLink>
      <div class="flex items-center gap-x-2">
        <a
          v-for="social in socials"
          :key="social.name"
          :href="social.url"
          target="_blank"
          rel="noopener noreferrer"
          :aria-label="social.name"
          class="leading-0 text-gray-500 transition-colors hover:text-white"
        >
          <Icon :name="social.icon" />
        </a>
      </div>
    </div>
  </nav>
</template>
