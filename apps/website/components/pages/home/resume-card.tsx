'use client'

import classNames from 'classnames'
import { useCallback, useState } from 'react'

export default function ResumeCard() {
  const [isChecked, setChecked] = useState<boolean>(false)

  const goToResume = useCallback(() => {
    if (isChecked) {
      window.open('https://resume.smnl.dev', '_blank')
    }
  }, [isChecked])

  return (
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
      <button
        className={classNames(
          'bg-sm-black text-sm-white disabled:text-sm-white/50 disabled:bg-sm-black/50 z-10 mt-4 rounded-lg px-4 py-2 font-medium transition-colors',
          { 'hover:bg-sm-white hover:text-sm-black': isChecked }
        )}
        disabled={!isChecked}
        onClick={goToResume}
      >
        Take me there
      </button>
    </div>
  )
}
