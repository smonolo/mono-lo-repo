'use client'

import data from '@/data/layout/navbar.json'
import classNames from 'classnames'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

export default function Navbar() {
  const pathname = usePathname()

  return (
    <nav className="fixed left-0 top-0 grid w-full grid-cols-3 border-b border-solid border-neutral-800 px-5 drop-shadow backdrop-blur-md lg:w-full">
      <div className="flex w-full items-center justify-center py-3">
        <Link href="/" className="flex w-fit items-center gap-x-2">
          <div className="bg-sm-blue h-[20px] w-[20px] rounded-sm md:h-[25px] md:w-[25px]" />
          <span className="text-sm-white text-sm font-medium md:text-base">
            Stefano Monolo
          </span>
        </Link>
      </div>
      <div className="flex w-full items-center justify-center gap-x-5 border-x border-dashed border-neutral-800 py-3">
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
      <div className="flex w-full items-center justify-center gap-x-3 py-3">
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
    </nav>
  )
}
