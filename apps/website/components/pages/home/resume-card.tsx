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
    <div className="mt-10 rounded-2xl bg-neutral-800/60 p-10 md:rounded-3xl md:p-12">
      <h3 className="font-display text-2xl font-semibold">
        Looking for my resume?
      </h3>
      <p className="mt-2 text-sm text-neutral-300 md:text-base">
        You are (not) in the right place! I made a site for that, which
        represents how the PDF version looks but with a bit of responsiveness
        for those who are always on the phone.
      </p>
      <div className="mt-5 flex items-center gap-x-2">
        <input
          id="resume-checkbox"
          type="checkbox"
          onChange={(event) => setChecked(event.target.checked)}
        />
        <label htmlFor="resume-checkbox" className="text-sm text-neutral-300">
          I understand that the resume does not have a dark theme, and that I
          have prepared my eyes for the light.
        </label>
      </div>
      <button
        className={classNames(
          'bg-sm-blue text-sm-white disabled:text-sm-white/50 disabled:bg-sm-blue/50 group z-10 mt-5 flex items-center gap-x-2 rounded-lg px-4 py-2 font-medium transition-colors',
          { 'hover:bg-sm-blue/80': isChecked },
          isChecked ? 'cursor-pointer' : 'cursor-not-allowed'
        )}
        disabled={!isChecked}
        onClick={goToResume}
      >
        <span>Take me there</span>
        <i
          className={classNames(
            'bi bi-arrow-right relative transition-transform',
            { 'group-hover:translate-x-1': isChecked }
          )}
        />
      </button>
    </div>
  )
}
