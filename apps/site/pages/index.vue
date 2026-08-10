<script setup lang="ts">
const circles = [
  'w-10 h-10 group-hover:w-2 group-hover:h-2',
  'w-8 h-8 group-hover:w-4 group-hover:h-4',
  'w-6 h-6 group-hover:w-6 group-hover:h-6',
  'w-4 h-4 group-hover:w-8 group-hover:h-8',
  'w-2 h-2 group-hover:w-10 group-hover:h-10',
]

const showResumeSiteWarning = () => {
  alert(
    "Heads up: the resume site doesn't have dark mode. Your eyes might need a moment."
  )
}
</script>

<template>
  <div class="space-y-10">
    <hgroup class="space-y-2 md:px-[50px]">
      <h1 class="font-heading text-3xl font-semibold">
        Hi there, I'm Stefano.
      </h1>
      <p class="text-gray-300">
        I'm a software engineer passionate about the web, modern user
        experiences, and backend systems.
      </p>
    </hgroup>

    <div
      class="bg-sm-blue flex h-56 items-center justify-center gap-x-8 rounded-2xl"
    >
      <div
        v-for="key in 3"
        :key="key"
        class="h-14 w-14 rounded bg-white transition-transform hover:scale-125"
      />
    </div>

    <div class="space-y-4 text-gray-300 md:px-[50px]">
      <p>This banner has no utility, but it gives the page some color.</p>
      <p>
        I work as a software engineer at
        <a
          href="https://www.klarna.com/?ref=smnl.dev"
          target="_blank"
          rel="noopener noreferrer"
          class="text-white hover:text-[#ffa8cd]"
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
      class="group flex h-32 items-center justify-center gap-x-4 rounded-2xl bg-white"
    >
      <div
        v-for="(circle, index) in circles"
        :key="index"
        :class="circle"
        class="bg-sm-blue rounded-full transition-[width,height]"
      />
    </div>

    <div class="space-y-4 text-gray-300 md:px-[50px]">
      <p>Another banner. This one is white and has circles.</p>
      <p>
        Outside tech, I serve as a volunteer for the Italian Civil Protection in
        Lombardy, with training covering wildfire fighting, flood response,
        high-risk operations, and rescue diving. My hobbies include gaming,
        getting my boating license, and working towards an advanced scuba
        certification.
      </p>
      <p>
        I've kept this site minimal. If you're looking for my resume and want to
        learn more about my experience, you can find it at
        <a
          href="https://resume.smnl.dev/?ref=smnl.dev"
          target="_blank"
          rel="noopener noreferrer"
          class="hover:text-sm-blue text-white"
          @click="showResumeSiteWarning"
        >
          resume.smnl.dev</a
        >.
      </p>
      <p>That's all for now. Thanks for stopping by!</p>
    </div>
  </div>
</template>
