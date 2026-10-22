/**
 * Regenerates public/docs/aib-camera-guida-operativa.pdf (and .body.html)
 * from content/aib-camera/guida-operativa.md
 *
 * Assets live under public/docs/ (not public/aib-camera/) so the static
 * directory does not collide with pages/aib-camera and force a trailing slash.
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
const mdPath = join(root, 'content/aib-camera/guida-operativa.md')
const outDir = join(root, 'public/docs')
const outPdf = join(outDir, 'aib-camera-guida-operativa.pdf')
const outBody = join(outDir, 'aib-camera-guida-operativa.body.html')

const DOC_VERSION = '22 ottobre 2026'
const APP_VERSION = '1.0.0'

const raw = await readFile(mdPath, 'utf8')
const markdown = raw.replace(/^<!--[\s\S]*?-->\s*/, '')
const body = await marked.parse(markdown)

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

const html = `<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="utf-8" />
  <title>AIB Camera — Guida operativa</title>
  <style>
    @page { size: A4; margin: 16mm 15mm 18mm; }
    ${sharedCss}
    /* Meta table right after h1: tighten first block */
    h1 + hr { display: none; }
  </style>
</head>
<body>
${body}
</body>
</html>`

await mkdir(outDir, { recursive: true })
await writeFile(outBody, body, 'utf8')

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

const browser = await puppeteer.launch({
  headless: true,
  ...(executablePath ? { executablePath } : {}),
  args: ['--no-sandbox', '--disable-setuid-sandbox'],
})

try {
  const page = await browser.newPage()
  await page.setContent(html, { waitUntil: 'networkidle0' })
  const pdf = await page.pdf({
    format: 'A4',
    printBackground: true,
    displayHeaderFooter: true,
    headerTemplate: `<div></div>`,
    footerTemplate: `
      <div style="width:100%;font-size:8px;color:#71717a;padding:0 15mm;display:flex;justify-content:space-between;font-family:Helvetica,Arial,sans-serif;">
        <span>AIB Camera — Guida operativa · app ${APP_VERSION} · ${DOC_VERSION}</span>
        <span>Pagina <span class="pageNumber"></span> / <span class="totalPages"></span></span>
      </div>
    `,
    margin: {
      top: '14mm',
      right: '14mm',
      bottom: '16mm',
      left: '14mm',
    },
  })
  await writeFile(outPdf, pdf)
  console.log(`Wrote ${outPdf}`)
  console.log(`Wrote ${outBody}`)
} finally {
  await browser.close()
}
