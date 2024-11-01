<template>
  <li class="w-full rounded-xl bg-[#f7f7f7] p-6">
    <a
      :href="url"
      target="_blank"
      class="text-lg font-semibold underline-offset-2 hover:underline md:text-xl"
    >
      {{ school }}
    </a>
    <p class="text-sm font-medium md:text-base">{{ degree }}</p>
  </li>
</template>

<script setup lang="ts">
export type EducationCardProps = {
  school: string
  degree: string
  url: string
}

defineComponent({ name: 'EducationCard' })

defineProps<EducationCardProps>()
</script>
