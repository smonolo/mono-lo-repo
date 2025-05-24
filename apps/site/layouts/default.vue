<script setup lang="ts">
import Navbar from '~/components/layout/Navbar.vue'
import Footer from '~/components/layout/Footer.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col justify-between gap-y-36">
    <div class="space-y-12">
      <Navbar />
      <div class="max-w-full px-4 md:mx-auto md:max-w-[700px] md:px-0">
        <main>
          <slot />
        </main>
      </div>
    </div>
    <Footer />
  </div>
</template>
