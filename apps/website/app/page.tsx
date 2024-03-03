import Link from 'next/link'

export default function Home() {
  return (
    <section className="flex h-screen w-full items-center justify-center">
      <div className="flex w-fit flex-col gap-y-4 text-center">
        <h1 className="text-4xl font-bold md:text-5xl">Not yet</h1>
        <p className="text-neutral-300 md:text-lg">
          Website is{' '}
          <Link
            href="https://github.com/smonolo/mono-lo-repo"
            target="_blank"
            className="hover:text-sm-white underline underline-offset-2"
          >
            under construction
          </Link>
          . Visit{' '}
          <Link
            href="https://smnl.dev"
            target="_blank"
            className="hover:text-sm-white underline underline-offset-2"
          >
            smnl.dev
          </Link>{' '}
          for now.
        </p>
      </div>
    </section>
  )
}
