<script setup lang="ts">
import moment from 'moment'

defineComponent({ name: 'ScreenClock' })

const currentTime = ref(new Date().getTime())

let interval: NodeJS.Timeout

onMounted(() => {
  interval = setInterval(() => {
    currentTime.value = new Date().getTime()
  }, 1000)
})

onUnmounted(() => {
  clearInterval(interval)
})
</script>

<template>
  <div>
    <ClientOnly>
      <div
        class="w-full whitespace-nowrap border border-slate-950 px-1.5 py-0.5 text-center font-bold tracking-wide dark:border-slate-100"
      >
        <p>{{ moment(currentTime).format('dd, DD.MM.yyyy') }}</p>
        <p>{{ moment(currentTime).format('HH:mm:ss') }}</p>
      </div>
    </ClientOnly>
  </div>
</template>
