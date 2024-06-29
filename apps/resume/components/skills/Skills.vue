<template>
  <section id="skills">
    <h3 class="text-2xl font-semibold">Skills</h3>
    <div class="mt-4 flex flex-col gap-y-4">
      <SkillsSection v-for="(section, key) in sections" :key :="section" />
    </div>
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
    highlighted: ['TypeScript', 'React', 'Vue'],
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
      'REST',
      'Docker',
      'CI/CD',
      'MySQL',
      'PostgreSQL',
      'Git',
      'Scrum',
    ],
    highlighted: ['Node.js', 'AWS', 'REST', 'Git', 'Scrum'],
  },
]
</script>
