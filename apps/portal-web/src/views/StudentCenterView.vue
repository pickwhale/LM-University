<template>
  <div class="portal-page">
    <PortalNav show-logout @logout="logout" />

    <section class="hero">
      <p class="eyebrow">{{ t('student.eyebrow') }}</p>
      <h1>{{ profile?.fullName || auth.me?.displayName || t('student.titleFallback') }}</h1>
      <p>{{ t('student.subtitle') }}</p>
      <div class="hero-actions">
        <button class="primary-link" type="button" :disabled="loading" @click="load">
          {{ loading ? t('student.refreshing') : t('student.refresh') }}
        </button>
        <router-link class="secondary-link" to="/universities">{{ t('student.continueBrowse') }}</router-link>
      </div>
      <p v-if="error" class="notice notice--error">{{ error }}</p>
    </section>

    <section class="dashboard-grid">
      <article class="card profile-card">
        <div class="profile-card__header">
          <img v-if="profileAvatarUrl" class="profile-card__avatar" :src="profileAvatarUrl" :alt="profileDisplayName" />
          <span v-else class="profile-card__avatar profile-card__avatar--fallback">{{ profileInitial }}</span>
          <div>
            <h2>{{ t('student.profile') }}</h2>
            <strong>{{ profileDisplayName }}</strong>
          </div>
        </div>
        <div class="profile-card__details">
          <p>{{ t('student.studentNo') }}: {{ profile?.studentNo || auth.me?.studentNo || '-' }}</p>
          <p>{{ t('student.college') }}: {{ profile?.college || auth.me?.college || '-' }}</p>
          <p>{{ t('student.phone') }}: {{ profile?.contactNumber || auth.me?.contactNumber || '-' }}</p>
        </div>
      </article>

      <article class="card">
        <h2>{{ t('student.consultation') }}</h2>
        <textarea v-model="question" rows="5" :placeholder="t('student.questionPlaceholder')"></textarea>
        <button class="primary-link" type="button" :disabled="submittingQuestion || !question.trim()" @click="submitQuestion">
          {{ submittingQuestion ? t('student.submittingQuestion') : t('student.submitQuestion') }}
        </button>
      </article>
    </section>

    <section class="record-section recommendation-section">
      <div class="section-heading">
        <div>
          <h2>{{ t('student.recommendations') }}</h2>
          <p>
            {{ recommendations?.latestGrade != null ? t('student.latestGrade', { grade: recommendations.latestGrade }) : recommendations?.message || t('student.recommendationLoading') }}
          </p>
        </div>
      </div>

      <div v-if="recommendations && (recommendations.universities.length || recommendations.majors.length)" class="recommendation-layout">
        <div class="recommendation-column">
          <h3>{{ t('student.recommendedUniversities') }}</h3>
          <div class="recommendation-list">
            <router-link
              v-for="item in recommendations.universities.slice(0,3)"
              :key="`university-rec-${item.id}`"
              class="recommendation-card"
              :to="recommendationRoute(item)"
            >
              <img v-if="imageUrl(item.imagePath)" :src="imageUrl(item.imagePath)" :alt="item.name" />
              <span v-else class="image-placeholder">{{ t('common.noImage') }}</span>
              <div class="recommendation-card__body">
                <div class="recommendation-card__title">
                  <strong>{{ item.name }}</strong>
                  <span class="recommendation-tag">{{ item.recommendationType }}</span>
                </div>
                <p class="recommendation-meta">
                  {{ item.province || '-' }} / {{ item.institutionType || t('universities.defaultType') }}
                </p>
                <p>{{ item.reason }}</p>
                <div class="recommendation-stats">
                  <span>{{ t('student.cutoff') }} {{ item.cutOffScore ?? '-' }}</span>
                  <span>{{ t('student.margin') }} {{ item.margin ?? '-' }}</span>
                </div>
              </div>
            </router-link>
          </div>
        </div>

        <div class="recommendation-column">
          <h3>{{ t('student.recommendedMajors') }}</h3>
          <div class="recommendation-list">
            <router-link
              v-for="item in recommendations.majors.slice(0,3)"
              :key="`major-rec-${item.id}`"
              class="recommendation-card"
              :to="recommendationRoute(item)"
            >
              <img v-if="imageUrl(item.imagePath)" :src="imageUrl(item.imagePath)" :alt="item.name" />
              <span v-else class="image-placeholder">{{ t('common.noImage') }}</span>
              <div class="recommendation-card__body">
                <div class="recommendation-card__title">
                  <strong>{{ item.name }}</strong>
                  <span class="recommendation-tag">{{ item.recommendationType }}</span>
                </div>
                <p class="recommendation-meta">
                  {{ item.universityName || '-' }} / {{ item.majorCode || '-' }}
                </p>
                <p>{{ item.reason }}</p>
                <div class="recommendation-stats">
                  <span>{{ t('student.cutoff') }} {{ item.cutOffScore ?? '-' }}</span>
                  <span>{{ t('student.margin') }} {{ item.margin ?? '-' }}</span>
                  <span>{{ t('student.quota') }} {{ item.enrollmentQuota ?? '-' }}</span>
                </div>
              </div>
            </router-link>
          </div>
        </div>
      </div>
      <p v-else class="empty-text">{{ recommendations?.message || t('student.noRecommendations') }}</p>
    </section>

    <section class="record-section">
      <h2>{{ t('student.universityApplications') }}</h2>
      <div v-if="universityApplications.length" class="record-list">
        <router-link
          v-for="item in universityApplications"
          :key="item.id"
          class="record-card record-card--clickable"
          :to="`/universities/${item.universityId}`"
        >
          <strong>{{ item.universityNameSnapshot || `#${item.universityId}` }}</strong>
          <span :class="['status-pill', statusClass(item.status)]">{{ statusText(item.status) }}</span>
          <p>{{ item.reviewComment || t('student.waitingReview') }}</p>
        </router-link>
      </div>
      <p v-else class="empty-text">{{ t('student.noUniversityApplications') }}</p>
    </section>

    <section class="record-section">
      <h2>{{ t('student.majorApplications') }}</h2>
      <div v-if="majorApplications.length" class="record-list">
        <router-link
          v-for="item in majorApplications"
          :key="item.id"
          class="record-card record-card--clickable"
          :to="`/majors/${item.majorId}`"
        >
          <strong>{{ item.majorNameSnapshot || `#${item.majorId}` }}</strong>
          <span :class="['status-pill', statusClass(item.status)]">{{ statusText(item.status) }}</span>
          <p>{{ item.universityNameSnapshot || '-' }} / {{ item.reviewComment || t('student.waitingReview') }}</p>
        </router-link>
      </div>
      <p v-else class="empty-text">{{ t('student.noMajorApplications') }}</p>
    </section>

    <section class="record-section">
      <h2>{{ t('student.results') }}</h2>
      <div v-if="admissionResults.length || academicResults.length" class="record-list">
        <article v-for="item in admissionResults" :key="`admission-${item.id}`" class="record-card">
          <strong>{{ t('student.admissionResult', { status: statusText(item.resultStatus) }) }}</strong>
          <p>{{ item.feedback || t('student.noFeedback') }}</p>
        </article>
        <article v-for="item in academicResults" :key="`academic-${item.id}`" class="record-card">
          <strong>{{ item.reportNo || t('student.reportFallback') }}</strong>
          <p>{{ item.grade ?? '-' }} / {{ item.gradeEvaluation || item.reportContent || t('student.noEvaluation') }}</p>
        </article>
      </div>
      <p v-else class="empty-text">{{ t('student.noResults') }}</p>
    </section>

    <section class="record-section">
      <h2>{{ t('student.favorites') }}</h2>
      <div v-if="favorites.length" class="favorites-grid">
        <article v-for="item in favorites" :key="item.id" class="favorite-card">
          <router-link class="favorite-link" :to="favoriteRoute(item)">
            <img v-if="imageUrl(item.picturePath)" :src="imageUrl(item.picturePath)" :alt="item.name" />
            <span v-else class="image-placeholder">{{ t('common.noImage') }}</span>
            <div class="favorite-card__body">
              <span class="favorite-card__type">{{ favoriteTypeText(item.targetType) }}</span>
              <strong>{{ item.name }}</strong>
              <p>{{ item.remark || t('detail.favoriteAdded') }}</p>
            </div>
          </router-link>
          <div class="favorite-card__actions">
            <router-link class="inline-link" :to="favoriteRoute(item)">{{ t('common.viewDetails') }}</router-link>
            <button
              class="link-button danger"
              type="button"
              :disabled="removingFavoriteId === item.id"
              @click="removeFavorite(item.id)"
            >
              {{ removingFavoriteId === item.id ? t('student.cancelingFavorite') : t('student.cancelFavorite') }}
            </button>
          </div>
        </article>
      </div>
      <p v-else class="empty-text">{{ t('student.noFavorites') }}</p>
    </section>

    <section class="record-section">
      <h2>{{ t('student.consultationReplies') }}</h2>
      <div v-if="consultations.length" class="record-list">
        <article v-for="item in consultations" :key="item.id" class="record-card">
          <strong>{{ item.question }}</strong>
          <p>{{ item.replied ? item.reply : t('student.replyPending') }}</p>
        </article>
      </div>
      <p v-else class="empty-text">{{ t('student.noConsultations') }}</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PortalNav from '../components/PortalNav.vue'
