<template>
  <div>
    <div class="h-4 w-full bg-[#008cff]" />
    <main
      class="mx-auto flex max-w-[1600px] flex-col gap-y-10 p-4 sm:p-8 lg:gap-y-16 lg:p-20"
    >
      <Details />
      <Experience />
      <Projects />
      <Skills />
      <div
        class="flex flex-col gap-y-10 lg:grid lg:grid-cols-2 lg:gap-x-8 lg:gap-y-12"
      >
        <Education />
        <Volunteering />
      </div>
      <Languages />
    </main>
    <div class="h-4 w-full bg-[#008cff]" />
  </div>
</template>

<script setup lang="ts">
import Details from '@/components/Details.vue'
import Experience from '@/components/experience/Experience.vue'
import Projects from '@/components/projects/Projects.vue'
import Skills from '@/components/skills/Skills.vue'
import Education from '@/components/education/Education.vue'
import Volunteering from '@/components/volunteering/Volunteering.vue'
import Languages from '@/components/languages/Languages.vue'

useHead({ title: 'Stefano Monolo - Resume' })
</script>
