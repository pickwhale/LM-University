import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import UniversitiesView from '../views/UniversitiesView.vue'
import UniversityDetailView from '../views/UniversityDetailView.vue'
import MajorsView from '../views/MajorsView.vue'
import MajorDetailView from '../views/MajorDetailView.vue'
import NewsView from '../views/NewsView.vue'
import NewsDetailView from '../views/NewsDetailView.vue'
import SitePageView from '../views/SitePageView.vue'
import StudentCenterView from '../views/StudentCenterView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/login', component: LoginView },
    { path: '/universities', component: UniversitiesView },
    { path: '/universities/:id', component: UniversityDetailView },
    { path: '/majors', component: MajorsView },
    { path: '/majors/:id', component: MajorDetailView },
    { path: '/news', component: NewsView },
    { path: '/news/:id', component: NewsDetailView },
    { path: '/pages/:slug', component: SitePageView },
    { path: '/me', component: StudentCenterView }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') {
    if (await auth.ensureSession()) {
      return '/me'
    }
    return true
  }
  if (to.path !== '/me') {
    return true
  }
  if (!(await auth.ensureSession())) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
