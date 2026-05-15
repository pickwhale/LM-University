import http from './http'
import { accessToken } from './http'

export type EntityId = string | number

export interface ApiEnvelope<T> {
  success: boolean
  data: T
  timestamp: string
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
}

export interface Province {
  id: EntityId
  name: string
}

export interface University {
  id: EntityId
  name: string
  website?: string
  imagePath?: string
  provinceId?: number
  institutionType?: string
  keyness?: string
  introduction?: string
  phone?: string
  clickCount?: number
}

export interface Major {
  id: EntityId
  code: string
  universityId: EntityId
  name: string
  coverPath?: string
  durationOfStudy?: string
  cutOffScore?: string
  enrollmentQuota?: number
  curriculum?: string
  clickCount?: number
}

export interface NewsArticle {
  id: EntityId
  title: string
  introduction?: string
  picturePath?: string
  content: string
  publishedAt?: string
}

export interface SitePage {
  id: EntityId
  slug: string
  title: string
  subtitle?: string
  content: string
  picture1Path?: string
  picture2Path?: string
  picture3Path?: string
}

export interface StudentProfile {
  id: EntityId
  studentNo: string
  fullName: string
  avatarPath?: string
  gender?: string
  college?: string
  contactNumber?: string
  score?: number | null
}

export interface UniversityApplication {
  id: EntityId
  registrationNo?: string
  universityId: EntityId
  status: string
  reviewComment?: string
  submittedAt?: string
  reviewedAt?: string
  universityNameSnapshot?: string
  institutionTypeSnapshot?: string
  provinceNameSnapshot?: string
}

export interface MajorApplication {
  id: EntityId
  majorId: EntityId
  status: string
  reviewComment?: string
  submittedAt?: string
  reviewedAt?: string
  majorCodeSnapshot?: string
  majorNameSnapshot?: string
  universityNameSnapshot?: string
}

export interface Favorite {
  id: EntityId
  targetType: string
  targetId: EntityId
  name: string
  picturePath?: string
  recommendationType?: string
  remark?: string
}

export interface Consultation {
  id: EntityId
  question: string
  reply?: string
  replied?: boolean
  repliedAt?: string
}

export interface AdmissionResult {
  id: EntityId
  applicationId: EntityId
  resultStatus: string
  feedback?: string
  feedbackAt?: string
}

export interface AcademicResult {
  id: EntityId
  reportNo?: string
  reportContent?: string
  grade?: number
  gradeEvaluation?: string
  enteredAt?: string
}

export interface RecommendationItem {
  id: EntityId
  type: 'UNIVERSITY' | 'MAJOR'
  name: string
  universityName?: string
  province?: string
  institutionType?: string
  majorCode?: string
  imagePath?: string
  latestGrade?: number
  cutOffScore?: number
  margin?: number
  recommendationType: string
  reason: string
  score: number
  clickCount?: number
  enrollmentQuota?: number
}

export interface StudentRecommendations {
  latestGrade?: number
  message: string
  universities: RecommendationItem[]
  majors: RecommendationItem[]
}

export interface AiConversation {
  id: EntityId
  title: string
  createdAt?: string
  updatedAt?: string
}

export interface AiMessage {
  id: EntityId
  conversationId: EntityId
  role: 'user' | 'assistant'
  content: string
  sourcesJson?: string
  createdAt?: string
}

export interface AiStreamDone {
  conversationId: EntityId
  messageId: EntityId
  sources: Array<{ type: string; id: string | number; title: string }>
}

const unwrap = async <T>(promise: Promise<{ data: ApiEnvelope<T> }>) => {
  const response = await promise
  return response.data.data
}

export const fetchProvinces = () => unwrap<Province[]>(http.get('/public/provinces'))

export const fetchUniversities = (params: { page?: number; size?: number; keyword?: string; provinceId?: EntityId }) =>
  unwrap<PageResult<University>>(http.get('/public/universities', { params }))

