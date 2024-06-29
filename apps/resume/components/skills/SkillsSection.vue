<template>
  <div>
    <h5 class="font-medium uppercase">{{ title }}</h5>
    <div class="mt-2 flex flex-wrap gap-2">
      <span
        v-for="(skill, key) in skills"
        :key
        class="w-fit rounded-lg px-3 py-1.5 font-medium"
        :class="
          highlighted.includes(skill)
            ? 'bg-[#008cff] text-white'
            : 'bg-[#dddddd]'
        "
      >
        {{ skill }}
      </span>
    </div>
  </div>
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
