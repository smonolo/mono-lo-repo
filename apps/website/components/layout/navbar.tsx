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
      <div className="mx-auto flex h-14 max-w-[1000px] items-center justify-between md:h-16">
        <div className="flex w-full items-center">
          <Link href="/" className="group flex w-fit items-center gap-x-2">
            <div className="bg-sm-blue h-[20px] w-[20px] rounded-sm md:h-[25px] md:w-[25px]" />
            <span className="text-sm-white text-sm font-medium underline-offset-2 group-hover:underline md:text-base">
              Stefano Monolo
            </span>
          </Link>
        </div>
        <div
          className={classNames(
            'bg-sm-black/80 absolute left-0 top-0 z-50 h-screen w-full',
            { hidden: !isMenuOpen }
          )}
          onClick={() => setMenuOpen(false)}
        >
          <div
            className="bg-sm-black absolute left-0 top-0 w-full border-b border-neutral-800"
            onClick={event => event.stopPropagation()}
          >
            <div className="mx-auto max-w-[1000px] px-5 py-8 md:px-0">
              <ul className="flex w-fit flex-col gap-2">
                {Object.entries(data.items).map(([key, value]) => (
                  <li key={key}>
                    <Link
                      href={value.url}
                      className={classNames(
                        'hover:text-sm-white text-xl font-medium text-neutral-300 underline-offset-2 transition-colors hover:underline',
                        { 'text-sm-white': pathname === value.url }
                      )}
                      onClick={() => setMenuOpen(false)}
                    >
                      {value.text}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
        <div className="flex w-fit items-center gap-x-3 md:gap-x-5">
          <ul className="flex w-fit items-center gap-x-3">
            {Object.entries(data.socials).map(([icon, url]) => (
              <li key={icon}>
                <Link
                  href={url}
                  target={url.startsWith('https://') ? '_blank' : '_self'}
                  onClick={() => setMenuOpen(false)}
                >
                  <i
                    className={`bi bi-${icon} hover:text-sm-white text-neutral-300 transition-colors`}
                  />
                </Link>
              </li>
            ))}
          </ul>
          <div className="h-6 w-px bg-neutral-800" />
          <div
            className="flex cursor-pointer flex-col gap-y-1 py-2"
            onClick={() => setMenuOpen(true)}
          >
            {Array.from({ length: 3 }).map((_, index) => (
              <div key={index} className="bg-sm-white h-px w-5" />
            ))}
          </div>
        </div>
      </div>
    </nav>
  )
}
