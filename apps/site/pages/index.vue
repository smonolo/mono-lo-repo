<script setup lang="ts">
const circles = [
  'w-10 h-10 group-hover:w-2 group-hover:h-2',
  'w-8 h-8 group-hover:w-4 group-hover:h-4',
  'w-6 h-6 group-hover:w-6 group-hover:h-6',
  'w-4 h-4 group-hover:w-8 group-hover:h-8',
  'w-2 h-2 group-hover:w-10 group-hover:h-10',
]
</script>

<template>
  <div class="space-y-10">
    <hgroup class="space-y-2">
      <h1 class="font-heading text-3xl font-semibold">
        Hi there, I'm Stefano.
      </h1>
      <p class="text-gray-300">
        I'm a software engineer passionate about the web and its technologies.
      </p>
    </hgroup>
    <div
      class="bg-sm-blue flex h-56 items-center justify-center gap-x-8 rounded-2xl"
    >
      <div
        v-for="key in 3"
        :key
        class="h-14 w-14 rounded bg-white transition-transform hover:scale-125"
      />
    </div>
    <div class="space-y-4 text-gray-300">
      <p>This banner has no utility. But it's a nice way to fill the site.</p>
      <p>
        I work at
        <a
          href="https://www.weroad.com"
          target="_blank"
          class="text-white hover:text-[#ff4758]"
        >
          WeRoad
        </a>
        as a software engineer. My job is to build and maintain the platform
        that travel coordinators use every day to manage their trips. There are
        more than 3.5k of them, and the platform is crucial to their business.
      </p>
      <p>
        I use
        <a
          href="https://vuejs.org"
          target="_blank"
          class="text-white hover:text-[#42b883]"
        >
          Vue
        </a>
        and
        <a
          href="https://nuxt.com"
          target="_blank"
          class="text-white hover:text-[#42b883]"
        >
          Nuxt
        </a>
        every day, but most business and private sites I worked on were made
        using
        <a
          href="https://react.dev"
          target="_blank"
          class="text-white hover:text-[#58c4dc]"
        >
          React</a
        >. Before that, I officially started building sites and web apps using
        <a
          href="https://jquery.com"
          target="_blank"
          class="text-white hover:text-[#0769ad]"
          >jQuery</a
        >
        and
        <a
          href="https://expressjs.com"
          target="_blank"
          class="text-white hover:text-gray-400"
          >Express</a
        >.
      </p>
      <p>
        I'm also a big fan of setting up infrastructure and networks, and
        building backend applications myself when I work on full stack projects.
        I like using cloud providers, Linux-based machines and Docker to go from
        zero to a working product with no hassle.
      </p>
    </div>
    <div
      class="group flex h-32 items-center justify-center gap-x-4 rounded-2xl bg-white"
    >
      <div
        v-for="(circle, key) in circles"
        :key
        :class="circle"
        class="bg-sm-blue rounded-full transition-[width,height]"
      />
    </div>
    <div class="space-y-4 text-gray-300">
      <p>Another banner. But this one is white and has circles.</p>
      <p>
        This site used to have more content, but I removed it because there's
        another one for that. If you're looking for my resume and want to know
        more about me, you can find it at
        <a
          href="https://resume.smnl.dev"
          target="_blank"
          class="hover:text-sm-blue text-white"
          >resume.smnl.dev</a
        >.
      </p>
      <p>
        I got invited to Dub's partners program and have a link to share. If you
        need a URL shortener and link manager that is just great,
        <a
          href="https://refer.dub.co/stefano"
          target="_blank"
          class="text-white hover:text-gray-400"
          >try out Dub</a
        >, you won't regret it.
      </p>
      <p>
        If you're looking for a list of projects I'm working on or have worked
        on, you can find it at
        <a
          href="https://old.smnl.dev"
          target="_blank"
          class="hover:text-sm-blue text-white"
          >old.smnl.dev</a
        >.
      </p>
      <p>
        No more banners. But this is the end. If you scroll more, you'll find
        the footer.
      </p>
    </div>
  </div>
</template>
