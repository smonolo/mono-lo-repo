<script setup lang="ts">
type Project = {
  name: string
  domain: string
  details: string
  githubUrl?: string
}

const props = defineProps<{
  open: boolean
  projects: Project[]
}>()

const emit = defineEmits<{
  close: []
}>()

const dialog = ref<HTMLDialogElement | null>(null)
const isVisible = ref(false)
let previousOverflow = ''
let scrollLocked = false
let closeTimer: ReturnType<typeof setTimeout> | undefined
let openFrame: number | undefined

const unlockScroll = () => {
  if (!scrollLocked) return
  document.body.style.overflow = previousOverflow
  scrollLocked = false
}

const syncDialog = (open: boolean) => {
  if (!dialog.value) return

  if (open) {
    if (closeTimer) clearTimeout(closeTimer)
    if (openFrame) cancelAnimationFrame(openFrame)
    if (!dialog.value.open) {
      previousOverflow = document.body.style.overflow
      document.body.style.overflow = 'hidden'
      scrollLocked = true
      dialog.value.showModal()
    }
    openFrame = requestAnimationFrame(() => {
      isVisible.value = true
    })
  } else {
    if (openFrame) cancelAnimationFrame(openFrame)
    isVisible.value = false
    if (dialog.value.open) {
      closeTimer = setTimeout(() => {
        dialog.value?.close()
        unlockScroll()
      }, 220)
    }
  }
}

watch(() => props.open, syncDialog, { flush: 'post' })
onMounted(() => syncDialog(props.open))
onBeforeUnmount(() => {
  if (closeTimer) clearTimeout(closeTimer)
  if (openFrame) cancelAnimationFrame(openFrame)
  if (dialog.value?.open) dialog.value.close()
  unlockScroll()
})

const onClose = () => {
  isVisible.value = false
  unlockScroll()
  emit('close')
}
const onBackdropClick = (event: MouseEvent) => {
  if (event.target === dialog.value) emit('close')
}
</script>

<template>
  <dialog
    ref="dialog"
    aria-labelledby="projects-modal-title"
    class="projects-dialog font-display fixed inset-0 m-auto max-h-[min(80vh,48rem)] w-[calc(100%-2rem)] max-w-2xl overflow-y-auto rounded-[28px] border-0 bg-sm-black p-0 text-white shadow-2xl"
    :class="{ 'modal-visible': isVisible }"
    @cancel.prevent="emit('close')"
    @close="onClose"
    @click="onBackdropClick"
  >
    <div class="p-12 sm:p-14">
      <div class="mb-6 flex items-start justify-between gap-4">
        <h2 id="projects-modal-title" class="text-2xl font-semibold">
          Projects
        </h2>
        <button
          type="button"
          aria-label="Close projects"
          class="flex h-9 w-9 items-center justify-center rounded-lg bg-transparent text-2xl leading-none text-gray-300 transition-colors hover:bg-gray-500/20 hover:text-white focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-sm-blue"
          @click="emit('close')"
        >
          &times;
        </button>
      </div>

      <ul class="divide-y divide-gray-700">
        <li v-for="project in projects" :key="project.name" class="space-y-3 py-5 first:pt-0 last:pb-0">
          <h3 class="text-xl font-semibold">{{ project.name }}</h3>
          <p class="line-clamp-2 leading-relaxed text-gray-300">{{ project.details }}</p>
          <div class="flex flex-wrap gap-x-5 gap-y-2">
            <a
              :href="`https://${project.domain}/?ref=smnl.dev`"
              target="_blank"
              rel="noopener noreferrer"
              class="text-white hover:text-sm-blue hover:underline focus-visible:underline"
            >
              {{ project.domain }}
            </a>
            <a
              v-if="project.githubUrl"
              :href="project.githubUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="text-white hover:text-sm-blue hover:underline focus-visible:underline"
            >
              GitHub
            </a>
          </div>
        </li>
      </ul>
    </div>
  </dialog>
</template>

<style scoped>
.projects-dialog {
  opacity: 0;
  transform: scale(0.97);
  transition: opacity 220ms ease, transform 220ms ease;
}

.projects-dialog.modal-visible {
  opacity: 1;
  transform: scale(1);
}

.projects-dialog::backdrop {
  background: rgb(0 0 0 / 85%);
  backdrop-filter: blur(8px);
  opacity: 0;
  transition: opacity 220ms ease;
}

.projects-dialog.modal-visible::backdrop {
  opacity: 1;
}

@media (prefers-reduced-motion: reduce) {
  .projects-dialog,
  .projects-dialog::backdrop {
    transition-duration: 0ms;
  }
}
</style>
