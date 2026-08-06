export interface ResumeNamingContext {
  ownerName?: string
  ownerSchool?: string
  graduationYear?: string
  companyName?: string
  positionName?: string
  positionType?: string
  recruitmentType?: string
  resumeCategory?: string
  workLocation?: string
}

export interface SavedResumeNamingTemplate {
  id: string
  name: string
  template: string
}

export const resumeNamingFields = [
  { token: '{姓名}', key: 'ownerName', label: '姓名' },
  { token: '{公司名称}', key: 'companyName', label: '公司名称' },
  { token: '{岗位名称}', key: 'positionName', label: '岗位名称' },
  { token: '{岗位类别}', key: 'positionType', label: '岗位类别' },
  { token: '{投递批次}', key: 'recruitmentType', label: '投递批次' },
  { token: '{简历类别}', key: 'resumeCategory', label: '简历类别' },
  { token: '{工作地点}', key: 'workLocation', label: '工作地点' },
  { token: '{学校}', key: 'ownerSchool', label: '学校' },
  { token: '{毕业年份}', key: 'graduationYear', label: '毕业年份' }
] as const

export const resumeNamingPresets = [
  { value: 'company', label: '公司定制：姓名-公司名称-岗位名称-学校', template: '{姓名}-{公司名称}-{岗位名称}-{学校}' },
  { value: 'general', label: '通用：姓名-岗位名称-学校-毕业年份', template: '{姓名}-{岗位名称}-{学校}-{毕业年份}' },
  { value: 'simple', label: '简洁：姓名-岗位名称', template: '{姓名}-{岗位名称}' },
  { value: 'full', label: '完整：姓名-公司名称-岗位名称-学校-毕业年份', template: '{姓名}-{公司名称}-{岗位名称}-{学校}-{毕业年份}' }
] as const

export function templateForResumeName(templateKey: string, customTemplate?: string) {
  return resumeNamingPresets.find((item) => item.value === templateKey)?.template || resumeNamingPresets[0].template
}

export function parseSavedResumeNamingTemplates(value?: string, legacyTemplate?: string): SavedResumeNamingTemplate[] {
  try {
    const parsed = JSON.parse(value || '[]')
    if (Array.isArray(parsed)) {
      return parsed
        .map((item) => ({ id: String(item?.id || ''), name: String(item?.name || '').trim(), template: String(item?.template || '').trim() }))
        .filter((item) => item.id && item.name && item.template)
    }
  } catch {
    // Fall through to the legacy single-template value.
  }
  const legacy = legacyTemplate?.trim()
  return legacy ? [{ id: 'legacy-custom', name: '原自定义模板', template: legacy }] : []
}

export function templateForSelection(templateKey: string, customTemplates: SavedResumeNamingTemplate[]) {
  if (templateKey.startsWith('custom:')) {
    return customTemplates.find((item) => `custom:${item.id}` === templateKey)?.template || resumeNamingPresets[0].template
  }
  return templateForResumeName(templateKey)
}

export function renderResumeName(template: string, context: ResumeNamingContext) {
  const values = Object.fromEntries(resumeNamingFields.map((field) => [field.token, String(context[field.key] || '').trim()]))
  const rendered = template.replace(/\{[^}]+\}/g, (token) => values[token] || '')
  return rendered
    .split('-')
    .map((segment) => segment.trim())
    .filter(Boolean)
    .join('-')
    .replace(/[\\/:*?"<>|]/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 180)
}
