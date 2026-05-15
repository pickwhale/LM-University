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
  top: 8px;
  right: 8px;
  z-index: 50;
  display: inline-flex;
  padding: 2px;
  border: 1px solid rgba(23, 33, 47, 0.1);
  border-radius: 999px;
  background: rgba(255, 253, 247, 0.9);
  color: #17212f;
  box-shadow: 0 6px 14px rgba(23, 33, 47, 0.1);
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
  background: rgba(23, 33, 47, 0.07);
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
  background: #17212f;
  color: #fff8e8;
  box-shadow: 0 4px 8px rgba(23, 33, 47, 0.14);
}

@media (max-width: 620px) {
  .language-selector {
    right: 8px;
    top: auto;
    bottom: 8px;
  }
}
</style>
