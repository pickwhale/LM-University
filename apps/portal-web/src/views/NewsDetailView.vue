<template>
  <div class="portal-page">
    <PortalNav />

    <section v-if="loading" class="state-panel">{{ t('newsDetail.loading') }}</section>
    <article v-else-if="article" class="article-page">
      <img v-if="imageUrl(article.picturePath)" :src="imageUrl(article.picturePath)" :alt="article.title" />
      <p class="eyebrow">{{ article.publishedAt || t('newsDetail.fallbackEyebrow') }}</p>
      <h1>{{ article.title }}</h1>
      <p class="lead">{{ article.introduction }}</p>
      <div class="rich-content" v-html="safeContent"></div>
    </article>
    <section v-else class="state-panel">{{ t('newsDetail.notFound') }}</section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import PortalNav from '../components/PortalNav.vue'
import { fetchNewsArticle, imageUrl, type NewsArticle } from '../api/portal'
import { useLanguageStore } from '../stores/language'
import { sanitizeHtml } from '../utils/sanitize'

const route = useRoute()
const language = useLanguageStore()
const t = language.t
const article = ref<NewsArticle | null>(null)
const loading = ref(true)
const safeContent = computed(() => sanitizeHtml(article.value?.content))

onMounted(async () => {
  loading.value = true
  try {
    article.value = await fetchNewsArticle(String(route.params.id))
  } finally {
    loading.value = false
  }
})
</script>
