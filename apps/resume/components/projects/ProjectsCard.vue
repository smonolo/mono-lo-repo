<template>
  <div class="w-full rounded-xl bg-[#f7f7f7] p-6">
    <h3 class="text-lg font-semibold text-[#111827] md:text-xl">{{ title }}</h3>
    <p
      class="mt-2 text-sm font-medium leading-relaxed text-[#4b5563] md:text-base"
    >
      {{ description }}
    </p>
  </div>
</template>

<script setup lang="ts">
export type ProjectCardProps = {
  title: string
  description: string
}

defineComponent({ name: 'ProjectsCard' })

defineProps<ProjectCardProps>()
</script>
