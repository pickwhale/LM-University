<template>
  <div class="portal-page">
    <PortalNav />

    <section v-if="loading" class="state-panel">{{ t('page.loading') }}</section>
    <article v-else-if="page" class="article-page">
      <div v-if="pageImages.length" class="page-images">
        <img v-for="path in pageImages" :key="path" :src="imageUrl(path)" :alt="page.title" />
      </div>
      <p class="eyebrow">{{ page.slug }}</p>
      <h1>{{ page.title }}</h1>
      <p class="lead">{{ page.subtitle }}</p>
      <div class="rich-content">{{ page.content }}</div>
    </article>
    <section v-else class="state-panel">{{ t('page.notFound') }}</section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PortalNav from '../components/PortalNav.vue'
import { fetchSitePage, imagePaths, imageUrl, type SitePage } from '../api/portal'
import { useLanguageStore } from '../stores/language'

const route = useRoute()
const language = useLanguageStore()
const t = language.t
const page = ref<SitePage | null>(null)
const loading = ref(false)
const pageImages = computed(() => page.value ? [
  ...imagePaths(page.value.picture1Path),
  ...imagePaths(page.value.picture2Path),
  ...imagePaths(page.value.picture3Path)
] : [])

const load = async () => {
  loading.value = true
  try {
    page.value = await fetchSitePage(String(route.params.slug))
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => route.params.slug, load)
</script>
