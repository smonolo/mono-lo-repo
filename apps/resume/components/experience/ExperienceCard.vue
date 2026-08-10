<template>
  <div class="w-full rounded-xl bg-[#f7f7f7] p-6">
    <div
      class="flex w-full flex-col items-start justify-between gap-2 lg:flex-row"
    >
      <div class="w-fit">
        <h3 class="text-lg font-semibold text-[#111827] md:text-xl">
          {{ position }} at
          <a
            :href="url"
            target="_blank"
            class="underline-offset-2 hover:underline"
          >
            {{ company }}
          </a>
        </h3>
        <p class="mt-0.5 text-sm font-medium text-[#6b7280] md:text-base">
          {{ period }} · {{ location }}
        </p>
      </div>
      <ul class="flex w-fit flex-wrap items-center gap-1 md:gap-x-2">
        <li
          v-for="(skill, key) in skills"
          :key
          class="rounded-md bg-[#dddddd] px-2.5 py-0.5 text-xs font-medium text-[#111827] md:text-sm"
        >
          {{ skill }}
        </li>
      </ul>
    </div>
    <ul v-if="!!achievements.length" class="mt-3 flex flex-col gap-y-0.5">
      <li
        v-for="(achievement, key) in achievements"
        :key="key"
        class="flex items-start gap-x-2 text-sm text-[#4b5563] md:text-base"
      >
        <span class="text-[#6b7280]">•</span>
        <p class="font-medium leading-snug">{{ achievement }}</p>
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
  url: string
}

defineComponent({ name: 'ExperienceCard' })

defineProps<ExperienceCardProps>()
</script>
