<template>
  <li>
    <h3 class="text-sm font-medium uppercase text-[#111827] md:text-base">
      {{ title }}
    </h3>
    <ul class="mt-2 flex flex-wrap gap-2">
      <li
        v-for="(skill, key) in skills"
        :key
        class="flex w-fit items-center rounded-lg bg-[#f7f7f7] px-3 py-1.5 text-sm font-medium text-[#111827] md:text-base"
      >
        {{ skill }}
      </li>
    </ul>
  </li>
</template>

<script setup lang="ts">
export type SkillsSectionProps = {
  title: string
  skills: string[]
}

defineComponent({ name: 'SkillsSection' })

defineProps<SkillsSectionProps>()
</script>
