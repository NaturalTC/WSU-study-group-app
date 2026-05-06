import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import Profile from './pages/Profile'
import SetupProfile from './pages/SetupProfile'
import StudyGroups from './pages/StudyGroups'
import GroupChat from './pages/GroupChat'
import GroupChatIndex from './pages/GroupChatIndex'
import ResetPassword from './pages/ResetPassword'
import ForgotPassword from './pages/ForgotPassword'
import ChangePassword from './pages/ChangePassword'
import VerifyPending from './pages/VerifyPending'
import VerifySuccess from './pages/VerifySuccess'
import VerifyError from './pages/VerifyError'
import Leaderboard from './pages/Leaderboard'
import Meetings from './pages/Meetings'
import Friends from './pages/Friends'
import CourseStudents from './pages/CourseStudents'

// Requires a token, but blocks users who already have a profile
// (prevents someone from re-running setup and overwriting their data)
function SetupRoute({ children }) {
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
  if (profile) return <Navigate to="/profile" replace />
  return children
}

function App() {
  return (
    <Routes>
      <Route path="/"                    element={<Home />}           />
      <Route path="/login"               element={<Login />}          />
      <Route path="/register"            element={<Register />}       />
      <Route path="/forgot-password"     element={<ForgotPassword />} />
      <Route path="/reset-password"      element={<ResetPassword />}  />
      <Route path="/verify-pending"      element={<VerifyPending />}  />
      <Route path="/verify-success"      element={<VerifySuccess />}  />
      <Route path="/verify-error"        element={<VerifyError />}    />

      <Route path="/setup-profile"       element={<SetupRoute><SetupProfile /></SetupRoute>} />

      <Route path="/profile"             element={<ProtectedRoute><Profile /></ProtectedRoute>}      />
      <Route path="/study-groups"        element={<ProtectedRoute><StudyGroups /></ProtectedRoute>}  />
      <Route path="/group-chat"           element={<ProtectedRoute><GroupChatIndex /></ProtectedRoute>} />
      <Route path="/group-chat/:groupId" element={<ProtectedRoute><GroupChat /></ProtectedRoute>}    />
      <Route path="/change-password"     element={<ProtectedRoute><ChangePassword /></ProtectedRoute>} />
      <Route path="/leaderboard"                element={<ProtectedRoute><Leaderboard /></ProtectedRoute>}     />
      <Route path="/meetings"                   element={<ProtectedRoute><Meetings /></ProtectedRoute>}       />
      <Route path="/friends"                    element={<ProtectedRoute><Friends /></ProtectedRoute>}        />
      <Route path="/courses/:courseId/students" element={<ProtectedRoute><CourseStudents /></ProtectedRoute>} />
    </Routes>
  )
}

export default App
