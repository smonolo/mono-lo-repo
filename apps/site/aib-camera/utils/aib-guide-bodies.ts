import type { AibLocale } from '#aib/composables/useAibLocale'
import enBody from '#aib/assets/field-guide.body.html?raw'
import itBody from '#aib/assets/guida-operativa.body.html?raw'

/** Guide HTML bundled at build time — no runtime fetch / filesystem read. */
export function aibGuideBody(locale: AibLocale): string {
  return locale === 'en' ? enBody : itBody
}
