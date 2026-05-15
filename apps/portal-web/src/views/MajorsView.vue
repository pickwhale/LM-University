<template>
  <div class="portal-page">
    <PortalNav />

    <section class="hero">
      <p class="eyebrow">{{ t('majors.eyebrow') }}</p>
      <h1>{{ t('majors.title') }}</h1>
      <p>{{ t('majors.subtitle') }}</p>
    </section>

    <section class="filter-bar filter-bar--simple">
      <input v-model="keyword" :placeholder="t('majors.keyword')" @keyup.enter="reload" />
      <button :disabled="loading" @click="reload">{{ loading ? t('common.searching') : t('common.search') }}</button>
    </section>

    <section v-if="majors.length" class="grid catalog-grid">
      <article v-for="item in majors" :key="item.id" class="card catalog-card">
        <img v-if="imageUrl(item.coverPath)" :src="imageUrl(item.coverPath)" :alt="item.name" />
        <h3>{{ item.name }}</h3>
        <p>{{ item.code }} / {{ item.durationOfStudy || t('common.pendingDuration') }}</p>
        <p>{{ t('majors.cutoff') }}: {{ item.cutOffScore || '-' }} / {{ t('majors.quota') }}: {{ item.enrollmentQuota ?? '-' }}</p>
        <router-link :to="`/majors/${item.id}`">{{ t('common.viewDetails') }}</router-link>
      </article>
    </section>
    <p v-else class="empty-text">{{ loading ? t('majors.loading') : t('majors.empty') }}</p>

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
import { fetchMajors, imageUrl, type Major } from '../api/portal'
import { useLanguageStore } from '../stores/language'

const language = useLanguageStore()
const t = language.t
const majors = ref<Major[]>([])
const keyword = ref('')
const page = ref(1)
const size = 9
const total = ref(0)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const result = await fetchMajors({ page: page.value, size, keyword: keyword.value || undefined })
    majors.value = result.items
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
