<template>
  <section class="language-selector" :aria-label="language.t('language.label')">
    <div class="language-selector__options">
      <button
        v-for="option in language.options"
        :key="option.code"
        class="language-selector__option"
        :class="{ 'is-active': language.current === option.code }"
        type="button"
        @click="language.setLanguage(option.code)"
      >
        {{ compactLabel(option.code) }}
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { LanguageCode } from '../stores/language'
import { useLanguageStore } from '../stores/language'

const language = useLanguageStore()

const compactLabel = (code: LanguageCode) => {
  if (code === 'zh') return '中'
  return code.toUpperCase()
}
</script>

<style scoped>
.language-selector {
  position: fixed;
  right: 8px;
  top: 8px;
  z-index: 5000;
  display: inline-flex;
  padding: 2px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 999px;
  background: rgba(252, 248, 240, 0.94);
  color: #162234;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(10px);
  font-size: 0.62rem;
  font-weight: 800;
}

.language-selector__options {
  display: grid;
  grid-template-columns: repeat(3, 26px);
  gap: 2px;
  padding: 2px;
  border-radius: 999px;
  background: rgba(22, 34, 52, 0.08);
}

.language-selector__option {
  width: 26px;
  min-height: 20px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-weight: 800;
}

.language-selector__option.is-active {
  background: #162234;
  color: #fff8e8;
  box-shadow: 0 4px 8px rgba(22, 34, 52, 0.16);
}
</style>
