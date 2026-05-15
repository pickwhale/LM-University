<template>
  <div class="portal-page">
    <PortalNav />

    <section v-if="loading" class="state-panel">{{ t('detail.loadingUniversity') }}</section>

    <section v-else-if="university" class="detail-layout">
      <div class="detail-media">
        <img v-if="imageUrl(university.imagePath)" :src="imageUrl(university.imagePath)" :alt="university.name" />
        <span v-else class="image-placeholder">{{ t('common.noImage') }}</span>
      </div>
      <article class="detail-panel">
        <p class="eyebrow">{{ university.institutionType || t('detail.universityTypeFallback') }}</p>
        <h1>{{ university.name }}</h1>
        <p>{{ university.introduction || t('common.noIntro') }}</p>
        <div class="fact-grid">
          <span>{{ t('detail.tier') }}: {{ university.keyness || '-' }}</span>
          <span>{{ t('detail.phone') }}: {{ university.phone || '-' }}</span>
          <a v-if="university.website" :href="university.website" target="_blank" rel="noopener noreferrer">
            {{ t('detail.website') }}: {{ university.website }}
          </a>
          <span v-else>{{ t('detail.website') }}: -</span>
        </div>
        <div class="hero-actions">
          <button class="primary-link" type="button" :disabled="submitting === 'apply'" @click="applyUniversity">
            {{ submitting === 'apply' ? t('detail.submitting') : t('detail.applyUniversity') }}
          </button>
          <button class="secondary-link" type="button" :disabled="submitting === 'favorite'" @click="favoriteUniversity">
            {{ submitting === 'favorite' ? t('detail.favoriting') : t('detail.favorite') }}
          </button>
        </div>
        <p v-if="message" class="notice">{{ message }}</p>
        <p v-if="error" class="notice notice--error">{{ error }}</p>
      </article>
    </section>

    <section v-else class="state-panel">{{ t('detail.universityNotFound') }}</section>

    <section class="section-head">
      <div>
        <p class="eyebrow">{{ t('detail.majorsEyebrow') }}</p>
        <h2>{{ t('detail.universityMajors') }}</h2>
      </div>
    </section>

    <section v-if="majors.length" class="grid catalog-grid">
      <article v-for="major in majors" :key="major.id" class="card catalog-card">
        <img v-if="imageUrl(major.coverPath)" :src="imageUrl(major.coverPath)" :alt="major.name" />
        <h3>{{ major.name }}</h3>
        <p>{{ major.code }} / {{ major.durationOfStudy || t('common.pendingDuration') }}</p>
        <router-link :to="`/majors/${major.id}`">{{ t('common.viewDetails') }}</router-link>
      </article>
    </section>
    <p v-else class="empty-text">{{ t('detail.noUniversityMajors') }}</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PortalNav from '../components/PortalNav.vue'
import {
  checkIsFavorited,
  createFavorite,
  createUniversityApplication,
  fetchMajors,
  fetchUniversity,
  imageUrl,
  type Major,
  type University
} from '../api/portal'
import { useAuthStore } from '../stores/auth'
import { useLanguageStore } from '../stores/language'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const language = useLanguageStore()
const t = language.t
const university = ref<University | null>(null)
const majors = ref<Major[]>([])
const message = ref('')
const error = ref('')
const loading = ref(true)
const submitting = ref<'apply' | 'favorite' | ''>('')
const isFavorited = ref(false)

const errorMessage = (caught: unknown) =>
  (caught as any)?.response?.data?.detail || (caught as Error)?.message || t('common.operationFailed')

const requireLogin = async () => {
  if (await auth.ensureSession()) {
    return true
  }
  await router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

const applyUniversity = async () => {
  if (!university.value || !(await requireLogin())) return
  submitting.value = 'apply'
  error.value = ''
  message.value = ''
  try {
    await createUniversityApplication(university.value.id)
    message.value = t('detail.universitySubmitted')
  } catch (caught) {
    error.value = errorMessage(caught)
  } finally {
    submitting.value = ''
  }
}

const favoriteUniversity = async () => {
  if (!university.value || !(await requireLogin())) return
  if (isFavorited.value) {
    message.value = t('detail.alreadyFavorited')
    return
  }
  submitting.value = 'favorite'
  error.value = ''
  message.value = ''
  try {
    await createFavorite({
      targetType: 'UNIVERSITY',
      targetId: university.value.id,
      name: university.value.name,
      picturePath: university.value.imagePath || '',
      recommendationType: university.value.keyness,
      remark: university.value.institutionType
    })
    isFavorited.value = true
    message.value = t('detail.favoriteAdded')
  } catch (caught) {
    error.value = errorMessage(caught)
  } finally {
    submitting.value = ''
  }
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const id = String(route.params.id)
    university.value = await fetchUniversity(id)
    majors.value = (await fetchMajors({ page: 1, size: 12, universityId: id })).items

    if (auth.isAuthenticated) {
      try {
        isFavorited.value = await checkIsFavorited('UNIVERSITY', university.value.id)
      } catch {
        isFavorited.value = false
      }
    }
  } catch (caught) {
    error.value = errorMessage(caught)
  } finally {
    loading.value = false
  }
})
</script>
