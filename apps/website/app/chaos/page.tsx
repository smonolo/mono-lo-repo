'use client'

import ProjectsHeader from '@/components/pages/chaos/projects-header'
import ProjectsList from '@/components/pages/chaos/projects-list'
import { useState } from 'react'

export default function Page() {
  const [filter, setFilter] = useState('')

  return (
    <section>
      <ProjectsHeader setFilter={setFilter} />
      <ProjectsList filter={filter} />
    </section>
  )
}
