import { defineStore } from 'pinia'
import http, { accessToken, refreshToken } from '../api/http'

export interface MePayload {
  accountId: number
  username: string
  role: string
  displayName?: string
  profileId?: number
  studentNo?: string
  studentName?: string
  college?: string
  contactNumber?: string
}

export const useAuthStore = defineStore('portal-auth', {
  state: () => ({
    me: null as MePayload | null,
    loading: false
  }),
  getters: {
    isAuthenticated: () => !!accessToken.get()
  },
  actions: {
    clearSession() {
      accessToken.clear()
      refreshToken.clear()
      this.me = null
    },
    async login(username: string, password: string) {
      this.loading = true
      try {
        this.clearSession()
        const response = await http.post('/auth/login', { username, password, role: 'STUDENT' })
        const payload = response.data.data
        accessToken.set(payload.accessToken)
        refreshToken.set(payload.refreshToken)
        this.me = payload.account
        await this.loadMe()
      } catch (error) {
        this.clearSession()
        throw error
      } finally {
        this.loading = false
      }
    },
    async loadMe() {
      if (!accessToken.get()) {
        this.me = null
        throw new Error('未登录')
      }
      const response = await http.get('/auth/me')
      const account = response.data.data
      if (account.role !== 'STUDENT') {
        this.clearSession()
        throw new Error('当前账号不是学生账号')
      }
      this.me = account
      return account as MePayload
    },
    async ensureSession() {
      if (!accessToken.get()) {
        this.me = null
        return false
      }
      try {
        await this.loadMe()
        return true
      } catch {
        this.clearSession()
        return false
      }
    },
    async logout() {
      this.clearSession()
    }
  }
})
