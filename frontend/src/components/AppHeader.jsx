import { useState, useRef, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

function AppHeader() {
    const { profile, logout } = useAuth()
    const location = useLocation()
    const navigate = useNavigate()
    const [mobileOpen, setMobileOpen]   = useState(false)
    const [avatarOpen, setAvatarOpen]   = useState(false)
    const avatarRef = useRef(null)

    const initials = (() => {
        const parts = (profile?.name ?? '').trim().split(' ')
        return parts.length >= 2
            ? `${parts[0].charAt(0)}${parts[parts.length - 1].charAt(0)}`
            : (profile?.name?.charAt(0)?.toUpperCase() ?? '?')
    })()

    const navLinks = [
        { label: 'Study Groups', to: '/study-groups' },
        { label: 'Group Chat',   to: '/group-chat'   },
    ]

    const isActive = (to) =>
        to === '/group-chat'
            ? location.pathname.startsWith('/group-chat')
            : location.pathname === to

    // Close avatar dropdown when clicking outside
    useEffect(() => {
        const handler = (e) => {
            if (avatarRef.current && !avatarRef.current.contains(e.target)) {
                setAvatarOpen(false)
            }
        }
        document.addEventListener('mousedown', handler)
        return () => document.removeEventListener('mousedown', handler)
    }, [])

    return (
        <header className="fixed top-0 left-0 right-0 z-50 bg-white border-b border-gray-100 shadow-sm">
            <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between gap-6">

                {/* Logo */}
                <Link to="/" className="flex items-center gap-2.5 flex-shrink-0">
                    <div className="w-8 h-8 bg-blue-700 rounded-lg flex items-center justify-center shadow">
                        <span className="text-white font-display text-base font-bold">W</span>
                    </div>
                    <span className="font-display text-lg text-wsu-navy hidden sm:block">WSU StudyGroup</span>
                </Link>

                {/* Desktop nav */}
                <nav className="hidden md:flex items-center gap-1">
                    {navLinks.map(link => (
                        <Link
                            key={link.to}
                            to={link.to}
                            className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-200 ${
                                isActive(link.to)
                                    ? 'bg-blue-700 text-white shadow-sm'
                                    : 'text-wsu-slate hover:text-wsu-navy hover:bg-wsu-mist'
                            }`}
                        >
                            {link.label}
                        </Link>
                    ))}
                </nav>

                {/* Right side */}
                <div className="flex items-center gap-3">

                    {/* Mobile hamburger */}
                    <button
                        onClick={() => setMobileOpen(!mobileOpen)}
                        className="md:hidden p-2 rounded-lg hover:bg-wsu-mist transition-colors"
                        aria-label="Toggle menu"
                    >
                        <svg className="w-5 h-5 text-wsu-navy" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                d={mobileOpen ? 'M6 18L18 6M6 6l12 12' : 'M4 6h16M4 12h16M4 18h16'} />
                        </svg>
                    </button>

                    {/* Avatar + dropdown */}
                    <div className="relative" ref={avatarRef}>
                        <button
                            onClick={() => setAvatarOpen(prev => !prev)}
                            className="w-9 h-9 rounded-full bg-blue-700 flex items-center justify-center shadow hover:bg-blue-800 transition-colors flex-shrink-0"
                            aria-label="Account menu"
                        >
                            <span className="text-white text-xs font-bold font-display">{initials}</span>
                        </button>

                        {avatarOpen && (
                            <div className="absolute right-0 top-11 w-48 bg-white rounded-xl shadow-lg border border-gray-100 py-1.5 animate-fade-in">
                                <div className="px-4 py-2 border-b border-gray-100 mb-1">
                                    <p className="text-xs font-semibold text-wsu-navy truncate">{profile?.name}</p>
                                    <p className="text-xs text-wsu-slate truncate">{profile?.major}</p>
                                </div>
                                <button
                                    onClick={() => { setAvatarOpen(false); navigate('/profile') }}
                                    className="w-full text-left px-4 py-2 text-sm text-wsu-navy hover:bg-wsu-mist transition-colors"
                                >
                                    Profile
                                </button>
                                <button
                                    onClick={() => { setAvatarOpen(false); logout() }}
                                    className="w-full text-left px-4 py-2 text-sm text-red-500 hover:bg-red-50 transition-colors"
                                >
                                    Log Out
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Mobile dropdown */}
            {mobileOpen && (
                <div className="md:hidden border-t border-gray-100 bg-white px-4 py-3 space-y-1">
                    {navLinks.map(link => (
                        <Link
                            key={link.to}
                            to={link.to}
                            onClick={() => setMobileOpen(false)}
                            className={`flex items-center px-4 py-2.5 rounded-xl text-sm font-semibold transition-all duration-200 ${
                                isActive(link.to)
                                    ? 'bg-blue-700 text-white'
                                    : 'text-wsu-navy hover:bg-wsu-mist'
                            }`}
                        >
                            {link.label}
                        </Link>
                    ))}
                    <div className="pt-2 border-t border-gray-100 mt-2">
                        <button
                            onClick={() => { setMobileOpen(false); logout() }}
                            className="w-full text-left px-4 py-2.5 rounded-xl text-sm font-semibold text-red-500 hover:bg-red-50 transition-colors"
                        >
                            Log Out
                        </button>
                    </div>
                </div>
            )}
        </header>
    )
}

export default AppHeader
