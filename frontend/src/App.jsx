import {Routes, Route} from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import StudyGroups from './pages/StudyGroups'
import GroupChat from './pages/GroupChat'
import Profile from './pages/Profile'

function App() {
    return (
        <Routes>
            <Route path="/" element={<Home/>}/>
            <Route path="/login" element={<Login/>}/>
            <Route path="/register" element={<Register/>}/>
            <Route path="/study-groups" element={<StudyGroups/>}/>
            <Route path="/group-chat/:groupId" element={<GroupChat/>}/>
            <Route path="/profile" element={<Profile/>}/>
        </Routes>
    )
}

export default App