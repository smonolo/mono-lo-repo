import Link from 'next/link'
import data from '@/data/home.json'

export default function Home() {
  return (
    <section className="mx-auto flex h-screen w-full max-w-[1000px] flex-col justify-center px-10">
      <h1 className="text-3xl font-bold md:text-4xl">Hi there.</h1>
      <p className="mt-2 text-neutral-300 md:text-lg">
        I am a software engineer deeply passionate about the web and its
        technologies.
      </p>
      <div className="mt-2 flex w-fit flex-col gap-2 sm:flex-row sm:items-center">
        <p className="text-sm text-neutral-700 md:text-base">
          (I will try to keep this up to date)
        </p>
        <Link
          href="https://github.com/smonolo/mono-lo-repo"
          target="_blank"
          className="w-fit rounded-full border border-solid border-blue-600 bg-blue-500/20 px-2 py-0.5 text-xs font-medium text-blue-600 transition-colors hover:bg-blue-500/30"
        >
          Under development
        </Link>
      </div>
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
      <div className="mt-5 flex w-fit items-center gap-x-3 rounded-full border border-solid border-neutral-800 bg-neutral-900 px-3 py-1">
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
