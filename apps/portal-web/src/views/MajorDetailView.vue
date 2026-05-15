<template>
  <div class="portal-page">
    <PortalNav />

    <!-- 加载中 -->
    <section v-if="loading" class="state-panel">
      {{ t('common.loading') || '加载中...' }}
    </section>

    <!-- 错误状态 -->
    <section v-else-if="error" class="state-panel state-panel--error">
      {{ error }}
    </section>

    <!-- 专业详情 -->
    <section v-else-if="major" class="detail-layout">
      <div class="detail-media">
        <img
          v-if="major.coverPath"
          :src="imageUrl(major.coverPath)"
          :alt="major.name"
        />
        <span v-else class="image-placeholder">
          {{ t('common.noImage') || '暂无图片' }}
        </span>
      </div>

      <article class="detail-panel">
        <p class="eyebrow">
          {{ major.code || t('detail.majorEyebrow') || '专业详情' }}
        </p>
        <h1>{{ major.name }}</h1>
        <div class="fact-grid">
          <span>{{ t('detail.majorCode') || '专业代码' }}：{{ major.code || '-' }}</span>
          <span>{{ t('detail.duration') || '学制' }}：{{ major.durationOfStudy || '-' }}</span>
        </div>

        <div class="hero-actions">
          <button
            class="primary-link"
            type="button"
            :disabled="submitting === 'apply'"
            @click="applyMajor"
          >
            {{ submitting === 'apply' ? (t('detail.submitting') || '提交中...') : (t('detail.applyMajor') || '报名该专业') }}
          </button>
          <button
            class="secondary-link"
            type="button"
            :disabled="submitting === 'favorite'"
            @click="favoriteMajor"
          >
            {{ submitting === 'favorite' ? (t('detail.favoriting') || '收藏中...') : (isFavorited ? (t('detail.alreadyFavorited') || '已收藏') : (t('detail.favorite') || '收藏')) }}
          </button>
        </div>

        <p v-if="message" class="notice">{{ message }}</p>
        <p v-if="error" class="notice notice--error">{{ error }}</p>
      </article>
    </section>

    <!-- 专业不存在 -->
    <section v-else class="state-panel">
      {{ t('detail.majorNotFound') || '专业不存在' }}
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PortalNav from '../components/PortalNav.vue'
import {
  checkIsFavorited,
  createFavorite,
  createMajorApplication,
  fetchMajor,
  imageUrl,
  type Major
} from '../api/portal'
import { useAuthStore } from '../stores/auth'
import { useLanguageStore } from '../stores/language'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const language = useLanguageStore()
const t = language.t

const major = ref<Major | null>(null)
const message = ref('')
const error = ref('')
const loading = ref(true)
const submitting = ref<'apply' | 'favorite' | ''>('')
const isFavorited = ref(false)

const errorMessage = (caught: unknown) =>
  (caught as any)?.response?.data?.detail ||
  (caught as Error)?.message ||
  t('common.operationFailed') ||
  '操作失败'

const requireLogin = async () => {
  if (await auth.ensureSession()) {
    return true
  }
  await router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

const applyMajor = async () => {
  if (!major.value || !(await requireLogin())) return
  submitting.value = 'apply'
  error.value = ''
  message.value = ''
  try {
    await createMajorApplication(major.value.id)
    message.value = t('detail.majorSubmitted') || '报名已提交'
  } catch (caught) {
    error.value = errorMessage(caught)
  } finally {
    submitting.value = ''
  }
}

const favoriteMajor = async () => {
  if (!major.value || !(await requireLogin())) return
  if (isFavorited.value) {
    message.value = t('detail.alreadyFavorited') || '已收藏过'
    return
  }
  submitting.value = 'favorite'
  error.value = ''
  message.value = ''
  try {
    await createFavorite({
      targetType: 'MAJOR',
      targetId: major.value.id,
      name: major.value.name,
      picturePath: major.value.coverPath || '',
      recommendationType: major.value.code,
      remark: major.value.durationOfStudy
    })
    isFavorited.value = true
    message.value = t('detail.favoriteAdded') || '收藏成功'
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
    major.value = await fetchMajor(id)

    if (auth.isAuthenticated && major.value) {
      try {
        isFavorited.value = await checkIsFavorited('MAJOR', major.value.id)
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

<style scoped>
/* 复用 UniversityDetailView 的样式体系，确保风格统一 */
.portal-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
}

.state-panel {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  font-size: 16px;
}

.state-panel--error {
  color: #d32f2f;
}

.detail-layout {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  margin-bottom: 40px;
}

.detail-media {
  flex: 1 1 400px;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-media img {
  width: 100%;
  height: auto;
  object-fit: cover;
}

.image-placeholder {
  color: #aaa;
  font-size: 16px;
  padding: 40px;
}

.detail-panel {
  flex: 1 1 400px;
}

.eyebrow {
  font-size: 14px;
  color: #888;
  margin-bottom: 8px;
  text-transform: uppercase;
}

h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
}

p {
  line-height: 1.6;
  color: #444;
  margin-bottom: 20px;
}

.fact-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
}

.fact-grid span {
  background: #f0f2f5;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  color: #333;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.primary-link,
.secondary-link {
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.primary-link {
  background: #1890ff;
  color: white;
}

.primary-link:hover {
  background: #1677cc;
}

.secondary-link {
  background: #f0f2f5;
  color: #333;
}

.secondary-link:hover {
  background: #e0e0e0;
}

.primary-link:disabled,
.secondary-link:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.notice {
  margin-top: 12px;
  padding: 8px 16px;
  background: #f6ffed;
  border-radius: 6px;
  color: #52c41a;
  font-size: 14px;
}

.notice--error {
  background: #fff2f0;
  color: #ff4d4f;
}
</style>