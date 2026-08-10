<template>
  <section id="skills">
    <div class="flex w-full items-center justify-between gap-x-2">
      <h2 class="text-xl font-semibold md:text-2xl">Skills</h2>
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
    title: 'Backend',
    skills: ['Node.js', 'Go', 'Python', 'Java', 'Kafka'],
  },
  {
    title: 'AI & LLMs',
    skills: [
      'LLMs',
      'AI Agents',
      'Prompt Engineering',
      'Agentic Workflows',
      'OpenAI API',
    ],
  },
  {
    title: 'Frontend',
    skills: [
      'TypeScript',
      'Vue',
      'Nuxt',
      'React',
      'React Native',
      'Next.js',
      'Vitest',
      'Playwright',
      'HTML',
      'CSS',
    ],
  },
  {
    title: 'Database',
    skills: ['PostgreSQL', 'MySQL', 'MongoDB', 'Redis'],
  },
  {
    title: 'DevOps',
    skills: ['Docker', 'AWS', 'Google Cloud', 'CI/CD', 'Git', 'Linux'],
  },
]
</script>
