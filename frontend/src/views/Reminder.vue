<template>
  <h1 class="page-title">提醒管理</h1>
  <div class="panel">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增提醒</el-button>
    </div>
    <el-table :data="rows">
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column label="提醒时间" width="190">
        <template #default="{ row }">{{ formatDateTime(row.remindTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="light" round>
            {{ normalizeStatus(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :disabled="normalizeStatus(row.status) === '已完成'" @click="complete(row.id)">完成</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="dialogVisible" title="提醒事项" width="560px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="内容"><el-input v-model="form.content" type="textarea" /></el-form-item>
      <el-form-item label="提醒时间">
        <el-date-picker v-model="form.remindTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status">
          <el-option label="未完成" value="未完成" />
          <el-option label="已完成" value="已完成" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { reminderApi } from '../api'
import type { Reminder } from '../types'
import { formatDateTime } from '../utils/time'

const rows = ref<Reminder[]>([])
const dialogVisible = ref(false)
const form = reactive<Reminder>({ title: '', content: '', remindTime: '', status: '未完成' })

onMounted(load)

async function load() {
  rows.value = await reminderApi.list() as unknown as Reminder[]
  notifyReminderChanged()
}

function normalizeStatus(status?: string) {
  return status === '已完成' ? '已完成' : '未完成'
}

function statusType(status?: string) {
  return normalizeStatus(status) === '已完成' ? 'success' : 'warning'
}

function openCreate() {
  Object.assign(form, { id: undefined, title: '', content: '', remindTime: '', status: '未完成' })
  dialogVisible.value = true
}

function openEdit(row: Reminder) {
  Object.assign(form, { ...row, status: normalizeStatus(row.status) })
  dialogVisible.value = true
}

async function save() {
  form.status = normalizeStatus(form.status)
  form.id ? await reminderApi.update(form.id, form) : await reminderApi.create(form)
  dialogVisible.value = false
  await load()
}

async function complete(id: number) {
  await reminderApi.complete(id)
  await load()
}

async function remove(id: number) {
  await reminderApi.remove(id)
  await load()
}

function notifyReminderChanged() {
  window.dispatchEvent(new Event('reminders-updated'))
}
</script>
