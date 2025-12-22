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
    <div class="space-y-4 text-gray-300 md:px-[50px]">
      <p>This banner has no utility. But it's a nice way to fill the site.</p>
      <p>
        I work at
        <a
          href="https://www.klarna.com/?ref=smnl.dev"
          target="_blank"
          class="text-white hover:text-[#ffa8cd]"
          >Klarna</a
        >
        as a software engineer. I build and maintain services that help
        customers in financial hardship get support, plus some stuff around
        disputes, returns and refunds.
      </p>
      <p>
        Before that, I was at
        <a
          href="https://www.weroad.com/?ref=smnl.dev"
          target="_blank"
          class="text-white hover:text-[#ff4758]"
        >
          WeRoad</a
        >
        where I built and maintained two internal platforms that around 4k
        travel coordinators and 500+ partners used every day. I led the frontend
        work on my team and also did backend stuff when needed.
      </p>
      <p>
        And there are a lot of other companies and projects I could mention, but
        that's enough.
      </p>
      <p>
        I also enjoy messing around with infrastructure and networks, and
        building backend services when I'm working on full-stack stuff. Cloud
        providers, VMs, containers—that kind of thing.
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
    <div class="space-y-4 text-gray-300 md:px-[50px]">
      <p>Another banner. But this one is white and has circles.</p>
      <p>
        This site used to have more content, but I removed it because there's
        another one for that. If you're looking for my resume and want to know
        more about me, you can find it at
        <a
          href="https://resume.smnl.dev/?ref=smnl.dev"
          target="_blank"
          class="hover:text-sm-blue text-white"
          @click="showResumeSiteWarning"
          >resume.smnl.dev</a
        >.
      </p>
      <p>
        Got invited to Dub's partner program, so here's my referral link. If you
        need a solid URL shortener and link manager,
        <a
          href="https://refer.dub.co/stefano"
          target="_blank"
          class="text-white hover:text-gray-400"
          >give Dub a shot</a
        >.
      </p>
      <p>
        No more banners. But this is the end. If you scroll more, you'll find
        the footer.
      </p>
    </div>
  </div>
</template>
