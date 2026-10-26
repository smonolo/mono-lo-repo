export type AibLocale = 'it' | 'en'

const COOKIE = 'aib-lang'
const STATE = 'aib-lang'

function normalizeLocale(value: unknown): AibLocale {
  return value === 'en' ? 'en' : 'it'
}

export function useAibLocale() {
  const cookie = useCookie<string>(COOKIE, {
    default: () => 'it',
    sameSite: 'lax',
    maxAge: 60 * 60 * 24 * 365,
  })

  const locale = useState<AibLocale>(STATE, () => normalizeLocale(cookie.value))

  const isEn = computed(() => locale.value === 'en')

  function setLocale(next: AibLocale) {
    const value = normalizeLocale(next)
    locale.value = value
    cookie.value = value
  }

  function toggleLocale() {
    setLocale(locale.value === 'it' ? 'en' : 'it')
  }

  useHead(() => ({
    htmlAttrs: { lang: locale.value },
  }))

  return {
    locale,
    isEn,
    setLocale,
    toggleLocale,
  }
}
