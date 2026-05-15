<template>
  <div class="portal-page">
    <PortalNav />

    <section class="hero hero--wide">
      <div>
        <p class="eyebrow">{{ t('home.eyebrow') }}</p>
        <h1>{{ t('home.title') }}</h1>
        <p>{{ t('home.subtitle') }}</p>
        <div class="hero-actions">
          <router-link class="primary-link" to="/universities">{{ t('home.browseUniversities') }}</router-link>
          <router-link class="secondary-link" to="/majors">{{ t('home.viewMajors') }}</router-link>
        </div>
      </div>
      <div class="hero-card">
        <strong>{{ summary.universityCount }}</strong>
        <span>{{ t('home.universityCount') }}</span>
        <strong>{{ summary.majorCount }}</strong>
        <span>{{ t('home.majorCount') }}</span>
        <strong>{{ summary.newsCount }}</strong>
        <span>{{ t('home.newsCount') }}</span>
      </div>
    </section>

    <section class="section-head">
      <div>
        <p class="eyebrow">{{ t('home.featuredEyebrow') }}</p>
        <h2>{{ t('home.featuredUniversities') }}</h2>
      </div>
      <router-link to="/universities">{{ t('common.viewAll') }}</router-link>
    </section>

    <section v-if="universities.length" class="card-grid">
      <article v-for="item in universities" :key="item.id" class="catalog-card">
        <img v-if="imageUrl(item.imagePath)" :src="imageUrl(item.imagePath)" :alt="item.name" />
        <div class="catalog-card__body">
          <p>{{ item.institutionType || t('universities.defaultType') }} / {{ item.keyness || t('universities.defaultTier') }}</p>
          <h3>{{ item.name }}</h3>
          <span>{{ item.introduction || t('common.noIntro') }}</span>
          <router-link :to="`/universities/${item.id}`">{{ t('common.viewDetails') }}</router-link>
        </div>
      </article>
    </section>
    <p v-else class="empty-text">{{ t('home.noUniversities') }}</p>

    <section class="section-head">
      <div>
        <p class="eyebrow">{{ t('home.latestNewsEyebrow') }}</p>
        <h2>{{ t('home.latestNews') }}</h2>
      </div>
      <router-link to="/news">{{ t('common.moreNews') }}</router-link>
    </section>

    <section v-if="news.length" class="news-list">
      <router-link v-for="item in news" :key="item.id" class="news-item" :to="`/news/${item.id}`">
        <img v-if="imageUrl(item.picturePath)" :src="imageUrl(item.picturePath)" :alt="item.title" />
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.introduction || t('home.readFull') }}</p>
        </div>
      </router-link>
    </section>
    <p v-else class="empty-text">{{ t('home.noNews') }}</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PortalNav from '../components/PortalNav.vue'
import { fetchMajors, fetchNews, fetchUniversities, imageUrl, type NewsArticle, type University } from '../api/portal'
import { useLanguageStore } from '../stores/language'

const language = useLanguageStore()
const t = language.t
const universities = ref<University[]>([])
const news = ref<NewsArticle[]>([])
const summary = reactive({ universityCount: 0, majorCount: 0, newsCount: 0 })

onMounted(async () => {
  const [universityPage, majorPage, newsPage] = await Promise.all([
    fetchUniversities({ page: 1, size: 3 }),
    fetchMajors({ page: 1, size: 1 }),
    fetchNews({ page: 1, size: 4 })
  ])
  universities.value = universityPage.items
  news.value = newsPage.items
  summary.universityCount = universityPage.total
  summary.majorCount = majorPage.total
  summary.newsCount = newsPage.total
})
</script>
