import ResumeCard from '@/components/pages/home/resume-card'

export default function Home() {
  return (
    <section>
      <h1 className="font-display text-3xl font-bold md:text-4xl">
        Hi there 👋
      </h1>
      <p className="mt-2 text-neutral-300 md:text-lg">
        I&apos;m a software engineer passionate about the web and its
        technologies.
      </p>
      <ResumeCard />
    </section>
  )
}
