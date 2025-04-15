<script setup lang="ts">
import Navbar from '~/components/layout/Navbar.vue'
</script>

<template>
  <div>
    <Navbar />
    <div class="max-w-full px-4 pt-36 md:mx-auto md:max-w-[600px] md:px-0">
      <main>
        <slot />
      </main>
    </div>
  </div>
</template>
