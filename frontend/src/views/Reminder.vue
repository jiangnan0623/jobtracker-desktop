<template>
  <h1 class="page-title">日程管理</h1>

  <div class="panel schedule-panel">
    <div class="schedule-toolbar">
      <div class="schedule-toolbar-left">
        <el-button type="primary" @click="openCreate()">新增日程</el-button>
        <el-segmented v-model="viewMode" :options="viewOptions" />
      </div>
      <div v-if="viewMode !== 'list'" class="schedule-month-tools">
        <el-button @click="shiftPeriod(-1)">{{ prevLabel }}</el-button>
        <el-date-picker
          v-if="viewMode === 'month'"
          v-model="selectedMonth"
          type="month"
          value-format="YYYY-MM"
          class="schedule-period-picker"
          @change="handleMonthPick"
        />
        <el-date-picker
          v-else
          v-model="selectedDate"
          type="date"
          value-format="YYYY-MM-DD"
          class="schedule-period-picker"
          @change="handleDatePick"
        />
        <strong>{{ periodTitle }}</strong>
        <el-button @click="shiftPeriod(1)">{{ nextLabel }}</el-button>
        <el-button @click="goToday">今天</el-button>
      </div>
    </div>

    <div class="schedule-overview">
      <button class="schedule-stat-card" @click="openQuickList('今日', todayItems)">
        <span>今日</span>
        <strong>{{ todayItems.length }}</strong>
      </button>
      <button class="schedule-stat-card" @click="openQuickList(weekSummaryLabel, weekItems)">
        <span>{{ weekSummaryLabel }}</span>
        <strong>{{ weekTotal }}</strong>
      </button>
      <button class="schedule-stat-card" @click="openQuickList(monthSummaryLabel, monthItems)">
        <span>{{ monthSummaryLabel }}</span>
        <strong>{{ monthTotal }}</strong>
      </button>
      <button class="schedule-stat-card warning" @click="openQuickList('未完成', pendingItems)">
        <span>未完成</span>
        <strong>{{ pendingTotal }}</strong>
      </button>
      <button class="schedule-stat-card urgent" @click="openQuickList('紧急', urgentItems)">
        <span>紧急</span>
        <strong>{{ urgentTotal }}</strong>
      </button>
      <div class="schedule-legend">
        <div class="legend-group">
          <span class="legend-title">优先级</span>
          <span class="legend-item"><i class="schedule-low"></i>低</span>
          <span class="legend-item"><i class="schedule-medium"></i>中</span>
          <span class="legend-item"><i class="schedule-high"></i>高</span>
          <span class="legend-item"><i class="schedule-urgent"></i>紧急</span>
        </div>
        <div class="legend-group">
          <span class="legend-title">状态</span>
          <span class="legend-item"><i class="schedule-pending"></i>未完成</span>
          <span class="legend-item"><i class="schedule-done"></i>完成</span>
          <span class="legend-item"><i class="schedule-cancelled"></i>取消</span>
        </div>
      </div>
    </div>

    <div v-if="viewMode === 'month'" class="schedule-calendar">
      <div v-for="day in weekDays" :key="day" class="calendar-week-head">{{ day }}</div>
      <button
        v-for="day in calendarDays"
        :key="day.key"
        class="calendar-day"
        :class="{ muted: !day.inCurrentMonth, today: day.isToday, focused: day.isFocused }"
        @click="selectDateAndCreate(day.dateText)"
      >
        <div class="calendar-day-head">
          <span>{{ day.day }}</span>
          <el-tag v-if="day.items.length" size="small" type="info" effect="plain">{{ day.items.length }}</el-tag>
        </div>
        <div class="calendar-events">
          <button
            v-for="item in day.items.slice(0, 4)"
            :key="item.id"
            class="calendar-event"
            :class="[priorityClass(item), statusClass(item)]"
            @click.stop="openEdit(item)"
          >
            <span>{{ formatClock(item.remindTime) }}</span>
            <strong>{{ item.title }}</strong>
            <em>{{ item.scheduleType || '自定义' }}</em>
          </button>
          <span v-if="day.items.length > 4" class="calendar-more">+{{ day.items.length - 4 }} 更多</span>
        </div>
      </button>
    </div>

    <div v-else-if="viewMode === 'week'" class="schedule-week">
      <button
        v-for="day in weekViewDays"
        :key="day.key"
        class="week-day"
        :class="{ today: day.isToday, focused: day.isFocused }"
        @click="selectDateAndCreate(day.dateText)"
      >
        <div class="week-day-head">
          <div>
            <strong>{{ day.weekday }}</strong>
            <span>{{ day.label }}</span>
          </div>
          <el-tag v-if="day.items.length" size="small" effect="plain">{{ day.items.length }}</el-tag>
        </div>
        <div class="week-events">
          <button
            v-for="item in day.items"
            :key="item.id"
            class="week-event"
            :class="[priorityClass(item), statusClass(item)]"
            @click.stop="openEdit(item)"
          >
            <div class="week-event-time">{{ scheduleTimeRange(item) }}</div>
            <strong>{{ item.title }}</strong>
            <span>{{ item.scheduleType || '自定义' }} · {{ item.importance || '普通' }}</span>
          </button>
          <span v-if="!day.items.length" class="schedule-empty">暂无日程</span>
        </div>
      </button>
    </div>

    <div v-else-if="viewMode === 'day'" class="schedule-day-view">
      <button class="day-add-target" @click="openCreate(formatDate(currentDate))">
        新增 {{ formatDate(currentDate) }} 的日程
      </button>
      <div v-if="dayItems.length" class="day-agenda">
        <button
          v-for="item in dayItems"
          :key="item.id"
          class="day-agenda-item"
          :class="[priorityClass(item), statusClass(item)]"
          @click="openEdit(item)"
        >
          <span class="day-agenda-time">{{ scheduleTimeRange(item) }}</span>
          <div>
            <small>{{ item.scheduleType || '自定义' }} · {{ item.priority || '中' }} · {{ item.importance || '普通' }}</small>
            <strong>{{ item.title }}</strong>
            <p>{{ item.content || '无备注' }}</p>
          </div>
        </button>
      </div>
      <div v-else class="empty-state schedule-day-empty">当天暂无日程</div>
    </div>

    <el-table v-else :data="sortedRows" class="schedule-list-table">
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" width="110">
        <template #default="{ row }"><el-tag effect="light">{{ row.scheduleType || '自定义' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="时间" width="260">
        <template #default="{ row }">{{ scheduleTimeLabel(row) }}</template>
      </el-table-column>
      <el-table-column label="优先级" width="100">
        <template #default="{ row }"><el-tag :class="priorityClass(row)" effect="light">{{ row.priority || '中' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="重要程度" width="110">
        <template #default="{ row }">{{ row.importance || '普通' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag class="schedule-status-tag" :class="statusClass(row)" effect="light" round>{{ normalizeStatus(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">查看/编辑</el-button>
          <el-button size="small" :disabled="normalizeStatus(row.status) !== '未完成'" @click="complete(row.id)">完成</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="dialogVisible" :title="form.id ? '日程详情' : '新增日程'" width="680px">
    <el-form :model="form" label-width="100px" class="schedule-form">
      <el-form-item label="标题"><el-input v-model="form.title" placeholder="例如：腾讯一面、笔试截止、复盘简历" /></el-form-item>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="开始时间">
            <el-date-picker v-model="form.remindTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" clearable />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="类型">
            <el-select v-model="form.scheduleType" filterable allow-create default-first-option>
              <el-option v-for="item in scheduleTypes" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="未完成" value="未完成" />
              <el-option label="已完成" value="已完成" />
              <el-option label="已取消" value="已取消" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="优先级">
            <el-segmented v-model="form.priority" :options="priorityOptions" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="重要程度">
            <el-segmented v-model="form.importance" :options="importanceOptions" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="关联投递">
        <el-select v-model="form.relatedApplicationId" filterable clearable placeholder="可选，关联后方便从日程回到投递事项">
          <el-option v-for="item in applicationOptions" :key="item.id" :label="applicationLabel(item)" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button v-if="form.relatedApplicationId" @click="goApplication(form.relatedApplicationId)">查看投递</el-button>
      <el-button v-if="form.id" type="danger" @click="removeFromDialog">删除</el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="quickListVisible" :title="quickListTitle" width="760px">
    <div v-if="quickListRows.length" class="quick-schedule-list">
      <button
        v-for="item in quickListRows"
        :key="item.id"
        class="quick-schedule-item"
        :class="[priorityClass(item), statusClass(item)]"
        @click="openEditFromQuickList(item)"
      >
        <span>{{ scheduleTimeLabel(item) }}</span>
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.scheduleType || '自定义' }} · {{ item.priority || '中' }} · {{ item.importance || '普通' }} · {{ normalizeStatus(item.status) }}</p>
        </div>
      </button>
    </div>
    <div v-else class="empty-state quick-schedule-empty">暂无日程</div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applicationApi, reminderApi } from '../api'
