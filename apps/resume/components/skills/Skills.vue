<template>
  <section id="skills">
    <div class="flex w-full items-center justify-between gap-x-2">
      <h2 class="text-xl font-semibold md:text-2xl">Skills</h2>
      <p class="flex items-center gap-x-1 text-sm font-medium">
        <i class="bi bi-star-fill text-[#008cff]" /> Preferred
      </p>
    </div>
    <ul class="mt-4 flex flex-col gap-y-4">
      <SkillsSection v-for="(section, key) in sections" :key :="section" />
    </ul>
  </section>
</template>

<script setup lang="ts">
import SkillsSection, {
  type SkillsSectionProps,
} from '@/components/skills/SkillsSection.vue'

defineComponent({ name: 'Skills' })

const sections: SkillsSectionProps[] = [
  {
    title: 'Frontend',
    skills: [
      'JavaScript',
      'TypeScript',
      'React',
      'Next.js',
      'Vue',
      'Nuxt',
      'Electron',
      'Express',
      'HTML',
      'CSS',
      'Redux',
      'Jest',
      'Vite',
    ],
    highlighted: ['TypeScript', 'React', 'Next.js', 'Vue', 'Nuxt'],
  },
  {
    title: 'Other',
    skills: [
      'Node.js',
      'Java',
      'Python',
      'Go',
      'Rust',
      'AWS',
      'Google Cloud',
      'Linux',
      'gRPC',
      'Docker',
      'CI/CD',
      'MySQL',
      'PostgreSQL',
      'Git',
    ],
    highlighted: ['Node.js', 'Go', 'Linux', 'Docker', 'Git'],
  },
]
</script>
