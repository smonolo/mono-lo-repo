import { aibLocaleHref, type AibLocale } from '#aib/composables/useAibLocale'

const LINK_HREFS: Record<string, string> = {
  privacy: '/aib-camera/privacy',
  terms: '/aib-camera/terms',
}

/** Rewrites data-aib anchors / code markers in copy HTML for the page. */
export function aibHtml(
  html: string,
  extras: { pdfHref?: string; codeClass?: string; locale?: AibLocale } = {}
): string {
  const locale = extras.locale ?? 'it'
  let out = html.replace(
    /<a\s+data-aib="(privacy|terms|pdf)">/g,
    (_, key: string) => {
      const base =
        key === 'pdf' && extras.pdfHref
          ? extras.pdfHref
          : LINK_HREFS[key] ?? '#'
      const href = key === 'pdf' ? base : aibLocaleHref(base, locale)
      return `<a href="${href}">`
    }
  )
  if (extras.codeClass) {
    out = out.replace(
      /<code\s+data-aib="code">/g,
      `<code class="${extras.codeClass}">`
    )
  }
  return out
}
