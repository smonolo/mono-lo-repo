type Props = {
  setFilter: (value: string) => void
}

export default function ProjectsHeader({ setFilter }: Props) {
  return (
    <div className="flex w-full items-center justify-between gap-x-4">
      <h1 className="font-display text-3xl font-bold md:text-4xl">Chaos</h1>
      <input
        type="text"
        placeholder="Search..."
        className="w-[220px] rounded-lg border border-neutral-800 bg-neutral-800/40 px-3 py-2 text-sm outline-none placeholder:text-neutral-500"
        onChange={(event) => setFilter(event.target.value)}
      />
    </div>
  )
}
