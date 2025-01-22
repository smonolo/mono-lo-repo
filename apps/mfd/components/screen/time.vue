<script setup lang="ts">
import moment from 'moment'

defineComponent({ name: 'ScreenTime' })
</script>

<template>
  <div>
    <ClientOnly>
      <div
        class="whitespace-nowrap border border-slate-100 px-1.5 py-0.5 text-center font-bold tracking-wide"
      >
        {{ moment().format('dd, DD.MM.yyyy') }}
      </div>
      <div
        class="whitespace-nowrap border-x border-b border-slate-100 px-1.5 py-0.5 text-center font-bold tracking-wide"
      >
        {{ moment().format('HH:mm:ss') }}
      </div>
    </ClientOnly>
  </div>
</template>
