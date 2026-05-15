import axios from 'axios'

const ACCESS_TOKEN_KEY = 'university.admin.accessToken'
const REFRESH_TOKEN_KEY = 'university.admin.refreshToken'

const baseURL = import.meta.env.VITE_API_BASE_URL

export const accessToken = {
  get: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  set: (value: string) => localStorage.setItem(ACCESS_TOKEN_KEY, value),
  clear: () => localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export const refreshToken = {
  get: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  set: (value: string) => localStorage.setItem(REFRESH_TOKEN_KEY, value),
  clear: () => localStorage.removeItem(REFRESH_TOKEN_KEY)
}

const http = axios.create({
  baseURL,
  timeout: 15000
})

const isAuthEndpoint = (url?: string) => {
  if (!url) {
    return false
  }
  return url.includes('/auth/login') || url.includes('/auth/refresh')
}

http.interceptors.request.use((config) => {
  const token = accessToken.get()
  if (token && !isAuthEndpoint(config.url)) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise: Promise<string | null> | null = null

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (
      error.response?.status === 401 &&
      !originalRequest?._retry &&
      !isAuthEndpoint(originalRequest?.url) &&
      refreshToken.get()
    ) {
      originalRequest._retry = true
      refreshPromise ??= axios
        .post(`${baseURL}/auth/refresh`, { refreshToken: refreshToken.get() })
        .then((res) => {
          const payload = res.data.data
          accessToken.set(payload.accessToken)
          refreshToken.set(payload.refreshToken)
          return payload.accessToken as string
        })
        .catch(() => {
          accessToken.clear()
          refreshToken.clear()
          return null
        })
        .finally(() => {
          refreshPromise = null
        })

      const token = await refreshPromise
      if (token) {
        originalRequest.headers.Authorization = `Bearer ${token}`
        return http(originalRequest)
      }
    }
    return Promise.reject(error)
  }
)

export default http
