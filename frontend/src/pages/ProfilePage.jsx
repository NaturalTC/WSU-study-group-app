import { useState } from 'react'
import { Link } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'

// ─────────────────────────────────────────────────────────────────
// MOCK DATA — Backend Integration Point
//
// ENDPOINT:  GET /api/profile/{userId}
// TODO: Replace MOCK_USER with real API call using JWT token
// ─────────────────────────────────────────────────────────────────
const MOCK_USER = {
    firstName:   'Joe',
    lastName:    'Shmo',
    displayName: 'Joe S.',
    email:       'joeshmo@westfield.ma.edu',
    major:       'Computer Science',
    year:        'Junior',
}

const AVATAR_OPTIONS = [
    { bg: 'bg-blue-700'   },
    { bg: 'bg-red-500'    },
    { bg: 'bg-green-600'  },
    { bg: 'bg-purple-600' },
    { bg: 'bg-yellow-500' },
    { bg: 'bg-pink-500'   },
    { bg: 'bg-indigo-600' },
    { bg: 'bg-orange-500' },
    { bg: 'bg-teal-600'   },
    { bg: 'bg-gray-700'   },
]

function ProfilePage() {
    const user = MOCK_USER

    const [activePanel, setActivePanel]         = useState('profile')
    const [selectedAvatar, setSelectedAvatar]   = useState(0)
    const [showLogoutModal, setShowLogoutModal] = useState(false)

    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword:     '',
        confirmPassword: '',
    })
    const [passwordError,   setPasswordError]   = useState(null)
    const [passwordSuccess, setPasswordSuccess] = useState(false)

    const handlePasswordChange = (e) => {
        setPasswordData({ ...passwordData, [e.target.name]: e.target.value })
    }

    const handlePasswordSubmit = (e) => {
        e.preventDefault()
        setPasswordError(null)
        setPasswordSuccess(false)

        if (passwordData.newPassword !== passwordData.confirmPassword) {
            setPasswordError('New passwords do not match.')
            return
        }
        if (passwordData.newPassword.length < 8) {
            setPasswordError('Password must be at least 8 characters.')
            return
        }

        // TODO: Replace with real API call
        // ENDPOINT: PUT /api/profile/password
        // PAYLOAD: { currentPassword, newPassword }
        console.log('Password update payload (placeholder):', passwordData)
        setPasswordSuccess(true)
        setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' })
    }

    const handleLogout = () => {
        // TODO: Clear JWT token from localStorage and redirect to /login
        // localStorage.removeItem('token')
        // navigate('/login')
        console.log('Logout placeholder triggered')
        setShowLogoutModal(false)
        alert('Logout placeholder triggered!')
    }

    const currentAvatar = AVATAR_OPTIONS[selectedAvatar]

    // ── Nav items ──
    const NAV_ITEMS = [
        { key: 'profile',   label: 'My Profile'         },
        { key: 'customize', label: 'Customize Profile'   },
        { key: 'security',  label: 'Security Settings'   },
    ]

    return (
        <div className="flex flex-col min-h-screen">
            <Header />

            <main className="flex-1 bg-wsu-chalk px-6 pt-24 pb-12">
                <div className="max-w-5xl mx-auto">
                    <div className="grid grid-cols-4 gap-6">

                        {/* ── Left Sidebar ── */}
                        <div className="col-span-1">
                            <div className="card p-0 overflow-hidden rounded-x1">
                                <ul>
                                    {NAV_ITEMS.map((item, index) => (
                                        <li key={item.key}>
                                            <button
                                                onClick={() => setActivePanel(item.key)}
                                                className={`w-full px-4 py-3 text-sm font-medium text-left transition-colors
                          ${index !== NAV_ITEMS.length - 1 ? 'border-b border-gray-100' : ''}
                          ${activePanel === item.key
                                                    ? 'bg-blue-50 text-blue-700 font-semibold'
                                                    : 'text-wsu-navy hover:bg-blue-50 hover:text-blue-700'}`}
                                            >
                                                {item.label}
                                            </button>
                                        </li>
                                    ))}

                                    {/* Log Out */}
                                    <li>
                                        <button
                                            onClick={() => setShowLogoutModal(true)}
                                            className="w-full px-4 py-3 text-sm font-medium text-left border-t border-gray-200 text-red-500 hover:bg-red-50 hover:text-red-600 transition-colors"
                                        >
                                            Log Out
                                        </button>
                                    </li>
                                </ul>
                            </div>
                        </div>

                        {/* ── Right Panel ── */}
                        <div className="col-span-3">

                            {/* ── My Profile ── */}
                            {activePanel === 'profile' && (
                                <div className="card animate-fade-up">
                                    <div className="flex flex-col items-center mb-6">
                                        <div className={`w-28 h-28 rounded-full ${currentAvatar.bg} flex items-center justify-center shadow-md mb-4 transition-all duration-300`}>
                      <span className="text-white text-4xl font-bold font-display">
                        {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                      </span>
                                        </div>
                                        <h1 className="font-display text-3xl text-wsu-navy font-bold">{user.displayName}</h1>
                                        <p className="text-wsu-slate text-sm mt-1">{user.year} · {user.major}</p>
                                    </div>

                                    <hr className="border-gray-200 mb-6" />

                                    <div className="mb-6">
                                        <h2 className="text-xs font-semibold text-wsu-slate uppercase tracking-widest mb-3">
                                            Contact Info
                                        </h2>
                                        <div className="bg-gray-50 rounded-lg px-4 py-3">
                                            <p className="text-sm text-wsu-navy font-semibold">Email</p>
                                            <p className="text-sm text-wsu-slate">{user.email}</p>
                                        </div>
                                    </div>

                                    <div>
                                        <h2 className="text-xs font-semibold text-wsu-slate uppercase tracking-widest mb-3">
                                            Academic Info
                                        </h2>
                                        <div className="grid grid-cols-2 gap-3">
                                            <div className="bg-gray-50 rounded-lg px-4 py-3">
                                                <p className="text-sm text-wsu-navy font-semibold">Major</p>
                                                <p className="text-sm text-wsu-slate">{user.major}</p>
                                            </div>
                                            <div className="bg-gray-50 rounded-lg px-4 py-3">
                                                <p className="text-sm text-wsu-navy font-semibold">Year</p>
                                                <p className="text-sm text-wsu-slate">{user.year}</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* ── Customize Profile ── */}
                            {activePanel === 'customize' && (
                                <div className="card animate-fade-up">
                                    <h2 className="font-display text-2xl text-wsu-navy font-bold mb-1">
                                        Customize Profile
                                    </h2>
                                    <p className="text-sm text-wsu-slate mb-6">
                                        Choose an avatar color to represent you across the app
                                    </p>

                                    <hr className="border-gray-200 mb-6" />

                                    {/* Preview */}
                                    <div className="flex flex-col items-center mb-8">
                                        <div className={`w-24 h-24 rounded-full ${currentAvatar.bg} flex items-center justify-center shadow-md mb-3 transition-all duration-300`}>
                      <span className="text-white text-3xl font-bold font-display">
                        {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                      </span>
                                        </div>
                                        <p className="text-xs text-wsu-slate">Preview</p>
                                    </div>

                                    {/* Avatar Grid */}
                                    <h3 className="text-xs font-semibold text-wsu-slate uppercase tracking-widest mb-3">
                                        Choose Avatar Color
                                    </h3>
                                    <div className="grid grid-cols-5 gap-4 mb-8">
                                        {AVATAR_OPTIONS.map((avatar, index) => (
                                            <button
                                                key={index}
                                                onClick={() => setSelectedAvatar(index)}
                                                className={`w-14 h-14 rounded-full ${avatar.bg} flex items-center justify-center text-white text-sm font-bold transition-all duration-200
                          ${selectedAvatar === index
                                                    ? 'ring-4 ring-offset-2 ring-blue-700 scale-110'
                                                    : 'hover:scale-105 opacity-80 hover:opacity-100'}`}
                                            >
                                                {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                                            </button>
                                        ))}
                                    </div>

                                    <button
                                        onClick={() => setActivePanel('profile')}
                                        className="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md"
                                    >
                                        Save & View Profile
                                    </button>
                                </div>
                            )}

                            {/* ── Security Settings ── */}
                            {activePanel === 'security' && (
                                <div className="card animate-fade-up">
                                    <h2 className="font-display text-2xl text-wsu-navy font-bold mb-1">
                                        Security Settings
                                    </h2>
                                    <p className="text-sm text-wsu-slate mb-6">
                                        Update your password to keep your account secure
                                    </p>

                                    <hr className="border-gray-200 mb-6" />

                                    {passwordError && (
                                        <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-6">
                                            {passwordError}
                                        </div>
                                    )}

                                    {passwordSuccess && (
                                        <div className="bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg px-4 py-3 mb-6">
                                            Password updated successfully!
                                        </div>
                                    )}

                                    <form onSubmit={handlePasswordSubmit} className="space-y-5">
                                        <div>
                                            <label className="form-label" htmlFor="currentPassword">
                                                Current Password
                                            </label>
                                            <input
                                                id="currentPassword"
                                                name="currentPassword"
                                                type="password"
                                                required
                                                placeholder="••••••••"
                                                className="form-input"
                                                value={passwordData.currentPassword}
                                                onChange={handlePasswordChange}
                                            />
                                        </div>
                                        <div>
                                            <label className="form-label" htmlFor="newPassword">
                                                New Password
                                            </label>
                                            <input
                                                id="newPassword"
                                                name="newPassword"
                                                type="password"
                                                required
                                                placeholder="Min. 8 characters"
                                                className="form-input"
                                                value={passwordData.newPassword}
                                                onChange={handlePasswordChange}
                                            />
                                        </div>
                                        <div>
                                            <label className="form-label" htmlFor="confirmPassword">
                                                Confirm New Password
                                            </label>
                                            <input
                                                id="confirmPassword"
                                                name="confirmPassword"
                                                type="password"
                                                required
                                                placeholder="••••••••"
                                                className="form-input"
                                                value={passwordData.confirmPassword}
                                                onChange={handlePasswordChange}
                                            />
                                        </div>
                                        <button
                                            type="submit"
                                            className="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md"
                                        >
                                            Update Password
                                        </button>
                                    </form>
                                </div>
                            )}

                            {/* Back to home */}
                            <p className="text-center mt-6 text-sm text-wsu-slate">
                                <Link to="/" className="hover:text-wsu-crimson transition-colors">
                                    ← Back to home
                                </Link>
                            </p>
                        </div>
                    </div>
                </div>
            </main>

            {/* ── Logout Confirmation Modal ── */}
            {showLogoutModal && (
                <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl shadow-xl p-6 max-w-sm w-full mx-4 animate-fade-up">
                        <h2 className="font-display text-xl text-wsu-navy font-bold mb-2">Log Out</h2>
                        <p className="text-sm text-wsu-slate mb-6">
                            Are you sure you want to log out of your WSU StudyGroup account?
                        </p>
                        <div className="flex gap-3">
                            <button
                                onClick={() => setShowLogoutModal(false)}
                                className="flex-1 border-2 border-gray-200 text-wsu-navy font-semibold px-4 py-2 rounded-lg hover:bg-gray-50 transition-all duration-200 text-sm"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleLogout}
                                className="flex-1 bg-red-500 hover:bg-red-600 text-white font-semibold px-4 py-2 rounded-lg transition-all duration-200 text-sm"
                            >
                                Log Out
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <Footer />
        </div>
    )
}

export default ProfilePage