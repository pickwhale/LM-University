<template>
  <nav class="top-nav">
    <router-link to="/" class="brand">{{ t('brand.name') }}</router-link>
    <div>
      <router-link to="/universities">{{ t('nav.universities') }}</router-link>
      <router-link to="/majors">{{ t('nav.majors') }}</router-link>
      <router-link to="/news">{{ t('nav.news') }}</router-link>
      <router-link to="/pages/about-us">{{ t('nav.about') }}</router-link>
      <router-link to="/me">{{ t('nav.studentCenter') }}</router-link>
      <button v-if="showLogout" class="link-button" type="button" @click="$emit('logout')">
        {{ t('common.logout') }}
      </button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useLanguageStore } from '../stores/language'

defineProps<{
  showLogout?: boolean
}>()

defineEmits<{
  logout: []
}>()

const language = useLanguageStore()
const t = language.t
</script>
