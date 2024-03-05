import Link from 'next/link'

export default function Home() {
  return (
    <section>
      <h1 className="text-3xl font-bold md:text-4xl">Hi there.</h1>
      <p className="mt-2 text-neutral-300 md:text-lg">
        I am a software engineer deeply passionate about the web and its
        technologies.
      </p>
      <p className="mt-6 text-sm text-neutral-300 md:text-base">
        Want to know more? Take a look at my{' '}
        <Link
          href="https://prev.smnl.dev"
          target="_blank"
          className="text-sm-white hover:text-sm-blue underline underline-offset-2 transition-colors"
        >
          contentful site
        </Link>{' '}
        while this is under construction.
      </p>
    </section>
  )
}
