import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import AppHeader from '../components/AppHeader'
import BadgeIcon from '../components/BadgeIcon'
import { useAuth } from '../context/AuthContext'
import { useBadges } from '../context/BadgesContext'
import api from '../api/axios'

const YEAR_OPTIONS = ['Freshman', 'Sophomore', 'Junior', 'Senior']

const CATEGORY_ORDER = ['logins', 'streak', 'messages', 'emojis', 'groups', 'sessions', 'points', 'helper']

const CATEGORY_META = {
  logins:   { label: 'Login Milestones',  hint: 'Log in to StudyNest to unlock these badges. Badges are awarded at 1, 10, 25, 50, and 100 logins.' },
  streak:   { label: 'Login Streaks',     hint: 'Log in on consecutive days without missing one. Streaks of 3, 7, 14, and 30 days each earn a badge.' },
  messages: { label: 'Messages Sent',     hint: 'Send messages in any group chat. Milestones at 1, 25, 100, and 500 messages.' },
  emojis:   { label: 'Emojis Sent',       hint: 'Use the emoji button in group chat. Earned at 1, 10, and 50 emojis sent.' },
  groups:   { label: 'Study Groups',      hint: 'Join study groups for your courses. Badges unlock at 1, 3, and 5 groups joined.' },
  sessions: { label: 'Study Sessions',    hint: 'Attend scheduled study sessions. Milestones at 1, 5, 10, and 25 sessions.' },
  points:   { label: 'Points Earned',     hint: 'Earn points through all your StudyNest activity. Awarded at 100, 500, 1,000, and 5,000 points.' },
  helper:   { label: 'Helping Others',    hint: 'Help a classmate in a study group. Badges earned at 1, 5, and 10 assists.' },
}

