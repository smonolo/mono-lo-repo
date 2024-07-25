import data from '@/data/layout/navbar.json'
import Link from 'next/link'

export default function PagesList() {
  return (
    <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-3">
      {Object.values(data.items).map((item, index) => (
        <Link
          key={index}
          href={item.url}
          className="group flex w-full items-center justify-center gap-x-2 rounded-xl bg-gradient-to-b from-slate-700/50 to-slate-800/50 p-5 text-lg font-medium transition-colors hover:from-slate-700/60 hover:to-slate-800/60 md:p-7"
        >
          <span>{item.text}</span>
          <i className="bi bi-arrow-right relative right-0 text-sm transition-[right] group-hover:-right-1.5" />
        </Link>
      ))}
    </div>
  )
}
