import type { AibLocale } from '~/composables/useAibLocale'
import enBody from '~/assets/aib-camera/field-guide.body.html?raw'
import itBody from '~/assets/aib-camera/guida-operativa.body.html?raw'

/** Guide HTML bundled at build time — no runtime fetch / filesystem read. */
export function aibGuideBody(locale: AibLocale): string {
  return locale === 'en' ? enBody : itBody
}
