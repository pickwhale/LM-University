<template>
  <div class="portal-login">
    <section class="portal-login__stage">
      <div class="portal-login__story">
        <p class="eyebrow">{{ t('login.eyebrow') }}</p>
        <h1>{{ t('login.title') }}</h1>
        <p class="lead">{{ t('login.subtitle') }}</p>

        <div class="story-band">
          <span>{{ t('login.band.admissions') }}</span>
          <span>{{ t('login.band.majors') }}</span>
          <span>{{ t('login.band.scholarships') }}</span>
          <span>{{ t('login.band.results') }}</span>
        </div>
      </div>

      <div class="portal-login__card">
        <p class="eyebrow">{{ t('login.studentAccess') }}</p>
        <h2>{{ t('login.formTitle') }}</h2>
        <p class="caption">{{ t('login.formCaption') }}</p>

        <label class="field">
          <span>{{ t('login.studentNo') }}</span>
          <input v-model="username" :placeholder="t('login.studentNoPlaceholder')" @keyup.enter="submit" />
        </label>

        <label class="field">
          <span>{{ t('login.password') }}</span>
          <input
            v-model="password"
            type="password"
            :placeholder="t('login.passwordPlaceholder')"
            @keyup.enter="submit"
          />
        </label>

        <button class="submit-button" :disabled="auth.loading" @click="submit">
          {{ auth.loading ? t('login.submitting') : t('login.submit') }}
        </button>

        <p v-if="error" class="error-text">{{ error }}</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useLanguageStore } from '../stores/language'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const language = useLanguageStore()
const t = language.t
const username = ref('')
const password = ref('')
const error = ref('')

const submit = async () => {
  error.value = ''
  try {
    await auth.login(username.value.trim(), password.value)
    await router.replace(String(route.query.redirect || '/me'))
  } catch (err: any) {
    error.value =
      err.response?.data?.detail ??
      err.response?.data?.message ??
      err.message ??
      t('login.error')
  }
}
</script>

<style scoped>
.portal-login {
  --cream: #fff9ef;
  --ink: #1f2935;
  --copy: #5b6877;
  --accent: #d96941;
  min-height: 100vh;
  padding: 28px;
  background:
    radial-gradient(circle at 18% 18%, rgba(230, 124, 73, 0.2), transparent 28%),
    radial-gradient(circle at 80% 26%, rgba(244, 199, 111, 0.2), transparent 24%),
    linear-gradient(140deg, #fff3d7 0%, #f8e1c5 42%, #f3d7cf 100%);
}

.portal-login__stage {
  max-width: 1160px;
  min-height: calc(100vh - 56px);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(340px, 430px);
  gap: 26px;
  align-items: center;
}

.portal-login__story {
  padding: 44px;
  border-radius: 36px;
  color: var(--ink);
  background:
    linear-gradient(165deg, rgba(255, 255, 255, 0.65), rgba(255, 248, 240, 0.78)),
    rgba(255, 255, 255, 0.55);
  box-shadow: 0 24px 60px rgba(122, 81, 44, 0.12);
}

.eyebrow {
  margin: 0 0 14px;
  color: var(--accent);
  font: 700 0.82rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.portal-login__story h1,
.portal-login__card h2 {
  margin: 0;
  color: #2e2016;
  font-family: Georgia, "Times New Roman", serif;
  line-height: 1.05;
}

.portal-login__story h1 {
  max-width: 11ch;
  font-size: clamp(2.8rem, 5vw, 4.8rem);
}

.lead,
.caption {
  font-family: "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.lead {
  max-width: 42ch;
  margin: 22px 0 28px;
  color: var(--copy);
  font-size: 1.05rem;
  line-height: 1.75;
}

.story-band {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.story-band span {
  padding: 12px 18px;
  border-radius: 999px;
  color: #5a3625;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(217, 105, 65, 0.16);
  font: 700 0.92rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.portal-login__card {
  padding: 32px 28px;
  border-radius: 30px;
  background: rgba(255, 253, 247, 0.95);
  box-shadow: 0 24px 60px rgba(122, 81, 44, 0.16);
}

.portal-login__card h2 {
  font-size: clamp(2rem, 3vw, 2.7rem);
}

.caption {
  margin: 12px 0 0;
  color: var(--copy);
  font-size: 0.98rem;
  line-height: 1.6;
}

.field {
  display: block;
  margin-bottom: 16px;
}

.field:first-of-type {
  margin-top: 24px;
}

.field span {
  display: block;
  margin-bottom: 8px;
  color: #624b3e;
  font: 700 0.82rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.field input {
  width: 100%;
  min-height: 54px;
  padding: 0 16px;
  border: 1px solid rgba(84, 61, 48, 0.12);
  border-radius: 16px;
  outline: none;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.95);
  color: var(--ink);
  font: 500 1rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.field input:focus {
  border-color: rgba(217, 105, 65, 0.42);
  box-shadow: 0 0 0 4px rgba(217, 105, 65, 0.12);
  transform: translateY(-1px);
}

.submit-button {
  width: 100%;
  margin-top: 8px;
  padding: 16px 18px;
  border: 0;
  border-radius: 18px;
  background: linear-gradient(135deg, #1f2a38, #34495e);
  color: var(--cream);
  cursor: pointer;
  font: 800 0.98rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 16px 28px rgba(31, 42, 56, 0.24);
}

.submit-button:disabled {
  opacity: 0.72;
  cursor: wait;
}

.error-text {
  margin: 16px 0 0;
  color: #b54139;
  font: 600 0.92rem/1.5 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

@media (max-width: 980px) {
  .portal-login__stage {
    grid-template-columns: 1fr;
  }

  .portal-login__story h1 {
    max-width: none;
  }
}

@media (max-width: 640px) {
  .portal-login {
    padding: 18px;
  }

  .portal-login__story,
  .portal-login__card {
    padding: 24px 20px;
    border-radius: 24px;
  }
}
</style>
