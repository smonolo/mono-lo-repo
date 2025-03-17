<script setup lang="ts">
import FilesTable from '~/components/files/Table.vue'
import Auth from '~/components/Auth.vue'

const logged = ref(false)

onMounted(() => {
  logged.value = !!localStorage.getItem('authKey')
})
</script>

<template>
  <Auth v-if="!logged" @login="logged = true" />
  <FilesTable v-else @logout="logged = false" />
</template>
