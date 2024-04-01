import Link from 'next/link'

export default function Footer() {
  return (
    <footer className="mx-auto flex w-full max-w-[1000px] items-center justify-between gap-x-5 border-t border-solid border-neutral-800 px-5 py-4 lg:px-0">
      <span className="text-sm text-neutral-300">
        © {new Date().getFullYear()} Stefano Monolo
      </span>
      <Link
        href="https://git.new/monorepo"
        target="_blank"
        className="hover:text-sm-white text-sm text-neutral-300 underline underline-offset-2 transition-colors"
      >
        Source code on GitHub
      </Link>
    </footer>
  )
}
