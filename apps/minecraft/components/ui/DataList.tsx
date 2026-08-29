import type { ReactNode } from 'react'

export type DataListItem = {
  label: string
  value: ReactNode
}

type Props = {
  items: DataListItem[]
  className?: string
}

export function DataList({ items, className = '' }: Props) {
  return (
    <div className={`divide-y divide-neutral-800 text-sm ${className}`}>
      {items.map(item => (
        <div
          key={item.label}
          className="flex items-center justify-between py-2.5"
        >
          <span className="text-gray-400">{item.label}</span>
          <span suppressHydrationWarning className="font-normal text-white">
            {item.value}
          </span>
        </div>
      ))}
    </div>
  )
}
