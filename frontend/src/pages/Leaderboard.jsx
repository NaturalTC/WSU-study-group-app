import { useState, useEffect } from 'react'
import AppHeader from '../components/AppHeader'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'
import campusPhoto from '../assets/DroneShow_Sept2025-63-X4.jpg'

const RANK_LABELS = [
  { min: 0,    label: 'Hatchling',     color: 'text-gray-500   dark:text-gray-400',   bg: 'bg-gray-100   dark:bg-gray-700' },
  { min: 100,  label: 'Fledgling',     color: 'text-green-600  dark:text-green-400',  bg: 'bg-green-50   dark:bg-green-900/30' },
  { min: 300,  label: 'Night Owl',     color: 'text-blue-600   dark:text-blue-400',   bg: 'bg-blue-50    dark:bg-blue-900/30' },
  { min: 600,  label: 'Nest Guardian', color: 'text-purple-600 dark:text-purple-400', bg: 'bg-purple-50  dark:bg-purple-900/30' },
  { min: 1000, label: 'Wise Owl',      color: 'text-yellow-600 dark:text-yellow-400', bg: 'bg-yellow-50  dark:bg-yellow-900/30' },
]

function getRankLabel(points) {
  return [...RANK_LABELS].reverse().find(r => points >= r.min) ?? RANK_LABELS[0]
}

const MEDALS = ['🥇', '🥈', '🥉']

const HOW_TO_EARN = [
  { icon: '👥', action: 'Join a study group',      pts: '+10 pts' },
  { icon: '💬', action: 'Send messages in chat',   pts: '+1 pt each' },
  { icon: '⭐', action: 'Create study group',         pts: '+15 pts' },
  { icon: '📅', action: 'Attend a study session',  pts: '+25 pts' },
  { icon: '🔥', action: 'Daily login streak',      pts: '+5 pts/day' },
]

