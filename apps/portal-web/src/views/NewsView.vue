<template>
  <div class="portal-page">
    <PortalNav />

    <section class="hero">
      <p class="eyebrow">{{ t('news.eyebrow') }}</p>
      <h1>{{ t('news.title') }}</h1>
      <p>{{ t('news.subtitle') }}</p>
    </section>

    <section class="filter-bar filter-bar--simple">
      <input v-model="keyword" :placeholder="t('news.keyword')" @keyup.enter="reload" />
      <button :disabled="loading" @click="reload">{{ loading ? t('common.searching') : t('common.search') }}</button>
    </section>

    <section v-if="news.length" class="news-list news-list--large">
      <router-link v-for="item in news" :key="item.id" class="news-item" :to="`/news/${item.id}`">
        <img v-if="imageUrl(item.picturePath)" :src="imageUrl(item.picturePath)" :alt="item.title" />
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.introduction || t('home.readFull') }}</p>
          <span>{{ item.publishedAt || item.createdAt }}</span>
        </div>
      </router-link>
    </section>
    <p v-else class="empty-text">{{ loading ? t('news.loading') : t('news.empty') }}</p>

    <div class="pager">
      <button :disabled="loading || page <= 1" @click="goPage(page - 1)">{{ t('common.previousPage') }}</button>
      <span>{{ t('common.pageInfo', { page, total }) }}</span>
      <button :disabled="loading || page * size >= total" @click="goPage(page + 1)">{{ t('common.nextPage') }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PortalNav from '../components/PortalNav.vue'
import { fetchNews, imageUrl, type NewsArticle } from '../api/portal'
import { useLanguageStore } from '../stores/language'

const language = useLanguageStore()
const t = language.t
const news = ref<(NewsArticle & { createdAt?: string })[]>([])
const keyword = ref('')
const page = ref(1)
const size = 8
const total = ref(0)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const result = await fetchNews({ page: page.value, size, keyword: keyword.value || undefined })
    news.value = result.items
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const reload = async () => {
  page.value = 1
  await load()
}

const goPage = async (target: number) => {
  page.value = target
  await load()
}

onMounted(load)
</script>