import type { JobApplication, Reminder } from '../types'
import { formatDateTime } from '../utils/time'

type CalendarDay = {
  key: string
  date: Date
  dateText: string
  day: number
  inCurrentMonth: boolean
  isToday: boolean
  isFocused: boolean
  weekday?: string
  label?: string
  items: Reminder[]
}

const router = useRouter()
const route = useRoute()
const rows = ref<Reminder[]>([])
const applicationOptions = ref<JobApplication[]>([])
const dialogVisible = ref(false)
const quickListVisible = ref(false)
const quickListTitle = ref('')
const quickListRows = ref<Reminder[]>([])
const viewMode = ref('month')
const currentMonth = ref(startOfMonth(new Date()))
const currentDate = ref(new Date())
const selectedMonth = ref(formatMonth(new Date()))
const selectedDate = ref(formatDate(new Date()))
const form = reactive<Reminder>(emptyForm())

const viewOptions = [
  { label: '月视图', value: 'month' },
  { label: '周视图', value: 'week' },
  { label: '日视图', value: 'day' },
  { label: '列表视图', value: 'list' }
]
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const scheduleTypes = ['笔试', '面试', '网申截止', '宣讲会', '复盘', '自定义']
const priorityOptions = ['低', '中', '高', '紧急']
const importanceOptions = ['普通', '重要', '核心']