function Leaderboard() {
  const { profile } = useAuth()
  const [leaders, setLeaders]   = useState([])
  const [loading, setLoading]   = useState(true)
  const [tab, setTab]           = useState('allTime') // allTime | weekly — weekly pending backend

  const MOCK_LEADERS = [
    { id: 'u1', name: 'Sarah K.',     major: 'Computer Science', points: 1520 },
    { id: 'u2', name: 'Marcus T.',    major: 'Biology',          points: 1210 },
    { id: 'u3', name: 'Priya M.',     major: 'Psychology',       points: 980  },
    { id: 'u4', name: 'Jordan L.',    major: 'Business',         points: 740  },
    { id: 'u5', name: 'Alex R.',      major: 'Criminal Justice', points: 610  },
    { id: 'u6', name: 'Taylor W.',    major: 'Education',        points: 430  },
    { id: 'u7', name: 'Casey B.',     major: 'Nursing',          points: 295  },
    { id: 'u8', name: 'Morgan D.',    major: 'History',          points: 180  },
    { id: 'u9', name: 'Riley S.',     major: 'Communication',    points: 90   },
  ]

  useEffect(() => {
    setLoading(true)
    api.get('/leaderboard')
      .then(res => setLeaders(Array.isArray(res.data) ? res.data : res.data?.leaderboard ?? []))
      .catch(() => setLeaders(MOCK_LEADERS))
      .finally(() => setLoading(false))
  }, [])

  const myRank = leaders.findIndex(u => u.id === profile?.id || u.name === profile?.name) + 1
  const myPoints = profile?.points ?? 0
  const myRankLabel = getRankLabel(myPoints)

  const topThree = leaders.slice(0, 3)

  return (
    <div
      className="flex flex-col min-h-screen bg-cover bg-center bg-fixed transition-colors duration-300"
      style={{ backgroundImage: `url(${campusPhoto})` }}
    >
      <AppHeader />

      <main className="flex-1 pt-16">

        {/* ── Hero Banner ── */}
        <div className="bg-gradient-to-br from-wsu-navy/75 via-blue-900/75 to-blue-800/75 text-white">
          <div className="max-w-5xl mx-auto px-6 py-16">
            <div className="flex flex-col md:flex-row items-start md:items-end justify-between gap-6">
              <div>
                <h1 className="font-display text-3xl md:text-4xl font-bold leading-tight">
                  Owl Rankings
                </h1>
                <p className="text-blue-200 mt-1 text-sm">
                  Top students at Westfield State University
                </p>
              </div>

              {/* Your stat card */}
              <div className="bg-white/10 border border-white/20 backdrop-blur-sm rounded-2xl px-6 py-4 min-w-[200px] text-center self-center md:self-auto">
                <p className="text-xs text-blue-200 font-semibold uppercase tracking-wider mb-1">Your Rank</p>
                <p className="font-display text-4xl font-bold text-wsu-gold">
                  {myRank > 0 ? `#${myRank}` : '—'}
                </p>
                <p className="text-xs text-blue-200 mt-1">{myPoints} points</p>
                <span className={`inline-block mt-2 text-xs font-semibold px-3 py-1 rounded-full bg-white/20 text-white`}>
                  {myRankLabel.label}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="max-w-5xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* ── Leaderboard Table ── */}
          <div className="lg:col-span-2 space-y-4">

            {/* Tab switcher */}
            <div className="flex gap-2 mb-2">
              {[['allTime', 'All Time'], ['weekly', 'This Week']].map(([key, label]) => (
                <button
                  key={key}
                  onClick={() => setTab(key)}
                  className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-200 ${
                    tab === key
                      ? 'bg-blue-700 text-white shadow-sm'
                      : 'bg-white dark:bg-gray-800 text-wsu-slate dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:bg-wsu-mist dark:hover:bg-gray-700'
                  }`}
                >
                  {label}
                </button>
              ))}
            </div>

            {loading && (
              <div className="flex justify-center py-20">
                <div className="animate-spin w-8 h-8 border-4 border-blue-700 border-t-transparent rounded-full" />
              </div>
            )}

            {!loading && leaders.length === 0 && (
              <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm px-6 py-14 text-center">
                <div className="text-4xl mb-3">🏆</div>
                <p className="text-wsu-navy dark:text-white font-semibold">No rankings yet</p>
                <p className="text-wsu-slate dark:text-gray-400 text-sm mt-1">Be the first to earn points!</p>
              </div>
            )}

            {!loading && leaders.length > 0 && (
              <>
                {/* Top 3 podium cards */}
                <div className="grid grid-cols-3 gap-3 mb-4">
                  {[topThree[1], topThree[0], topThree[2]].map((user, visualIdx) => {
                    if (!user) return <div key={visualIdx} />
                    const rank = leaders.indexOf(user) + 1
                    const isMe = user.id === profile?.id || user.name === profile?.name
                    const heights = ['h-28', 'h-36', 'h-24']
                    const medal = MEDALS[rank - 1]
                    const podiumColors = [
                      'from-gray-300 to-gray-400',
                      'from-yellow-300 to-yellow-500',
                      'from-orange-300 to-orange-500',
                    ]
                    return (
                      <div
                        key={user.id ?? rank}
                        className={`flex flex-col items-center justify-end ${heights[visualIdx]} rounded-2xl bg-gradient-to-b ${podiumColors[visualIdx]} p-3 ${isMe ? 'ring-2 ring-blue-500' : ''}`}
                      >
                        <div className="text-xl mb-1">{medal}</div>
                        <p className="text-xs font-bold text-wsu-navy text-center leading-tight truncate w-full text-center">
                          {user.name?.split(' ')[0] ?? 'Student'}
                        </p>
                        <p className="text-xs font-semibold text-wsu-navy/70">
                          {user.points ?? 0} pts
                        </p>
                      </div>
                    )
                  })}
                </div>

                {/* Rankings list */}
                <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden">
                  {leaders.map((user, idx) => {
                    const rank = idx + 1
                    const isMe = user.id === profile?.id || user.name === profile?.name
                    const rankLabel = getRankLabel(user.points ?? 0)
                    return (
                      <div
                        key={user.id ?? idx}
                        className={`flex items-center gap-4 px-5 py-3.5 border-b border-gray-50 dark:border-gray-700 last:border-0 transition-colors ${
                          isMe
                            ? 'bg-blue-50 dark:bg-blue-900/20'
                            : 'hover:bg-wsu-mist dark:hover:bg-gray-700/50'
                        }`}
                      >
                        {/* Rank */}
                        <div className="w-8 text-center flex-shrink-0">
                          {rank <= 3
                            ? <span className="text-lg">{MEDALS[rank - 1]}</span>
                            : <span className="text-sm font-bold text-wsu-slate dark:text-gray-400">#{rank}</span>
                          }
                        </div>

                        {/* Avatar */}
                        {user.profilePicURL ? (
                          <img src={user.profilePicURL} alt={user.name} className={`w-9 h-9 rounded-full object-cover flex-shrink-0 ${isMe ? 'ring-2 ring-blue-500' : ''}`} />
                        ) : (
                          <div className={`w-9 h-9 rounded-full flex items-center justify-center text-white text-sm font-bold flex-shrink-0 ${
                            isMe ? 'bg-blue-700' : 'bg-wsu-navy dark:bg-blue-800'
                          }`}>
                            {user.name?.charAt(0)?.toUpperCase() ?? '?'}
                          </div>
                        )}

                        {/* Name + rank label */}
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <p className={`text-sm font-semibold truncate ${isMe ? 'text-blue-700 dark:text-blue-400' : 'text-wsu-navy dark:text-white'}`}>
                              {user.name ?? 'Student'}
                              {isMe && <span className="ml-1 text-xs text-blue-500">(you)</span>}
                            </p>
                          </div>
                          <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${rankLabel.bg} ${rankLabel.color}`}>
                            {rankLabel.label}
                          </span>
                        </div>

                        {/* Points */}
                        <div className="text-right flex-shrink-0">
                          <p className="text-sm font-bold text-wsu-navy dark:text-white">{(user.points ?? 0).toLocaleString()}</p>
                          <p className="text-xs text-wsu-slate dark:text-gray-400">pts</p>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </>
            )}
          </div>

          {/* ── Sidebar ── */}
          <div className="space-y-5">

            {/* Your Progress */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-5">
              <h3 className="font-display text-base font-bold text-wsu-navy dark:text-white mb-4">Your Progress</h3>
              {(() => {
                const current = getRankLabel(myPoints)
                const nextIdx = RANK_LABELS.findIndex(r => r.label === current.label) + 1
                const next    = RANK_LABELS[nextIdx]
                const pct     = next
                  ? Math.min(100, Math.round(((myPoints - current.min) / (next.min - current.min)) * 100))
                  : 100
                return (
                  <>
                    <div className="flex items-center justify-between mb-2">
                      <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${current.bg} ${current.color}`}>
                        {current.label}
                      </span>
                      {next && (
                        <span className="text-xs text-wsu-slate dark:text-gray-400">{next.label}</span>
                      )}
                    </div>
                    <div className="h-2 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden mb-2">
                      <div
                        className="h-full bg-gradient-to-r from-blue-600 to-blue-400 rounded-full transition-all duration-700"
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                    <p className="text-xs text-wsu-slate dark:text-gray-400">
                      {myPoints} pts
                      {next && ` · ${next.min - myPoints} more to ${next.label}`}
                    </p>
                  </>
                )
              })()}
            </div>

            {/* How to earn points */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-5">
              <h3 className="font-display text-base font-bold text-wsu-navy dark:text-white mb-4">How to Earn Points</h3>
              <ul className="space-y-2.5">
                {HOW_TO_EARN.map(item => (
                  <li key={item.action} className="flex items-center justify-between gap-3">
                    <div className="flex items-center gap-2">
                      <span className="text-base">{item.icon}</span>
                      <span className="text-xs text-wsu-slate dark:text-gray-300">{item.action}</span>
                    </div>
                    <span className="text-xs font-bold text-blue-700 dark:text-blue-400 flex-shrink-0">{item.pts}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Rank tiers */}
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-5">
              <h3 className="font-display text-base font-bold text-wsu-navy dark:text-white mb-4">Rank Tiers</h3>
              <ul className="space-y-2">
                {RANK_LABELS.map(r => (
                  <li key={r.label} className="flex items-center justify-between">
                    <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${r.bg} ${r.color}`}>{r.label}</span>
                    <span className="text-xs text-wsu-slate dark:text-gray-400">{r.min}+ pts</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

export default Leaderboard
