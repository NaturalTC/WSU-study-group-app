import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import AppHeader from '../components/AppHeader'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'

const YEAR_OPTIONS = ['Freshman', 'Sophomore', 'Junior', 'Senior']

function Profile() {
    const { profile, setProfile } = useAuth()

    const [groups, setGroups]           = useState([])
    const [dataLoading, setDataLoading] = useState(true)

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
        <div className="flex flex-col min-h-screen bg-wsu-chalk dark:bg-gray-900 transition-colors duration-300">
            <AppHeader />

            <main className="flex-1 pt-20">

                {/* ── Profile Banner ── */}
                <div className="bg-white dark:bg-gray-800 border-b border-gray-100 dark:border-gray-700">
                    <div className="max-w-4xl mx-auto px-6 py-8">
                        <div className="flex flex-col sm:flex-row items-start gap-6">

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
                                    {profile?.major && (
                                        <span className="text-sm text-wsu-slate dark:text-gray-300">{profile.major}</span>
                                    )}
                                    {profile?.year && (
                                        <span className="text-xs font-semibold bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 px-2.5 py-1 rounded-full">
                                            {profile.year}
                                        </span>
                                    )}
                                </div>
                                {profile?.bio && (
                                    <p className="text-sm text-wsu-slate dark:text-gray-300 mt-2 leading-relaxed max-w-lg">
                                        {profile.bio}
                                    </p>
                                )}
                            </div>

                            {/* Edit link */}
                            <button
                                onClick={openEdit}
                                className="text-sm font-semibold text-blue-700 dark:text-blue-400 hover:underline transition-colors flex-shrink-0"
                            >
                                Edit Profile
                            </button>
                        </div>

                        {/* Stats */}
                        <div className="flex gap-6 mt-6 pt-6 border-t border-gray-100 dark:border-gray-700">
                            <div>
                                <p className="text-xl font-display font-bold text-wsu-navy dark:text-white">
                                    {dataLoading ? '—' : groups.length}
                                </p>
                                <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5">Study Groups</p>
                            </div>
                            <div className="w-px bg-gray-100 dark:bg-gray-700" />
                            <div>
                                <p className="text-xl font-display font-bold text-wsu-navy dark:text-white">
                                    {profile?.points ?? 0}
                                </p>
                                <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5">Points 🏆</p>
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
                        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm px-6 py-14 text-center">
                            <div className="w-12 h-12 bg-blue-50 dark:bg-blue-900/30 rounded-2xl flex items-center justify-center mx-auto mb-3">
                                <svg className="w-6 h-6 text-blue-700 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                                </svg>
                            </div>
                            <p className="text-wsu-navy dark:text-white font-semibold mb-1">No study groups yet</p>
                            <p className="text-wsu-slate dark:text-gray-400 text-sm mb-5">Join a group for your courses to start collaborating.</p>
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
                                <div key={group.id} className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-5 flex flex-col gap-3 hover:shadow-md transition-shadow duration-200">
                                    <div className="flex items-start justify-between gap-2">
                                        <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center text-blue-700 dark:text-blue-400 font-display font-bold text-sm flex-shrink-0">
                                            {group.course?.courseCode?.split(' ')[0]?.charAt(0) ?? 'G'}
                                        </div>
                                        <span className="text-xs font-semibold bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 px-2.5 py-1 rounded-full">
                                            {group.course?.courseCode}
                                        </span>
                                    </div>
                                    <div>
                                        <p className="font-semibold text-wsu-navy dark:text-white text-sm leading-snug">{group.name}</p>
                                        <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5">{group.course?.courseName}</p>
                                    </div>
                                    <div className="flex items-center justify-between mt-auto pt-3 border-t border-gray-50 dark:border-gray-700">
                                        <div className="flex items-center gap-1.5">
                                            <div className="flex -space-x-1.5">
                                                {(group.members ?? []).slice(0, 3).map((m, i) => (
                                                    <div key={i} className="w-6 h-6 rounded-full bg-wsu-navy dark:bg-blue-800 text-white text-xs flex items-center justify-center border-2 border-white dark:border-gray-800 font-semibold">
                                                        {m.name?.charAt(0) ?? '?'}
                                                    </div>
                                                ))}
                                            </div>
                                            <span className="text-xs text-wsu-slate dark:text-gray-400">{group.members?.length ?? 0} members</span>
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

            {/* ── Edit Profile Modal ── */}
            {editOpen && (
                <div className="fixed inset-0 bg-black/50 z-50 flex items-end sm:items-center justify-center sm:px-6 backdrop-blur-sm">
                    <div className="bg-white dark:bg-gray-800 w-full sm:max-w-lg sm:rounded-2xl rounded-t-2xl shadow-2xl animate-fade-up">

                        <div className="flex justify-center pt-3 pb-1 sm:hidden">
                            <div className="w-10 h-1 bg-gray-200 rounded-full" />
                        </div>

                        <div className="px-6 pt-4 pb-6">
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="font-display text-xl text-wsu-navy dark:text-white">Edit Profile</h2>
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
