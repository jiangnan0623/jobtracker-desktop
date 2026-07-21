<template>
  <h1 class="page-title">Dashboard</h1>
  <div class="stat-grid">
    <div class="stat"><span>总投递数量</span><strong>{{ data.totalApplications || 0 }}</strong></div>
    <div class="stat"><span>面试数量</span><strong>{{ data.interviewCount || 0 }}</strong></div>
    <div class="stat"><span>Offer 数量</span><strong>{{ data.offerCount || 0 }}</strong></div>
    <div class="stat"><span>今日提醒</span><strong>{{ data.todayReminders?.length || 0 }}</strong></div>
  </div>
  <div class="chart-grid">
    <div class="panel"><div ref="pieRef" class="chart"></div></div>
    <div class="panel"><div ref="lineRef" class="chart"></div></div>
    <div class="panel"><div ref="barRef" class="chart"></div></div>
    <div class="panel">
      <h3>今日提醒</h3>
      <el-timeline>
        <el-timeline-item v-for="item in data.todayReminders" :key="item.id" :timestamp="formatClock(item.remindTime)">
          {{ item.title }}
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
  <div class="panel">
    <div class="section-heading">
      <h3>最近投递记录</h3>
      <span>按投递时间倒序排列；未填写投递时间时，用最近更新时间兜底。</span>
    </div>
    <el-table :data="data.recentApplications || []">
      <el-table-column prop="companyName" label="公司" min-width="130" show-overflow-tooltip />
      <el-table-column prop="positionName" label="岗位" min-width="170" show-overflow-tooltip />
      <el-table-column prop="positionType" label="岗位类别" min-width="170">
        <template #default="{ row }">
          <el-tooltip :content="row.positionType || '-'" placement="top" :disabled="!row.positionType">
            <div class="multi-tag-cell">
              <el-tag v-for="tag in visibleMultiTags(row.positionType)" :key="tag" class="category-tag" :style="tagStyle(tag)" size="small">{{ tag }}</el-tag>
              <el-tag v-if="hiddenMultiTagCount(row.positionType)" class="more-tag" size="small" type="info">+{{ hiddenMultiTagCount(row.positionType) }}</el-tag>
              <span v-if="!row.positionType" class="muted">-</span>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="recruitmentType" label="投递批次" width="110" show-overflow-tooltip />
      <el-table-column prop="resumeCategory" label="简历类别" min-width="140">
        <template #default="{ row }">
          <el-tooltip :content="row.resumeCategory || '-'" placement="top" :disabled="!row.resumeCategory">
            <div class="multi-tag-cell">
              <el-tag v-for="tag in visibleMultiTags(row.resumeCategory)" :key="tag" class="category-tag" :style="tagStyle(tag)" size="small">{{ tag }}</el-tag>
              <el-tag v-if="hiddenMultiTagCount(row.resumeCategory)" class="more-tag" size="small" type="info">+{{ hiddenMultiTagCount(row.resumeCategory) }}</el-tag>
              <span v-if="!row.resumeCategory" class="muted">-</span>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="currentStatus" label="状态" width="120">
        <template #default="{ row }">
          <span class="status-pill" :class="statusMeta(row.currentStatus).className">
            <el-icon><component :is="statusMeta(row.currentStatus).icon" /></el-icon>
            <span>{{ row.currentStatus }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="appliedTime" label="投递时间" width="170">
        <template #default="{ row }">{{ formatDate(row.appliedTime) || '未填写' }}</template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="100" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onMounted, ref } from 'vue'
import { ChatDotRound, CircleCheck, Collection, EditPen, Medal, Promotion, Star, User, Warning } from '@element-plus/icons-vue'
import { dashboardApi } from '../api'
import { formatDateTime, formatTime } from '../utils/time'

const pieRef = ref<HTMLDivElement>()
const lineRef = ref<HTMLDivElement>()
const barRef = ref<HTMLDivElement>()
const data = ref<any>({})

onMounted(async () => {
  data.value = await dashboardApi.overview()
  await nextTick()
  renderCharts()
})

function renderCharts() {
  const status = data.value.statusCount || {}
  echarts.init(pieRef.value!).setOption({
    title: { text: '投递状态分布' },
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: '62%', data: Object.entries(status).map(([name, value]) => ({ name, value })) }]
  })
  const trend = data.value.weeklyTrend || {}
  echarts.init(lineRef.value!).setOption({
    title: { text: '每周投递趋势' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(trend) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', smooth: true, data: Object.values(trend) }]
  })
  const company = data.value.companyCount || {}
  echarts.init(barRef.value!).setOption({
    title: { text: '公司投递数量' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(company) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'bar', data: Object.values(company) }]
  })
}

function formatDate(value?: string) {
  return formatDateTime(value)
}

function formatClock(value?: string) {
  return formatTime(value)
}

function splitMultiValue(value?: string) {
  return value ? value.split(/[、,，;；]/).map(item => item.trim()).filter(Boolean) : []
}

function visibleMultiTags(value?: string) {
  return splitMultiValue(value).slice(0, 2)
}

function hiddenMultiTagCount(value?: string) {
  return Math.max(splitMultiValue(value).length - 2, 0)
}

function tagStyle(value: string) {
  const palettes = [
    { color: '#1d4ed8', bg: '#eff6ff', border: '#bfdbfe' },
    { color: '#047857', bg: '#ecfdf5', border: '#a7f3d0' },
    { color: '#b45309', bg: '#fffbeb', border: '#fde68a' },
    { color: '#7c3aed', bg: '#f5f3ff', border: '#ddd6fe' },
    { color: '#be123c', bg: '#fff1f2', border: '#fecdd3' },
    { color: '#0e7490', bg: '#ecfeff', border: '#a5f3fc' }
  ]
  const hash = Array.from(value).reduce((sum, char) => sum + char.charCodeAt(0), 0)
  const item = palettes[hash % palettes.length]
  return { color: item.color, backgroundColor: item.bg, borderColor: item.border }
}

function statusMeta(status?: string) {
  const map: Record<string, any> = {
    收藏: { className: 'status-favorite', icon: Star },
    待投递: { className: 'status-pending', icon: Collection },
    已投递: { className: 'status-submitted', icon: Promotion },
    笔试: { className: 'status-written', icon: EditPen },
    一面: { className: 'status-interview', icon: ChatDotRound },
    二面: { className: 'status-interview', icon: ChatDotRound },
    三面: { className: 'status-interview', icon: ChatDotRound },
    四面: { className: 'status-interview', icon: ChatDotRound },
    主管面: { className: 'status-interview', icon: User },
    'HR 面': { className: 'status-interview', icon: User },
    面试中: { className: 'status-interview', icon: ChatDotRound },
    Offer: { className: 'status-offer', icon: Medal },
    淘汰: { className: 'status-rejected', icon: Warning }
  }
  return map[status || ''] || { className: 'status-default', icon: CircleCheck }
}
</script>
