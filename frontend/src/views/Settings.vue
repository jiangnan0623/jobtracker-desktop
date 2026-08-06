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
      <el-alert class="settings-alert" type="info" :closable="false" show-icon title="桌面端会把配置持久化保存，重启客户端后仍然生效。" />

      <el-form class="settings-form" label-position="top" :model="form">
        <el-form-item label="简历保存目录"><div class="path-row"><el-input v-model="form.resumeDir" placeholder="请选择或输入简历保存目录" /><el-button :icon="FolderOpened" @click="chooseDir('resumeDir')">选择目录</el-button></div></el-form-item>
        <el-form-item label="面试笔记保存目录"><div class="path-row"><el-input v-model="form.noteDir" placeholder="请选择或输入面试笔记保存目录" /><el-button :icon="FolderOpened" @click="chooseDir('noteDir')">选择目录</el-button></div></el-form-item>
        <el-form-item label="普通笔记保存目录"><div class="path-row"><el-input v-model="form.generalNoteDir" placeholder="请选择或输入普通笔记保存目录" /><el-button :icon="FolderOpened" @click="chooseDir('generalNoteDir')">选择目录</el-button></div></el-form-item>

        <el-divider content-position="center">简历命名信息</el-divider>
        <div class="settings-name-grid">
          <el-form-item label="姓名"><el-input v-model="form.resumeOwnerName" placeholder="例如：演示用户" /></el-form-item>
          <el-form-item label="学校"><el-input v-model="form.resumeOwnerSchool" placeholder="例如：示例大学" /></el-form-item>
          <el-form-item label="毕业届别"><el-input v-model="form.resumeGraduationYear" placeholder="例如：20XX 届" /></el-form-item>
        </div>

        <el-divider content-position="center">默认命名规则</el-divider>
        <div class="default-naming-rule">
          <el-form-item label="默认模板">
            <el-select v-model="form.resumeNamingTemplate" @change="refreshDefaultTemplate">
              <el-option v-for="item in namingTemplateOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <div class="naming-rule-detail">
            <span>模板规则</span><code>{{ selectedTemplateRule }}</code>
            <span>示例文件名</span><strong>{{ namingPreview }}.pdf</strong>
            <p>公司名称、岗位名称等岗位字段使用示例内容预览；新增岗位时会自动替换为真实数据。</p>
          </div>
        </div>

        <el-divider content-position="center">自定义模板库</el-divider>
        <section class="template-library">
          <div class="template-library-head">
            <span>已保存 {{ customTemplates.length }} 个模板，可在新增岗位时直接选择。</span>
            <div class="template-library-toolbar">
              <el-button v-if="templateLibraryExpanded" size="small" @click="openTemplateEditor()">新建模板</el-button>
              <el-button text size="small" @click="templateLibraryExpanded = !templateLibraryExpanded">{{ templateLibraryExpanded ? '收起' : '展开' }}</el-button>
            </div>
          </div>
          <div v-if="templateLibraryExpanded && customTemplates.length" class="template-card-list">
            <article v-for="item in customTemplates" :key="item.id" class="template-card template-card-compact">
              <div class="template-card-content">
                <strong>{{ item.name }}</strong>
                <div class="template-card-detail"><span>规则</span><code>{{ item.template }}</code></div>
                <div class="template-card-detail"><span>示例</span><span>{{ renderTemplatePreview(item.template) }}.pdf</span></div>
              </div>
              <div class="template-card-actions">
                <el-button text @click="openTemplateEditor(item)">编辑</el-button>
                <el-button text @click="copyTemplate(item)">复制</el-button>
                <el-button text type="danger" @click="removeCustomTemplate(item)">删除</el-button>
              </div>
            </article>
          </div>
          <div v-else-if="templateLibraryExpanded" class="template-library-empty">暂未创建自定义模板。你可以创建适合不同公司、岗位或简历版本的命名规则。</div>
        </section>
      </el-form>

      <div class="settings-actions"><el-button @click="loadSettings">取消修改</el-button><el-button type="primary" :loading="saving" @click="saveSettings">保存设置</el-button></div>
    </div>

    <el-dialog v-model="templateEditorVisible" :title="editingTemplateId ? '编辑命名模板' : '新建命名模板'" width="640px" class="template-editor-dialog" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="模板名称"><el-input v-model="templateDraft.name" maxlength="40" show-word-limit placeholder="例如：公司岗位定制版" /></el-form-item>
        <el-form-item label="命名规则"><el-input ref="templateInput" v-model="templateDraft.template" placeholder="点击下方字段组成模板" /></el-form-item>
        <el-form-item label="常用字段"><div class="template-token-list"><el-button v-for="field in commonNamingFields" :key="field.token" size="small" @click="insertTemplateToken(field.token)">{{ field.label }}</el-button></div></el-form-item>
        <el-form-item label="更多字段"><div class="template-token-list"><el-button v-for="field in extraNamingFields" :key="field.token" size="small" @click="insertTemplateToken(field.token)">{{ field.label }}</el-button></div></el-form-item>
        <div class="form-item-hint">点击字段会插入光标位置；系统会自动补充分隔符，也可在规则中手动调整连接符。</div>
        <div class="template-editor-preview"><span>实时预览</span><strong>{{ renderTemplatePreview(templateDraft.template) || '请填写命名规则' }}<template v-if="templateDraft.template">.pdf</template></strong></div>
      </el-form>
      <template #footer><el-button @click="templateEditorVisible = false">取消</el-button><el-button type="primary" :loading="templateSaving" @click="saveTemplate">保存模板</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'
