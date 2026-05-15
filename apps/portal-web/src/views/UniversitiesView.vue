<template>
  <div class="portal-page">
    <PortalNav />

    <section class="hero">
      <p class="eyebrow">{{ t('universities.eyebrow') }}</p>
      <h1>{{ t('universities.title') }}</h1>
      <p>{{ t('universities.subtitle') }}</p>
    </section>

    <section class="filter-bar">
      <input v-model="keyword" :placeholder="t('universities.keyword')" @keyup.enter="reload" />
      <select v-model="provinceId">
        <option value="">{{ t('universities.allProvinces') }}</option>
        <option v-for="province in provinces" :key="province.id" :value="province.id">{{ province.name }}</option>
      </select>
      <button :disabled="loading" @click="reload">{{ loading ? t('common.searching') : t('common.search') }}</button>
    </section>

    <section v-if="universities.length" class="grid catalog-grid">
      <article v-for="item in universities" :key="item.id" class="card catalog-card">
        <img v-if="imageUrl(item.imagePath)" :src="imageUrl(item.imagePath)" :alt="item.name" />
        <h3>{{ item.name }}</h3>
        <p>{{ item.institutionType || t('universities.defaultType') }} / {{ item.keyness || t('universities.defaultTier') }}</p>
        <p>{{ item.phone || t('common.noContact') }}</p>
        <router-link :to="`/universities/${item.id}`">{{ t('common.viewDetails') }}</router-link>
      </article>
    </section>
    <p v-else class="empty-text">{{ loading ? t('universities.loading') : t('universities.empty') }}</p>

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
import { fetchProvinces, fetchUniversities, imageUrl, type EntityId, type Province, type University } from '../api/portal'
import { useLanguageStore } from '../stores/language'

const language = useLanguageStore()
const t = language.t
const universities = ref<University[]>([])
const provinces = ref<Province[]>([])
const keyword = ref('')
const provinceId = ref<EntityId | ''>('')
const page = ref(1)
const size = 9
const total = ref(0)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const result = await fetchUniversities({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      provinceId: provinceId.value || undefined
    })
    universities.value = result.items
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

onMounted(async () => {
  provinces.value = await fetchProvinces()
  await load()
})
</script>
