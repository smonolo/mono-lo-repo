'use client'

import data from '@/data/layout/navbar.json'
import classNames from 'classnames'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

export default function Navbar() {
  const pathname = usePathname()

  return (
    <nav className="fixed left-1/2 top-5 mx-auto flex w-11/12 max-w-[1000px] -translate-x-1/2 items-center justify-between gap-x-5 rounded-2xl border border-solid border-neutral-800 bg-neutral-900 px-5 py-3 drop-shadow lg:w-full">
      <Link href="/" className="flex w-fit items-center gap-x-2">
        <div className="bg-sm-blue h-[20px] w-[20px] md:h-[25px] md:w-[25px]" />
        <span className="text-sm-white text-sm font-medium md:text-base">
          Stefano Monolo
        </span>
      </Link>
      <div className="flex w-fit items-center gap-x-4">
        <div className="flex w-fit items-center gap-x-3">
          {Object.entries(data.items).map(([key, value]) => (
            <Link
              key={key}
              href={value.url}
              className={classNames(
                'hover:text-sm-white text-sm text-neutral-300 transition-colors md:text-base',
                { 'text-sm-white': pathname === value.url }
              )}
            >
              {value.text}
            </Link>
          ))}
        </div>
        <div className="h-[20px] w-[1px] bg-neutral-800" />
        <div className="flex w-fit items-center gap-x-3">
          {Object.entries(data.socials).map(([icon, url]) => (
            <Link
              key={icon}
              href={url}
              target={url.startsWith('https://') ? '_blank' : '_self'}
            >
              <i
                className={`bi bi-${icon} hover:text-sm-white text-sm text-neutral-300 transition-colors`}
              />
            </Link>
          ))}
        </div>
      </div>
    </nav>
  )
}
