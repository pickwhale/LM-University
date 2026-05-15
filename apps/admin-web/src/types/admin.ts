export interface AuditedEntity {
  id: number
  createdAt: string
  updatedAt: string
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
}

export interface DashboardSummary {
  provinceCount: number
  studentCount: number
  universityCount: number
  majorCount: number
  universityApplicationCount: number
  pendingUniversityApplicationCount: number
  majorApplicationCount: number
  pendingMajorApplicationCount: number
  newsCount: number
  pageCount: number
  settingCount: number
}

export interface Province extends AuditedEntity {
  name: string
}

export interface University extends AuditedEntity {
  name: string
  website?: string
  imagePath?: string
  provinceId?: number
  institutionType?: string
  keyness?: string
  introduction?: string
  phone?: string
  clickCount: number
}

export interface Major extends AuditedEntity {
  code: string
  universityId: number
  name: string
  coverPath?: string
  durationOfStudy?: string
  cutOffScore?: string
  enrollmentQuota?: number
  curriculum?: string
  clickCount: number
}

export interface Student extends AuditedEntity {
  accountId?: number
  studentNo: string
  fullName: string
  avatarPath?: string
  gender?: string
  college?: string
  contactNumber?: string
  score?: number | null
}

export interface UniversityApplication extends AuditedEntity {
  registrationNo?: string
  studentId: number
  universityId: number
  status: string
  reviewComment?: string
  submittedAt?: string
  reviewedAt?: string
  studentNoSnapshot?: string
  studentNameSnapshot?: string
  contactNumberSnapshot?: string
  collegeSnapshot?: string
  universityNameSnapshot?: string
  institutionTypeSnapshot?: string
  provinceNameSnapshot?: string
}

export interface MajorApplication extends AuditedEntity {
  studentId: number
  majorId: number
  status: string
  reviewComment?: string
  submittedAt?: string
  reviewedAt?: string
  studentNoSnapshot?: string
  studentNameSnapshot?: string
  contactNumberSnapshot?: string
  majorCodeSnapshot?: string
  majorNameSnapshot?: string
  universityNameSnapshot?: string
  provinceNameSnapshot?: string
}

export interface AdmissionResult extends AuditedEntity {
  applicationId?: number
  resultStatus: string
  feedback?: string
  feedbackAt?: string
}

export interface AcademicResult extends AuditedEntity {
  studentId?: number
  reportNo?: string
  reportContent?: string
  grade?: number
  gradeEvaluation?: string
  enteredAt?: string
}

export interface Consultation extends AuditedEntity {
  studentId?: number
  question: string
  reply?: string
  replied: boolean
  repliedAt?: string
}

export interface NewsArticle extends AuditedEntity {
  title: string
  introduction?: string
  picturePath?: string
  content: string
  publishedAt?: string
}

export interface SitePage extends AuditedEntity {
  slug: string
  title: string
  subtitle?: string
  content: string
  picture1Path?: string
  picture2Path?: string
  picture3Path?: string
}

export interface AppSetting extends AuditedEntity {
  settingKey: string
  settingValue?: string
}

export interface AiConfig {
  enabled: boolean
  providerName: string
  endpointUrl: string
  httpMethod: string
  headersTemplate: string
  bodyTemplate: string
  model: string
  temperature: number
  maxTokens: number
  systemPrompt: string
  streamProtocol: 'AUTO' | 'SSE' | 'TEXT'
  responseTextPath: string
  doneMarker: string
  timeoutSeconds: number
  apiKeySet: boolean
}

export interface AiConfigPayload {
  enabled: boolean
  providerName: string
  endpointUrl: string
  httpMethod: string
  apiKey?: string
  headersTemplate: string
  bodyTemplate: string
  model: string
  temperature: number
  maxTokens: number
  systemPrompt: string
  streamProtocol: 'AUTO' | 'SSE' | 'TEXT'
  responseTextPath: string
  doneMarker: string
  timeoutSeconds: number
}

export interface AiConfigTestResponse {
  success: boolean
  message: string
  preview?: string
}

export interface ProvincePayload {
  name: string
}

export interface UniversityPayload {
  name: string
  website?: string
  imagePath?: string
  provinceId: number | null
  institutionType?: string
  keyness?: string
  introduction?: string
  phone?: string
}

export interface MajorPayload {
  code: string
  universityId: number | null
  name: string
  coverPath?: string
  durationOfStudy?: string
  cutOffScore?: string
  enrollmentQuota?: number | null
  curriculum?: string
}

export interface StudentPayload {
  studentNo: string
  password?: string
  fullName: string
  avatarPath?: string
  gender?: string
  college?: string
  contactNumber?: string
  score?: number | null
}

export interface NewsPayload {
  title: string
  introduction?: string
  picturePath?: string
  content: string
  publishedAt?: string | null
}

export interface SitePagePayload {
  slug: string
  title: string
  subtitle?: string
  content: string
  picture1Path?: string
  picture2Path?: string
  picture3Path?: string
}

export interface AppSettingPayload {
  settingKey: string
  settingValue?: string
}

export interface ReviewPayload {
  status: string
  reviewComment?: string
}

export interface AdmissionResultPayload {
  applicationId: number | null
  resultStatus: string
  feedback?: string
  feedbackAt?: string | null
}

export interface AcademicResultPayload {
  studentId: number | null
  reportNo?: string
  reportContent?: string
  grade?: number | null
  gradeEvaluation?: string
  enteredAt?: string | null
}

export interface ConsultationReplyPayload {
  reply: string
}
