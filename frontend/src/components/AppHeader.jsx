import { useState, useRef, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'
import { useEvents } from '../context/EventsContext'
import { useToast } from '../context/ToastContext'
import OwlLogo from './OwlLogo'

function SunIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="4" strokeWidth={2} />
      <path strokeLinecap="round" strokeWidth={2}
        d="M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
    </svg>
  )
}

function MoonIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
        d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
    </svg>
  )
}

function formatEventDate(isoStr) {
  const d    = new Date(isoStr)
  const now  = new Date(); now.setHours(0, 0, 0, 0)
  const tom  = new Date(now); tom.setDate(tom.getDate() + 1)
  const time = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  if (d.toDateString() === now.toDateString())  return `Today · ${time}`
  if (d.toDateString() === tom.toDateString())  return `Tomorrow · ${time}`
  return d.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' }) + ` · ${time}`
}

function AppHeader() {
    const { profile, logout }          = useAuth()
    const { theme, toggleTheme }       = useTheme()
    const { getUpcoming, removeEvent } = useEvents()
    const { addToast }                 = useToast()

    const handleThemeToggle = () => {
        toggleTheme()
        addToast({
            title: theme === 'light' ? 'Dark mode enabled' : 'Light mode enabled',
            description: theme === 'light' ? 'Easy on the eyes.' : 'Welcome back to the light.',
            type: 'system',
            duration: 2500,
        })
    }
    const location  = useLocation()
    const navigate  = useNavigate()

    const [mobileOpen, setMobileOpen] = useState(false)
    const [avatarOpen, setAvatarOpen] = useState(false)
    const [bellOpen,   setBellOpen]   = useState(false)

    const avatarRef = useRef(null)
    const bellRef   = useRef(null)

    const upcoming = getUpcoming()

    const initials = (() => {
        const parts = (profile?.name ?? '').trim().split(' ')
        return parts.length >= 2
            ? `${parts[0].charAt(0)}${parts[parts.length - 1].charAt(0)}`
            : (profile?.name?.charAt(0)?.toUpperCase() ?? '?')
    })()

    const navLinks = [
        { label: 'Study Groups', to: '/study-groups' },
        { label: 'Group Chats',  to: '/group-chat'   },
        { label: 'Leaderboard',  to: '/leaderboard'  },
    ]

    const isActive = (to) => {
        if (to === '/group-chat') return location.pathname.startsWith('/group-chat')
        return location.pathname === to
    }

    useEffect(() => {
        const handler = (e) => {
            if (avatarRef.current && !avatarRef.current.contains(e.target)) setAvatarOpen(false)
            if (bellRef.current   && !bellRef.current.contains(e.target))   setBellOpen(false)
        }
        document.addEventListener('mousedown', handler)
        return () => document.removeEventListener('mousedown', handler)
    }, [])

    return (
        <header className="fixed top-0 left-0 right-0 z-50 bg-white dark:bg-gray-900 border-b border-gray-100 dark:border-gray-800 shadow-sm transition-colors duration-300">
            <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between gap-6">

                {/* Logo */}
                <Link to="/" className="flex items-center gap-2.5 flex-shrink-0">
                    <div className="w-9 h-9 bg-wsu-navy rounded-xl flex items-center justify-center shadow">
                        <OwlLogo size={26} />
                    </div>
                    <span className="font-display text-lg text-wsu-navy dark:text-white hidden sm:block">StudyNest</span>
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
                                    : 'text-wsu-slate dark:text-gray-300 hover:text-wsu-navy dark:hover:text-white hover:bg-wsu-mist dark:hover:bg-gray-800'
                            }`}
                        >
                            {link.label}
                            {link.to === '/leaderboard' && <span className="ml-1.5 text-xs">🏆</span>}
                        </Link>
                    ))}
                </nav>

                {/* Right side */}
                <div className="flex items-center gap-2">

                    {/* Theme toggle */}
                    <button
                        onClick={handleThemeToggle}
                        className="p-2 rounded-lg text-wsu-slate dark:text-gray-300 hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                        aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
                    >
                        {theme === 'dark' ? <SunIcon /> : <MoonIcon />}
                    </button>

                    {/* Bell / Reminders */}
                    <div className="relative" ref={bellRef}>
                        <button
                            onClick={() => { setBellOpen(p => !p); setAvatarOpen(false) }}
                            className="relative p-2 rounded-lg text-wsu-slate dark:text-gray-300 hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                            aria-label="Upcoming events"
                        >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                    d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                            </svg>
                            {upcoming.length > 0 && (
                                <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
                                    {upcoming.length > 9 ? '9+' : upcoming.length}
                                </span>
                            )}
                        </button>

                        {bellOpen && (
                            <div className="absolute right-0 top-11 w-80 bg-white dark:bg-gray-900 rounded-xl shadow-lg border border-gray-100 dark:border-gray-700 overflow-hidden animate-fade-in">
                                <div className="px-4 py-3 border-b border-gray-100 dark:border-gray-700 flex items-center justify-between">
                                    <p className="text-sm font-semibold text-wsu-navy dark:text-white">Upcoming Events</p>
                                    {upcoming.length > 0 && (
                                        <span className="text-xs text-wsu-slate dark:text-gray-400">{upcoming.length} scheduled</span>
                                    )}
                                </div>

                                {upcoming.length === 0 ? (
                                    <div className="px-4 py-8 text-center">
                                        <div className="text-3xl mb-2">📅</div>
                                        <p className="text-sm text-wsu-slate dark:text-gray-400">No upcoming events.</p>
                                        <p className="text-xs text-wsu-slate dark:text-gray-500 mt-1">Open a group chat to schedule one.</p>
                                    </div>
                                ) : (
                                    <ul className="max-h-72 overflow-y-auto divide-y divide-gray-50 dark:divide-gray-800">
                                        {upcoming.map(ev => (
                                            <li key={ev.id} className="flex items-start gap-3 px-4 py-3 hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors">
                                                <div className="w-8 h-8 bg-blue-50 dark:bg-blue-900/30 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5">
                                                    <span className="text-sm">📅</span>
                                                </div>
                                                <div className="flex-1 min-w-0">
                                                    <p className="text-sm font-semibold text-wsu-navy dark:text-white truncate">{ev.title}</p>
                                                    <p className="text-xs text-blue-600 dark:text-blue-400 font-medium">{formatEventDate(ev.eventDate)}</p>
                                                    <p className="text-xs text-wsu-slate dark:text-gray-400 truncate">{ev.groupName}</p>
                                                    {ev.notes && (
                                                        <p className="text-xs text-wsu-slate dark:text-gray-500 mt-0.5 line-clamp-1">{ev.notes}</p>
                                                    )}
                                                </div>
                                                <button
                                                    onClick={() => removeEvent(ev.groupId, ev.id)}
                                                    className="text-gray-300 dark:text-gray-600 hover:text-red-400 dark:hover:text-red-400 text-lg leading-none flex-shrink-0 transition-colors"
                                                    title="Remove reminder"
                                                >
                                                    ×
                                                </button>
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Mobile hamburger */}
                    <button
                        onClick={() => setMobileOpen(!mobileOpen)}
                        className="md:hidden p-2 rounded-lg hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                        aria-label="Toggle menu"
                    >
                        <svg className="w-5 h-5 text-wsu-navy dark:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                d={mobileOpen ? 'M6 18L18 6M6 6l12 12' : 'M4 6h16M4 12h16M4 18h16'} />
                        </svg>
                    </button>

                    {/* Avatar + dropdown */}
                    <div className="relative" ref={avatarRef}>
                        <button
                            onClick={() => { setAvatarOpen(prev => !prev); setBellOpen(false) }}
                            className="w-9 h-9 rounded-full bg-blue-700 flex items-center justify-center shadow hover:bg-blue-800 transition-colors flex-shrink-0"
                            aria-label="Account menu"
                        >
                            <span className="text-white text-xs font-bold font-display">{initials}</span>
                        </button>

                        {avatarOpen && (
                            <div className="absolute right-0 top-11 w-52 bg-white dark:bg-gray-900 rounded-xl shadow-lg border border-gray-100 dark:border-gray-700 py-1.5 animate-fade-in">
                                <div className="px-4 py-2 border-b border-gray-100 dark:border-gray-700 mb-1">
                                    <p className="text-xs font-semibold text-wsu-navy dark:text-white truncate">{profile?.name}</p>
                                    <p className="text-xs text-wsu-slate dark:text-gray-400 truncate">{profile?.major}</p>
                                    {(profile?.points ?? 0) > 0 && (
                                        <p className="text-xs text-blue-600 dark:text-blue-400 font-semibold mt-0.5">
                                            🏆 {profile.points} pts
                                        </p>
                                    )}
                                </div>
                                <button
                                    onClick={() => { setAvatarOpen(false); navigate('/profile') }}
                                    className="w-full text-left px-4 py-2 text-sm text-wsu-navy dark:text-gray-200 hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                                >
                                    Profile
                                </button>
                                <button
                                    onClick={() => { setAvatarOpen(false); navigate('/leaderboard') }}
                                    className="w-full text-left px-4 py-2 text-sm text-wsu-navy dark:text-gray-200 hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                                >
                                    Leaderboard 🏆
                                </button>
                                <div className="border-t border-gray-100 dark:border-gray-700 my-1" />
                                <button
                                    onClick={() => { setAvatarOpen(false); logout() }}
                                    className="w-full text-left px-4 py-2 text-sm text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
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
                <div className="md:hidden border-t border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900 px-4 py-3 space-y-1 transition-colors duration-300">
                    {navLinks.map(link => (
                        <Link
                            key={link.to}
                            to={link.to}
                            onClick={() => setMobileOpen(false)}
                            className={`flex items-center px-4 py-2.5 rounded-xl text-sm font-semibold transition-all duration-200 ${
                                isActive(link.to)
                                    ? 'bg-blue-700 text-white'
                                    : 'text-wsu-navy dark:text-gray-200 hover:bg-wsu-mist dark:hover:bg-gray-800'
                            }`}
                        >
                            {link.label}
                            {link.to === '/leaderboard' && <span className="ml-1">🏆</span>}
                        </Link>
                    ))}
                    <div className="pt-2 border-t border-gray-100 dark:border-gray-700 mt-2">
                        <button
                            onClick={handleThemeToggle}
                            className="flex items-center gap-2 w-full px-4 py-2.5 rounded-xl text-sm font-semibold text-wsu-slate dark:text-gray-300 hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                        >
                            {theme === 'dark' ? <SunIcon /> : <MoonIcon />}
                            {theme === 'dark' ? 'Light Mode' : 'Dark Mode'}
                        </button>
                        <button
                            onClick={() => { setMobileOpen(false); logout() }}
                            className="w-full text-left px-4 py-2.5 rounded-xl text-sm font-semibold text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
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
