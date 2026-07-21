const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('jobTrackerDesktop', {
  selectDirectory: () => ipcRenderer.invoke('select-directory')
})
