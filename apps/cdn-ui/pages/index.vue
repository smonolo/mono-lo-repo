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

const updatePage = (newPage: number) => {
  if (newPage >= 1 && newPage <= (data.value?.totalPages || 1)) {
    page.value = newPage
  }
}

const openFile = (fileName: string) => {
  window.open(`https://cdn.smnl.it/${fileName}`)
}
</script>

<template>
  <div v-if="!!data">
    <table class="w-full">
      <thead>
        <tr>
          <th
            class="border border-white p-3 text-left font-normal transition-colors group-hover:bg-[#008cff]"
          >
            File ({{ data.totalFiles }})
          </th>
          <th
            class="border border-white p-3 text-left font-normal transition-colors group-hover:bg-[#008cff]"
          >
            Size ({{ filesize(data.totalSize) }})
          </th>
          <th
            class="border border-white p-3 text-left font-normal transition-colors group-hover:bg-[#008cff]"
          >
            Upload Date
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="file in data.files"
          :key="file.fileName"
          class="group cursor-pointer"
          @click="openFile(file.fileName)"
        >
          <td
            class="border border-white p-3 text-left font-normal transition-colors group-hover:bg-[#008cff]"
          >
            {{ file.fileName }}
          </td>
          <td
            class="border border-white p-3 text-left font-normal transition-colors group-hover:bg-[#008cff]"
          >
            {{ filesize(file.size) }}
          </td>
          <td
            class="border border-white p-3 text-left font-normal transition-colors group-hover:bg-[#008cff]"
          >
            {{ dayjs(file.birthTime).format('DD/MM/YYYY HH:mm:ss') }}
          </td>
        </tr>
      </tbody>
    </table>
    <div class="mt-4 flex w-fit border border-white">
      <button
        class="cursor-pointer p-3 hover:bg-white hover:text-black"
        @click="updatePage(page - 1)"
      >
        Previous
      </button>
      <span class="border-x border-white p-3">{{ page }}</span>
      <button
        class="cursor-pointer p-3 hover:bg-white hover:text-black"
        @click="updatePage(page + 1)"
      >
        Next
      </button>
    </div>
  </div>
</template>
