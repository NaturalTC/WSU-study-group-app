import { useState, useEffect, useRef } from 'react'
import { getNotifications, markAsRead, markAllAsRead } from '../api/notifications'

const TYPE_ICON = {
    SESSION_SCHEDULED: '📅',
    BADGE_EARNED:      '🏅',
    MEMBER_JOINED:     '👋',
}

function NotificationBell() {
    const [notifications, setNotifications] = useState([])
    const [open, setOpen]                   = useState(false)
    const [loading, setLoading]             = useState(false)
    const bellRef = useRef(null)

    const unreadCount = notifications.filter(n => !n.read).length

    // Fetch notifications on mount and every 30 seconds
    useEffect(() => {
        fetchNotifications()
        const interval = setInterval(fetchNotifications, 30_000)
        return () => clearInterval(interval)
    }, [])

    // Close dropdown when clicking outside
    useEffect(() => {
        const handler = (e) => {
            if (bellRef.current && !bellRef.current.contains(e.target)) {
                setOpen(false)
            }
        }
        document.addEventListener('mousedown', handler)
        return () => document.removeEventListener('mousedown', handler)
    }, [])

    async function fetchNotifications() {
        try {
            const res = await getNotifications()
            setNotifications(res.data)
        } catch {
            // silently ignore — bell just stays empty if backend is unreachable
        }
    }

    async function handleMarkAsRead(id) {
        try {
            await markAsRead(id)
            setNotifications(prev =>
                prev.map(n => n.id === id ? { ...n, read: true } : n)
            )
        } catch { /* ignore */ }
    }

    async function handleMarkAllAsRead() {
        try {
            setLoading(true)
            await markAllAsRead()
            setNotifications(prev => prev.map(n => ({ ...n, read: true })))
        } catch { /* ignore */ }
        finally { setLoading(false) }
    }

    function formatDate(iso) {
        const d = new Date(iso)
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
    }

    return (
        <div className="relative" ref={bellRef}>

            {/* Bell button */}
            <button
                onClick={() => setOpen(prev => !prev)}
                className="relative p-2 rounded-lg hover:bg-wsu-mist transition-colors"
                aria-label="Notifications"
            >
                <svg className="w-5 h-5 text-wsu-navy" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                        d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-12 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>

                {/* Unread badge */}
                {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 min-w-[16px] h-4 px-1 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center leading-none">
                        {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                )}
            </button>

            {/* Dropdown */}
            {open && (
                <div className="absolute right-0 top-11 w-80 bg-white rounded-xl shadow-lg border border-gray-100 animate-fade-in z-50 overflow-hidden">

                    {/* Header */}
                    <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
                        <span className="text-sm font-semibold text-wsu-navy">Notifications</span>
                        {unreadCount > 0 && (
                            <button
                                onClick={handleMarkAllAsRead}
                                disabled={loading}
                                className="text-xs text-blue-600 hover:text-blue-800 font-medium disabled:opacity-50"
                            >
                                Mark all as read
                            </button>
                        )}
                    </div>

                    {/* List */}
                    <ul className="max-h-80 overflow-y-auto divide-y divide-gray-50">
                        {notifications.length === 0 ? (
                            <li className="px-4 py-8 text-center text-sm text-wsu-slate">
                                No notifications yet
                            </li>
                        ) : (
                            notifications.map(n => (
                                <li
                                    key={n.id}
                                    onClick={() => !n.read && handleMarkAsRead(n.id)}
                                    className={`flex gap-3 px-4 py-3 transition-colors ${
                                        n.read
                                            ? 'bg-white'
                                            : 'bg-blue-50 cursor-pointer hover:bg-blue-100'
                                    }`}
                                >
                                    {/* Type icon */}
                                    <span className="text-lg flex-shrink-0 mt-0.5">
                                        {TYPE_ICON[n.type] ?? '🔔'}
                                    </span>

                                    <div className="flex-1 min-w-0">
                                        <p className={`text-sm leading-snug ${n.read ? 'text-wsu-slate' : 'text-wsu-navy font-medium'}`}>
                                            {n.message}
                                        </p>
                                        <p className="text-xs text-gray-400 mt-0.5">
                                            {formatDate(n.createdAt)}
                                        </p>
                                    </div>

                                    {/* Unread dot */}
                                    {!n.read && (
                                        <span className="w-2 h-2 bg-blue-500 rounded-full flex-shrink-0 mt-1.5" />
                                    )}
                                </li>
                            ))
                        )}
                    </ul>
                </div>
            )}
        </div>
    )
}

export default NotificationBell
