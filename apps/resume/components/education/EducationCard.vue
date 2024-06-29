<template>
  <div class="w-full rounded-xl bg-[#f7f7f7] p-6">
    <h5 class="text-xl font-semibold">{{ school }}</h5>
    <span class="font-medium">{{ degree }}</span>
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
