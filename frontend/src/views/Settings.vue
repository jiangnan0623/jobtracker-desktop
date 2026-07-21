<template>
  <div>
    <h1 class="page-title">系统设置</h1>

    <div class="panel settings-panel">
      <div class="section-heading">
        <div>
          <h3>保存位置</h3>
          <span>修改后立即影响新上传的简历和新保存的 Markdown 笔记；已有文件仍保留在原路径。</span>
        </div>
      </div>

      <el-alert
        class="settings-alert"
        type="info"
        :closable="false"
        show-icon
        title="桌面端会把配置持久化保存，重启客户端后仍然生效。"
      />

      <el-form class="settings-form" label-position="top" :model="form">
        <el-form-item label="简历保存目录">
          <div class="path-row">
            <el-input v-model="form.resumeDir" placeholder="请选择或输入简历保存目录" />
            <el-button :icon="FolderOpened" @click="chooseDir('resumeDir')">选择目录</el-button>
          </div>
        </el-form-item>

        <el-form-item label="面试笔记保存目录">
          <div class="path-row">
            <el-input v-model="form.noteDir" placeholder="请选择或输入面试笔记保存目录" />
            <el-button :icon="FolderOpened" @click="chooseDir('noteDir')">选择目录</el-button>
          </div>
        </el-form-item>

        <el-form-item label="普通笔记保存目录">
          <div class="path-row">
            <el-input v-model="form.generalNoteDir" placeholder="请选择或输入普通笔记保存目录" />
            <el-button :icon="FolderOpened" @click="chooseDir('generalNoteDir')">选择目录</el-button>
          </div>
        </el-form-item>
      </el-form>

      <div class="settings-actions">
        <el-button @click="loadSettings">恢复当前配置</el-button>
        <el-button type="primary" :loading="saving" @click="saveSettings">保存设置</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'
import { storageApi } from '../api'

type StorageKey = 'resumeDir' | 'noteDir' | 'generalNoteDir'

const saving = ref(false)
const form = reactive({
  resumeDir: '',
  noteDir: '',
  generalNoteDir: ''
})

async function loadSettings() {
  const data = await storageApi.get() as unknown as typeof form
  form.resumeDir = data.resumeDir || ''
  form.noteDir = data.noteDir || ''
  form.generalNoteDir = data.generalNoteDir || ''
}

async function chooseDir(key: StorageKey) {
  if (!window.jobTrackerDesktop?.selectDirectory) {
    ElMessage.info('当前不是桌面客户端环境，请直接手动输入目录路径')
    return
  }
  const dir = await window.jobTrackerDesktop.selectDirectory()
  if (dir) {
    form[key] = dir
  }
}

async function saveSettings() {
  saving.value = true
  try {
    const data = await storageApi.update({ ...form }) as unknown as typeof form
    form.resumeDir = data.resumeDir
    form.noteDir = data.noteDir
    form.generalNoteDir = data.generalNoteDir
    ElMessage.success('保存位置已更新')
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>
