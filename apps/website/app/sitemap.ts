import type { MetadataRoute } from 'next'

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    {
      url: 'https://smnl.dev/',
      priority: 1,
    },
  ]
}
