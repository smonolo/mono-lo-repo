<script setup lang="ts">
const circles = [
  'w-10 h-10 group-hover:w-2 group-hover:h-2',
  'w-8 h-8 group-hover:w-4 group-hover:h-4',
  'w-6 h-6 group-hover:w-6 group-hover:h-6',
  'w-4 h-4 group-hover:w-8 group-hover:h-8',
  'w-2 h-2 group-hover:w-10 group-hover:h-10',
]

const activeProjects = [
  {
    name: 'Rails',
    description: 'A minimalist railway simulator',
    domain: 'rails.smnl.dev',
  },
  {
    name: 'MFD',
    description: 'A personal dashboard',
    domain: 'mfd.smnl.dev',
  },
  {
    name: 'MXC',
    description: 'FiveM resources and tools',
    domain: 'markz3d.com',
  },
]

const showResumeSiteWarning = () => {
  alert(
    "Heads up: the resume site doesn't have dark mode. Your eyes might need a moment."
  )
}
</script>

<template>
  <div class="space-y-12 md:space-y-16">
    <hgroup class="space-y-2 md:px-[50px]">
      <h1 class="font-heading text-3xl font-semibold">
        Hi there, I'm Stefano.
      </h1>
      <p class="text-gray-300">
        I'm a software engineer passionate about technology, gaming, transport,
        and emergency response.
      </p>
    </hgroup>

    <div
      class="bg-sm-blue flex h-44 items-center justify-center gap-x-8 rounded-2xl"
    >
      <div
        v-for="key in 3"
        :key="key"
        class="h-14 w-14 rounded bg-white transition-transform hover:scale-125"
      />
    </div>

    <div class="space-y-4 text-gray-300 md:px-[50px]">
      <p>
        I work as a software engineer at
        <a
          href="https://www.klarna.com/?ref=smnl.dev"
          target="_blank"
          rel="noopener noreferrer"
          class="text-white hover:text-[#ffa8cd] hover:underline focus-visible:underline"
        >
          Klarna</a
        >, where my main focus is building and maintaining services for
        financial hardship and credit disputes, such as deceased customers and
        bankruptcy. I additionally support systems for standard disputes,
        returns, and refunds.
      </p>
      <p>
        Beyond product engineering, I enjoy working on infrastructure and
        networking, including cloud providers, VMs, containers, and full-stack
        backend services.
      </p>
      <p>
        I experiment with AI daily, leveraging LLMs, agentic workflows, and
        prompt engineering across my projects. It has become an essential part
        of how I design systems, automate routine tasks, and write code.
      </p>
    </div>

    <div
      aria-hidden="true"
      class="bg-sm-purple group flex h-44 flex-col items-center justify-center gap-3 overflow-hidden rounded-2xl"
    >
      <div
        v-for="(line, index) in [
          {
            width: 'w-36 sm:w-48',
            shift: 'group-hover:-translate-x-4',
            duration: 'duration-300',
          },
          {
            width: 'w-48 sm:w-64',
            shift: 'group-hover:translate-x-2',
            duration: 'duration-500',
          },
          {
            width: 'w-32 sm:w-40',
            shift: 'group-hover:translate-x-6',
            duration: 'duration-700',
          },
        ]"
        :key="index"
        class="flex h-5 items-center gap-3"
        :class="index === 1 ? 'sm:-translate-x-5' : 'sm:translate-x-5'"
      >
        <div
          v-if="index !== 1"
          :class="[line.shift, line.duration]"
          class="bg-sm-black h-5 w-5 shrink-0 rounded-sm transition-transform"
        />
        <div
          :class="[line.width, line.shift, line.duration]"
          class="h-1 rounded-full bg-white transition-transform"
        />
        <div
          v-if="index === 1"
          :class="[line.shift, line.duration]"
          class="bg-sm-black h-5 w-5 shrink-0 rounded-sm transition-transform"
        />
      </div>
    </div>

    <div class="space-y-4 text-gray-300 md:px-[50px]">
      <p>
        Outside work, I spend time on a few projects of my own. They give me
        room to try new ideas, build useful tools, and refine the small details
        that make something enjoyable to use. These are the ones I'm working on
        right now.
      </p>
      <ul class="divide-y divide-gray-700">
        <li
          v-for="project in activeProjects"
          :key="project.name"
          class="flex items-center justify-between gap-4 py-3"
        >
          <span class="min-w-0">
            <span class="text-white">{{ project.name }}</span>
            <span class="text-gray-500"> - {{ project.description }}</span>
          </span>
          <a
            :href="`https://${project.domain}/?ref=smnl.dev`"
            target="_blank"
            rel="noopener noreferrer"
            class="focus-visible:outline-sm-blue hover:text-sm-blue shrink-0 text-right text-gray-300 transition-colors hover:underline focus-visible:rounded-sm focus-visible:underline focus-visible:outline-2 focus-visible:outline-offset-4"
          >
            {{ project.domain }}
          </a>
        </li>
      </ul>
    </div>

    <div
      class="group flex h-44 items-center justify-center gap-x-4 rounded-2xl bg-white"
    >
      <div
        v-for="(circle, index) in circles"
        :key="index"
        :class="circle"
        class="bg-sm-blue rounded-full transition-[width,height]"
      />
    </div>

    <div class="space-y-4 text-gray-300 md:px-[50px]">
      <p>
        Outside tech, I serve as a volunteer for the
        <a
          href="https://www.regione.lombardia.it/sicurezza-e-protezione-civile/pianificazione-allertamento-e-gestione-emergenze/gestione-delle-emergenze/il-sistema-di-protezione-civile?ref=smnl.dev"
          target="_blank"
          rel="noopener noreferrer"
          class="text-white hover:text-[#45b777] hover:underline focus-visible:underline"
        >
          Italian Civil Protection in Lombardy</a
        >, with training covering wildfire fighting, flood response, high-risk
        operations, and rescue diving. My hobbies include gaming, getting my
        boating license, and working towards an advanced scuba certification.
      </p>
      <p>
        I've kept this site minimal. If you're looking for my resume and want to
        learn more about my experience, you can find it at
        <a
          href="https://resume.smnl.dev/?ref=smnl.dev"
          target="_blank"
          rel="noopener noreferrer"
          class="hover:text-sm-blue text-white hover:underline focus-visible:underline"
          @click="showResumeSiteWarning"
        >
          resume.smnl.dev</a
        >.
      </p>
    </div>

    <div class="space-y-12">
      <div
        aria-hidden="true"
        class="group flex h-44 items-center justify-center gap-5 overflow-hidden rounded-2xl bg-[#eb8744]"
      >
        <svg
          viewBox="0 0 100 100"
          class="h-8 w-8 text-white transition-transform duration-500 group-hover:-translate-y-3 motion-reduce:transition-none"
        >
          <path
            d="M50 12 90 90 10 90Z"
            fill="currentColor"
            stroke="currentColor"
            stroke-linejoin="round"
            stroke-width="14"
          />
        </svg>
        <svg
          viewBox="0 0 100 100"
          class="text-sm-black h-16 w-16 transition-transform duration-700 group-hover:translate-y-3 motion-reduce:transition-none"
        >
          <path
            d="M50 12 90 90 10 90Z"
            fill="currentColor"
            stroke="currentColor"
            stroke-linejoin="round"
            stroke-width="14"
          />
        </svg>
        <svg
          viewBox="0 0 100 100"
          class="h-12 w-12 text-white transition-transform duration-300 group-hover:-translate-y-1 motion-reduce:transition-none"
        >
          <path
            d="M50 12 90 90 10 90Z"
            fill="currentColor"
            stroke="currentColor"
            stroke-linejoin="round"
            stroke-width="14"
          />
        </svg>
      </div>
      <p class="text-gray-300 md:px-[50px]">
        That's it, now you'll find the footer.
      </p>
    </div>
  </div>
</template>
