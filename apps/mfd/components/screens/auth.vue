<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useOptionsStore } from '~/stores/options'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'

const authStore = useAuthStore()
const optionsStore = useOptionsStore()
const googleBtnRef = ref<HTMLElement | null>(null)

const initAuth = () => {
  if (googleBtnRef.value) {
    authStore.renderGoogleButton(googleBtnRef.value)
  }
}

onMounted(() => {
  authStore.fetchSession()
  nextTick(() => {
    initAuth()
  })
})

const lowerActions = reactive({
  lower0: {
    label: 'In',
    action: () => {
      if (authStore.isAuthenticated) {
        authStore.logout()
      } else {
        authStore.triggerGoogleSignIn(googleBtnRef.value)
      }
    },
  },
  lower9: useMainButtonConfig(),
})

watchEffect(() => {
  lowerActions.lower0.label = authStore.isAuthenticated ? 'Out' : 'In'
})

defineExpose<ScreenConfig>({
  lowerButtonActions: lowerActions,
})

const options = computed(() => {
  if (authStore.isAuthenticated && authStore.user) {
    return [
      {
        name: 'name',
        label: 'Name',
        value: authStore.user.name,
      },
      {
        name: 'email',
        label: 'Email',
        value: authStore.user.email,
      },
      {
        name: 'role',
        label: 'Role',
        value: authStore.user.role === 'admin' ? 'Administrator' : 'Standard',
      },
    ]
  }

  return [
    {
      name: 'status',
      label: 'Status',
      value: 'Unauthenticated',
    },
    {
      name: 'role',
      label: 'Access Level',
      value: 'Guest',
    },
  ]
})

watchEffect(() => {
  optionsStore.setOptions(options.value)
})
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>Authentication</span>
    </div>

    <div class="p-10 space-y-4">
      <div
        v-if="authStore.authError"
        class="border border-red-500 p-2 text-red-500 dark:border-red-400 dark:text-red-400"
      >
        <span>AUTH ERROR: {{ authStore.authError }}</span>
      </div>

      <OptionsCard header="Account" :options="options" />

      <div
        id="google-signin-hidden-btn"
        ref="googleBtnRef"
        style="position: fixed; top: -9999px; left: -9999px; opacity: 0; pointer-events: auto;"
      />
    </div>
  </div>
</template>
