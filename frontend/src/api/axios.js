import axios from 'axios'

// Single axios instance used across the entire app.
// Base URL is set once here — no more hardcoding http://localhost:8080 in every file.
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor — runs before every request leaves the browser.
// Reads the JWT from localStorage and attaches it automatically.
// This means no file has to manually write Authorization: Bearer <token> ever again.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const PUBLIC_PATHS = ['/', '/login', '/register', '/verify-pending', '/verify-success', '/verify-error', '/forgot-password', '/reset-password']

// Response interceptor — runs on every response before your .then() sees it.
// If the server returns 401/403 on a protected page, clear the token and redirect to login.
// On public pages (verify, login, register, etc.) just clear the token — don't redirect,
// because the user may have a stale token and is intentionally on a public flow.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      const isPublicPage = PUBLIC_PATHS.includes(window.location.pathname)
      if (!isPublicPage) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api
