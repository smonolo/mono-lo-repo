<template>
  <div class="h-4 w-full bg-[#008cff]" />
  <main class="mx-auto flex max-w-[1600px] flex-col gap-y-16 p-20">
    <Details />
    <Experience />
    <div class="grid grid-cols-2 gap-x-5">
      <Education />
      <Skills />
    </div>
  </main>
  <div class="h-4 w-full bg-[#008cff]" />
</template>

<script setup lang="ts">
import Details from '@/components/Details.vue'
import Experience from '@/components/experience/Experience.vue'
import Education from '@/components/education/Education.vue'
import Skills from '@/components/skills/Skills.vue'

defineComponent({ name: 'App' })
</script>
