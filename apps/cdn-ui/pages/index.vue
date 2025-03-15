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
}

const route = useRoute()

const response = await fetch(
  `https://cdn.smnl.it/list?authKey=${route.query.authKey}`
)
const data: Response = await response.json()
</script>

<template>
  <table class="w-full">
    <thead>
      <tr>
        <th>{{ data.files.length }}/{{ filesize(data.totalSize) }}</th>
        <th>Name</th>
        <th>Size</th>
        <th>Upload Date</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(file, index) in data.files" :key="file.fileName">
        <td>
          {{ data.files.length - index }}
        </td>
        <td>
          <a
            :href="`https://cdn.smnl.it/${file.fileName}`"
            target="_blank"
            class="underline"
          >
            {{ file.fileName }}
          </a>
        </td>
        <td>{{ filesize(file.size) }}</td>
        <td>{{ dayjs(file.birthTime).format('DD/MM/YYYY HH:mm:ss') }}</td>
      </tr>
    </tbody>
  </table>
</template>
