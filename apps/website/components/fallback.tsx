import Link from 'next/link'

type Props = {
  title: string
  text: string
}

export default function Fallback({ title, text }: Props) {
  return (
    <section className="flex w-full justify-center py-20">
      <div className="flex w-fit flex-col gap-y-2 text-center">
        <h1 className="text-3xl font-bold md:text-4xl">{title}</h1>
        <p className="text-neutral-300 md:text-lg">{text}</p>
        <Link
          href="/"
          className="text-sm-white hover:text-sm-blue mx-auto mt-4 w-fit underline underline-offset-2 transition-colors md:text-lg"
        >
          Go to safety
        </Link>
      </div>
    </section>
  )
}
