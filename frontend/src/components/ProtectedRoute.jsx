import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children }) {
  const { profile, loading } = useAuth()
  const token = localStorage.getItem('token')

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-wsu-chalk">
        <div className="animate-spin w-8 h-8 border-4 border-blue-700 border-t-transparent rounded-full" />
      </div>
    )
  }

  if (!token) return <Navigate to="/login" replace />
  if (!profile) return <Navigate to="/setup-profile" replace />
  return children
}
