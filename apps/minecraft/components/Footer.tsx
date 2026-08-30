export default function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="mx-auto w-full max-w-6xl px-4 sm:px-6 lg:px-8">
      <div className="flex w-full items-center justify-between border-t border-neutral-800 py-4 text-xs text-neutral-400">
        <p>© {currentYear} Stefano Monolo.</p>
        <p className="underline transition-colors hover:text-white">
          <a
            href="https://git.new/monorepo"
            target="_blank"
            rel="noopener noreferrer"
          >
            Source code on GitHub
          </a>
        </p>
      </div>
    </footer>
  )
}