export const fetchUniversity = (id: EntityId) => unwrap<University>(http.get(`/public/universities/${id}`))

export const fetchMajors = (params: { page?: number; size?: number; keyword?: string; universityId?: EntityId }) =>
  unwrap<PageResult<Major>>(http.get('/public/majors', { params }))

export const fetchMajor = (id: EntityId) => unwrap<Major>(http.get(`/public/majors/${id}`))

export const fetchNews = (params: { page?: number; size?: number; keyword?: string }) =>
  unwrap<PageResult<NewsArticle>>(http.get('/public/news', { params }))

export const fetchNewsArticle = (id: EntityId) => unwrap<NewsArticle>(http.get(`/public/news/${id}`))

export const fetchSitePage = (slug: string) => unwrap<SitePage>(http.get(`/public/pages/${slug}`))

export const fetchProfile = () => unwrap<StudentProfile>(http.get('/student/profile'))

export const updateProfile = (payload: Partial<StudentProfile>) =>
  unwrap<StudentProfile>(http.put('/student/profile', payload))

export const fetchUniversityApplications = () =>
  unwrap<PageResult<UniversityApplication>>(http.get('/student/university-applications', { params: { page: 1, size: 50 } }))

export const createUniversityApplication = (universityId: EntityId) =>
  unwrap<UniversityApplication>(http.post('/student/university-applications', { universityId }))

export const fetchMajorApplications = () =>
  unwrap<PageResult<MajorApplication>>(http.get('/student/major-applications', { params: { page: 1, size: 50 } }))

export const createMajorApplication = (majorId: EntityId) =>
  unwrap<MajorApplication>(http.post('/student/major-applications', { majorId }))

export const fetchFavorites = () =>
  unwrap<PageResult<Favorite>>(http.get('/student/favorites', { params: { page: 1, size: 50 } }))

export const checkIsFavorited = (targetType: string, targetId: EntityId) =>
  unwrap<boolean>(http.get('/student/favorites/check', { params: { targetType, targetId } }))

export const createFavorite = (payload: {
  targetType: string
  targetId: EntityId
  name: string
  picturePath?: string
  recommendationType?: string
  remark?: string
}) => unwrap<Favorite>(http.post('/student/favorites', payload))

export const deleteFavorite = (id: EntityId) => unwrap<void>(http.delete(`/student/favorites/${id}`))

export const fetchConsultations = () =>
  unwrap<PageResult<Consultation>>(http.get('/student/consultations', { params: { page: 1, size: 50 } }))

export const createConsultation = (question: string) =>
  unwrap<Consultation>(http.post('/student/consultations', { question }))

export const fetchAdmissionResults = () =>
  unwrap<PageResult<AdmissionResult>>(http.get('/student/admission-results', { params: { page: 1, size: 50 } }))

export const fetchAcademicResults = () =>
  unwrap<PageResult<AcademicResult>>(http.get('/student/academic-results', { params: { page: 1, size: 50 } }))

export const fetchStudentRecommendations = (limit = 10) =>
  unwrap<StudentRecommendations>(http.get('/student/recommendations', { params: { limit } }))

export const fetchAiConversations = () =>
  unwrap<AiConversation[]>(http.get('/student/ai/conversations'))

export const fetchAiMessages = (conversationId: EntityId) =>
  unwrap<AiMessage[]>(http.get(`/student/ai/conversations/${conversationId}/messages`))

export const deleteAiConversation = (conversationId: EntityId) =>
  unwrap<void>(http.delete(`/student/ai/conversations/${conversationId}`))