function Profile() {
    const { profile, setProfile } = useAuth()
    const { earned: badges }      = useBadges()

    const [groups, setGroups]               = useState([])
    const [dataLoading, setDataLoading]     = useState(true)
    const [showBadgesModal, setShowBadgesModal] = useState(false)

    const topBadges = [...badges]
        .sort((a, b) => b.tier - a.tier || new Date(b.earnedAt) - new Date(a.earnedAt))
        .slice(0, 7)

    const earnedByCategory = badges.reduce((acc, badge) => {
        if (!acc[badge.category]) acc[badge.category] = []
        acc[badge.category].push(badge)
        return acc
    }, {})

    const [editOpen, setEditOpen]       = useState(false)
    const [editForm, setEditForm]       = useState({ name: '', major: '', year: '', bio: '' })
    const [editLoading, setEditLoading] = useState(false)
    const [editError, setEditError]     = useState('')

    useEffect(() => {
        api.get('/groups/my')
            .then(res => setGroups(res.data))
            .catch(() => {})
            .finally(() => setDataLoading(false))
    }, [])

    const openEdit = () => {
        setEditForm({
            name:  profile?.name  ?? '',
            major: profile?.major ?? '',
            year:  profile?.year  ?? '',
            bio:   profile?.bio   ?? '',
        })
        setEditError('')
        setEditOpen(true)
    }

    const handleEditSubmit = async (e) => {
        e.preventDefault()
        setEditLoading(true)
        setEditError('')
        try {
            const res = await api.put('/profiles', editForm)
            setProfile(res.data)
            setEditOpen(false)
        } catch (err) {
            setEditError(err.response?.data?.message || 'Failed to update profile.')
        } finally {
            setEditLoading(false)
        }
    }

    const initials = (() => {
        const parts = (profile?.name ?? '').trim().split(' ')
        return parts.length >= 2
            ? `${parts[0].charAt(0)}${parts[parts.length - 1].charAt(0)}`
            : (profile?.name?.charAt(0)?.toUpperCase() ?? '?')
    })()

    return (
        <div className="flex flex-col min-h-screen bg-wsu-chalk dark:bg-gray-950 transition-colors duration-300">
            <AppHeader />

            <main className="flex-1 pt-20">

                {/* ── Profile Banner ── */}
                <div className="bg-white dark:bg-gray-900 border-b border-gray-100 dark:border-gray-800">
                    <div className="max-w-4xl mx-auto px-6 py-8">
                        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">

                            {/* Avatar */}
                            <div className="w-20 h-20 rounded-2xl bg-blue-700 flex items-center justify-center text-white font-display text-3xl font-bold shadow-md flex-shrink-0">
                                {initials}
                            </div>

                            {/* Info */}
                            <div className="flex-1 min-w-0">
                                <h1 className="font-display text-2xl text-wsu-navy dark:text-white font-bold leading-tight">
                                    {profile?.name}
                                </h1>
                                <div className="flex flex-wrap items-center gap-2 mt-1.5">
                                    {profile?.year && (
                                        <span className="text-xs font-semibold bg-blue-50 text-blue-700 px-2.5 py-1 rounded-full">
                                            {profile.year}
                                        </span>
                                    )}
                                    {profile?.major && (
                                        <span className="text-sm text-wsu-slate">{profile.major}</span>
                                    )}
                                </div>
                                {profile?.bio && (
                                    <p className="text-sm text-wsu-slate mt-2 leading-relaxed max-w-lg">
                                        {profile.bio}
                                    </p>
                                )}
                            </div>

                            {/* Edit button */}
                            <button
                                onClick={openEdit}
                                className="flex items-center gap-2 text-sm font-semibold px-4 py-2.5 rounded-xl border border-gray-200 text-wsu-navy hover:bg-wsu-mist hover:border-blue-200 transition-all duration-200 flex-shrink-0"
                            >
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536M9 11l6-6 3 3-6 6H9v-3z" />
                                </svg>
                                Edit Profile
                            </button>
                        </div>

                        {/* Stats */}
                        <div className="flex items-start gap-6 mt-6 pt-6 border-t border-gray-100 dark:border-gray-700">
                            <div>
                                <div className="h-8 flex items-end">
                                    <p className="text-3xl leading-none font-display font-bold text-wsu-navy dark:text-white">
                                        {dataLoading ? '—' : groups.length}
                                    </p>
                                </div>
                                <p className="text-xs text-wsu-slate dark:text-gray-400 mt-1">Study Groups</p>
                            </div>
                            <div className="w-px self-stretch bg-gray-100 dark:bg-gray-700" />
                            <div>
                                <div className="h-8 flex items-end">
                                    <p className="text-3xl leading-none font-display font-bold text-wsu-navy dark:text-white">
                                        {profile?.points ?? 0}
                                    </p>
                                </div>
                                <p className="text-xs text-wsu-slate dark:text-gray-400 mt-1">Points</p>
                            </div>
                            <div className="w-px self-stretch bg-gray-100 dark:bg-gray-700" />
                            <div>
                                <div className="h-8 flex items-center">
                                    {badges.length === 0 ? (
                                        <p className="text-3xl leading-none font-display font-bold text-wsu-slate dark:text-gray-500">—</p>
                                    ) : (
                                        <div
                                            className="flex items-center -space-x-3 cursor-pointer"
                                            onClick={() => setShowBadgesModal(true)}
                                        >
                                            {[...topBadges].reverse().map((badge, i) => (
                                                <div key={badge.id} className="relative" style={{ zIndex: i }}>
                                                    <BadgeIcon badge={badge} size="sm" />
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                                <button
                                    onClick={() => setShowBadgesModal(true)}
                                    disabled={badges.length === 0}
                                    className="text-xs text-wsu-slate dark:text-gray-400 mt-1 hover:text-blue-700 dark:hover:text-blue-400 hover:underline transition-colors disabled:cursor-default disabled:no-underline"
                                >
                                    Badges
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                {/* ── Study Groups ── */}
                <div className="max-w-4xl mx-auto px-6 py-8">
                    <div className="flex items-center justify-between mb-5">
                        <h2 className="font-display text-xl text-wsu-navy dark:text-white font-bold">My Study Groups</h2>
                        <Link to="/study-groups" className="text-sm text-blue-700 font-semibold hover:underline">
                            Browse all →
                        </Link>
                    </div>

                    {dataLoading ? (
                        <div className="flex justify-center py-16">
                            <div className="animate-spin w-6 h-6 border-4 border-blue-700 border-t-transparent rounded-full" />
                        </div>
                    ) : groups.length === 0 ? (
                        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm px-6 py-14 text-center">
                            <div className="w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center mx-auto mb-3">
                                <svg className="w-6 h-6 text-blue-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                                </svg>
                            </div>
                            <p className="text-wsu-navy font-semibold mb-1">No study groups yet</p>
                            <p className="text-wsu-slate text-sm mb-5">Join a group for your courses to start collaborating.</p>
                            <Link
                                to="/study-groups"
                                className="inline-flex items-center gap-2 bg-blue-700 hover:bg-blue-800 text-white text-sm font-semibold px-5 py-2.5 rounded-lg transition-all duration-200"
                            >
                                Browse Study Groups
                            </Link>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            {groups.map(group => (
                                <div key={group.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 flex flex-col gap-3 hover:shadow-md transition-shadow duration-200">
                                    <div className="flex items-start justify-between gap-2">
                                        <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center text-blue-700 font-display font-bold text-sm flex-shrink-0">
                                            {group.course?.courseCode?.split(' ')[0]?.charAt(0) ?? 'G'}
                                        </div>
                                        <span className="text-xs font-semibold bg-blue-50 text-blue-700 px-2.5 py-1 rounded-full">
                                            {group.course?.courseCode}
                                        </span>
                                    </div>
                                    <div>
                                        <p className="font-semibold text-wsu-navy text-sm leading-snug">{group.name}</p>
                                        <p className="text-xs text-wsu-slate mt-0.5">{group.course?.courseName}</p>
                                    </div>
                                    <div className="flex items-center justify-between mt-auto pt-3 border-t border-gray-50">
                                        <div className="flex items-center gap-1.5">
                                            <div className="flex -space-x-1.5">
                                                {(group.members ?? []).slice(0, 3).map((m, i) => (
                                                    <div key={i} className="w-6 h-6 rounded-full bg-wsu-navy text-white text-xs flex items-center justify-center border-2 border-white font-semibold">
                                                        {m.name?.charAt(0) ?? '?'}
                                                    </div>
                                                ))}
                                            </div>
                                            <span className="text-xs text-wsu-slate">{group.members?.length ?? 0} members</span>
                                        </div>
                                        <Link
                                            to={`/group-chat/${group.id}`}
                                            className="text-xs font-semibold text-blue-700 hover:text-blue-800 hover:underline transition-colors"
                                        >
                                            Open Chat →
                                        </Link>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </main>

            {/* ── All Badges Modal ── */}
            {showBadgesModal && (
                <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
                    <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl max-w-lg w-full max-h-[80vh] flex flex-col animate-fade-up">

                        {/* Header */}
                        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100 dark:border-gray-700 flex-shrink-0">
                            <div>
                                <h2 className="font-display text-xl text-wsu-navy dark:text-white">All Badges</h2>
                                <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5">{badges.length} earned</p>
                            </div>
                            <button
                                onClick={() => setShowBadgesModal(false)}
                                className="w-8 h-8 flex items-center justify-center rounded-xl hover:bg-wsu-mist dark:hover:bg-gray-800 text-wsu-slate text-xl leading-none transition-colors"
                            >
                                ×
                            </button>
                        </div>

                        {/* Scrollable category tiles */}
                        <div className="overflow-y-auto flex-1 px-5 py-5 grid grid-cols-2 gap-3 content-start">
                            {CATEGORY_ORDER.map(cat => {
                                const meta      = CATEGORY_META[cat]
                                const catBadges = [...(earnedByCategory[cat] ?? [])].sort((a, b) => a.tier - b.tier)
                                const hasEarned = catBadges.length > 0

                                return hasEarned ? (
                                    <div key={cat} className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm px-5 py-4">
                                        <p className="text-xs font-semibold text-wsu-slate dark:text-gray-400 uppercase tracking-widest mb-3">
                                            {meta.label}
                                        </p>
                                        <div className="flex -space-x-3">
                                            {catBadges.map((badge, i) => (
                                                <div key={badge.id} className="relative" style={{ zIndex: i }}>
                                                    <BadgeIcon badge={badge} size="md" />
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ) : (
                                    <div key={cat} className="bg-wsu-mist dark:bg-gray-800/60 rounded-2xl border border-gray-100 dark:border-gray-700 px-5 py-4">
                                        <p className="text-xs font-semibold text-wsu-slate/60 dark:text-gray-500 uppercase tracking-widest mb-2">
                                            {meta.label}
                                        </p>
                                        <div className="flex items-start gap-2.5">
                                            <span className="text-sm flex-shrink-0 mt-0.5">🔒</span>
                                            <p className="text-xs text-wsu-slate dark:text-gray-400 leading-relaxed">{meta.hint}</p>
                                        </div>
                                    </div>
                                )
                            })}
                        </div>
                    </div>
                </div>
            )}

            {/* ── Edit Profile Modal ── */}
            {editOpen && (
                <div className="fixed inset-0 bg-black/50 z-50 flex items-end sm:items-center justify-center sm:px-6 backdrop-blur-sm">
                    <div className="bg-white w-full sm:max-w-lg sm:rounded-2xl rounded-t-2xl shadow-2xl animate-fade-up">

                        <div className="flex justify-center pt-3 pb-1 sm:hidden">
                            <div className="w-10 h-1 bg-gray-200 rounded-full" />
                        </div>

                        <div className="px-6 pt-4 pb-6">
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="font-display text-xl text-wsu-navy">Edit Profile</h2>
                                <button
                                    onClick={() => setEditOpen(false)}
                                    className="w-8 h-8 flex items-center justify-center rounded-xl hover:bg-wsu-mist text-wsu-slate text-xl leading-none transition-colors"
                                >
                                    ×
                                </button>
                            </div>

                            {editError && (
                                <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl px-4 py-3 mb-4">
                                    {editError}
                                </div>
                            )}

                            <form onSubmit={handleEditSubmit} className="space-y-4">
                                <div>
                                    <label className="form-label">Full Name</label>
                                    <input
                                        type="text"
                                        required
                                        className="form-input"
                                        value={editForm.name}
                                        onChange={e => setEditForm(p => ({ ...p, name: e.target.value }))}
                                    />
                                </div>
                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <label className="form-label">Major</label>
                                        <input
                                            type="text"
                                            required
                                            className="form-input"
                                            value={editForm.major}
                                            onChange={e => setEditForm(p => ({ ...p, major: e.target.value }))}
                                        />
                                    </div>
                                    <div>
                                        <label className="form-label">Year</label>
                                        <select
                                            required
                                            className="form-input"
                                            value={editForm.year}
                                            onChange={e => setEditForm(p => ({ ...p, year: e.target.value }))}
                                        >
                                            <option value="">Select</option>
                                            {YEAR_OPTIONS.map(y => (
                                                <option key={y} value={y}>{y}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                                <div>
                                    <label className="form-label">
                                        Bio <span className="font-normal text-wsu-slate">(optional)</span>
                                    </label>
                                    <textarea
                                        rows={3}
                                        placeholder="Tell your classmates about yourself..."
                                        className="form-input resize-none"
                                        value={editForm.bio}
                                        onChange={e => setEditForm(p => ({ ...p, bio: e.target.value }))}
                                    />
                                </div>
                                <div className="flex gap-3 pt-1">
                                    <button
                                        type="button"
                                        onClick={() => setEditOpen(false)}
                                        className="flex-1 py-3 rounded-xl border border-gray-200 text-wsu-navy text-sm font-semibold hover:bg-wsu-mist transition-colors"
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        type="submit"
                                        disabled={editLoading}
                                        className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white text-sm font-semibold rounded-xl transition-all disabled:opacity-60 disabled:cursor-not-allowed"
                                    >
                                        {editLoading ? 'Saving...' : 'Save Changes'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}

export default Profile
