import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import AdminHomeView from '../views/AdminHomeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/', component: AdminHomeView }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') {
    if (await auth.ensureSession()) {
      return '/'
    }
    return true
  }
  if (!(await auth.ensureSession())) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
