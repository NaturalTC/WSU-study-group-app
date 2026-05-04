import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import AppHeader from '../components/AppHeader'
import api from '../api/axios'

export default function GroupChatIndex() {
    const [groups, setGroups]     = useState([])
    const [loading, setLoading]   = useState(true)

    useEffect(() => {
        api.get('/groups/my')
            .then(res => setGroups(res.data))
            .catch(() => {})
            .finally(() => setLoading(false))
    }, [])

    return (
        <div className="flex flex-col min-h-screen bg-wsu-chalk">
            <AppHeader />

            <main className="flex-1 pt-24 pb-12">
                <div className="max-w-2xl mx-auto px-6">

                    <div className="mb-8">
                        <h1 className="font-display text-3xl text-wsu-navy font-bold">Group Chat</h1>
                        <p className="text-wsu-slate text-sm mt-1">Select a group to open its chat.</p>
                    </div>

                    {loading ? (
                        <div className="flex justify-center py-20">
                            <div className="animate-spin w-6 h-6 border-4 border-blue-700 border-t-transparent rounded-full" />
                        </div>
                    ) : groups.length === 0 ? (
                        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm px-6 py-14 text-center">
                            <div className="w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center mx-auto mb-3">
                                <svg className="w-6 h-6 text-blue-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                                </svg>
                            </div>
                            <p className="text-wsu-navy font-semibold mb-1">No group chats yet</p>
                            <p className="text-wsu-slate text-sm mb-5">Join a study group to start chatting with your classmates.</p>
                            <Link
                                to="/study-groups"
                                className="inline-flex items-center gap-2 bg-blue-700 hover:bg-blue-800 text-white text-sm font-semibold px-5 py-2.5 rounded-lg transition-all duration-200"
                            >
                                Browse Study Groups
                            </Link>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {groups.map(group => (
                                <Link
                                    key={group.id}
                                    to={`/group-chat/${group.id}`}
                                    className="flex items-center gap-4 bg-white rounded-2xl border border-gray-100 shadow-sm px-5 py-4 hover:shadow-md hover:border-blue-100 transition-all duration-200 group"
                                >
                                    {/* Icon */}
                                    <div className="w-11 h-11 rounded-xl bg-blue-50 flex items-center justify-center text-blue-700 font-display font-bold text-base flex-shrink-0">
                                        {group.course?.courseCode?.split(' ')[0]?.charAt(0) ?? 'G'}
                                    </div>

                                    {/* Info */}
                                    <div className="flex-1 min-w-0">
                                        <p className="font-semibold text-wsu-navy text-sm truncate">{group.name}</p>
                                        <p className="text-xs text-wsu-slate mt-0.5 truncate">
                                            {group.course?.courseCode} · {group.members?.length ?? 0} member{(group.members?.length ?? 0) !== 1 ? 's' : ''}
                                        </p>
                                    </div>

                                    {/* Arrow */}
                                    <svg className="w-4 h-4 text-gray-300 group-hover:text-blue-700 transition-colors flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                    </svg>
                                </Link>
                            ))}
                        </div>
                    )}
                </div>
            </main>
        </div>
    )
}
