<template>
  <li>
    <h3 class="text-sm font-medium uppercase md:text-base">{{ title }}</h3>
    <ul class="mt-2 flex flex-wrap gap-2">
      <li
        v-for="(skill, key) in skills"
        :key
        class="flex w-fit items-center gap-x-1 rounded-lg px-3 py-1.5 text-sm font-medium md:text-base"
        :class="
          highlighted.includes(skill)
            ? 'bg-[#008cff] text-white'
            : 'bg-[#dddddd]'
        "
      >
        <i v-if="highlighted.includes(skill)" class="bi bi-star-fill text-xs" />
        {{ skill }}
      </li>
    </ul>
  </li>
</template>

<script setup lang="ts">
export type SkillsSectionProps = {
  title: string
  skills: string[]
  highlighted: string[]
}

defineComponent({ name: 'SkillsSection' })

defineProps<SkillsSectionProps>()
</script>
