<template>
  <div class="w-full rounded-xl bg-[#f7f7f7] p-6">
    <div
      class="flex w-full flex-col items-start justify-between gap-2 lg:flex-row"
    >
      <div class="w-fit">
        <h3 class="text-lg font-semibold md:text-xl">
          {{ position }} at
          <a
            :href="url"
            target="_blank"
            class="underline-offset-2 hover:underline"
          >
            {{ company }}
          </a>
        </h3>
        <p class="text-sm font-medium md:text-base">
          {{ period }} · {{ location }}
        </p>
      </div>
      <ul class="flex w-fit flex-wrap items-center gap-1 md:gap-x-2">
        <li
          v-for="(skill, key) in skills"
          :key
          class="rounded-md bg-[#dddddd] px-2.5 py-0.5 text-xs font-medium md:text-sm"
        >
          {{ skill }}
        </li>
      </ul>
    </div>
    <ul class="mt-4">
      <li
        v-for="(achievement, key) in achievements"
        :key="key"
        class="flex items-center gap-x-2 text-sm md:text-base"
      >
        <span class="text-[#333333]">•</span>
        <p class="font-medium">{{ achievement }}</p>
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
