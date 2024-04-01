import ExperienceCard from '@/components/common/experiences/experience-card'

export type Experience = {
  brand: string
  company: string
  website: string
  position: string
  startYear: number
  endYear: number | string
  description: string
  projects?: string[]
}

type Props = {
  experiences: Experience[]
}

export default function ExperiencesList({ experiences }: Props) {
  return (
    <div className="mt-10 flex w-full flex-col gap-y-5">
      {experiences.map((experience, index) => (
        <ExperienceCard key={index} {...experience} />
      ))}
    </div>
  )
}
