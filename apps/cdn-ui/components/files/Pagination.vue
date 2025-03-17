<script setup lang="ts">
type Props = {
  page: number
  totalPages: number
}

type Emits = {
  'update:page': [page: number]
}

defineComponent({ name: 'FilesPagination' })

const props = defineProps<Props>()

const emit = defineEmits<Emits>()

const updatePage = (newPage: number) => {
  if (newPage >= 1 && newPage <= props.totalPages) {
    emit('update:page', newPage)
  }
}
</script>

<template>
  <div class="flex w-fit border border-white">
    <button
      class="cursor-pointer p-3 transition-colors hover:bg-white hover:text-black"
      @click="updatePage(page - 1)"
    >
      Previous
    </button>
    <span class="border-x border-white p-3">{{ page }}</span>
    <button
      class="cursor-pointer p-3 transition-colors hover:bg-white hover:text-black"
      @click="updatePage(page + 1)"
    >
      Next
    </button>
  </div>
</template>
