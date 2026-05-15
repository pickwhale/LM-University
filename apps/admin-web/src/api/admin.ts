import http from './http'
import type {
  AiConfig,
  AiConfigPayload,
  AiConfigTestResponse,
  AppSetting,
  AppSettingPayload,
  AcademicResult,
  AcademicResultPayload,
  AdmissionResult,
  AdmissionResultPayload,
  Consultation,
  ConsultationReplyPayload,
  DashboardSummary,
  Major,
  MajorApplication,
  MajorPayload,
  NewsArticle,
  NewsPayload,
  PageResult,
  Province,
  ProvincePayload,
  ReviewPayload,
  SitePage,
  SitePagePayload,
  Student,
  StudentPayload,
  University,
  UniversityApplication,
  UniversityPayload
} from '../types/admin'

interface ApiEnvelope<T> {
  success: boolean
  data: T
  timestamp: string
}

export interface UploadedFile {
  relativePath: string
  originalFileName: string
  contentType: string
  size: number
  url: string
}

const unwrap = async <T>(promise: Promise<{ data: ApiEnvelope<T> }>) => {
  const response = await promise
  return response.data.data
}

export const uploadFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return unwrap<UploadedFile>(
    http.post('/files', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  )
}

export const fetchDashboardSummary = () =>
  unwrap<DashboardSummary>(http.get('/admin/dashboard'))

export const fetchProvinces = () =>
  unwrap<Province[]>(http.get('/admin/provinces'))

export const createProvince = (payload: ProvincePayload) =>
  unwrap<Province>(http.post('/admin/provinces', payload))

export const updateProvince = (id: number, payload: ProvincePayload) =>
  unwrap<Province>(http.put(`/admin/provinces/${id}`, payload))

export const deleteProvince = (id: number) =>
  unwrap<void>(http.delete(`/admin/provinces/${id}`))

export const fetchUniversities = (params: { page: number; size: number; keyword?: string }) =>
  unwrap<PageResult<University>>(http.get('/admin/universities', { params }))

export const createUniversity = (payload: UniversityPayload) =>
  unwrap<University>(http.post('/admin/universities', payload))

export const updateUniversity = (id: number, payload: UniversityPayload) =>
  unwrap<University>(http.put(`/admin/universities/${id}`, payload))

export const deleteUniversity = (id: number) =>
  unwrap<void>(http.delete(`/admin/universities/${id}`))

export const fetchMajors = (params: { page: number; size: number; keyword?: string }) =>
  unwrap<PageResult<Major>>(http.get('/admin/majors', { params }))

export const createMajor = (payload: MajorPayload) =>
  unwrap<Major>(http.post('/admin/majors', payload))

export const updateMajor = (id: number, payload: MajorPayload) =>
  unwrap<Major>(http.put(`/admin/majors/${id}`, payload))

export const deleteMajor = (id: number) =>
  unwrap<void>(http.delete(`/admin/majors/${id}`))

export const fetchStudents = (params: { page: number; size: number; keyword?: string }) =>
  unwrap<PageResult<Student>>(http.get('/admin/students', { params }))

export const createStudent = (payload: StudentPayload) =>
  unwrap<Student>(http.post('/admin/students', payload))

export const updateStudent = (id: number, payload: StudentPayload) =>
  unwrap<Student>(http.put(`/admin/students/${id}`, payload))

export const deleteStudent = (id: number) =>
  unwrap<void>(http.delete(`/admin/students/${id}`))

export const fetchUniversityApplications = (params: { page: number; size: number; status?: string }) =>
  unwrap<PageResult<UniversityApplication>>(http.get('/admin/university-applications', { params }))

export const reviewUniversityApplication = (id: number, payload: ReviewPayload) =>
  unwrap<UniversityApplication>(http.put(`/admin/university-applications/${id}/review`, payload))

export const fetchMajorApplications = (params: { page: number; size: number; status?: string }) =>
  unwrap<PageResult<MajorApplication>>(http.get('/admin/major-applications', { params }))

export const reviewMajorApplication = (id: number, payload: ReviewPayload) =>
  unwrap<MajorApplication>(http.put(`/admin/major-applications/${id}/review`, payload))

export const fetchAdmissionResults = (params: { page: number; size: number }) =>
  unwrap<PageResult<AdmissionResult>>(http.get('/admin/admission-results', { params }))

export const createAdmissionResult = (payload: AdmissionResultPayload) =>
  unwrap<AdmissionResult>(http.post('/admin/admission-results', payload))

export const updateAdmissionResult = (id: number, payload: AdmissionResultPayload) =>
  unwrap<AdmissionResult>(http.put(`/admin/admission-results/${id}`, payload))

export const fetchAcademicResults = (params: { page: number; size: number }) =>
  unwrap<PageResult<AcademicResult>>(http.get('/admin/academic-results', { params }))

export const createAcademicResult = (payload: AcademicResultPayload) =>
  unwrap<AcademicResult>(http.post('/admin/academic-results', payload))

export const updateAcademicResult = (id: number, payload: AcademicResultPayload) =>
  unwrap<AcademicResult>(http.put(`/admin/academic-results/${id}`, payload))

export const fetchConsultations = (params: { page: number; size: number }) =>
  unwrap<PageResult<Consultation>>(http.get('/admin/consultations', { params }))

export const replyConsultation = (id: number, payload: ConsultationReplyPayload) =>
  unwrap<Consultation>(http.put(`/admin/consultations/${id}/reply`, payload))

export const fetchNews = (params: { page: number; size: number; keyword?: string }) =>
  unwrap<PageResult<NewsArticle>>(http.get('/admin/news', { params }))

export const createNews = (payload: NewsPayload) =>
  unwrap<NewsArticle>(http.post('/admin/news', payload))

export const updateNews = (id: number, payload: NewsPayload) =>
  unwrap<NewsArticle>(http.put(`/admin/news/${id}`, payload))

export const deleteNews = (id: number) =>
  unwrap<void>(http.delete(`/admin/news/${id}`))

export const fetchPages = () =>
  unwrap<SitePage[]>(http.get('/admin/pages'))

export const createPage = (payload: SitePagePayload) =>
  unwrap<SitePage>(http.post('/admin/pages', payload))

export const updatePage = (id: number, payload: SitePagePayload) =>
  unwrap<SitePage>(http.put(`/admin/pages/${id}`, payload))

export const deletePage = (id: number) =>
  unwrap<void>(http.delete(`/admin/pages/${id}`))

export const fetchSettings = () =>
  unwrap<AppSetting[]>(http.get('/admin/settings'))

export const createSetting = (payload: AppSettingPayload) =>
  unwrap<AppSetting>(http.post('/admin/settings', payload))

export const updateSetting = (id: number, payload: AppSettingPayload) =>
  unwrap<AppSetting>(http.put(`/admin/settings/${id}`, payload))

export const deleteSetting = (id: number) =>
  unwrap<void>(http.delete(`/admin/settings/${id}`))

export const fetchAiConfig = () =>
  unwrap<AiConfig>(http.get('/admin/ai-config'))

export const updateAiConfig = (payload: AiConfigPayload) =>
  unwrap<AiConfig>(http.put('/admin/ai-config', payload))

export const testAiConfig = (message: string) =>
  unwrap<AiConfigTestResponse>(http.post('/admin/ai-config/test', { message }))
