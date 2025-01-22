<script setup lang="ts">
import { useScreenStore } from '~/stores/screen'

defineComponent({ name: 'ScreenFunctions' })

const screenStore = useScreenStore()
</script>

<template>
  <div>
    <div
      v-for="(func, key) in screenStore.functionsOnDisplay"
      :key
      class="bg-yellow-300 p-1 text-center text-black"
    >
      <div>{{ func }}</div>
    </div>
  </div>
</template>
