import data from '@/data/pages/chaos.json'
import ProjectCard from '@/components/pages/chaos/project-card'
import { useMemo } from 'react'

type Props = {
  filter: string
}

export default function ProjectsList({ filter }: Props) {
  const filteredItems = useMemo(() => {
    const sortedItems = data.items.sort((a) => (a.current ? -1 : 1))

    if (filter) {
      const lowerFilter = filter.toLowerCase()

      return sortedItems.filter((i) =>
        [i.project, i.company, i.website].some((v) =>
          v?.toLowerCase().includes(lowerFilter)
        )
      )
    }

    return sortedItems
  }, [filter])

  return (
    <div className="mt-6 flex w-full flex-col">
      {filteredItems.map((item, index) => (
        <ProjectCard key={index} {...item} />
      ))}
    </div>
  )
}
