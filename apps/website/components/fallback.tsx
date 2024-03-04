type Props = {
  title: string
  text: string
}

export default function Fallback({ title, text }: Props) {
  return (
    <section className="flex h-screen w-full items-center justify-center">
      <div className="flex w-fit flex-col gap-y-2 text-center">
        <h1 className="text-3xl font-bold md:text-4xl">{title}</h1>
        <p className="text-neutral-300 md:text-lg">{text}</p>
      </div>
    </section>
  )
}
