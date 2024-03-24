import classNames from 'classnames'
import Link from 'next/link'

type Props = {
  brand: string
  company: string
  website: string
  position: string
  startYear: number
  endYear: number | string
  description: string
  projects?: string[]
}

export default function ExperienceCard({
  brand,
  company,
  website,
  position,
  startYear,
  endYear,
  description,
  projects,
}: Props) {
  return (
    <div
      className={classNames(
        'w-full border-l-2 border-solid px-5',
        typeof endYear === 'string'
          ? 'border-blue-600 bg-blue-950/30 py-5'
          : 'border-neutral-700 py-3'
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
        {company}, {startYear} - {endYear}
      </span>
      <p className="mt-5 text-sm text-neutral-300">{description}</p>
      {!!projects?.length && (
        <div className="mt-5 flex w-fit items-center gap-x-2">
          {projects?.map((project, index) => (
            <span
              key={index}
              className="h-fit rounded-full border border-solid border-blue-600 bg-blue-900 px-1.5 text-xs font-medium text-blue-300"
            >
              {project}
            </span>
          ))}
        </div>
      )}
    </div>
  )
}
