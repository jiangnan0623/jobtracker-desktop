<template>
  <el-container class="layout" :class="{ collapsed: sidebarCollapsed, 'has-warning': unfinishedCount > 0 }">
    <el-aside :width="sidebarCollapsed ? '76px' : '248px'" class="sidebar">
      <div class="brand">
        <span class="brand-mark" aria-hidden="true">J</span>
        <span class="brand-copy" :aria-hidden="sidebarCollapsed">
          <strong class="brand-text">JobTracker</strong>
          <small>求职进度管理</small>
        </span>
      </div>
      <el-menu
        router
        :default-active="$route.path"
      >
        <el-menu-item index="/dashboard" :title="sidebarCollapsed ? 'Dashboard' : ''"><el-icon><DataBoard /></el-icon><template #title><span class="menu-label">Dashboard</span></template></el-menu-item>
        <el-menu-item index="/applications" :title="sidebarCollapsed ? '投递管理' : ''"><el-icon><FolderOpened /></el-icon><template #title><span class="menu-label">投递管理</span></template></el-menu-item>
        <el-menu-item index="/resume" :title="sidebarCollapsed ? '简历管理' : ''"><el-icon><Document /></el-icon><template #title><span class="menu-label">简历管理</span></template></el-menu-item>
        <el-menu-item index="/notes" :title="sidebarCollapsed ? '笔记管理' : ''"><el-icon><Memo /></el-icon><template #title><span class="menu-label">笔记管理</span></template></el-menu-item>
        <el-menu-item index="/reminder" :title="sidebarCollapsed ? '日程管理' : ''"><el-icon><Bell /></el-icon><template #title><span class="menu-label">日程管理</span></template></el-menu-item>
        <el-menu-item index="/settings" :title="sidebarCollapsed ? '系统设置' : ''"><el-icon><Setting /></el-icon><template #title><span class="menu-label">系统设置</span></template></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <el-button
          class="collapse-btn"
          text
          :aria-label="sidebarCollapsed ? '展开侧栏' : '收起侧栏'"
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <el-icon><component :is="sidebarCollapsed ? Expand : Fold" /></el-icon>
        </el-button>
        <span class="topbar-divider"></span>
        <div class="topbar-context">
          <small>Workspace</small>
          <strong>个人求职助手</strong>
        </div>
      </el-header>
      <button v-if="unfinishedCount > 0" class="global-warning" @click="$router.push('/reminder?quick=pending')">
        <el-icon><Bell /></el-icon>
        <strong>未完成预警</strong>
        <span>当前还有 {{ unfinishedCount }} 个未完成日程，请及时处理。</span>
      </button>
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

const unfinishedCount = computed(() => reminders.value.filter((item) => item.status === '未完成').length)

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
