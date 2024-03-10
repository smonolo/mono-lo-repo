type Props = {
  setFilter: (value: string) => void
}

export default function ProjectsHeader({ setFilter }: Props) {
  return (
    <div className="flex w-full items-center justify-between gap-x-4">
      <h1 className="text-3xl font-bold md:text-4xl">Chaos</h1>
      <input
        type="text"
        placeholder="Search..."
        className="w-[220px] rounded-full border border-solid border-neutral-800 bg-neutral-900 px-3 py-1.5 text-sm outline-none placeholder:text-neutral-600"
        onChange={(event) => setFilter(event.target.value)}
      />
    </div>
  )
}
