<script setup lang="ts">
defineComponent({ name: 'MobileDisabled' })
</script>

<template>
  <div
    class="absolute z-50 flex h-screen w-full items-center justify-center bg-black text-white xl:hidden"
  >
    <div class="flex w-fit flex-col gap-y-2">
      <h1 class="text-2xl font-bold">Disabled on mobile</h1>
      <p>
        I'm sorry, but this site is disable for devices with smaller displays.
        Please use a tablet, laptop or desktop to view.
      </p>
    </div>
  </div>
</template>
