import { createContext, useContext, useState, useEffect } from 'react'
import api from '../api/axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)

  // On mount, restore session from token
  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) { setLoading(false); return }

    api.get('/profiles')
      .then(res => setProfile(res.data))
      .catch(() => {
        // 404 = logged in but no profile yet — keep the token, just leave profile null
        // 401/403 = bad token — the axios interceptor already removes it
      })
      .finally(() => setLoading(false))
  }, [])

  // Returns the profile on success, null if no profile exists yet (404)
  const login = async (token) => {
    localStorage.setItem('token', token)
    try {
      const res = await api.get('/profiles')
      setProfile(res.data)
      return res.data
    } catch (err) {
      if (err.response?.status === 404) {
        return null // valid token, but profile not set up yet
      }
      throw err
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    setProfile(null)
    window.location.href = '/login'
  }

  return (
    <AuthContext.Provider value={{ profile, setProfile, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
