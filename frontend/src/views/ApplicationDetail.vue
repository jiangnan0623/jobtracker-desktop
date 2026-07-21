<template>
  <h1 class="page-title">{{ application.companyName }} - {{ application.positionName }}</h1>

  <div class="panel">
    <el-descriptions :column="3" border>
      <el-descriptions-item label="当前状态">
        <span class="status-pill" :class="statusMeta(application.currentStatus).className">
          <el-icon><component :is="statusMeta(application.currentStatus).icon" /></el-icon>
          <span>{{ application.currentStatus || '-' }}</span>
        </span>
      </el-descriptions-item>
      <el-descriptions-item label="进度结果">
        <el-tag :type="resultMeta(application.progressResult).type">{{ application.progressResult || '进行中' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="操作时间">{{ formatDate(application.progressOperatedTime) || '-' }}</el-descriptions-item>
      <el-descriptions-item label="岗位类别">{{ application.positionType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="投递批次">{{ application.recruitmentType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="简历类别">{{ application.resumeCategory || '-' }}</el-descriptions-item>
      <el-descriptions-item label="绑定简历">
        <span>{{ detail.resume?.versionName || '未绑定' }}</span>
        <el-button v-if="detail.resume?.fileType === 'pdf'" size="small" link type="primary" @click="previewResume">预览</el-button>
      </el-descriptions-item>
      <el-descriptions-item label="地点">{{ application.workLocation || '-' }}</el-descriptions-item>
      <el-descriptions-item label="来源">{{ application.source || '-' }}</el-descriptions-item>
      <el-descriptions-item label="薪资">{{ application.salary || '-' }}</el-descriptions-item>
      <el-descriptions-item label="投递时间">{{ formatDate(application.appliedTime) || '-' }}</el-descriptions-item>
      <el-descriptions-item label="链接" :span="3">
        <a v-if="application.jobLink" :href="application.jobLink" target="_blank">{{ application.jobLink }}</a>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="备注" :span="3">{{ application.remark || '-' }}</el-descriptions-item>
    </el-descriptions>
  </div>

  <div class="panel">
    <div class="toolbar">
      <h3>投递进度</h3>
      <el-button @click="openProgressDialog">编辑流程</el-button>
    </div>

    <div v-if="selectedProgressName" class="progress-manager">
      <el-select v-model="progressForm.currentStatus" filterable placeholder="当前进度">
        <el-option v-for="item in flowSteps" :key="item.name" :label="item.name" :value="item.name" />
      </el-select>
      <el-segmented v-model="progressForm.progressResult" :options="progressResultOptions" />
      <el-date-picker
        v-model="progressForm.progressOperatedTime"
        type="datetime"
        value-format="YYYY-MM-DDTHH:mm:ss"
        placeholder="操作时间"
      />
      <el-button type="primary" @click="saveCurrentProgress">保存进度</el-button>
      <el-button @click="closeProgressManager">取消</el-button>
    </div>

    <el-timeline class="status-timeline">
      <el-timeline-item
        v-for="item in progressItems"
        :key="item.name"
        :timestamp="item.timestamp"
        :color="item.color"
        :icon="item.icon"
        :hollow="!item.active"
      >
        <div
          class="timeline-row timeline-clickable"
          :class="{ inactive: !item.active, selected: item.name === selectedProgressName }"
          @click="openProgressManager(item.name)"
        >
          <div class="timeline-title">
            <strong>{{ item.name }}</strong>
            <el-tag v-if="item.current" size="small" :type="resultMeta(application.progressResult).type">
              {{ application.progressResult || '进行中' }}
            </el-tag>
          </div>
          <span>{{ item.description }}</span>
          <span v-if="item.current && application.progressOperatedTime" class="muted">
            操作时间：{{ formatDate(application.progressOperatedTime) }}
          </span>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>

  <div class="panel" v-if="application.jobDescription">
    <h3>岗位 JD</h3>
    <div class="jd-content markdown-preview" v-html="jobDescriptionHtml"></div>
  </div>

  <div class="panel">
    <div class="toolbar">
      <h3>面试流程</h3>
      <el-button @click="openInterview">新增面试记录</el-button>
    </div>
    <el-timeline>
      <el-timeline-item
        v-for="item in detail.interviewRecords"
        :key="item.id"
        :timestamp="formatDate(item.interviewTime)"
        color="#409eff"
        :icon="ChatDotRound"
      >
        <strong>{{ item.round }}</strong> - {{ item.result || '待记录' }} - 难度 {{ item.difficulty || '-' }}/5
        <p class="muted">{{ item.questions }}</p>
        <p>{{ item.summary }}</p>
        <el-button size="small" @click="editInterview(item)">编辑</el-button>
        <el-button size="small" type="danger" @click="removeInterview(item.id!)">删除</el-button>
      </el-timeline-item>
    </el-timeline>
  </div>

  <div class="panel">
    <div class="toolbar">
      <h3>Markdown 面试笔记</h3>
      <el-button @click="openNote">新增笔记</el-button>
    </div>
    <el-table :data="detail.interviewNotes">
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
      <el-table-column prop="updatedTime" label="更新时间" width="180">
        <template #default="{ row }">{{ formatDate(row.updatedTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button size="small" @click="previewNote(row)">预览</el-button>
          <el-button size="small" @click="editNote(row)">编辑</el-button>
          <el-button size="small" @click="download(noteApi.downloadUrl(row.id))">下载</el-button>
          <el-button size="small" type="danger" @click="removeNote(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="progressVisible" title="自定义投递流程" width="680px">
    <p class="muted">每行一个阶段，格式为：阶段名称 | 阶段说明。流程会同步为当前岗位可选状态。</p>
    <el-input v-model="progressText" type="textarea" :rows="12" />
    <el-alert
      v-if="progressMismatch"
      class="progress-alert"
      type="warning"
      :closable="false"
      show-icon
      title="当前状态不在新流程中"
      description="保存前请选择一个流程阶段同步为当前状态，避免详情状态和进度条点亮位置不一致。"
    />
    <el-form label-width="120px" class="progress-sync-form">
      <el-form-item label="当前状态">
        <el-select v-model="progressStatusAfterSave" filterable placeholder="选择流程中的阶段">
          <el-option v-for="item in progressDialogSteps" :key="item.name" :label="item.name" :value="item.name" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="resetProgressFlow">恢复默认</el-button>
      <el-button @click="progressVisible = false">取消</el-button>
      <el-button type="primary" @click="saveProgressFlow">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="interviewVisible" title="面试记录" width="680px">
    <el-form :model="interviewForm" label-width="100px">
      <el-form-item label="面试轮次"><el-input v-model="interviewForm.round" placeholder="一面 / 二面 / 主管面 / HR 面" /></el-form-item>
      <el-form-item label="面试时间"><el-date-picker v-model="interviewForm.interviewTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      <el-form-item label="面试官"><el-input v-model="interviewForm.interviewer" /></el-form-item>
      <el-form-item label="面试问题"><el-input v-model="interviewForm.questions" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="面试结果"><el-input v-model="interviewForm.result" /></el-form-item>
      <el-form-item label="难度评价"><el-rate v-model="interviewForm.difficulty" /></el-form-item>
      <el-form-item label="总结备注"><el-input v-model="interviewForm.summary" type="textarea" :rows="3" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="interviewVisible = false">取消</el-button>
      <el-button type="primary" @click="saveInterview">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="noteVisible" title="Markdown 笔记" width="90%">
    <el-input v-model="noteForm.title" placeholder="笔记标题" style="margin-bottom: 12px" />
    <div class="markdown-layout">
      <el-input v-model="noteForm.content" type="textarea" :rows="18" />
      <div class="markdown-preview" v-html="preview"></div>
    </div>
    <template #footer>
      <el-button @click="noteVisible = false">取消</el-button>
      <el-button type="primary" @click="saveNote">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="notePreviewVisible" :title="notePreviewTitle" width="86%" top="4vh" destroy-on-close>
    <div class="markdown-preview note-dialog-preview" v-html="notePreviewHtml"></div>
  </el-dialog>

  <el-dialog v-model="resumePreviewVisible" :title="detail.resume?.versionName || 'PDF 预览'" width="86%" top="4vh" destroy-on-close>
    <iframe v-if="resumePreviewUrl" class="pdf-preview" :src="resumePreviewUrl"></iframe>
  </el-dialog>
</template>

<script setup lang="ts">
import MarkdownIt from 'markdown-it'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Collection, EditPen, Files, Medal, Promotion, Star, User, Warning } from '@element-plus/icons-vue'
import { applicationApi, interviewApi, noteApi, resumeApi } from '../api'
import type { InterviewNote, InterviewRecord, JobApplication } from '../types'
import { formatDateTime } from '../utils/time'

type ProgressStep = { name: string; description: string }

const route = useRoute()
const jobId = Number(route.params.id)
const md = new MarkdownIt({ html: false, linkify: true, breaks: true })
const detail = ref<any>({ application: {}, resume: null, interviewRecords: [], interviewNotes: [] })
const application = computed<JobApplication>(() => detail.value.application || {})
const interviewVisible = ref(false)
const noteVisible = ref(false)
const notePreviewVisible = ref(false)
const progressVisible = ref(false)
const progressText = ref('')
const progressStatusAfterSave = ref('')
const selectedProgressName = ref('')
const resumePreviewVisible = ref(false)
const resumePreviewUrl = ref('')
const notePreviewTitle = ref('')
const notePreviewContent = ref('')
const interviewForm = reactive<InterviewRecord>({ jobId, round: '' })
const noteForm = reactive<InterviewNote>({
  jobId,
  title: '',
  content: '# 面试复盘\n\n## 面试问题\n\n## 自我评价\n\n## 后续准备\n'
})
const progressForm = reactive({
  currentStatus: '',
  progressResult: '进行中',
  progressOperatedTime: ''
})
const progressResultOptions = ['进行中', '成功', '失败']
const preview = computed(() => md.render(noteForm.content || ''))
const notePreviewHtml = computed(() => md.render(notePreviewContent.value || ''))
const jobDescriptionHtml = computed(() => md.render(application.value.jobDescription || ''))

const defaultProgress: ProgressStep[] = [
  { name: '收藏', description: '值得关注，后续可安排投递' },
  { name: '待投递', description: '信息已记录，等待投递' },
  { name: '已投递', description: '简历已投递，等待反馈' },
  { name: '笔试', description: '进入笔试或测评阶段' },
  { name: '面试中', description: '正在推进面试流程' },
  { name: '一面', description: '第一轮面试' },
  { name: '二面', description: '第二轮面试' },
  { name: '三面', description: '第三轮面试' },
  { name: '四面', description: '第四轮面试' },
  { name: '主管面', description: '主管或业务负责人面试' },
  { name: 'HR 面', description: 'HR 沟通或终面' },
  { name: 'Offer', description: '已获得 Offer' },
  { name: '淘汰', description: '流程结束，已淘汰或主动放弃' }
]

const statusConfig: Record<string, any> = {
  收藏: { color: '#f59e0b', icon: Star, className: 'status-favorite' },
  待投递: { color: '#667085', icon: Collection, className: 'status-pending' },
  已投递: { color: '#409eff', icon: Promotion, className: 'status-submitted' },
  笔试: { color: '#7c3aed', icon: EditPen, className: 'status-written' },
  一面: { color: '#0e7490', icon: ChatDotRound, className: 'status-interview' },
  二面: { color: '#0e7490', icon: ChatDotRound, className: 'status-interview' },
  三面: { color: '#0e7490', icon: ChatDotRound, className: 'status-interview' },
  四面: { color: '#0e7490', icon: ChatDotRound, className: 'status-interview' },
  主管面: { color: '#0e7490', icon: User, className: 'status-interview' },
  'HR 面': { color: '#0e7490', icon: User, className: 'status-interview' },
  面试中: { color: '#06b6d4', icon: ChatDotRound, className: 'status-interview' },
  Offer: { color: '#16a34a', icon: Medal, className: 'status-offer' },
  淘汰: { color: '#dc2626', icon: Warning, className: 'status-rejected' }
}

const flowSteps = computed(() => parseProgressFlow(application.value.progressFlow))
const progressDialogSteps = computed(() => parseProgressFlow(progressText.value))
const progressMismatch = computed(() => {
  const current = application.value.currentStatus
  return Boolean(current && progressDialogSteps.value.length && !progressDialogSteps.value.some((item) => item.name === current))
})
const progressItems = computed(() => {
  const current = application.value.currentStatus || '待投递'
  const currentIndex = flowSteps.value.findIndex((item) => item.name === current)
  return flowSteps.value.map((item, index) => {
    const meta = statusMeta(item.name)
    const isCurrent = item.name === current
    const active = current === '淘汰' ? item.name === '淘汰' || index <= Math.max(currentIndex, 2) : currentIndex >= 0 && index <= currentIndex
    return {
      ...item,
      active,
      current: isCurrent,
      color: timelineColor(active, isCurrent, meta.color),
      icon: meta.icon,
      timestamp: timestampFor(item.name)
    }
  })
})

onMounted(load)

watch(progressText, () => {
  const steps = parseProgressFlow(progressText.value)
  if (!steps.length) {
    progressStatusAfterSave.value = ''
    return
  }
  if (!steps.some((item) => item.name === progressStatusAfterSave.value)) {
    progressStatusAfterSave.value = steps[0].name
  }
})

async function load() {
  detail.value = await applicationApi.detail(jobId)
}

function syncProgressForm(statusName = application.value.currentStatus) {
  const isCurrent = statusName === application.value.currentStatus
  progressForm.currentStatus = statusName || flowSteps.value[0]?.name || '待投递'
  progressForm.progressResult = isCurrent ? application.value.progressResult || '进行中' : '进行中'
  progressForm.progressOperatedTime = isCurrent ? application.value.progressOperatedTime || nowValue() : nowValue()
}

function statusMeta(status?: string) {
  return statusConfig[status || '待投递'] || { color: '#667085', icon: Files, className: 'status-default' }
}

function resultMeta(result?: string) {
  if (result === '成功') return { type: 'success', color: '#16a34a' }
  if (result === '失败') return { type: 'danger', color: '#dc2626' }
  return { type: 'info', color: '#409eff' }
}

function timelineColor(active: boolean, current: boolean, fallback: string) {
  if (!active) return '#d0d5dd'
  if (current && application.value.progressResult) return resultMeta(application.value.progressResult).color
  return fallback
}

function parseProgressFlow(value?: string) {
  if (!value?.trim()) return defaultProgress
  return value.split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const [name, ...desc] = line.split('|')
      return { name: name.trim(), description: desc.join('|').trim() || '自定义流程节点' }
    })
}

function stringifyProgressFlow(items = defaultProgress) {
  return items.map((item) => `${item.name} | ${item.description}`).join('\n')
}

function openProgressDialog() {
  progressText.value = application.value.progressFlow || stringifyProgressFlow()
  const current = application.value.currentStatus || ''
  const steps = parseProgressFlow(progressText.value)
  progressStatusAfterSave.value = steps.some((item) => item.name === current) ? current : steps[0]?.name || ''
  progressVisible.value = true
}

function resetProgressFlow() {
  progressText.value = stringifyProgressFlow()
  const current = application.value.currentStatus || ''
  progressStatusAfterSave.value = defaultProgress.some((item) => item.name === current) ? current : defaultProgress[0].name
}

async function saveProgressFlow() {
  const steps = parseProgressFlow(progressText.value)
  if (!steps.length) {
    ElMessage.warning('请至少保留一个流程阶段')
    return
  }
  const currentStatus = progressStatusAfterSave.value
  if (!currentStatus || !steps.some((item) => item.name === currentStatus)) {
    ElMessage.warning('请选择一个流程中的阶段作为当前状态')
    return
  }
  const operatedTime = currentStatus === application.value.currentStatus ? application.value.progressOperatedTime : nowValue()
  await applicationApi.update(jobId, {
    ...application.value,
    currentStatus,
    progressResult: currentStatus === application.value.currentStatus ? application.value.progressResult || '进行中' : '进行中',
    progressOperatedTime: operatedTime,
    progressFlow: progressText.value
  })
  progressVisible.value = false
  await load()
}

function openProgressManager(statusName: string) {
  selectedProgressName.value = statusName
  syncProgressForm(statusName)
}

function closeProgressManager() {
  selectedProgressName.value = ''
}

async function saveCurrentProgress() {
  if (!progressForm.currentStatus) {
    ElMessage.warning('请选择当前进度')
    return
  }
  await applicationApi.update(jobId, {
    ...application.value,
    currentStatus: progressForm.currentStatus,
    progressResult: progressForm.progressResult,
    progressOperatedTime: progressForm.progressOperatedTime || nowValue()
  })
  ElMessage.success('进度已更新')
  selectedProgressName.value = ''
  await load()
}

function formatDate(value?: string) {
  return formatDateTime(value)
}

function nowValue() {
  const now = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

function timestampFor(status: string) {
  if (status === '已投递') return formatDate(application.value.appliedTime)
  const interview = detail.value.interviewRecords?.find((item: InterviewRecord) => item.round === status)
  return interview ? formatDate(interview.interviewTime) : ''
}

function openInterview() {
  Object.assign(interviewForm, { id: undefined, jobId, round: '', interviewTime: '', questions: '', result: '', difficulty: 0, summary: '' })
  interviewVisible.value = true
}

function editInterview(row: InterviewRecord) {
  Object.assign(interviewForm, row)
  interviewVisible.value = true
}

async function saveInterview() {
  interviewForm.id ? await interviewApi.update(interviewForm.id, interviewForm) : await interviewApi.create(interviewForm)
  interviewVisible.value = false
  await load()
}

async function removeInterview(id: number) {
  await interviewApi.remove(id)
  await load()
}

function openNote() {
  Object.assign(noteForm, {
    id: undefined,
    jobId,
    title: '',
    content: '# 面试复盘\n\n## 面试问题\n\n## 自我评价\n\n## 后续准备\n'
  })
  noteVisible.value = true
}

function editNote(row: InterviewNote) {
  Object.assign(noteForm, row)
  noteVisible.value = true
}

function previewNote(row: InterviewNote) {
  notePreviewTitle.value = row.title || row.fileName || 'Markdown 笔记预览'
  notePreviewContent.value = row.content || ''
  notePreviewVisible.value = true
}

async function saveNote() {
  noteForm.id ? await noteApi.update(noteForm.id, noteForm) : await noteApi.create(noteForm)
  noteVisible.value = false
  await load()
}

async function removeNote(id: number) {
  await noteApi.remove(id)
  await load()
}

function download(url: string) {
  window.open(url, '_blank')
}

function previewResume() {
  if (!detail.value.resume?.id) return
  resumePreviewUrl.value = resumeApi.previewUrl(detail.value.resume.id)
  resumePreviewVisible.value = true
}
</script>
