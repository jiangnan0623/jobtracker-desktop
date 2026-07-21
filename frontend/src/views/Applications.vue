<template>
  <h1 class="page-title">投递管理</h1>
  <div class="panel">
    <div class="application-filters">
      <div class="filter-row">
        <el-input v-model="query.companyName" placeholder="搜索公司" clearable />
        <el-select v-model="query.currentStatus" placeholder="投递状态" clearable filterable allow-create default-first-option>
          <el-option v-for="item in dynamicStatusOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.positionType" placeholder="岗位类别" clearable filterable allow-create default-first-option>
          <el-option v-for="item in dynamicTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.recruitmentType" placeholder="投递批次" clearable filterable allow-create default-first-option>
          <el-option v-for="item in recruitmentTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.resumeCategory" placeholder="简历类别" clearable filterable allow-create default-first-option>
          <el-option v-for="item in dynamicResumeCategoryOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-date-picker v-model="dateRange" type="datetimerange" start-placeholder="投递开始" end-placeholder="投递结束" value-format="YYYY-MM-DDTHH:mm:ss" />
      </div>
      <div class="filter-actions">
        <el-segmented v-model="sortMode" :options="sortOptions" />
        <el-switch v-model="groupByCompany" active-text="按公司合并" />
        <div class="action-spacer"></div>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button @click="openCreate">新增岗位</el-button>
      </div>
    </div>

    <el-table :data="displayRows" row-key="rowKey" :tree-props="{ children: 'children' }" default-expand-all>
      <el-table-column prop="companyName" label="公司" min-width="150">
        <template #default="{ row }">
          <el-tooltip :content="row.companyName" placement="top" :disabled="!row.companyName">
            <strong class="table-ellipsis">{{ row.companyName }}</strong>
          </el-tooltip>
          <el-tag v-if="row.isGroup" size="small" class="company-count">{{ row.children.length }} 个岗位</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="positionName" label="岗位" min-width="180">
        <template #default="{ row }">
          <el-tooltip :content="row.isGroup ? '同公司投递合集' : row.positionName" placement="top">
            <span class="table-ellipsis">{{ row.isGroup ? '同公司投递合集' : row.positionName }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="positionType" label="岗位类别" min-width="180">
        <template #default="{ row }">
          <el-tooltip v-if="!row.isGroup" :content="row.positionType || '-'" placement="top" :disabled="!row.positionType">
            <div class="multi-tag-cell">
              <el-tag v-for="tag in visibleMultiTags(row.positionType)" :key="tag" class="category-tag" :style="tagStyle(tag)" size="small">{{ tag }}</el-tag>
              <el-tag v-if="hiddenMultiTagCount(row.positionType)" class="more-tag" size="small" type="info">+{{ hiddenMultiTagCount(row.positionType) }}</el-tag>
              <span v-if="!row.positionType" class="muted">-</span>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="recruitmentType" label="投递批次" width="110" show-overflow-tooltip />
      <el-table-column prop="resumeCategory" label="简历类别" min-width="150">
        <template #default="{ row }">
          <el-tooltip v-if="!row.isGroup" :content="row.resumeCategory || '-'" placement="top" :disabled="!row.resumeCategory">
            <div class="multi-tag-cell">
              <el-tag v-for="tag in visibleMultiTags(row.resumeCategory)" :key="tag" class="category-tag" :style="tagStyle(tag)" size="small">{{ tag }}</el-tag>
              <el-tag v-if="hiddenMultiTagCount(row.resumeCategory)" class="more-tag" size="small" type="info">+{{ hiddenMultiTagCount(row.resumeCategory) }}</el-tag>
              <span v-if="!row.resumeCategory" class="muted">-</span>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="currentStatus" label="状态" width="130">
        <template #default="{ row }">
          <span v-if="!row.isGroup" class="status-pill" :class="statusMeta(row.currentStatus).className">
            <el-icon><component :is="statusMeta(row.currentStatus).icon" /></el-icon>
            <span>{{ row.currentStatus }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="appliedTime" label="投递时间" width="180">
        <template #default="{ row }">{{ row.isGroup ? '' : formatDate(row.appliedTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <template v-if="!row.isGroup">
            <el-button size="small" @click="$router.push(`/application/${row.id}`)">详情</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" @current-change="load" />
  </div>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑岗位' : '新增岗位'" width="780px">
    <el-form :model="form" label-width="110px">
      <el-row :gutter="12">
        <el-col :span="12"><el-form-item label="公司名称"><el-input v-model="form.companyName" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="岗位名称"><el-input v-model="form.positionName" /></el-form-item></el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="岗位类别">
            <el-select v-model="formPositionTypes" multiple filterable allow-create default-first-option placeholder="可多选或自定义">
              <el-option v-for="item in dynamicTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="投递批次">
            <el-select v-model="form.recruitmentType" filterable allow-create default-first-option clearable>
              <el-option v-for="item in recruitmentTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="绑定简历">
            <el-select v-model="form.resumeId" clearable filterable placeholder="选择已上传简历">
              <el-option v-for="resume in resumes" :key="resume.id" :label="resume.versionName" :value="resume.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="简历类别">
            <el-select v-model="formResumeCategories" multiple filterable allow-create default-first-option clearable placeholder="可多选或自定义">
              <el-option v-for="item in dynamicResumeCategoryOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12"><el-form-item label="工作地点"><el-input v-model="form.workLocation" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="薪资"><el-input v-model="form.salary" /></el-form-item></el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12"><el-form-item label="投递来源"><el-input v-model="form.source" /></el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="当前状态">
            <el-select v-model="form.currentStatus" filterable default-first-option>
              <el-option v-for="item in formStatusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="岗位链接"><el-input v-model="form.jobLink" /></el-form-item>
      <el-form-item label="投递时间"><el-date-picker v-model="form.appliedTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item label="岗位 JD"><el-input v-model="form.jobDescription" type="textarea" :rows="5" placeholder="粘贴岗位职责、要求、技术栈等信息" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ChatDotRound, CircleCheck, Collection, EditPen, Medal, Promotion, Star, User, Warning } from '@element-plus/icons-vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { applicationApi, recruitmentTypeOptions, resumeApi, resumeCategoryOptions, statusOptions, typeOptions } from '../api'
import type { JobApplication, Resume } from '../types'
import { formatDateTime } from '../utils/time'

type ApplicationRow = JobApplication & { rowKey?: string; isGroup?: boolean; children?: ApplicationRow[] }

const rows = ref<ApplicationRow[]>([])
const resumes = ref<Resume[]>([])
const dynamicStatusOptions = ref<string[]>([...statusOptions])
const dynamicTypeOptions = ref<string[]>([...typeOptions])
const dynamicResumeCategoryOptions = ref<string[]>([...resumeCategoryOptions])
const total = ref(0)
const dialogVisible = ref(false)
const groupByCompany = ref(false)
const dateRange = ref<string[]>([])
const sortMode = ref('appliedTime-desc')
const formPositionTypes = ref<string[]>([])
const formResumeCategories = ref<string[]>([])
const sortOptions = [
  { label: '投递时间↓', value: 'appliedTime-desc' },
  { label: '投递时间↑', value: 'appliedTime-asc' },
  { label: '公司名 A-Z', value: 'companyName-asc' },
  { label: '公司名 Z-A', value: 'companyName-desc' }
]
const query = reactive({
  companyName: '',
  currentStatus: '',
  positionType: '',
  recruitmentType: '',
  resumeCategory: '',
  sortField: 'appliedTime',
  sortOrder: 'desc',
  pageNo: 1,
  pageSize: 10
})
const form = reactive<JobApplication>(emptyForm())
const formStatusOptions = computed(() => {
  const flowOptions = parseProgressFlow(form.progressFlow)
  return flowOptions.length ? flowOptions : statusOptions
})

const displayRows = computed<ApplicationRow[]>(() => {
  if (!groupByCompany.value) {
    return rows.value.map(row => ({ ...row, rowKey: `job-${row.id}` }))
  }
  const groups = new Map<string, ApplicationRow[]>()
  rows.value.forEach(row => {
    const key = row.companyName || '未填写公司'
    groups.set(key, [...(groups.get(key) || []), { ...row, rowKey: `job-${row.id}` }])
  })
  return Array.from(groups.entries()).map(([companyName, children]) => ({
    rowKey: `company-${companyName}`,
    isGroup: true,
    companyName,
    positionName: '',
    currentStatus: '',
    children
  } as ApplicationRow))
})

watch(sortMode, value => {
  const [field, order] = value.split('-')
  query.sortField = field
  query.sortOrder = order
  query.pageNo = 1
  load()
})

watch(groupByCompany, () => {
  query.pageNo = 1
})

onMounted(async () => {
  await Promise.all([load(), loadResumes(), loadFilterOptions()])
})

function emptyForm(): JobApplication {
  return {
    companyName: '',
    positionName: '',
    currentStatus: '待投递',
    recruitmentType: '秋招'
  }
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

function formatDate(value?: string) {
  return formatDateTime(value)
}

function queryParams() {
  return {
    ...query,
    appliedStartTime: dateRange.value?.[0] || '',
    appliedEndTime: dateRange.value?.[1] || ''
  }
}

async function load() {
  const page: any = await applicationApi.page(queryParams())
  rows.value = page.records
  total.value = page.total
}

async function loadResumes() {
  resumes.value = await resumeApi.list() as unknown as Resume[]
}

async function loadStatusOptions() {
  const options = await applicationApi.statusOptions() as unknown as string[]
  dynamicStatusOptions.value = mergeOptions(statusOptions, options)
}

async function loadFilterOptions() {
  const [statuses, positionTypes, resumeCategories] = await Promise.all([
    applicationApi.statusOptions(),
    applicationApi.positionTypeOptions(),
    applicationApi.resumeCategoryOptions()
  ])
  dynamicStatusOptions.value = mergeOptions(statusOptions, statuses as unknown as string[])
  dynamicTypeOptions.value = mergeOptions(typeOptions, positionTypes as unknown as string[])
  dynamicResumeCategoryOptions.value = mergeOptions(resumeCategoryOptions, resumeCategories as unknown as string[])
}

function mergeOptions(...groups: string[][]) {
  return Array.from(new Set(groups.flat().map((item) => item?.trim()).filter(Boolean)))
}

function resetQuery() {
  Object.assign(query, {
    companyName: '',
    currentStatus: '',
    positionType: '',
    recruitmentType: '',
    resumeCategory: '',
    sortField: 'appliedTime',
    sortOrder: 'desc',
    pageNo: 1
  })
  sortMode.value = 'appliedTime-desc'
  dateRange.value = []
  load()
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

function parseProgressFlow(value?: string) {
  if (!value?.trim()) return []
  return value.split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => line.split('|')[0].trim())
    .filter(Boolean)
}

function openCreate() {
  Object.assign(form, emptyForm(), {
    id: undefined,
    positionType: '',
    resumeCategory: '',
    workLocation: '',
    salary: '',
    source: '',
    jobLink: '',
    jobDescription: '',
    appliedTime: '',
    resumeId: undefined,
    remark: ''
  })
  formPositionTypes.value = []
  formResumeCategories.value = []
  dialogVisible.value = true
}

function openEdit(row: JobApplication) {
  Object.assign(form, row)
  formPositionTypes.value = splitMultiValue(row.positionType)
  formResumeCategories.value = splitMultiValue(row.resumeCategory)
  dialogVisible.value = true
}

async function save() {
  form.positionType = formPositionTypes.value.join('、')
  form.resumeCategory = formResumeCategories.value.join('、')
  if (!formStatusOptions.value.includes(form.currentStatus)) {
    form.currentStatus = formStatusOptions.value[0] || '待投递'
  }
  form.id ? await applicationApi.update(form.id, form) : await applicationApi.create(form)
  dialogVisible.value = false
  await Promise.all([load(), loadFilterOptions()])
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除这条投递记录？')
  await applicationApi.remove(id)
  await load()
}
</script>
