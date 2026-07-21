<template>
  <h1 class="page-title">简历管理</h1>

  <div class="panel">
    <div class="toolbar">
      <el-upload :http-request="upload" :show-file-list="false" accept=".pdf,.doc,.docx">
        <el-button type="primary">上传简历</el-button>
      </el-upload>
      <el-button @click="$router.push('/settings')">保存位置设置</el-button>
    </div>

    <el-table :data="rows">
      <el-table-column prop="versionName" label="版本名称" show-overflow-tooltip />
      <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
      <el-table-column prop="fileType" label="类型" width="90" />
      <el-table-column label="大小" width="120">
        <template #default="{ row }">{{ Math.round(row.fileSize / 1024) }} KB</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" :disabled="row.fileType !== 'pdf'" @click="preview(row)">预览</el-button>
          <el-button size="small" @click="download(row.id)">下载</el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="dialogVisible" title="修改版本信息" width="520px">
    <el-form label-width="90px">
      <el-form-item label="版本名称"><el-input v-model="form.versionName" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="previewVisible" :title="previewTitle" width="86%" top="4vh" destroy-on-close>
    <iframe v-if="previewUrl" class="pdf-preview" :src="previewUrl"></iframe>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { resumeApi } from '../api'
import type { Resume } from '../types'

const rows = ref<Resume[]>([])
const dialogVisible = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('PDF 预览')
const form = reactive({ id: 0, versionName: '', remark: '' })

onMounted(load)

async function load() {
  rows.value = await resumeApi.list() as unknown as Resume[]
}

async function upload(option: any) {
  const fd = new FormData()
  fd.append('file', option.file)
  fd.append('versionName', option.file.name)
  await resumeApi.upload(fd)
  ElMessage.success('上传成功')
  await load()
}

function preview(row: Resume) {
  if (row.fileType !== 'pdf') {
    ElMessage.warning('当前仅支持 PDF 简历预览')
    return
  }
  previewTitle.value = row.versionName || row.fileName
  previewUrl.value = resumeApi.previewUrl(row.id)
  previewVisible.value = true
}

function openEdit(row: Resume) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  await resumeApi.update(form.id, form.versionName, form.remark)
  dialogVisible.value = false
  await load()
}

async function remove(id: number) {
  await resumeApi.remove(id)
  await load()
}

function download(id: number) {
  window.open(resumeApi.downloadUrl(id), '_blank')
}
</script>
