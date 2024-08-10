import classNames from 'classnames'
import Link from 'next/link'
import type { Experience } from '@/components/common/experiences/experiences-list'

export default function ExperienceCard({
  brand,
  company,
  website,
  position,
  startYear,
  endYear,
  description,
  projects,
}: Experience) {
  return (
    <div
      className={classNames(
        'w-full rounded-2xl p-6 md:p-8',
        typeof endYear === 'string'
          ? 'from-sm-blue/30 to-sm-blue/20 bg-gradient-to-b'
          : 'bg-neutral-800/40'
      )}
    >
      <div className="flex items-baseline gap-x-1">
        <span className="font-medium">{position}</span>
        <span className="text-sm text-neutral-400">at</span>
        <Link
          href={website}
          target="_blank"
          className="hover:text-sm-blue font-medium underline underline-offset-2 transition-colors"
        >
          {brand}
        </Link>
      </div>
      <span className="text-sm text-neutral-400">
        {company} · {startYear}-{endYear}
      </span>
      <p className="mt-5 text-sm text-neutral-300">{description}</p>
      {!!projects?.length && (
        <div className="mt-5 flex w-fit items-center gap-x-2">
          {projects?.map((project, index) => (
            <span
              key={index}
              className="h-fit rounded-full border border-blue-600 bg-blue-900 px-1.5 text-xs font-medium text-blue-300"
            >
              {project}
            </span>
          ))}
        </div>
      )}
    </div>
  )
}
