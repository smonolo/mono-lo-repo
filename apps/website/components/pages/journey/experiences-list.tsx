import data from '@/data/pages/journey.json'
import ExperienceCard from '@/components/pages/journey/experience-card'

export default function ExperiencesList() {
  return (
    <div className="mt-10 flex w-full flex-col gap-y-5">
      {data.experiences.map((experience, index) => (
        <ExperienceCard key={index} {...experience} />
      ))}
    </div>
  )
}
