<script setup lang="ts">
import dayjs from 'dayjs'
import { filesize } from 'filesize'

type File = {
  fileName: string
  size: number
  birthTime: string
}

type Response = {
  files: File[]
  totalSize: number
  totalFiles: number
  totalPages: number
}

const route = useRoute()

const page = ref(1)
const data = ref<Response | null>(null)

const fetchData = async () => {
  const response = await fetch(`https://cdn.smnl.it/list?page=${page.value}`, {
    headers: {
      authorization: route.query.authKey as string,
    },
  })
  data.value = await response.json()
}

watch(page, fetchData, { immediate: true })

const openFile = (fileName: string) => {
  window.open(`https://cdn.smnl.it/${fileName}`)
}
</script>

<template>
  <div v-if="!!data">
    <table class="w-full">
      <thead>
        <tr>
          <th>File ({{ data.totalFiles }})</th>
          <th>Size ({{ filesize(data.totalSize) }})</th>
          <th>Upload Date</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="file in data.files"
          :key="file.fileName"
          class="group cursor-pointer"
          @click="openFile(file.fileName)"
        >
          <td>{{ file.fileName }}</td>
          <td>{{ filesize(file.size) }}</td>
          <td>{{ dayjs(file.birthTime).format('DD/MM/YYYY HH:mm:ss') }}</td>
        </tr>
      </tbody>
    </table>
    <div class="mt-4 flex w-fit border border-white">
      <button
        v-if="data.totalPages > 1 && page > 1"
        class="cursor-pointer p-3 hover:bg-white hover:text-black"
        @click="page--"
      >
        Previous
      </button>
      <span class="border-x border-white p-3">{{ page }}</span>
      <button
        v-if="data.totalPages > 1 && page < data.totalPages"
        class="cursor-pointer p-3 hover:bg-white hover:text-black"
        @click="page++"
      >
        Next
      </button>
    </div>
  </div>
</template>
