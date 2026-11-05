import type { AibLocale } from '#aib/composables/useAibLocale'
import enBody from '#aib/assets/changelog.en.body.html?raw'
import itBody from '#aib/assets/changelog.body.html?raw'

export function aibChangelogBody(locale: AibLocale): string {
  return locale === 'en' ? enBody : itBody
}
