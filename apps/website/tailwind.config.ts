import type { Config } from 'tailwindcss'

const config: Config = {
  content: [
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    fontFamily: {
      display: ['Rowan', 'sans-serif'],
      body: ['Switzer', 'sans-serif'],
    },
    extend: {
      colors: {
        'sm-blue': '#008cff',
        'sm-black': '#121215',
        'sm-white': '#ffffff',
      },
    },
  },
}

export default config
