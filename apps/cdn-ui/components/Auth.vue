<script setup lang="ts">
type Emits = {
  login: []
}

defineComponent({ name: 'Auth' })

const emit = defineEmits<Emits>()

const authKey = ref()

const submit = () => {
  localStorage.setItem('authKey', authKey.value)
  emit('login')
}
</script>

<template>
  <div>
    <h1 class="text-3xl font-semibold">Hi, sign in now.</h1>
    <input
      v-model="authKey"
      class="mt-4 block w-96 max-w-full border border-white p-3 text-white outline-none"
    />
    <button
      class="mt-4 cursor-pointer border border-white p-3 text-white transition-colors hover:bg-white hover:text-black"
      @click="submit"
    >
      It's me
    </button>
  </div>
</template>
