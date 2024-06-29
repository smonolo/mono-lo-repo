<template>
  <section id="experience">
    <div class="flex items-center gap-x-4">
      <h3 class="text-2xl font-semibold">Experience</h3>
      <span
        class="h-fit rounded-md bg-[#008cff] px-2.5 py-0.5 text-sm font-medium text-white"
      >
        4 years
      </span>
    </div>
    <div class="mt-4 flex flex-col gap-y-3">
      <ExperienceCard v-for="(exp, key) in experience" :key :="exp" />
    </div>
  </section>
</template>

<script setup lang="ts">
import ExperienceCard, {
  type ExperienceCardProps,
} from '@/components/experience/ExperienceCard.vue'

defineComponent({ name: 'Experience' })

const experience: ExperienceCardProps[] = [
  {
    position: 'Frontend Engineer',
    company: 'Kampaay S.R.L.',
    period: 'August 2023 - Present',
    location: 'Viale Cassala, 30, Milan, Italy',
    achievements: [
      'Designed and developed a modern and accessible platform used by 150+ businesses across Italy and Sweden',
      'Developed and maintained an in-house internal back office used by 50+ employees across the two countries on a daily basis',
      'Attended technical and product meetings to define roadmaps and 20+ sprints by helping plan 100+ epics and organize workload',
      'Proposed and discussed customer experience and internal efficiency related initiatives to be worked on during sprints',
      'Worked on R&D initiatives including code refactoring, migration to new technologies, gRPC research and AI features',
    ],
    skills: ['TypeScript', 'Vue', 'Nuxt', 'React'],
  },
  {
    position: 'Frontend Engineer',
    company: 'EVNT Games LLC',
    period: 'November 2022 - March 2024',
    location: 'Remote, Europe',
    achievements: [
      'Migrated a Twitch Rivals tournament website to Next.js, improved SEO, created new features and completely changed its design',
      'Built the main marketing website using Next.js, used by customers to view insights and contact the company for business inquiries',
      'Developed a UEFN market related functional website used by 500+ users on a daily basis to view Fortnite maps and creators',
      'Created several private websites used by Twitch and YouTube content creators to display real-time to 5000+ active viewers',
      'Collaborated with external developers to integrate third-party services on gaming related products, such as Overwolf and Tebex',
    ],
    skills: ['TypeScript', 'React', 'Next.js'],
  },
  {
    position: 'Web Engineer',
    company: 'Moonsworth, LLC',
    period: 'January 2021 - November 2022',
    location: 'Remote, Europe',
    achievements: [
      'Worked with designers and other engineers to release a new major version of a Minecraft launcher used by 100k+ users every day',
      'Developed 2 internal UI component libraries shared across multiple products and published using GitHub Package Registry',
      'Maintained existing marketing websites and migrated the main one from Jekyll to Next.js, improving SEO and performance by 50%',
      'Helped designing new interfaces and layouts on Figma and taking technical decisions along with product managers and designers',
    ],
    skills: ['TypeScript', 'Python', 'Electron', 'React', 'Next.js'],
  },
  {
    position: 'Product Engineer',
    company: 'Medal B.V.',
    period: 'November 2020 - June 2021',
    location: 'Remote, Europe',
    achievements: [
      'Migrated a Discord bots listing website used by 25k+ users every day from Express to Next.js, improving performance and stability',
      'Collaborated with an external design agency to completely redesign the website, introducing a new design system and library',
      'Maintained an invite-only website that managed auctions based on traffic and previous engagement used by selected users and businesses',
      'Improved existing moderation tools that allowed volunteer and paid staff to review 100+ new Discord bots every day',
    ],
    skills: ['TypeScript', 'Express', 'React', 'Next.js'],
  },
  {
    position: 'Web Engineer',
    company: 'ESL FACEIT Group',
    period: 'March 2020 - December 2021',
    location: 'Remote, Europe',
    achievements: [
      'Joined a small side team of one developer and got familiar with 4 different codebases of products used by 50k+ users every day',
      'Worked on a major release of the popular Minecraft launcher Badlion Client, switching from version 2.0 to 3.0 in about 6 months',
      'Developed internal and content creator related tools to simplify daily tasks and enhance engagement and improve product marketing',
      'Reviewed and implemented user feedback and worked with backend developers to improve existing gRPC infrastructure and automation',
    ],
    skills: ['TypeScript', 'Python', 'Electron', 'React', 'Next.js'],
  },
]
</script>
