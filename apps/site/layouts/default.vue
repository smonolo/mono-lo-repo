<script setup lang="ts">
import Footer from '~/components/layout/Footer.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col justify-between gap-y-36">
    <div class="max-w-full px-4 pt-12 md:mx-auto md:max-w-[700px] md:px-0">
      <main>
        <slot />
      </main>
    </div>
    <Footer />
  </div>
</template>
