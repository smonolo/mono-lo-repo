'use client'

import Fallback from '@/components/fallback'

export default function Error() {
  return (
    <Fallback
      title="It did not work"
      text="Something weird happened but it looks fine now. Go back!"
    />
  )
}
