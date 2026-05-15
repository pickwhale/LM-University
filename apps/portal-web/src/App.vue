<template>
  <LanguageSelector />
  <AiChatWidget />
  <router-view />
</template>

<script setup lang="ts">
import AiChatWidget from './components/AiChatWidget.vue'
import LanguageSelector from './components/LanguageSelector.vue'
</script>
