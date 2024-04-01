import ExperiencesList from '@/components/common/experiences/experiences-list'
import data from '@/data/pages/attempts.json'

export default function Page() {
  return (
    <section>
      <h1 className="text-3xl font-bold md:text-4xl">Attempts</h1>
      <ExperiencesList experiences={data.experiences} />
    </section>
  )
}
