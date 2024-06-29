<template>
  <div class="w-full rounded-xl bg-[#f7f7f7] p-6">
    <div
      class="flex w-full flex-col items-start justify-between gap-2 lg:flex-row"
    >
      <div class="w-fit">
        <h5 class="text-lg font-semibold md:text-xl">
          {{ position }} at {{ company }}
        </h5>
        <span class="text-sm font-medium md:text-base"
          >{{ period }} · {{ location }}</span
        >
      </div>
      <div class="flex w-fit items-center gap-x-2">
        <span
          v-for="(skill, key) in skills"
          :key
          class="rounded-md bg-[#dddddd] px-2.5 py-0.5 text-xs font-semibold md:text-sm"
        >
          {{ skill }}
        </span>
      </div>
    </div>
    <ul class="mt-4">
      <li
        v-for="(achievement, key) in achievements"
        :key="key"
        class="flex items-center gap-x-2 text-sm md:text-base"
      >
        <span class="text-[#333333]">•</span>
        <span class="font-medium">{{ achievement }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
export type ExperienceCardProps = {
  position: string
  company: string
  period: string
  location: string
  achievements: string[]
  skills: string[]
}

defineComponent({ name: 'ExperienceCard' })

defineProps<ExperienceCardProps>()
</script>
