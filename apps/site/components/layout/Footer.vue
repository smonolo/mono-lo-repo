<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer
    class="flex w-full max-w-full flex-col items-start gap-2 px-4 py-8 sm:flex-row sm:items-center sm:justify-between md:mx-auto md:max-w-[600px] md:px-0"
  >
    <p class="text-sm text-gray-300">© {{ currentYear }} Stefano Monolo.</p>
    <p class="text-sm text-gray-300 underline hover:text-white">
      <a
        href="https://git.new/monorepo"
        target="_blank"
        rel="noopener noreferrer"
      >
        Source code on GitHub
      </a>
    </p>
  </footer>
</template>