export const streamAiChat = async (
  payload: { conversationId?: EntityId | null; message: string },
  handlers: {
    onDelta: (content: string) => void | Promise<void>
    onDone: (payload: AiStreamDone) => void | Promise<void>
    onError: (message: string) => void | Promise<void>
  }
) => {
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  const response = await fetch(`${apiBaseUrl}/student/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken.get() ?? ''}`
    },
    body: JSON.stringify(payload)
  })
  if (!response.ok || !response.body) {
    const text = await response.text()
    throw new Error(text || 'AI request failed')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let completed = false
  let receivedDelta = false
  while (true) {
    let chunk: ReadableStreamReadResult<Uint8Array>
    try {
      chunk = await reader.read()
    } catch (caught) {
      if (completed || (receivedDelta && isStreamCloseError(caught))) {
        return
      }
      throw caught
    }
    const { done, value } = chunk
    if (done) {
      break
    }
    buffer += decoder.decode(value, { stream: true })
    const events = buffer.split(/\r?\n\r?\n/)
    buffer = events.pop() ?? ''
    for (const rawEvent of events) {
      const result = await handleSseEvent(rawEvent, handlers, completed)
      receivedDelta = result.receivedDelta || receivedDelta
      completed = result.completed || completed
      if (completed) {
        await reader.cancel().catch(() => undefined)
        return
      }
    }
  }
  buffer += decoder.decode()
  if (buffer.trim()) {
    const result = await handleSseEvent(buffer, handlers, completed)
    receivedDelta = result.receivedDelta || receivedDelta
    completed = result.completed || completed
  }
}

const handleSseEvent = async (
  rawEvent: string,
  handlers: {
    onDelta: (content: string) => void | Promise<void>
    onDone: (payload: AiStreamDone) => void | Promise<void>
    onError: (message: string) => void | Promise<void>
  },
  completed = false
) => {
  const eventName = rawEvent.match(/^event:\s*(.+)$/m)?.[1]?.trim() || 'message'
  const data = rawEvent
    .split(/\r?\n/)
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trimStart())
    .join('\n')
  if (!data) {
    return { completed, receivedDelta: false }
  }
  let parsed: any
  try {
    parsed = JSON.parse(data)
  } catch {
    if (!completed) {
      throw new Error('Invalid AI stream response')
    }
    return { completed: true, receivedDelta: false }
  }
  if (eventName === 'delta') {
    await handlers.onDelta(parsed.content ?? '')
    return { completed: false, receivedDelta: true }
  } else if (eventName === 'done') {
    await handlers.onDone(parsed)
    return { completed: true, receivedDelta: false }
  } else if (eventName === 'error') {
    if (!completed) {
      await handlers.onError(parsed.message ?? 'AI request failed')
    }
    return { completed, receivedDelta: false }
  }
  return { completed, receivedDelta: false }
}

const isStreamCloseError = (caught: unknown) => {
  const message = caught instanceof Error ? caught.message : String(caught ?? '')
  return /network|fetch|stream|aborted|abort|terminated|incomplete|failed/i.test(message)
}

export const imagePaths = (value?: string | null) =>
  (value ?? '')
    .split(/[,;\n]/)
    .map((path) => path.trim())
    .filter(Boolean)

export const imageUrl = (value?: string | null) => {
  const rawPath = imagePaths(value)[0]
  if (!rawPath) return ''
  if (/^https?:\/\//i.test(rawPath)) {
    try {
      const parsedUrl = new URL(rawPath)
      const uploadIndex = parsedUrl.pathname.indexOf('/upload/')
      if (uploadIndex >= 0) {
        return `${import.meta.env.VITE_API_BASE_URL}/files/legacy/${parsedUrl.pathname.slice(uploadIndex + 1)}`
      }
    } catch {
      return rawPath
    }
    return rawPath
  }

  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  const cleanedPath = rawPath.replace(/\\/g, '/').replace(/^\/+/, '').replace(/^file\//, '')
  if (/^\d{4}\/\d{1,2}\//.test(cleanedPath)) {
    return `${apiBaseUrl}/files/${cleanedPath}`
  }
  return `${apiBaseUrl}/files/legacy/${cleanedPath}`
}
