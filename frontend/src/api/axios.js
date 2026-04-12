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

// Response interceptor — runs on every response before your .then() sees it.
// If the server returns 401 (token expired or invalid), log the user out automatically.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    // Re-throw so the calling code can still catch and show the error message
    return Promise.reject(error)
  }
)

export default api
