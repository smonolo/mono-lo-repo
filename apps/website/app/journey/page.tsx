import ExperiencesList from '@/components/common/experiences/experiences-list'
import data from '@/data/pages/journey.json'

export default function Page() {
  return (
    <section>
      <h1 className="font-display text-3xl font-bold md:text-4xl">Journey</h1>
      <ExperiencesList experiences={data.experiences} />
    </section>
  )
}
