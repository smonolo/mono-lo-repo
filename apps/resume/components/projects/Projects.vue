<template>
  <section id="projects">
    <div class="flex items-center gap-x-3">
      <h2 class="text-xl font-semibold md:text-2xl">Projects</h2>
    </div>
    <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
      <ProjectsCard v-for="(project, key) in projects" :key :="project" />
    </div>
  </section>
</template>

<script setup lang="ts">
import ProjectsCard, {
  type ProjectCardProps,
} from '@/components/projects/ProjectsCard.vue'

defineComponent({ name: 'Projects' })

const projects: ProjectCardProps[] = [
  {
    title: 'Open Source',
    description:
      'Contributed to several open source projects, developer tools, and AI-assisted workflows, including Italian government institutional websites.',
  },
  {
    title: 'MXC',
    description:
      'Built e-commerce and feedback sites for a popular FiveM resources creator.',
  },
  {
    title: 'Settings Hunt',
    description:
      'Built a game settings sharing platform for uploading and sharing config files and gaming guides.',
  },
  {
    title: 'Breeng',
    description:
      'Led a cross-functional team of 10 creating a multi-platform job seeking application, managing engineering strategy and weekly releases.',
  },
]
</script>
