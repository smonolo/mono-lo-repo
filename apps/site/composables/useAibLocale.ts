export type AibLocale = 'it' | 'en'

const STATE = 'aib-lang'
const QUERY_KEY = 'lang'

/** Parse a `lang` query value into a locale, or null if absent/invalid. */
export function parseAibLocale(value: unknown): AibLocale | null {
  const raw = Array.isArray(value) ? value[0] : value
  if (raw === 'en' || raw === 'it') return raw
  return null
}

export function normalizeAibLocale(value: unknown): AibLocale {
  return parseAibLocale(value) ?? 'it'
}

/** Path (+ optional query) that keeps the current site language shareable. */
export function withAibLocale(
  path: string,
  locale: AibLocale
): string | { path: string; query: { lang: 'en' } } {
  if (locale === 'en') {
    return { path, query: { lang: 'en' } }
  }
  return path
}

/** Absolute or relative href string with `?lang=en` when English. */
export function aibLocaleHref(path: string, locale: AibLocale): string {
  if (locale === 'en') {
    const sep = path.includes('?') ? '&' : '?'
    return `${path}${sep}lang=en`
  }
  return path
}

export function useAibLocale() {
  const route = useRoute()
  const router = useRouter()

  const locale = useState<AibLocale>(STATE, () =>
    normalizeAibLocale(route.query[QUERY_KEY])
  )

  const isEn = computed(() => locale.value === 'en')

  function applyLocale(next: AibLocale) {
    locale.value = normalizeAibLocale(next)
  }

  function syncQuery(next: AibLocale) {
    const current = parseAibLocale(route.query[QUERY_KEY])
    const desired = next === 'en' ? 'en' : null
    if (current === desired) return

    const query = { ...route.query }
    if (desired) {
      query[QUERY_KEY] = desired
    } else {
      delete query[QUERY_KEY]
    }
    void router.replace({ path: route.path, query, hash: route.hash })
  }

  function setLocale(next: AibLocale) {
    const value = normalizeAibLocale(next)
    applyLocale(value)
    syncQuery(value)
  }

  function toggleLocale() {
    setLocale(locale.value === 'it' ? 'en' : 'it')
  }

  function localePath(path: string) {
    return withAibLocale(path, locale.value)
  }

  // URL is source of truth: missing/invalid `lang` → Italian.
  watch(
    () => route.query[QUERY_KEY],
    value => {
      applyLocale(normalizeAibLocale(value))
    },
    { immediate: true }
  )

  useHead(() => ({
    htmlAttrs: { lang: locale.value },
  }))

  return {
    locale,
    isEn,
    setLocale,
    toggleLocale,
    localePath,
  }
}