import { storageApi } from '../api'
import { parseSavedResumeNamingTemplates, renderResumeName, resumeNamingFields, resumeNamingPresets, templateForSelection, type SavedResumeNamingTemplate } from '../utils/resumeNaming'

type StorageKey = 'resumeDir' | 'noteDir' | 'generalNoteDir'
type SettingsForm = { resumeDir: string; noteDir: string; generalNoteDir: string; resumeOwnerName: string; resumeOwnerSchool: string; resumeGraduationYear: string; resumeNamingTemplate: string; resumeCustomNamingTemplate: string; resumeCustomNamingTemplates: string }

const saving = ref(false)
const templateSaving = ref(false)
const templateEditorVisible = ref(false)
const templateLibraryExpanded = ref(false)
const editingTemplateId = ref('')
const templateInput = ref<any>()
const form = reactive<SettingsForm>({ resumeDir: '', noteDir: '', generalNoteDir: '', resumeOwnerName: '', resumeOwnerSchool: '', resumeGraduationYear: '', resumeNamingTemplate: 'general', resumeCustomNamingTemplate: '', resumeCustomNamingTemplates: '' })
const customTemplates = ref<SavedResumeNamingTemplate[]>([])
const templateDraft = reactive<SavedResumeNamingTemplate>({ id: '', name: '', template: '' })
const namingTemplateOptions = computed(() => resumeNamingPresets)
const selectedTemplate = computed(() => namingTemplateOptions.value.find(item => item.value === form.resumeNamingTemplate) || resumeNamingPresets.find(item => item.value === 'general')!)
const selectedTemplateRule = computed(() => selectedTemplate.value.template)
const commonNamingFields = resumeNamingFields.filter(item => ['{姓名}', '{公司名称}', '{岗位名称}', '{学校}', '{毕业年份}'].includes(item.token))
const extraNamingFields = resumeNamingFields.filter(item => !commonNamingFields.includes(item))
const namingPreview = computed(() => renderTemplatePreview(selectedTemplateRule.value))

function renderTemplatePreview(template: string) {
  return renderResumeName(template, { ownerName: form.resumeOwnerName || '演示用户', ownerSchool: form.resumeOwnerSchool || '示例大学', graduationYear: form.resumeGraduationYear || '20XX 届', companyName: '星云科技', positionName: '数据产品工程师', positionType: '技术研发', recruitmentType: '秋招', resumeCategory: '通用简历', workLocation: '远程' })
}

