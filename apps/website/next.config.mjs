/** @type {import('next').NextConfig} */
const nextConfig = {
  async redirects() {
    return [
      {
        source: '/about',
        destination: '/journey',
        permanent: false,
      },
      {
        source: '/work',
        destination: '/chaos',
        permanent: false,
      },
    ]
  },
}

export default nextConfig
