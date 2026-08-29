<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useScreenStore } from '~/stores/screen'
import { useMainButtonConfig } from '~/composables/buttons/configs/useMainButtonConfig'
import type { ScreenConfig } from '~/types/screen'

const authStore = useAuthStore()
const screenStore = useScreenStore()

onMounted(() => {
  authStore.fetchSession()
})

defineExpose<ScreenConfig>({
  lowerButtonActions: {
    lower8: {
      label: 'Tst',
      action: () => screenStore.setActiveScreen('test'),
    },
    lower9: useMainButtonConfig(),
  },
})
</script>

<template>
  <div class="h-full w-full">
    <div
      class="w-fit border border-slate-950 px-1.5 py-0.5 font-bold tracking-wide dark:border-slate-100"
    >
      <span>Documentation</span>
    </div>

    <!-- Unauthenticated / Non-Admin View -->
    <div v-if="!authStore.isAdmin" class="p-10 space-y-4">
      <div class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <p class="font-bold tracking-wide">Restricted System</p>
        <p>
          Administrator authorization is required to access system documentation.
        </p>
        <p>
          Please navigate to the Auth screen to sign in with an authorized Google account.
        </p>
      </div>
    </div>

    <div v-else class="p-10 space-y-6 text-sm leading-relaxed">
      <section class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <h2 class="font-bold tracking-wider uppercase text-xs">1. System Overview</h2>
        <p>
          The Multi-Function Display (MFD) is a software representation of modern glass-cockpit avionics architecture. It is designed to provide flight deck telemetry, server instrumentation, and secure administrative controls through a standardized hardware-bezel interaction model.
        </p>
        <p>
          All interface operations are routed through physical perimeter buttons, eliminating the need for cursor pointers, touch gestures, or external pointing devices.
        </p>
      </section>

      <section class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <h2 class="font-bold tracking-wider uppercase text-xs">2. Bezel Control Mapping</h2>
        <p>
          The bezel framework is divided into three primary functional banks:
        </p>
        <ul class="list-disc list-inside space-y-1 text-xs">
          <li><strong>Upper Bezel (Controls):</strong> Reserved for system lighting, display contrast toggles, and primary navigation bus selections.</li>
          <li><strong>Lower Bezel (Functions 0–9):</strong> Context-sensitive softkeys that adapt based on the currently active tactical screen.</li>
          <li><strong>Right Side Bezel (Navigation):</strong> Physical directional rock keys (UP ▲ / DOWN ▼), Execute (E), and Clear (C) keys for cursor traversal and list manipulation.</li>
        </ul>
      </section>

      <section class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <h2 class="font-bold tracking-wider uppercase text-xs">3. Identity & Access Protocol</h2>
        <p>
          Administrative operations are guarded by a zero-trust cryptographic pipeline:
        </p>
        <p>
          1. Authentication requests are issued via the <strong>ATH</strong> screen, invoking Google Identity Services over secure channels.
        </p>
        <p>
          2. The serverless proxy verifies cryptographic token signatures with provider root certificates, ensuring email identity matches authorized server maintainers.
        </p>
        <p>
          3. Sessions are maintained via tamper-evident HMAC-SHA256 encrypted cookies, completely shielding server-to-server API secrets from client exposure.
        </p>
      </section>

      <section class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <h2 class="font-bold tracking-wider uppercase text-xs">4. Minecraft Server Telemetry (SMEssential)</h2>
        <p>
          The Minecraft tactical sub-system interfaces directly with Paper 1.21 via a non-blocking embedded HTTP listener running on Java 21 virtual threads:
        </p>
        <ul class="list-disc list-inside space-y-1 text-xs">
          <li><strong>Zero Tick Interference:</strong> Telemetry collection executes on isolated threads, accessing in-memory concurrent caches with zero game tick delay.</li>
          <li><strong>Network Tunneling:</strong> Container traffic is routed through encrypted Cloudflare edge tunnels without opening inbound firewall ports.</li>
          <li><strong>Telemetry Scope:</strong> Real-time tracking includes 1-minute rolling TPS, tick time (MSPT), active heap memory, server uptime, player ping latency, and AFK status.</li>
        </ul>
      </section>

      <section class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <h2 class="font-bold tracking-wider uppercase text-xs">5. Emergency & Recovery Procedures</h2>
        <p>
          In the event of an unrecoverable telemetry disconnection or server outage:
        </p>
        <p>
          • Verify that the Cloudflare connector container status is active in the host environment.
        </p>
        <p>
          • Press the <strong>Ref</strong> softkey to force an immediate polling cycle.
        </p>
        <p>
          • If session degradation occurs, cycle authentication by pressing <strong>Out</strong> and re-authenticating on the <strong>ATH</strong> page.
        </p>
      </section>

      <section class="border border-slate-950 p-4 dark:border-slate-100 space-y-2">
        <h2 class="font-bold tracking-wider uppercase text-xs">6. Architectural Specifications</h2>
        <p class="text-xs">
          SYSTEM: MFD Avionics Client v1.0.0<br />
          PLATFORM: Nuxt 3 / Nitro / Vue 3 / Pinia / Tailwind CSS<br />
          TARGET ENVIRONMENT: Paper 1.21 / Java 21 (Virtual Threads)<br />
          TUNNEL PROTOCOL: Cloudflare Argo Tunnel (QUIC / TLS 1.3)
        </p>
      </section>
    </div>
  </div>
</template>
