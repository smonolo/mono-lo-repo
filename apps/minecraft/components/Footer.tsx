export default function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="w-full">
      <div className="mx-auto flex w-full max-w-5xl items-center justify-between border-t border-gray-800 px-4 py-8 text-sm text-gray-300 md:w-[60%] md:px-0">
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
