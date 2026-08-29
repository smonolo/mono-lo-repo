<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useScreenStore } from '~/stores/screen'
import { useOptionsStore } from '~/stores/options'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import OptionsCard from '~/components/common/options-card.vue'
import type { ScreenConfig } from '~/types/screen'
import type { Option } from '~/types/options'

const authStore = useAuthStore()
const screenStore = useScreenStore()
const optionsStore = useOptionsStore()

const scanlines = ref<boolean>(false)
const haptic = ref<boolean>(false)
const fpsTarget = ref<number>(60)
const keepAlive = ref<boolean>(true)
const bezelLight = ref<number>(100)

onMounted(() => {
  authStore.fetchSession()
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower8: {
      label: 'Test',
      action: () => screenStore.setActiveScreen('test'),
    },
    lower9: useMainButtonConfig(),
  },
})

const displayOptions = computed<Option[]>(() => [
  {
    name: 'diag_res',
    label: 'Resolution',
    value: '1100 x 900 px',
  },
  {
    name: 'diag_fps',
    label: 'Target Frame Rate',
    value: `${fpsTarget.value} FPS`,
    action: () => {
      fpsTarget.value = fpsTarget.value === 60 ? 120 : fpsTarget.value === 120 ? 30 : 60
    },
  },
  {
    name: 'diag_color',
    label: 'Color Space',
    value: 'sRGB Monochrome',
  },
  {
    name: 'diag_aspect',
    label: 'Pixel Aspect',
    value: '1:1 Square',
  },
  {
    name: 'diag_scanlines',
    label: 'Scanline Filter',
    value: scanlines.value ? 'Enabled' : 'Disabled',
    action: () => {
      scanlines.value = !scanlines.value
    },
  },
])

const networkOptions = computed<Option[]>(() => [
  {
    name: 'diag_link',
    label: 'Link Mode',
    value: 'Cloudflare Argo',
  },
  {
    name: 'diag_proto',
    label: 'Transport Protocol',
    value: 'HTTP/2 (QUIC)',
  },
  {
    name: 'diag_cipher',
    label: 'Cipher Suite',
    value: 'TLS_AES_256_GCM_SHA384',
  },
  {
    name: 'diag_timeout',
    label: 'Request Timeout',
    value: '5000 ms',
  },
  {
    name: 'diag_keepalive',
    label: 'Keep-Alive Ping',
    value: keepAlive.value ? 'Active (30s)' : 'Disabled',
    action: () => {
      keepAlive.value = !keepAlive.value
    },
  },
])

const subsystemOptions = computed<Option[]>(() => [
  {
    name: 'diag_clock',
    label: 'Clock Bus',
    value: 'Synchronized (UTC)',
  },
  {
    name: 'diag_telemetry',
    label: 'Telemetry Engine',
    value: 'Virtual Threads L1',
  },
  {
    name: 'diag_auth_gate',
    label: 'Auth Gateway',
    value: 'Google GIS + HMAC',
  },
  {
    name: 'diag_cache',
    label: 'Data Cache',
    value: 'In-Memory SWR',
  },
  {
    name: 'diag_failover',
    label: 'Failover Policy',
    value: 'Graceful Degradation',
  },
])

const hardwareOptions = computed<Option[]>(() => [
  {
    name: 'diag_haptic',
    label: 'Bezel Haptic',
    value: haptic.value ? 'Enabled' : 'Disabled',
    action: () => {
      haptic.value = !haptic.value
    },
  },
  {
    name: 'diag_light',
    label: 'Backlight Level',
    value: `${bezelLight.value}%`,
    action: () => {
      bezelLight.value = bezelLight.value >= 100 ? 25 : bezelLight.value + 25
    },
  },
  {
    name: 'diag_repeat',
    label: 'Key Repeat Rate',
    value: '250 ms',
  },
  {
    name: 'diag_debounce',
    label: 'Switch Debounce',
    value: '50 ms',
  },
])

const allOptions = computed<Option[]>(() => {
  if (!authStore.hasScreenPermission('diag')) return []
  return [
    ...displayOptions.value,
    ...networkOptions.value,
    ...subsystemOptions.value,
    ...hardwareOptions.value,
  ]
})

watchEffect(() => {
  if (allOptions.value.length) {
    optionsStore.setOptions(allOptions.value)
  }
})
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>System Diagnostics</span>
    </div>

    <!-- Unauthorized View -->
    <div v-if="!authStore.hasScreenPermission('diag')" class="p-10 space-y-4">
      <div class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <p class="font-bold tracking-wide">Restricted System</p>
        <p>
          Authorization is required to access system diagnostics.
        </p>
        <p>
          Please navigate to the Auth screen to sign in with an authorized account.
        </p>
      </div>
    </div>

    <!-- Authenticated Admin View -->
    <div v-else class="p-10 space-y-6">
      <OptionsCard header="Display Subsystem" :options="displayOptions" />
      <OptionsCard header="Network & Transport" :options="networkOptions" />
      <OptionsCard header="Core Architecture" :options="subsystemOptions" />
      <OptionsCard header="Hardware Interface" :options="hardwareOptions" />
    </div>
  </div>
</template>
