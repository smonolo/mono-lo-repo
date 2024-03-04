import Link from 'next/link'
import data from '@/data/home.json'

export default function Home() {
  return (
    <section className="mx-auto flex h-screen w-full max-w-[1000px] flex-col justify-center">
      <h1 className="text-3xl font-bold md:text-4xl">Hi there.</h1>
      <p className="mt-2 text-neutral-300 md:text-lg">
        I am a software engineer deeply passionate about the web and its
        technologies.
      </p>
      <p className="mt-2 text-neutral-700">
        (I will try to keep this up to date)
      </p>
      <p className="mt-6 text-neutral-300">
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
      <div className="mt-5 flex w-fit items-center gap-x-3 rounded-full border border-solid border-neutral-800 bg-neutral-900 px-3 py-1.5">
        {Object.entries(data.socials).map(([icon, url]) => (
          <Link
            key={icon}
            href={url}
            target={url.startsWith('https://') ? '_blank' : '_self'}
          >
            <i
              className={`bi bi-${icon} hover:text-sm-white text-sm text-neutral-300 transition-colors`}
            />
          </Link>
        ))}
      </div>
    </section>
  )
}
