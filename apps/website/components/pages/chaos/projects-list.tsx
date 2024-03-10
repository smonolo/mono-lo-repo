import data from '@/data/pages/chaos.json'
import ProjectCard from '@/components/pages/chaos/project-card'
import { useMemo } from 'react'

type Props = {
  filter: string
}

export default function ProjectsList({ filter }: Props) {
  const filteredItems = useMemo(() => {
    if (filter) {
      const lowerFilter = filter.toLowerCase()

      return data.items.filter((i) =>
        [i.project, i.company, i.website].some((v) =>
          v?.toLowerCase().includes(lowerFilter)
        )
      )
    }

    return data.items
  }, [filter])

  return (
    <div className="mt-6 flex w-full flex-col">
      {filteredItems.map((item, index) => (
        <ProjectCard key={index} {...item} />
      ))}
    </div>
  )
}
