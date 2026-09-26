<script setup lang="ts">
const socials = [
  {
    name: 'GitHub',
    icon: 'bi:github',
    url: 'https://github.com/smonolo?ref=smnl.dev',
  },
  {
    name: 'LinkedIn',
    icon: 'bi:linkedin',
    url: 'https://www.linkedin.com/in/stemon/?ref=smnl.dev',
  },
]
</script>

<template>
  <footer
    class="flex w-full max-w-full items-center justify-between gap-4 px-4 py-8 md:mx-auto md:max-w-[600px] md:px-0"
  >
    <div class="flex items-center gap-x-3">
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
    <p class="text-sm text-gray-300 underline hover:text-white">
      <a
        href="https://git.new/monorepo?ref=smnl.dev"
        target="_blank"
        rel="noopener noreferrer"
      >
        GitHub
      </a>
    </p>
  </footer>
</template>