const sortedRows = computed(() => [...rows.value].sort((a, b) => (a.remindTime || '').localeCompare(b.remindTime || '')))
const todayItems = computed(() => sortedRows.value.filter((item) => (item.remindTime || '').slice(0, 10) === formatDate(new Date())))
const weekItems = computed(() => weekViewDays.value.flatMap((day) => day.items))
const monthItems = computed(() => {
  const month = formatMonth(currentMonth.value)
  return sortedRows.value.filter((item) => (item.remindTime || '').slice(0, 7) === month)
})
const weekSummaryLabel = computed(() => isSameWeek(currentDate.value, new Date()) ? '本周' : '所选周')
const monthSummaryLabel = computed(() => isSameMonth(currentDate.value, new Date()) ? '本月' : '所选月')
const pendingItems = computed(() => sortedRows.value.filter((item) => normalizeStatus(item.status) === '未完成'))
const urgentItems = computed(() => sortedRows.value.filter((item) => normalizeStatus(item.status) === '未完成' && item.priority === '紧急'))
const weekTotal = computed(() => weekItems.value.length)
const monthTotal = computed(() => monthItems.value.length)
const pendingTotal = computed(() => pendingItems.value.length)
const urgentTotal = computed(() => urgentItems.value.length)
const periodTitle = computed(() => {
  if (viewMode.value === 'month') {
    return `${currentMonth.value.getFullYear()} 年 ${currentMonth.value.getMonth() + 1} 月`
  }
  if (viewMode.value === 'week') {
    const days = weekViewDays.value
    return `${days[0]?.dateText || ''} 至 ${days[6]?.dateText || ''}`
  }
  return formatDate(currentDate.value)
})
const prevLabel = computed(() => viewMode.value === 'month' ? '上一月' : viewMode.value === 'week' ? '上一周' : '前一天')
const nextLabel = computed(() => viewMode.value === 'month' ? '下一月' : viewMode.value === 'week' ? '下一周' : '后一天')
const calendarDays = computed<CalendarDay[]>(() => {
  const first = startOfMonth(currentMonth.value)
  const start = new Date(first)
  const weekday = (first.getDay() + 6) % 7
  start.setDate(first.getDate() - weekday)
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    const dateText = formatDate(date)
    return {
      key: dateText,
      date,
      dateText,
      day: date.getDate(),
      inCurrentMonth: date.getMonth() === currentMonth.value.getMonth(),
      isToday: dateText === formatDate(new Date()),
      isFocused: dateText === formatDate(currentDate.value),
      items: sortedRows.value.filter((item) => (item.remindTime || '').slice(0, 10) === dateText)
    }
  })
})
const weekViewDays = computed<CalendarDay[]>(() => {
  const start = startOfWeek(currentDate.value)
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    const dateText = formatDate(date)
    return {
      key: dateText,
      date,
      dateText,
      day: date.getDate(),
      inCurrentMonth: true,
      isToday: dateText === formatDate(new Date()),
      isFocused: dateText === formatDate(currentDate.value),
      weekday: weekDays[index],
      label: `${date.getMonth() + 1}/${date.getDate()}`,
      items: sortedRows.value.filter((item) => (item.remindTime || '').slice(0, 10) === dateText)
    } as CalendarDay & { weekday: string; label: string }
  })
})
const dayItems = computed(() => sortedRows.value.filter((item) => (item.remindTime || '').slice(0, 10) === formatDate(currentDate.value)))

