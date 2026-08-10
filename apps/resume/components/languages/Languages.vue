<template>
  <section id="languages">
    <h2 class="text-xl font-semibold md:text-2xl">Languages</h2>
    <div class="mt-4 flex flex-col gap-3 sm:flex-row">
      <div
        v-for="(lang, key) in languages"
        :key="key"
        class="w-full rounded-xl bg-[#f7f7f7] p-6 text-center"
      >
        <p class="text-base font-semibold text-[#111827] md:text-lg">
          {{ lang.name }}
        </p>
        <p class="mt-1 text-sm font-medium text-[#6b7280]">
          {{ lang.level }}
        </p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
type Language = {
  name: string
  level: string
}

defineComponent({ name: 'Languages' })

const languages: Language[] = [
  { name: 'Italian', level: 'Native' },
  { name: 'English', level: 'Fluent' },
]
</script>
