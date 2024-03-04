import Fallback from '@/components/fallback'

export default function NotFound() {
  return (
    <Fallback
      title="Definitely not"
      text="It looks like you visited the wrong page. Go back to safety now!"
    />
  )
}
