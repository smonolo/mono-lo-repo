<template>
  <div>
    <div class="h-4 w-full bg-[#008cff]" />
    <main
      class="mx-auto flex max-w-[1600px] flex-col gap-y-10 p-8 lg:gap-y-16 lg:p-20"
    >
      <Details />
      <Experience />
      <div class="flex flex-col gap-x-5 gap-y-6 lg:grid lg:grid-cols-2">
        <Education />
        <Skills />
      </div>
    </main>
    <div class="h-4 w-full bg-[#008cff]" />
  </div>
</template>

<script setup lang="ts">
import Details from '@/components/Details.vue'
import Experience from '@/components/experience/Experience.vue'
import Education from '@/components/education/Education.vue'
import Skills from '@/components/skills/Skills.vue'

useHead({ title: 'Stefano Monolo' })
</script>
