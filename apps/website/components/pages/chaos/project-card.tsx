import Link from 'next/link'
import { useMemo } from 'react'

type Props = {
  project: string
  company?: string
  year?: number
  website: string
  current?: boolean
}

export default function ProjectCard({
  project,
  company,
  year,
  website,
  current,
}: Props) {
  const projectInfo = useMemo(
    () => [company, year].filter(Boolean).join(' - '),
    [company, year]
  )

  return (
    <div className="flex w-full items-center justify-between gap-x-4 border-b border-solid border-neutral-800 px-2 py-4">
      <div className="flex w-fit flex-col gap-y-0.5">
        <div className="flex w-fit items-center gap-x-2">
          <h4 className="font-medium">{project}</h4>
          {current && (
            <span className="h-fit rounded-full border border-solid border-blue-600 bg-blue-900 px-1.5 text-[10px] text-blue-300">
              Current
            </span>
          )}
        </div>
        {projectInfo && (
          <div className="text-xs text-neutral-400">{projectInfo}</div>
        )}
      </div>
      <Link
        href={website}
        target="_blank"
        className="flex w-fit items-baseline gap-x-2"
      >
        <span className="hover:text-sm-white text-sm text-neutral-300 underline underline-offset-2 transition-colors">
          {website.replace('https://', '').replace('www.', '')}
        </span>
        <i className="bi bi-chevron-right text-xs text-neutral-500" />
      </Link>
    </div>
  )
}
