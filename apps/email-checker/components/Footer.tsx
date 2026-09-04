export default function Footer() {
  const currentYear = new Date().getFullYear()
  const authorName = process.env.NEXT_PUBLIC_AUTHOR_NAME || 'Stefano Monolo'
  const repoUrl = process.env.NEXT_PUBLIC_REPO_URL || 'https://git.new/monorepo'

  return (
    <footer className="mx-auto w-full max-w-6xl px-4 sm:px-6 lg:px-8">
      <div className="flex w-full items-center justify-between border-t border-neutral-800 py-4 text-xs text-neutral-400">
        <p>© {currentYear} {authorName}.</p>
        <p className="underline transition-colors hover:text-white">
          <a
            href={repoUrl}
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
