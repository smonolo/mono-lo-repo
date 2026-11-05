/**
 * Regenerates changelog HTML bodies from aib-camera/content/
 *
 * - Italian: changelog.md → assets/changelog.body.html
 * - English: changelog.en.md → assets/changelog.en.body.html
 *
 * Bodies are imported at build time (no runtime fetch). No PDF.
 *
 * Usage (from apps/site): npm run docs:aib-camera-changelog
 */
import { readFile, writeFile } from 'node:fs/promises'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { marked } from 'marked'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const assetDir = join(root, 'assets')

const docs = [
  {
    mdPath: join(root, 'content/changelog.md'),
    outBody: join(assetDir, 'changelog.body.html'),
  },
  {
    mdPath: join(root, 'content/changelog.en.md'),
    outBody: join(assetDir, 'changelog.en.body.html'),
  },
]

for (const doc of docs) {
  const raw = await readFile(doc.mdPath, 'utf8')
  const markdown = raw.replace(/^<!--[\s\S]*?-->\s*/, '')
  const body = await marked.parse(markdown)
  await writeFile(doc.outBody, body, 'utf8')
  console.log(`Wrote ${doc.outBody}`)
}
