<template>
  <div class="w-full rounded-xl bg-[#f7f7f7] p-6">
    <h5 class="text-lg font-semibold md:text-xl">{{ school }}</h5>
    <span class="text-sm font-medium md:text-base">{{ degree }}</span>
  </div>
</template>

<script setup lang="ts">
export type EducationCardProps = {
  school: string
  degree: string
}

defineComponent({ name: 'EducationCard' })

defineProps<EducationCardProps>()
</script>