import {
  createConsultation,
  deleteFavorite,
  fetchAcademicResults,
  fetchAdmissionResults,
  fetchConsultations,
  fetchFavorites,
  fetchMajorApplications,
  fetchProfile,
  fetchStudentRecommendations,
  fetchUniversityApplications,
  imageUrl,
  type AcademicResult,
  type AdmissionResult,
  type Consultation,
  type EntityId,
  type Favorite,
  type MajorApplication,
  type RecommendationItem,
  type StudentRecommendations,
  type StudentProfile,
  type UniversityApplication
} from '../api/portal'
import { useAuthStore } from '../stores/auth'
import { useLanguageStore } from '../stores/language'

const router = useRouter()
const auth = useAuthStore()
const language = useLanguageStore()
const t = language.t
const profile = ref<StudentProfile | null>(null)
const universityApplications = ref<UniversityApplication[]>([])
const majorApplications = ref<MajorApplication[]>([])
const admissionResults = ref<AdmissionResult[]>([])
const academicResults = ref<AcademicResult[]>([])
const favorites = ref<Favorite[]>([])
const consultations = ref<Consultation[]>([])
const recommendations = ref<StudentRecommendations | null>(null)
const question = ref('')
const loading = ref(false)
const submittingQuestion = ref(false)
const removingFavoriteId = ref<EntityId | null>(null)
const error = ref('')