onMounted(async () => {
  await Promise.all([load(), loadApplications()])
  openRouteQuickList()
})

watch(currentMonth, (value) => {
  selectedMonth.value = formatMonth(value)
})

watch(currentDate, (value) => {
  selectedDate.value = formatDate(value)
  currentMonth.value = startOfMonth(value)
})

watch(() => route.query.quick, () => {
  openRouteQuickList()
})

function emptyForm(): Reminder {
  return {
    id: undefined,
    title: '',
    content: '',
    remindTime: '',
    endTime: '',
    scheduleType: '自定义',
    priority: '中',
    importance: '普通',
    status: '未完成',
    relatedApplicationId: undefined
  }
}

async function load() {
  rows.value = await reminderApi.list() as unknown as Reminder[]
  notifyReminderChanged()
}

async function loadApplications() {
  const page: any = await applicationApi.page({ pageNo: 1, pageSize: 500, sortField: 'appliedTime', sortOrder: 'desc' })
  applicationOptions.value = page.records || []
}

function normalizeStatus(status?: string) {
  if (status === '已完成' || status === '已取消') return status
  return '未完成'
}

function priorityClass(item: Reminder) {
  const priority = item.priority || '中'
  return {
    低: 'schedule-low',
    中: 'schedule-medium',
    高: 'schedule-high',
    紧急: 'schedule-urgent'
  }[priority] || 'schedule-medium'
}

function statusClass(item: Reminder) {
  const status = normalizeStatus(item.status)
  if (status === '已完成') return 'schedule-done'
  if (status === '已取消') return 'schedule-cancelled'
  return 'schedule-pending'
}

function selectDateAndCreate(dateText: string) {
  handleDatePick(dateText)
  openCreate(dateText)
}

function openCreate(dateText?: string) {
  Object.assign(form, emptyForm(), {
    id: undefined,
    remindTime: dateText ? `${dateText}T09:00:00` : '',
    endTime: '',
    relatedApplicationId: undefined
  })
  dialogVisible.value = true
}

function openEdit(row: Reminder) {
  Object.assign(form, { ...emptyForm(), ...row, status: normalizeStatus(row.status) })
  dialogVisible.value = true
}

function openQuickList(title: string, items: Reminder[]) {
  quickListTitle.value = `${title}日程`
  quickListRows.value = [...items]
  quickListVisible.value = true
}

function openRouteQuickList() {
  if (route.query.quick === 'pending') {
    openQuickList('未完成', pendingItems.value)
  } else if (route.query.quick === 'urgent') {
    openQuickList('紧急', urgentItems.value)
  } else if (typeof route.query.quick === 'string' && route.query.quick.startsWith('application-')) {
    const applicationId = Number(route.query.quick.replace('application-', ''))
    if (applicationId) {
      openQuickList('关联投递', sortedRows.value.filter((item) => item.relatedApplicationId === applicationId))
    }
  }
}

