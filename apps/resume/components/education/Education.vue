<template>
  <section id="education">
    <h2 class="text-xl font-semibold md:text-2xl">Education</h2>
    <ul class="mt-4 flex flex-col gap-y-3">
      <EducationCard v-for="(edu, key) in education" :key :="edu" />
    </ul>
  </section>
</template>

<script setup lang="ts">
import EducationCard, {
  type EducationCardProps,
} from '@/components/education/EducationCard.vue'

defineComponent({ name: 'Education' })

const education: EducationCardProps[] = [
  {
    school: 'Università del Piemonte Orientale',
    degree: 'Bachelor of Technology - Computer Science',
    url: 'https://www.uniupo.it',
  },
  {
    school: 'Liceo Scientifico Claudio Cavalleri',
    degree: 'High School Diploma - Applied Sciences',
    url: 'https://www.liceocavalleri.edu.it',
  },
]
</script>
