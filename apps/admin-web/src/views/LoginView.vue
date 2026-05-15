<template>
  <div class="admin-login">
    <section class="admin-login__stage">
      <aside class="admin-login__brand">
        <p class="eyebrow">{{ t('admin.login.eyebrow') }}</p>
        <h1>{{ t('admin.login.title') }}</h1>
        <p class="lead">{{ t('admin.login.subtitle') }}</p>
        <div class="metrics">
          <article>
            <strong>01</strong>
            <span>{{ t('admin.login.metricJwt') }}</span>
          </article>
          <article>
            <strong>02</strong>
            <span>{{ t('admin.login.metricBackend') }}</span>
          </article>
          <article>
            <strong>03</strong>
            <span>{{ t('admin.login.metricWorkspace') }}</span>
          </article>
        </div>
      </aside>

      <div class="admin-login__card">
        <div class="card-top">
          <p class="eyebrow">{{ t('admin.login.access') }}</p>
          <h2>{{ t('admin.login.formTitle') }}</h2>
          <p>{{ t('admin.login.formCaption') }}</p>
        </div>

        <el-form class="login-form" @submit.prevent="submit">
          <el-form-item :label="t('admin.login.username')">
            <el-input
              v-model="form.username"
              :placeholder="t('admin.login.usernamePlaceholder')"
              size="large"
              @keyup.enter="submit"
            />
          </el-form-item>
          <el-form-item :label="t('admin.login.password')">
            <el-input
              v-model="form.password"
              show-password
              type="password"
              :placeholder="t('admin.login.passwordPlaceholder')"
              size="large"
              @keyup.enter="submit"
            />
          </el-form-item>

          <button class="submit-button" type="button" :disabled="auth.loading" @click="submit">
            <span>{{ auth.loading ? t('admin.login.submitting') : t('admin.login.submit') }}</span>
          </button>
        </el-form>

        <p v-if="error" class="error-text">{{ error }}</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useLanguageStore } from '../stores/language'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const language = useLanguageStore()
const t = language.t
const error = ref('')
const form = reactive({
  username: '',
  password: ''
})

const submit = async () => {
  error.value = ''
  try {
    await auth.login(form.username.trim(), form.password)
    await router.replace(String(route.query.redirect || '/'))
  } catch (err: any) {
    error.value =
      err.response?.data?.detail ??
      err.response?.data?.message ??
      err.message ??
      t('admin.login.error')
  }
}
</script>

<style scoped>
.admin-login {
  --bg-panel: rgba(10, 20, 37, 0.78);
  --line: rgba(255, 255, 255, 0.14);
  --gold: #f3b63a;
  --gold-soft: #ffd98a;
  --cream: #f5efe4;
  --danger: #c83f35;
  min-height: 100vh;
  padding: 32px;
  color: var(--cream);
  background:
    radial-gradient(circle at 15% 20%, rgba(243, 182, 58, 0.25), transparent 28%),
    radial-gradient(circle at 82% 18%, rgba(82, 164, 255, 0.2), transparent 24%),
    linear-gradient(135deg, #07101d 0%, #0d1728 45%, #111f34 100%);
}

.admin-login__stage {
  max-width: 1180px;
  min-height: calc(100vh - 64px);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(360px, 440px);
  gap: 28px;
  align-items: stretch;
}

.admin-login__brand,
.admin-login__card {
  border: 1px solid var(--line);
  backdrop-filter: blur(18px);
  box-shadow: 0 30px 70px rgba(0, 0, 0, 0.28);
}

.admin-login__brand {
  padding: 44px;
  border-radius: 34px;
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.03), rgba(255, 255, 255, 0.01)),
    var(--bg-panel);
}

.eyebrow {
  margin: 0 0 16px;
  color: var(--gold-soft);
  font: 600 0.82rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.admin-login__brand h1,
.card-top h2 {
  margin: 0;
  font-family: Georgia, "Times New Roman", serif;
  line-height: 1.04;
}

.admin-login__brand h1 {
  max-width: 10ch;
  font-size: clamp(2.8rem, 5vw, 5rem);
}

.lead {
  max-width: 44ch;
  margin: 22px 0 34px;
  color: rgba(245, 239, 228, 0.78);
  font: 400 1.08rem/1.7 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.metrics article {
  padding: 18px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.05);
}

.metrics strong {
  display: block;
  margin-bottom: 10px;
  color: var(--gold);
  font: 700 1.8rem/1 Georgia, "Times New Roman", serif;
}

.metrics span {
  color: rgba(245, 239, 228, 0.72);
  font: 500 0.92rem/1.5 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.admin-login__card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 34px 30px;
  border-radius: 30px;
  background: rgba(252, 248, 240, 0.96);
  color: #162234;
}

.card-top p:last-child {
  margin: 12px 0 0;
  color: #5f6778;
  font: 500 0.97rem/1.6 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.card-top h2 {
  font-size: clamp(2rem, 3vw, 2.6rem);
}

.login-form {
  margin-top: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-form-item__label) {
  color: #495366;
  font: 700 0.86rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 50px;
  border-radius: 16px;
  box-shadow: 0 0 0 1px rgba(17, 29, 46, 0.08) inset;
}

.submit-button {
  width: 100%;
  margin-top: 8px;
  padding: 16px 18px;
  border: 0;
  border-radius: 18px;
  background: linear-gradient(135deg, #f3b63a, #ff8f44);
  color: #1b2130;
  cursor: pointer;
  font: 800 1rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 18px 28px rgba(243, 182, 58, 0.28);
}

.submit-button:disabled {
  opacity: 0.68;
  cursor: wait;
}

.error-text {
  margin: 16px 0 0;
  color: var(--danger);
  font: 600 0.92rem/1.5 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

@media (max-width: 980px) {
  .admin-login__stage {
    grid-template-columns: 1fr;
  }

  .admin-login__brand h1 {
    max-width: none;
  }

  .metrics {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .admin-login {
    padding: 18px;
  }

  .admin-login__brand,
  .admin-login__card {
    padding: 24px 20px;
    border-radius: 24px;
  }
}
</style>
