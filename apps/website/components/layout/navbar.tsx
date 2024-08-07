'use client'

import data from '@/data/layout/navbar.json'
import classNames from 'classnames'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useState } from 'react'

export default function Navbar() {
  const pathname = usePathname()
  const [isMenuOpen, setMenuOpen] = useState<boolean>(false)

  return (
    <nav className="fixed left-0 top-0 w-full border-b border-neutral-800 px-5 drop-shadow backdrop-blur-md lg:w-full">
      <div className="mx-auto flex max-w-[1000px] items-center justify-between py-4">
        <div className="flex w-full items-center">
          <Link href="/" className="flex w-fit items-center gap-x-2">
            <div className="bg-sm-blue h-[20px] w-[20px] rounded-sm md:h-[25px] md:w-[25px]" />
            <span className="text-sm-white text-sm font-medium md:text-base">
              Stefano Monolo
            </span>
          </Link>
        </div>
        <div
          className={classNames(
            'bg-sm-black/80 absolute left-0 top-0 z-50 h-screen w-full md:relative md:block md:h-fit md:bg-transparent',
            { hidden: !isMenuOpen }
          )}
          onClick={() => setMenuOpen(false)}
        >
          <div
            className="bg-sm-black absolute left-0 top-0 flex w-full flex-col-reverse gap-x-4 gap-y-4 border-b border-neutral-800 px-5 py-8 md:relative md:flex-row md:items-center md:justify-end md:border-none md:bg-transparent md:p-0"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="flex w-fit items-center gap-x-3">
              {Object.entries(data.socials).map(([icon, url]) => (
                <Link
                  key={icon}
                  href={url}
                  target={url.startsWith('https://') ? '_blank' : '_self'}
                  onClick={() => setMenuOpen(false)}
                >
                  <i
                    className={`bi bi-${icon} hover:text-sm-white text-sm text-neutral-300 transition-colors`}
                  />
                </Link>
              ))}
            </div>
            <div className="hidden h-[20px] w-[1px] bg-neutral-800 md:block" />
            <div className="flex w-fit items-center gap-x-3">
              {Object.entries(data.items).map(([key, value]) => (
                <Link
                  key={key}
                  href={value.url}
                  className={classNames(
                    'hover:text-sm-white text-sm text-neutral-300 transition-colors md:text-base',
                    { 'text-sm-white': pathname === value.url }
                  )}
                  onClick={() => setMenuOpen(false)}
                >
                  {value.text}
                </Link>
              ))}
            </div>
          </div>
        </div>
        <div
          className="flex cursor-pointer flex-col gap-y-1 md:hidden"
          onClick={() => setMenuOpen(true)}
        >
          {Array.from({ length: 3 }).map((_, index) => (
            <div key={index} className="bg-sm-white h-px w-5" />
          ))}
        </div>
      </div>
    </nav>
  )
}
