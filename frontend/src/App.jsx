import {Routes, Route} from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import StudyGroups from './pages/StudyGroups'
import GroupChat from './pages/GroupChat'
import ResetPassword from './pages/ResetPassword'
import ForgotPassword from './pages/ForgotPassword'
import ChangePassword from './pages/ChangePassword'

function App() {
  return (
    <Routes>
      <Route path="/"                    element={<Home />}           />
      <Route path="/login"               element={<Login />}          />
      <Route path="/register"            element={<Register />}       />
      <Route path="/study-groups"        element={<StudyGroups />}    />
      <Route path="/group-chat/:groupId" element={<GroupChat />}      />
      <Route path="/reset-password"      element={<ResetPassword />}  />
      <Route path="/forgot-password"     element={<ForgotPassword />} />
      <Route path="/change-password"     element={<ChangePassword />} />
    </Routes>
  )
}

export default App