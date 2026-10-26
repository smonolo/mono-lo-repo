/**
 * Regenerates guide PDFs + HTML bodies from aib-camera/content/
 *
 * - Italian: guida-operativa.md
 *     → public/docs/aib-camera-guida-operativa.pdf
 *     → assets/guida-operativa.body.html (bundled into the site)
 * - English: guida-operativa.en.md
 *     → public/docs/aib-camera-field-guide.pdf
 *     → assets/field-guide.body.html (bundled into the site)
 *
 * PDFs stay under public/docs/; brand images under public/aib-assets/ —
 * never public/aib-camera/, which would collide with pages/aib-camera and
 * force a trailing slash on the home route.
 * Bodies are imported at build time so SSR/Vercel never fetch or read them.
 *
 * Usage (from apps/site): npm run docs:aib-camera-guida
 */
import { existsSync } from 'node:fs'
import { mkdir, readFile, writeFile } from 'node:fs/promises'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { marked } from 'marked'
import puppeteer from 'puppeteer'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const outDir = join(root, 'public/docs')
const assetDir = join(root, 'assets')

const DOC_VERSION_IT = '26 ottobre 2026'
const DOC_VERSION_EN = '26 October 2026'
const APP_VERSION = '1.0.0'

const docs = [
  {
    mdPath: join(root, 'content/guida-operativa.md'),
    outPdf: join(outDir, 'aib-camera-guida-operativa.pdf'),
    outBody: join(assetDir, 'guida-operativa.body.html'),
    lang: 'it',
    title: 'AIB Camera — Guida operativa',
    footerLabel: `AIB Camera — Guida operativa · app ${APP_VERSION} · ${DOC_VERSION_IT}`,
    pageLabel: 'Pagina',
  },
  {
    mdPath: join(root, 'content/guida-operativa.en.md'),
    outPdf: join(outDir, 'aib-camera-field-guide.pdf'),
    outBody: join(assetDir, 'field-guide.body.html'),
    lang: 'en',
    title: 'AIB Camera — Field guide',
    footerLabel: `AIB Camera — Field guide · app ${APP_VERSION} · ${DOC_VERSION_EN}`,
    pageLabel: 'Page',
  },
]

const sharedCss = `
  * { box-sizing: border-box; }
  :root {
    --ink: #121215;
    --muted: #3f3f46;
    --line: #d4d4d8;
    --wash: #f4f4f5;
    --accent: #0b5fff;
    --warn-bg: #fff7ed;
    --warn-border: #fb923c;
    --info-bg: #f0f9ff;
    --info-border: #38bdf8;
  }
  body {
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    font-size: 10.5pt;
    line-height: 1.5;
    color: var(--ink);
    margin: 0;
  }
  h1 {
    font-size: 20pt;
    margin: 0 0 0.55em;
    line-height: 1.2;
    letter-spacing: -0.01em;
  }
  h2 {
    font-size: 12.5pt;
    margin: 1.55em 0 0.5em;
    padding: 0.35em 0 0.3em;
    border-bottom: 1.5px solid var(--ink);
    page-break-after: avoid;
    break-after: avoid;
  }
  h3 {
    font-size: 11pt;
    margin: 1.1em 0 0.35em;
    page-break-after: avoid;
    break-after: avoid;
  }
  p, ul, ol, table { margin: 0 0 0.75em; }
  ul, ol { padding-left: 1.3em; }
  li { margin-bottom: 0.28em; }
  li::marker { color: var(--muted); }
  strong { font-weight: 600; }
  em { color: var(--muted); font-style: italic; }
  a { color: var(--accent); text-decoration: none; }
  hr {
    border: none;
    border-top: 1px solid var(--line);
    margin: 1.15em 0;
  }
  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 9.4pt;
    page-break-inside: avoid;
    break-inside: avoid;
  }
  th, td {
    border: 1px solid var(--line);
    padding: 0.45em 0.6em;
    text-align: left;
    vertical-align: top;
  }
  th { background: var(--wash); font-weight: 600; }
  code {
    font-family: ui-monospace, "SF Mono", Menlo, monospace;
    font-size: 8.8pt;
    background: var(--wash);
    padding: 0.08em 0.32em;
    border-radius: 3px;
  }
  .callout {
    background: var(--info-bg);
    border-left: 3px solid var(--info-border);
    padding: 0.7em 0.9em;
    margin: 0.9em 0 1em;
    page-break-inside: avoid;
    break-inside: avoid;
  }
  .callout-warn {
    background: var(--warn-bg);
    border-left-color: var(--warn-border);
  }
  .callout p { margin: 0; }
  .callout p + p { margin-top: 0.45em; }
  .doc-meta {
    width: 100%;
    margin: 0 0 1em;
    font-size: 9.5pt;
  }
  .doc-meta td {
    border: none;
    border-bottom: 1px solid var(--line);
    padding: 0.35em 0.4em 0.35em 0;
  }
  .doc-meta tr:last-child td { border-bottom: none; }
`

const executablePath =
  process.env.PUPPETEER_EXECUTABLE_PATH ||
  [
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    '/Applications/Chromium.app/Contents/MacOS/Chromium',
  ].find(path => {
    try {
      return existsSync(path)
    } catch {
      return false
    }
  })

await mkdir(outDir, { recursive: true })
await mkdir(assetDir, { recursive: true })

const browser = await puppeteer.launch({
  headless: true,
  ...(executablePath ? { executablePath } : {}),
  args: ['--no-sandbox', '--disable-setuid-sandbox'],
})

try {
  for (const doc of docs) {
    const raw = await readFile(doc.mdPath, 'utf8')
    const markdown = raw.replace(/^<!--[\s\S]*?-->\s*/, '')
    const body = await marked.parse(markdown)
    await writeFile(doc.outBody, body, 'utf8')

    const html = `<!DOCTYPE html>
<html lang="${doc.lang}">
<head>
  <meta charset="utf-8" />
  <title>${doc.title}</title>
  <style>
    @page { size: A4; margin: 16mm 15mm 18mm; }
    ${sharedCss}
    h1 + hr { display: none; }
  </style>
</head>
<body>
${body}
</body>
</html>`

    const page = await browser.newPage()
    await page.setContent(html, { waitUntil: 'networkidle0' })
    const pdf = await page.pdf({
      format: 'A4',
      printBackground: true,
      displayHeaderFooter: true,
      headerTemplate: `<div></div>`,
      footerTemplate: `
      <div style="width:100%;font-size:8px;color:#71717a;padding:0 15mm;display:flex;justify-content:space-between;font-family:Helvetica,Arial,sans-serif;">
        <span>${doc.footerLabel}</span>
        <span>${doc.pageLabel} <span class="pageNumber"></span> / <span class="totalPages"></span></span>
      </div>
    `,
      margin: {
        top: '14mm',
        right: '14mm',
        bottom: '16mm',
        left: '14mm',
      },
    })
    await writeFile(doc.outPdf, pdf)
    await page.close()
    console.log(`Wrote ${doc.outPdf}`)
    console.log(`Wrote ${doc.outBody}`)
  }
} finally {
  await browser.close()
}
