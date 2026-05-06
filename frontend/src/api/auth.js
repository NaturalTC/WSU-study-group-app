import api from './axios'

export const register = (email, password, name) =>
  api.post('/auth/register', { email, password, name })

export const login = async (email, password) => {
  const res = await api.post('/auth/login', { email, password })
  // Store the JWT immediately so the request interceptor picks it up on all future calls
  localStorage.setItem('token', res.data.token)
  return res.data
}

export const logout = () => {
  localStorage.removeItem('token')
  window.location.href = '/login'
}

export const resendVerification = (email) =>
  api.post('/auth/resend-verification', { email })

export const forgotPassword = (email) =>
  api.post('/auth/forgot-password', { email })

export const resetPassword = (token, newPassword) =>
  api.post('/auth/change-password', { token, newPassword })

export const updatePassword = (currentPassword, newPassword) =>
  api.post('/auth/update-password', { currentPassword, newPassword })
