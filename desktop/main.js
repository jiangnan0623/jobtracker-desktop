const { app, BrowserWindow, dialog, ipcMain } = require('electron')
const { spawn } = require('child_process')
const http = require('http')
const path = require('path')
const fs = require('fs')

const PORT = process.env.JOB_TRACKER_PORT || '18080'
const BASE_URL = `http://127.0.0.1:${PORT}`
let backendProcess = null

function backendJarPath() {
  if (app.isPackaged) {
    return path.join(process.resourcesPath, 'backend', 'app.jar')
  }
  return path.resolve(__dirname, '..', 'backend', 'target', 'job-tracker-0.0.1-SNAPSHOT.jar')
}

function javaCommand() {
  const runtimeRoot = app.isPackaged
    ? path.join(process.resourcesPath, 'runtime')
    : path.join(__dirname, 'resources', 'runtime')
  const bundledJava = path.join(runtimeRoot, 'bin', process.platform === 'win32' ? 'java.exe' : 'java')

  if (fs.existsSync(bundledJava)) {
    return bundledJava
  }

  return process.env.JOBTRACKER_JAVA || 'java'
}

function waitForBackend(timeoutMs = 30000) {
  const startedAt = Date.now()

  return new Promise((resolve, reject) => {
    const poll = () => {
      const request = http.get(`${BASE_URL}/api/dashboard`, response => {
        response.resume()
        resolve()
      })

      request.on('error', () => {
        if (Date.now() - startedAt > timeoutMs) {
          reject(new Error('后端服务启动超时'))
          return
        }
        setTimeout(poll, 500)
      })

      request.setTimeout(1000, () => {
        request.destroy()
      })
    }

    poll()
  })
}

async function startBackend() {
  const jarPath = backendJarPath()
  const userData = app.getPath('userData')

  backendProcess = spawn(javaCommand(), [
    '-jar',
    jarPath,
    '--spring.profiles.active=desktop'
  ], {
    cwd: path.dirname(jarPath),
    env: {
      ...process.env,
      JOB_TRACKER_PORT: PORT,
      JOB_TRACKER_DATA_DIR: userData
    },
    windowsHide: true
  })

  backendProcess.on('error', error => {
    dialog.showErrorBox('后端启动失败', `${error.message}\n\n请确认内置 Java 运行时存在，或设置 JOBTRACKER_JAVA 指向 java.exe。`)
  })

  backendProcess.stderr.on('data', chunk => {
    console.error(chunk.toString())
  })

  await waitForBackend()
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 820,
    minWidth: 1100,
    minHeight: 720,
    title: '个人求职助手',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  })

  win.loadURL(BASE_URL)
}

app.whenReady().then(async () => {
  ipcMain.handle('select-directory', async () => {
    const result = await dialog.showOpenDialog({
      title: '选择保存目录',
      properties: ['openDirectory', 'createDirectory']
    })
    return result.canceled ? null : result.filePaths[0]
  })

  try {
    await startBackend()
    createWindow()
  } catch (error) {
    dialog.showErrorBox('启动失败', error.message)
    app.quit()
  }
})

app.on('window-all-closed', () => {
  app.quit()
})

app.on('before-quit', () => {
  if (backendProcess && !backendProcess.killed) {
    backendProcess.kill()
  }
})
