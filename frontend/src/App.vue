<template>
  <el-container class="layout" :class="{ collapsed: sidebarCollapsed, 'has-warning': unfinishedCount > 0 }">
    <el-aside :width="sidebarCollapsed ? '72px' : '232px'" class="sidebar">
      <div class="brand">
        <span class="brand-mark">JT</span>
        <span v-if="!sidebarCollapsed" class="brand-text">求职进度管理</span>
      </div>
      <el-menu
        router
        :collapse="sidebarCollapsed"
        :default-active="$route.path"
        background-color="#18212f"
        text-color="#d7dde8"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard"><el-icon><DataBoard /></el-icon><template #title>Dashboard</template></el-menu-item>
        <el-menu-item index="/applications"><el-icon><FolderOpened /></el-icon><template #title>投递管理</template></el-menu-item>
        <el-menu-item index="/resume"><el-icon><Document /></el-icon><template #title>简历管理</template></el-menu-item>
        <el-menu-item index="/notes"><el-icon><Memo /></el-icon><template #title>笔记管理</template></el-menu-item>
        <el-menu-item index="/reminder"><el-icon><Bell /></el-icon><template #title>提醒管理</template></el-menu-item>
        <el-menu-item index="/settings"><el-icon><Setting /></el-icon><template #title>系统设置</template></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <el-button class="collapse-btn" text @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon><component :is="sidebarCollapsed ? Expand : Fold" /></el-icon>
        </el-button>
        <span>个人求职助手</span>
      </el-header>
      <div v-if="unfinishedCount > 0" class="global-warning">
        <el-icon><Bell /></el-icon>
        <strong>未完成预警</strong>
        <span>当前还有 {{ unfinishedCount }} 个未完成提醒，请及时处理。</span>
      </div>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Bell, DataBoard, Document, Expand, Fold, FolderOpened, Memo, Setting } from '@element-plus/icons-vue'
import { reminderApi } from './api'
import type { Reminder } from './types'

const route = useRoute()
const sidebarCollapsed = ref(false)
const reminders = ref<Reminder[]>([])
let timer: number | undefined

const unfinishedCount = computed(() => reminders.value.filter((item) => item.status !== '已完成').length)

async function loadReminderWarning() {
  try {
    reminders.value = await reminderApi.list() as unknown as Reminder[]
  } catch {
    reminders.value = []
  }
}

onMounted(() => {
  loadReminderWarning()
  window.addEventListener('reminders-updated', loadReminderWarning)
  timer = window.setInterval(loadReminderWarning, 30000)
})

onBeforeUnmount(() => {
  window.removeEventListener('reminders-updated', loadReminderWarning)
  if (timer) window.clearInterval(timer)
})

watch(() => route.fullPath, loadReminderWarning)
</script>
