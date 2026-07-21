declare module 'markdown-it'

interface Window {
  jobTrackerDesktop?: {
    selectDirectory: () => Promise<string | null>
  }
}
