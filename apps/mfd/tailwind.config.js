export default {
  content: [
    './components/**/*.{js,vue,ts}',
    './layouts/**/*.vue',
    './pages/**/*.vue',
    './plugins/**/*.{js,ts}',
    './app.vue',
    './error.vue',
  ],
  theme: {
    extend: {
      fontFamily: {
        mono: ['"Geist Mono"', 'monospace'],
        sans: ['"Geist Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
  darkMode: ['selector'],
}