function openEditFromQuickList(row: Reminder) {
  quickListVisible.value = false
  openEdit(row)
}

async function save() {
  if (!form.title?.trim()) {
    ElMessage.warning('请填写日程标题')
    return
  }
  if (!form.remindTime) {
    ElMessage.warning('请选择开始时间')
    return
  }
  if (form.endTime && form.endTime <= form.remindTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  form.status = normalizeStatus(form.status)
  form.scheduleType = form.scheduleType || '自定义'
  form.priority = form.priority || '中'
  form.importance = form.importance || '普通'
  form.id ? await reminderApi.update(form.id, form) : await reminderApi.create(form)
  dialogVisible.value = false
  await load()
  ElMessage.success('日程已保存')
}

async function complete(id: number) {
  await reminderApi.complete(id)
  await load()
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除这个日程？', '删除日程', { type: 'warning' })
  await reminderApi.remove(id)
  await load()
}

async function removeFromDialog() {
  if (!form.id) return
  await remove(form.id)
  dialogVisible.value = false
}

function shiftPeriod(offset: number) {
  if (viewMode.value === 'month') {
    const focus = currentDate.value
    const targetMonth = new Date(focus.getFullYear(), focus.getMonth() + offset, 1)
    currentDate.value = dateInMonth(targetMonth.getFullYear(), targetMonth.getMonth(), focus.getDate())
    return
  }
  const next = new Date(currentDate.value)
  next.setDate(next.getDate() + (viewMode.value === 'week' ? offset * 7 : offset))
  currentDate.value = next
}

function goToday() {
  const today = new Date()
  currentDate.value = today
}

function handleMonthPick(value?: string) {
  if (!value) return
  const [year, month] = value.split('-').map(Number)
  currentDate.value = dateInMonth(year, month - 1, currentDate.value.getDate())
}

function handleDatePick(value?: string) {
  if (!value) return
  const [year, month, day] = value.split('-').map(Number)
  currentDate.value = new Date(year, month - 1, day)
}

function startOfMonth(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), 1)
}

function startOfWeek(date: Date) {
  const result = new Date(date)
  const weekday = (date.getDay() + 6) % 7
  result.setDate(date.getDate() - weekday)
  return result
}

function dateInMonth(year: number, month: number, preferredDay: number) {
  const lastDay = new Date(year, month + 1, 0).getDate()
  return new Date(year, month, Math.min(preferredDay, lastDay))
}

function isSameMonth(left: Date, right: Date) {
  return left.getFullYear() === right.getFullYear() && left.getMonth() === right.getMonth()
}

function isSameWeek(left: Date, right: Date) {
  return formatDate(startOfWeek(left)) === formatDate(startOfWeek(right))
}

function formatDate(date: Date) {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

function formatMonth(date: Date) {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  return `${yyyy}-${mm}`
}

function formatClock(value?: string) {
  return value ? value.slice(11, 16) : ''
}

function scheduleTimeLabel(row: Reminder) {
  const start = formatDateTime(row.remindTime)
  return row.endTime ? `${start} - ${formatEndTime(row.remindTime, row.endTime)}` : start
}

function scheduleTimeRange(row: Reminder) {
  const start = formatClock(row.remindTime)
  return row.endTime ? `${start} - ${formatEndTime(row.remindTime, row.endTime)}` : start
}

function formatEndTime(start?: string, end?: string) {
  if (!end) return ''
  if (start && end <= start) return '结束时间异常'
  return start?.slice(0, 10) === end.slice(0, 10) ? formatClock(end) : formatDateTime(end)
}

function applicationLabel(item: JobApplication) {
  return [item.companyName, item.positionName].filter(Boolean).join(' - ') || `投递 #${item.id}`
}

function goApplication(id: number) {
  dialogVisible.value = false
  router.push(`/application/${id}`)
}

function notifyReminderChanged() {
  window.dispatchEvent(new Event('reminders-updated'))
}
</script>