function syncFromData(data: Partial<SettingsForm>, showFallbackMessage = false) {
  form.resumeDir = data.resumeDir || ''; form.noteDir = data.noteDir || ''; form.generalNoteDir = data.generalNoteDir || ''
  form.resumeOwnerName = data.resumeOwnerName || ''; form.resumeOwnerSchool = data.resumeOwnerSchool || ''; form.resumeGraduationYear = data.resumeGraduationYear || ''
  form.resumeCustomNamingTemplate = data.resumeCustomNamingTemplate || ''; form.resumeCustomNamingTemplates = data.resumeCustomNamingTemplates || ''
  customTemplates.value = parseSavedResumeNamingTemplates(form.resumeCustomNamingTemplates, form.resumeCustomNamingTemplate)
  const requested = data.resumeNamingTemplate || 'general'
  const valid = namingTemplateOptions.value.some(item => item.value === requested)
  form.resumeNamingTemplate = valid ? requested : 'general'
  if (!valid && showFallbackMessage && requested.startsWith('custom:')) ElMessage.warning('自定义模板不能作为默认模板，已切换为“通用版”')
}

async function loadSettings() { syncFromData(await storageApi.get() as unknown as SettingsForm, true) }
async function chooseDir(key: StorageKey) { if (!window.jobTrackerDesktop?.selectDirectory) return ElMessage.info('当前不是桌面客户端环境，请直接手动输入目录路径'); const dir = await window.jobTrackerDesktop.selectDirectory(); if (dir) form[key] = dir }
function payload() { return { ...form, resumeCustomNamingTemplates: JSON.stringify(customTemplates.value), resumeCustomNamingTemplate: '' } }
async function persist(message?: string) { const data = await storageApi.update(payload()) as unknown as SettingsForm; syncFromData(data); if (message) ElMessage.success(message) }
async function saveSettings() { saving.value = true; try { await persist('设置已更新') } finally { saving.value = false } }
function refreshDefaultTemplate() { if (!namingTemplateOptions.value.some(item => item.value === form.resumeNamingTemplate)) form.resumeNamingTemplate = 'general' }
function openTemplateEditor(item?: SavedResumeNamingTemplate) { editingTemplateId.value = item?.id || ''; Object.assign(templateDraft, item ? { ...item } : { id: '', name: '', template: '' }); templateEditorVisible.value = true }
function insertTemplateToken(token: string) { const input = templateInput.value?.input || templateInput.value?.$el?.querySelector('input'); const start = input?.selectionStart ?? templateDraft.template.length; const end = input?.selectionEnd ?? start; const prefix = start > 0 && templateDraft.template[start - 1] !== '-' ? '-' : ''; templateDraft.template = `${templateDraft.template.slice(0, start)}${prefix}${token}${templateDraft.template.slice(end)}`; requestAnimationFrame(() => { input?.focus(); input?.setSelectionRange(start + prefix.length + token.length, start + prefix.length + token.length) }) }
async function saveTemplate() { const name = templateDraft.name.trim(); const template = templateDraft.template.trim(); if (!name || !template) return ElMessage.warning('请填写模板名称和命名规则'); if (customTemplates.value.some(item => item.id !== editingTemplateId.value && item.name === name)) return ElMessage.warning('已有同名模板，请使用其他名称'); templateSaving.value = true; try { const id = editingTemplateId.value || `template-${Date.now()}`; const next = { id, name, template }; const index = customTemplates.value.findIndex(item => item.id === id); if (index >= 0) customTemplates.value.splice(index, 1, next); else customTemplates.value.push(next); await persist('模板已保存'); templateEditorVisible.value = false } finally { templateSaving.value = false } }
function copyTemplate(item: SavedResumeNamingTemplate) { openTemplateEditor({ ...item, id: '', name: `${item.name} 副本` }) }
async function removeCustomTemplate(item: SavedResumeNamingTemplate) { try { await ElMessageBox.confirm(`确认删除“${item.name}”吗？`, '删除自定义模板', { type: 'warning' }) } catch { return }; customTemplates.value = customTemplates.value.filter(value => value.id !== item.id); await persist('模板已删除') }

onMounted(loadSettings)
</script>
