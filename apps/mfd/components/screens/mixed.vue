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

const telemetryRate = ref<number>(1000)
const loggingVerbosity = ref<'standard' | 'verbose' | 'debug'>('standard')
const autoSync = ref<boolean>(true)
const failoverNode = ref<'primary' | 'secondary' | 'standby'>('primary')

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

const generalOptions = computed<Option[]>(() => [
  {
    name: 'mixed_rate',
    label: 'Telemetry Polling Interval',
    value: `${telemetryRate.value} ms`,
    action: () => {
      telemetryRate.value =
        telemetryRate.value === 1000
          ? 500
          : telemetryRate.value === 500
            ? 250
            : 1000
    },
  },
  {
    name: 'mixed_log',
    label: 'Diagnostic Log Verbosity',
    value: loggingVerbosity.value.toUpperCase(),
    action: () => {
      loggingVerbosity.value =
        loggingVerbosity.value === 'standard'
          ? 'verbose'
          : loggingVerbosity.value === 'verbose'
            ? 'debug'
            : 'standard'
    },
  },
])

const networkOptions = computed<Option[]>(() => [
  {
    name: 'mixed_sync',
    label: 'Cloudflare Tunnel Auto-Sync',
    value: autoSync.value ? 'Active' : 'Disabled',
    action: () => {
      autoSync.value = !autoSync.value
    },
  },
  {
    name: 'mixed_node',
    label: 'Active Failover Target',
    value: failoverNode.value.toUpperCase(),
    action: () => {
      failoverNode.value =
        failoverNode.value === 'primary'
          ? 'secondary'
          : failoverNode.value === 'secondary'
            ? 'standby'
            : 'primary'
    },
  },
])

const allOptions = computed<Option[]>(() => {
  if (!authStore.hasScreenPermission('mixed')) return []
  return [...generalOptions.value, ...networkOptions.value]
})

watchEffect(() => {
  if (allOptions.value.length) {
    optionsStore.setOptions(allOptions.value)
  }
})
</script>

<template>
  <div class="h-full w-full">
    <!-- Unauthorized View -->
    <div v-if="!authStore.hasScreenPermission('mixed')" class="space-y-4">
      <div class="space-y-2 border border-slate-950 p-4 dark:border-slate-100">
        <p class="font-bold tracking-wide">Restricted System</p>
        <p>Authorization is required to access mixed test screens.</p>
        <p>
          Please navigate to the Auth screen to sign in with an authorized
          account.
        </p>
      </div>
    </div>

    <!-- Authenticated View with Long Text and Options -->
    <div v-else class="space-y-6 text-sm leading-relaxed">
      <!-- Introductory Text Block -->
      <section
        class="space-y-2 border border-slate-950 p-4 dark:border-slate-100"
      >
        <h2 class="font-bold uppercase tracking-wider">
          1. System Configuration Overview
        </h2>
        <p>
          This test page contains a combination of descriptive architectural
          documentation and interactive option fields. Use the side bezel rock
          keys (UP ▲ / DOWN ▼) to navigate through options and observe how the
          viewport dynamically tracks the active element.
        </p>
        <p>
          When you navigate downward past visible boundaries, the viewport
          smoothly auto-scrolls to ensure the highlighted option remains in
          clear view.
        </p>
      </section>

      <!-- First Interactive Options Card -->
      <OptionsCard
        header="Telemetry & Diagnostic Parameters"
        :options="generalOptions"
      />

      <!-- Intermediary Long Text Section -->
      <section
        class="space-y-2 border border-slate-950 p-4 dark:border-slate-100"
      >
        <h2 class="font-bold uppercase tracking-wider">
          2. Operational Guidelines & Tunnel Routing
        </h2>
        <p>
          All high-frequency telemetry packets are multiplexed over QUIC tunnels
          using TLS 1.3 encryption. Adjusting the parameters above modifies
          in-memory state and updates server polling cycles without dropping
          active client connections.
        </p>
        <p>
          If packet loss exceeds the allowable threshold, failover nodes
          automatically reroute inbound requests through alternate edge tunnels.
        </p>
      </section>

      <!-- Second Interactive Options Card -->
      <OptionsCard
        header="Network & Failover Options"
        :options="networkOptions"
      />

      <!-- Closing Long Text Section -->
      <section
        class="space-y-2 border border-slate-950 p-4 dark:border-slate-100"
      >
        <h2 class="font-bold uppercase tracking-wider">
          3. Execution Protocol
        </h2>
        <p>
          Press the <strong>E</strong> key on the right bezel to cycle selected
          option values. Navigating back to the first option will automatically
          scroll the viewport back to the top of the documentation.
        </p>
      </section>
    </div>
  </div>
</template>
