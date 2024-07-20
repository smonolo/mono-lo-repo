'use client'

import classNames from 'classnames'
import Link from 'next/link'
import { useMemo, useState } from 'react'

export default function Home() {
  const [checked, setChecked] = useState<boolean>(false)

  const linkProps = useMemo(
    () => ({
      href: checked ? 'https://resume.smnl.dev' : '',
      target: checked ? '_blank' : '_self',
    }),
    [checked]
  )

  return (
    <section>
      <h1 className="text-3xl font-bold md:text-4xl">Hi there.</h1>
      <p className="mt-2 text-neutral-300 md:text-lg">
        I am a software engineer deeply passionate about the web and its
        technologies.
      </p>
      <div className="from-sm-blue/70 to-sm-blue/50 mt-6 rounded-xl bg-gradient-to-b px-10 py-8">
        <h3 className="text-2xl font-semibold">Looking for my resume?</h3>
        <p className="mt-2">
          You are (not) in the right place! I made a site for that, which
          represents exactly how the PDF version looks but with a bit of
          responsiveness for those who are always on the phone.
        </p>
        <div className="mt-4 flex items-center gap-x-2">
          <input
            type="checkbox"
            onChange={(event) => setChecked(event.target.checked)}
          />
          <p className="text-sm text-neutral-300">
            I understand that the resume does not have a dark theme, and that I
            have prepared my eyes for the light.
          </p>
        </div>
        <Link {...linkProps} className="mt-4 block w-fit">
          <button
            className={classNames(
              'bg-sm-black text-sm-white z-10 rounded-lg px-4 py-2 font-medium transition-colors disabled:opacity-50',
              { 'hover:bg-sm-white hover:text-sm-black': checked }
            )}
            disabled={!checked}
          >
            Take me there
          </button>
        </Link>
      </div>
    </section>
  )
}
