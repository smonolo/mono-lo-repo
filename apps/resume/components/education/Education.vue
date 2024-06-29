<template>
  <section id="education">
    <h3 class="text-2xl font-semibold">Education</h3>
    <div class="mt-4 flex flex-col gap-y-3">
      <EducationCard v-for="(edu, key) in education" :key :="edu" />
    </div>
    <div class="mt-3 grid grid-cols-2 gap-x-3">
      <div
        v-for="(lang, key) in languages"
        :key
        class="w-full rounded-xl bg-[#f7f7f7] px-6 py-2 text-center"
      >
        <span class="font-medium">{{ lang }}</span>
      </div>
    </div>
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
  },
  {
    school: 'Liceo Scientifico Claudio Cavalleri',
    degree: 'High School Diploma - Applied Sciences',
  },
]

const languages: string[] = ['Italian', 'English']
</script>
