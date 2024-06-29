<template>
  <div class="w-full rounded-xl bg-[#f7f7f7] p-6">
    <div class="flex w-full items-start justify-between">
      <div class="w-fit">
        <h5 class="text-xl font-semibold">{{ position }} at {{ company }}</h5>
        <span class="font-medium">{{ period }} · {{ location }}</span>
      </div>
      <div class="flex w-fit items-center gap-x-2">
        <span
          v-for="(skill, key) in skills"
          :key
          class="rounded-md bg-[#dddddd] px-2.5 py-0.5 text-sm font-semibold"
        >
          {{ skill }}
        </span>
      </div>
    </div>
    <ul class="mt-4">
      <li
        v-for="(achievement, key) in achievements"
        :key="key"
        class="flex items-center gap-x-2"
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
