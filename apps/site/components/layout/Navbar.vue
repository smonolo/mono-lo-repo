<script setup lang="ts">
import dayjs from 'dayjs'
import timezone from 'dayjs/plugin/timezone'
import utc from 'dayjs/plugin/utc'

dayjs.extend(utc)
dayjs.extend(timezone)

const now = ref(dayjs().tz('Europe/Rome'))

let interval: NodeJS.Timeout

onMounted(() => {
  interval = setInterval(() => (now.value = dayjs().tz('Europe/Rome')), 1000)
})

onUnmounted(() => {
  clearInterval(interval)
})

const socials = [
  { name: 'github', icon: 'bi:github', url: 'https://github.com/smonolo' },
  {
    name: 'linkedin',
    icon: 'bi:linkedin',
    url: 'https://www.linkedin.com/in/stemon/',
  },
  {
    name: 'discord',
    icon: 'bi:discord',
    url: 'https://discord.com/users/191598787410526208',
  },
]
</script>

<template>
  <nav>
    <div
      class="flex max-w-full items-center justify-between px-4 py-8 md:mx-auto md:max-w-[600px] md:px-0"
    >
      <NuxtLink to="/">
        <div class="bg-sm-blue h-7 w-7 rounded" />
      </NuxtLink>
      <div class="flex items-center gap-x-3">
        <div class="flex w-fit gap-x-2">
          <a
            v-for="social in socials"
            :key="social.name"
            :href="social.url"
            target="_blank"
            class="leading-0 text-gray-500 transition-colors hover:text-white"
          >
            <Icon :name="social.icon" />
          </a>
        </div>
        <div class="h-5 w-px bg-gray-800" />
        <span class="font-mono text-sm text-gray-500">
          {{ now.format('HH:mm:ss') }}
        </span>
      </div>
    </div>
  </nav>
</template>
