<script setup lang="ts">
import dayjs from 'dayjs'
import { filesize } from 'filesize'
import FilesPagination from '~/components/files/Pagination.vue'
import { useFiles } from '~/composables/useFiles'

type Emits = {
  logout: []
}

defineComponent({ name: 'FilesTable' })

const emit = defineEmits<Emits>()

const page = ref(1)

const { data, isFetching } = useFiles(page)

const openFile = (fileName: string) => {
  window.open(`https://cdn.smnl.it/${fileName}`)
}

const logout = () => {
  localStorage.removeItem('authKey')
  emit('logout')
}
</script>

<template>
  <div v-if="!!data && !isFetching">
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
    <div class="mt-4 flex w-full justify-between">
      <FilesPagination
        :page="page"
        :total-pages="data.totalPages"
        @update:page="page = $event"
      />
      <button
        class="cursor-pointer border border-red-500 p-3 transition-colors hover:bg-red-500"
        @click="logout"
      >
        Logout
      </button>
    </div>
  </div>
</template>
