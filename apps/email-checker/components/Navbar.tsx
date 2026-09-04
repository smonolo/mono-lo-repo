import Link from 'next/link'

export default function Navbar() {
  return (
    <header className="bg-sm-black w-full border-b border-neutral-800">
      <div className="mx-auto flex h-14 w-full items-center justify-between gap-x-2 px-4 sm:px-6 md:px-8">
        <Link
          href="/"
          className="flex shrink-0 items-center gap-x-2 transition-opacity hover:opacity-80"
          aria-label="Email Checker Home"
        >
          <div className="bg-sm-blue h-5 w-5 rounded sm:h-6 sm:w-6" />
          <span className="text-xs font-medium text-white sm:text-sm">
            Email Checker
          </span>
        </Link>
      </div>
    </header>
  )
}
