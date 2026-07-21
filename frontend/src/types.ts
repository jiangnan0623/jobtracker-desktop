export interface JobApplication {
  id?: number
  companyName: string
  positionName: string
  positionType?: string
  recruitmentType?: string
  resumeCategory?: string
  workLocation?: string
  salary?: string
  source?: string
  jobLink?: string
  jobDescription?: string
  currentStatus: string
  progressFlow?: string
  progressResult?: string
  progressOperatedTime?: string
  appliedTime?: string
  resumeId?: number
  remark?: string
}

export interface InterviewRecord {
  id?: number
  jobId: number
  round: string
  interviewTime?: string
  interviewer?: string
  questions?: string
  result?: string
  difficulty?: number
  summary?: string
}

export interface InterviewNote {
  id?: number
  jobId: number
  title: string
  fileName?: string
  filePath?: string
  content: string
  createdTime?: string
  updatedTime?: string
}

export interface Resume {
  id: number
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
  versionName: string
  remark?: string
}

export interface Reminder {
  id?: number
  title: string
  content?: string
  remindTime: string
  status: string
}

export interface NoteItem {
  id?: number
  parentId?: number | null
  type: 'FOLDER' | 'NOTE'
  title: string
  fileName?: string
  filePath?: string
  content?: string
  createdTime?: string
  updatedTime?: string
  children?: NoteItem[]
}