const profileDisplayName = computed(() => profile.value?.fullName || auth.me?.displayName || t('student.titleFallback'))
const profileAvatarUrl = computed(() => imageUrl(profile.value?.avatarPath))
const profileInitial = computed(() => profileDisplayName.value.trim().slice(0, 1).toUpperCase() || 'S')

const errorMessage = (caught: unknown) =>
  (caught as any)?.response?.data?.detail || (caught as Error)?.message || t('common.operationFailed')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const [profileRes, universityRes, majorRes, admissionRes, academicRes, favoriteRes, consultationRes, recommendationRes] = await Promise.all([
      fetchProfile(),
      fetchUniversityApplications(),
      fetchMajorApplications(),
      fetchAdmissionResults(),
      fetchAcademicResults(),
      fetchFavorites(),
      fetchConsultations(),
      fetchStudentRecommendations()
    ])
    profile.value = profileRes
    universityApplications.value = universityRes.items
    majorApplications.value = majorRes.items
    admissionResults.value = admissionRes.items
    academicResults.value = academicRes.items
    favorites.value = favoriteRes.items
    consultations.value = consultationRes.items
    recommendations.value = recommendationRes
  } catch (caught) {
    error.value = errorMessage(caught)
  } finally {
    loading.value = false
  }
}

const submitQuestion = async () => {
  if (!question.value.trim()) {
    return
  }
  submittingQuestion.value = true
  error.value = ''
  try {
    await createConsultation(question.value.trim())
    question.value = ''
    await load()
  } catch (caught) {
    error.value = errorMessage(caught)
  } finally {
    submittingQuestion.value = false
  }
}

const removeFavorite = async (id: EntityId) => {
  removingFavoriteId.value = id
  error.value = ''
  try {
    await deleteFavorite(id)
    await load()
  } catch (caught) {
    error.value = errorMessage(caught)
  } finally {
    removingFavoriteId.value = null
  }
}

const favoriteRoute = (favorite: Favorite) => {
  const type = favorite.targetType?.toLowerCase()
  if (type === 'university' || type === 'universityinformation') {
    return `/universities/${favorite.targetId}`
  }
  if (type === 'major' || type === 'professionalinformation') {
    return `/majors/${favorite.targetId}`
  }
  if (type === 'news') {
    return `/news/${favorite.targetId}`
  }
  return '/universities'
}

const favoriteTypeText = (targetType: string) => {
  const type = targetType?.toLowerCase()
  if (type === 'university' || type === 'universityinformation') return t('favorite.university')
  if (type === 'major' || type === 'professionalinformation') return t('favorite.major')
  if (type === 'news') return t('favorite.news')
  return targetType || t('favorite.default')
}

const recommendationRoute = (item: RecommendationItem) =>
  item.type === 'MAJOR' ? `/majors/${item.id}` : `/universities/${item.id}`

const statusText = (status: string) => t(`status.${status}`) || status || '-'

const statusClass = (status: string) => (status || 'pending').toLowerCase()

const logout = async () => {
  await auth.logout()
  await router.replace('/login')
}

onMounted(load)
</script>
