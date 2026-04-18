import { useState } from 'react'
import { Link } from 'react-router-dom'

// ─────────────────────────────────────────────────────────────────
// MOCK DATA — Backend Integration Point
//
// ENDPOINT: GET /api/profile/{userId}
// TODO: Replace MOCK_USER with real user data passed down from an
//       auth context once authentication is implemented.
// ─────────────────────────────────────────────────────────────────
const MOCK_USER = {
    firstName:   'Joe',
    lastName:    'Shmo',
    displayName: 'Joe S.',
    major:       'Computer Science',
    year:        'Junior',
    avatarBg:    'bg-blue-700',
}

function AppHeader() {
    const user = MOCK_USER

    const [sidebarOpen, setSidebarOpen] = useState(false)

    const initials = `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`

    return (
        <>
            {/* ── Top Bar ─────────────────────────────────────────── */}
            <header className="fixed top-0 left-0 right-0 z-50 bg-white border-b border-gray-100 shadow-sm">
                <div className="px-6 py-4 flex items-center justify-between">

                    {/* Logo */}
                    <div className="flex items-center gap-3">
                        <div className="w-9 h-9 bg-blue-700 rounded-lg flex items-center justify-center shadow">
                            <span className="text-white font-display text-lg font-bold">W</span>
                        </div>
                        <span className="font-display text-xl text-wsu-navy hidden sm:block">
                            WSU StudyGroup
                        </span>
                    </div>

                    {/* Hamburger + Avatar */}
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => setSidebarOpen(true)}
                            className="flex flex-col gap-1.5 p-2 rounded-lg hover:bg-wsu-mist transition-colors"
                            aria-label="Open menu"
                        >
                            <span className="block w-6 h-0.5 bg-wsu-navy" />
                            <span className="block w-6 h-0.5 bg-wsu-navy" />
                            <span className="block w-6 h-0.5 bg-wsu-navy" />
                        </button>
                        <div className={`w-10 h-10 rounded-full ${user.avatarBg} flex items-center justify-center shadow`}>
                            <span className="text-white text-sm font-bold font-display">{initials}</span>
                        </div>
                    </div>
                </div>
            </header>

            {/* ── Sidebar Overlay ─────────────────────────────────── */}
            {sidebarOpen && (
                <div className="fixed inset-0 z-50 flex">

                    {/* Backdrop */}
                    <div
                        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
                        onClick={() => setSidebarOpen(false)}
                    />

                    {/* Panel */}
                    <div className="relative ml-auto mr-3 my-3 w-72 h-[calc(100vh-1.5rem)] bg-white rounded-2xl shadow-2xl flex flex-col animate-fade-in overflow-hidden">

                        {/* Panel Header */}
                        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
                            <button
                                onClick={() => setSidebarOpen(false)}
                                className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-wsu-mist transition-colors text-wsu-slate text-xl leading-none"
                            >
                                ×
                            </button>
                            <div className="flex items-center gap-3">
                                <p className="text-xl font-display font-semibold text-wsu-navy">{user.displayName}</p>
                                <div className={`w-10 h-10 rounded-full ${user.avatarBg} flex items-center justify-center flex-shrink-0`}>
                                    <span className="text-white text-sm font-bold font-display">{initials}</span>
                                </div>
                            </div>
                        </div>

                        {/* Nav Items */}
                        <nav className="flex-1 px-3 py-4">
                            <p className="text-xs font-display font-semibold text-blue-700 uppercase tracking-widest px-3 mb-3">
                                Menu
                            </p>
                            <ul className="space-y-1">
                                <li>
                                    <Link
                                        to="/profile"
                                        onClick={() => setSidebarOpen(false)}
                                        className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-wsu-navy hover:bg-wsu-mist hover:text-blue-700 transition-all duration-200 text-sm font-medium"
                                    >
                                        Profile
                                    </Link>
                                </li>
                                <li>
                                    <Link
                                        to="/study-groups"
                                        onClick={() => setSidebarOpen(false)}
                                        className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-wsu-navy hover:bg-wsu-mist hover:text-blue-700 transition-all duration-200 text-sm font-medium"
                                    >
                                        Study Groups
                                    </Link>
                                </li>
                            </ul>
                        </nav>

                        {/* Log Out */}
                        <div className="px-3 py-4 border-t border-gray-100">
                            <button
                                onClick={() => {
                                    // TODO: Clear JWT token from localStorage and redirect to /login
                                    // localStorage.removeItem('token')
                                    // navigate('/login')
                                    console.log('Logout placeholder triggered')
                                    setSidebarOpen(false)
                                }}
                                className="flex items-center gap-3 w-full px-3 py-2.5 rounded-xl text-red-500 hover:bg-red-50 hover:text-red-600 transition-all duration-200 text-sm font-medium"
                            >
                                Log Out
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    )
}

export default AppHeader
