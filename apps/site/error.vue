<script setup lang="ts">
import type { NuxtError } from '#app'

type Props = {
  error: NuxtError
}

const props = defineProps<Props>()

useHead({ title: `${props.error.statusCode} | Stefano Monolo` })

const showErrorDetails = ref(false)
</script>

<template>
  <div>
    <NuxtLayout>
      <div class="space-y-8 border-t border-gray-800 pt-16">
        <div class="space-y-4">
          <a
            :href="`https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status/${error.statusCode}`"
            target="_blank"
            class="font-heading block w-fit text-6xl font-semibold hover:underline"
          >
            {{ error.statusCode }}
          </a>
          <p class="text-gray-300">
            It looks like something went wrong. If the problem is unexpected,
            please reach out to me so I can take a look. Otherwise, you can try
            refreshing the page or going back to the previous page.
          </p>
          <button
            class="text-sm-blue cursor-pointer font-medium hover:underline"
            @click="showErrorDetails = !showErrorDetails"
          >
            {{ showErrorDetails ? 'Hide' : 'Show' }} error details
          </button>
          <p
            v-if="showErrorDetails"
            class="rounded-lg border border-gray-700 bg-gray-800 p-4 font-mono text-sm text-gray-300"
          >
            {{ error.message }}
          </p>
        </div>
        <NuxtLink to="/">
          <button
            class="bg-sm-blue text-sm-black hover:bg-sm-blue/90 w-full cursor-pointer rounded-lg px-3 py-2 font-medium transition-colors"
          >
            Got it, take me back home
          </button>
        </NuxtLink>
      </div>
    </NuxtLayout>
  </div>
</template>
