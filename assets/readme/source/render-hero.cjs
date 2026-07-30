const fs = require('node:fs/promises')
const path = require('node:path')
const sharp = require('sharp')

const sourceDir = __dirname
const readmeDir = path.resolve(sourceDir, '..')
const layoutPath = path.join(sourceDir, 'hero-layout.svg')
const screenshotPath = path.join(readmeDir, 'screenshots', 'dashboard.png')
const outputPath = path.join(readmeDir, 'hero.png')

async function render() {
  const layout = await fs.readFile(layoutPath)
  const screenshotMask = Buffer.from(
    '<svg width="578" height="362"><rect width="578" height="362" rx="16" fill="#fff"/></svg>'
  )
  const screenshot = await sharp(screenshotPath)
    .resize(578, 362, { fit: 'cover' })
    .composite([{ input: screenshotMask, blend: 'dest-in' }])
    .png()
    .toBuffer()

  const overlay = Buffer.from(`
    <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="520">
      <rect x="570" y="52" width="578" height="362" rx="16"
            fill="none" stroke="#405066" stroke-width="2"/>
      <g transform="translate(822 382)"
         font-family="-apple-system, BlinkMacSystemFont, Segoe UI, PingFang SC, sans-serif"
         font-size="17" font-weight="700">
        <rect x="0" y="0" width="102" height="38" rx="19" fill="#eff6ff" stroke="#bfdbfe"/>
        <circle cx="20" cy="19" r="5" fill="#2f80ed"/>
        <text x="32" y="9" dominant-baseline="hanging" fill="#1d4ed8">已投递</text>
        <rect x="112" y="0" width="82" height="38" rx="19" fill="#ecfeff" stroke="#a5f3fc"/>
        <circle cx="132" cy="19" r="5" fill="#0891b2"/>
        <text x="144" y="9" dominant-baseline="hanging" fill="#0e7490">二面</text>
        <rect x="204" y="0" width="92" height="38" rx="19" fill="#f0fdf4" stroke="#bbf7d0"/>
        <circle cx="224" cy="19" r="5" fill="#22c55e"/>
        <text x="236" y="9" dominant-baseline="hanging" fill="#15803d">Offer</text>
      </g>
    </svg>
  `)

  const base = await sharp(layout).png().toBuffer()
  await sharp(base)
    .composite([
      { input: screenshot, left: 570, top: 52 },
      { input: overlay, left: 0, top: 0 }
    ])
    .png({ compressionLevel: 9 })
    .toFile(outputPath)
}

render().catch(error => {
  console.error(error)
  process.exitCode = 1
})
