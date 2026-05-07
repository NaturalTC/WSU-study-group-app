import { useState, useEffect, useRef } from 'react'
import AppHeader from '../components/AppHeader'
import api from '../api/axios'
import campusPhoto from '../assets/UHallSept2018_3-X3.jpg'

function initials(name = '') {
  const parts = name.trim().split(' ')
  return parts.length >= 2
    ? `${parts[0][0]}${parts[parts.length - 1][0]}`
    : name[0]?.toUpperCase() ?? '?'
}

const AVATAR_COLORS = [
  'bg-blue-700', 'bg-indigo-600', 'bg-violet-600',
  'bg-emerald-600', 'bg-teal-600', 'bg-cyan-600',
  'bg-rose-600', 'bg-orange-600', 'bg-pink-600',
]
function avatarColor(id) {
  return AVATAR_COLORS[(id ?? 0) % AVATAR_COLORS.length]
}

function ProfileModal({ person, onClose, onAction, friendState }) {
  const ref = useRef(null)

  useEffect(() => {
    const handler = (e) => { if (ref.current && !ref.current.contains(e.target)) onClose() }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [onClose])

  const { status, direction } = friendState ?? {}
  const [loading, setLoading] = useState(false)

  const handle = async (action) => {
    setLoading(true)
    try { await onAction(person, action) } finally { setLoading(false) }
  }

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
      <div ref={ref} className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-sm p-6 animate-fade-up">
        <div className="flex justify-end mb-2">
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 text-2xl leading-none">×</button>
        </div>

        <div className="flex flex-col items-center text-center gap-2">
          <div className={`w-16 h-16 rounded-2xl ${avatarColor(person.profileId)} flex items-center justify-center text-white font-display font-bold text-2xl shadow`}>
            {initials(person.name)}
          </div>
          <div>
            <h2 className="font-display text-xl text-wsu-navy dark:text-white font-bold">{person.name}</h2>
            <div className="flex items-center justify-center gap-2 mt-1 flex-wrap">
              {person.major && <span className="text-sm text-wsu-slate dark:text-gray-400">{person.major}</span>}
              {person.year && (
                <span className="text-xs font-semibold bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 px-2 py-0.5 rounded-full">
                  {person.year}
                </span>
              )}
            </div>
          </div>
          {person.bio && (
            <p className="text-sm text-wsu-slate dark:text-gray-400 leading-relaxed px-2">{person.bio}</p>
          )}
          {person.sharedGroupName && (
            <span className="text-xs text-emerald-700 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-900/20 px-3 py-1 rounded-full font-medium">
              In {person.sharedGroupName}
            </span>
          )}
        </div>

        <div className="mt-6">
          {status === 'ACCEPTED' ? (
            <button disabled={loading} onClick={() => handle('remove')}
              className="w-full py-3 rounded-xl text-sm font-semibold text-red-500 border border-red-200 dark:border-red-800 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all disabled:opacity-60">
              Remove Friend
            </button>
          ) : status === 'PENDING' && direction === 'SENT' ? (
            <button disabled={loading} onClick={() => handle('cancel')}
              className="w-full py-3 rounded-xl text-sm font-semibold border border-gray-200 dark:border-gray-700 text-wsu-slate dark:text-gray-300 hover:bg-wsu-mist dark:hover:bg-gray-700 transition-all disabled:opacity-60">
              Cancel Request
            </button>
          ) : status === 'PENDING' && direction === 'RECEIVED' ? (
            <div className="flex gap-3">
              <button disabled={loading} onClick={() => handle('accept')}
                className="flex-1 py-3 rounded-xl text-sm font-semibold bg-blue-700 text-white hover:bg-blue-800 transition-all disabled:opacity-60">
                Accept
              </button>
              <button disabled={loading} onClick={() => handle('decline')}
                className="flex-1 py-3 rounded-xl text-sm font-semibold border border-gray-200 dark:border-gray-700 text-wsu-slate dark:text-gray-300 hover:bg-wsu-mist dark:hover:bg-gray-700 transition-all disabled:opacity-60">
                Decline
              </button>
            </div>
          ) : (
            <button disabled={loading} onClick={() => handle('add')}
              className="w-full py-3 rounded-xl text-sm font-semibold bg-blue-700 text-white hover:bg-blue-800 transition-all disabled:opacity-60">
              {loading ? 'Sending…' : '+ Add Friend'}
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

function PersonCard({ person, onTap, badge, actionSlot }) {
  return (
    <div
      onClick={() => onTap(person)}
      className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-4 flex items-center gap-3 hover:shadow-md hover:border-blue-100 dark:hover:border-blue-800 cursor-pointer transition-all duration-200"
    >
      <div className={`w-11 h-11 rounded-xl ${avatarColor(person.profileId)} flex items-center justify-center text-white font-display font-bold text-base flex-shrink-0`}>
        {initials(person.name)}
      </div>
      <div className="flex-1 min-w-0">
        <p className="font-semibold text-wsu-navy dark:text-white text-sm leading-snug truncate">{person.name}</p>
        <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5 truncate">
          {[person.major, person.year].filter(Boolean).join(' · ')}
        </p>
        {badge && (
          <span className="inline-block text-xs text-emerald-700 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-900/20 px-2 py-0.5 rounded-full font-medium mt-1 truncate max-w-full">
            {badge}
          </span>
        )}
      </div>
      {actionSlot && <div onClick={e => e.stopPropagation()}>{actionSlot}</div>}
    </div>
  )
}

function Friends() {
  const [friends,      setFriends]      = useState([])
  const [incoming,     setIncoming]     = useState([])
  const [outgoing,     setOutgoing]     = useState([])
  const [suggestions,  setSuggestions]  = useState([])
  const [loading,      setLoading]      = useState(true)

  const [activeTab,     setActiveTab]     = useState('friends')
  const [search,        setSearch]        = useState('')
  const [searchResults, setSearchResults] = useState(null)
  const [searchLoading, setSearchLoading] = useState(false)
  const searchTimeout = useRef(null)

  const [selected,      setSelected]      = useState(null)
  const [actionLoading, setActionLoading] = useState(null)

  const friendState = (profileId) => {
    const f = friends.find(f => f.profileId === profileId)
    if (f) return { status: 'ACCEPTED', direction: null, friendshipId: f.friendshipId }
    const i = incoming.find(f => f.profileId === profileId)
    if (i) return { status: 'PENDING', direction: 'RECEIVED', friendshipId: i.friendshipId }
    const o = outgoing.find(f => f.profileId === profileId)
    if (o) return { status: 'PENDING', direction: 'SENT', friendshipId: o.friendshipId }
    return null
  }

  useEffect(() => {
    const load = async () => {
      try {
        const [fr, inc, out, sug] = await Promise.all([
          api.get('/friends'),
          api.get('/friends/requests/incoming'),
          api.get('/friends/requests/outgoing'),
          api.get('/friends/suggestions'),
        ])
        setFriends(fr.data)
        setIncoming(inc.data)
        setOutgoing(out.data)
        setSuggestions(sug.data)
      } catch (err) {
        console.error('Failed to load friends data:', err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  useEffect(() => {
    clearTimeout(searchTimeout.current)
    if (!search.trim()) { setSearchResults(null); return }

    setSearchLoading(true)
    searchTimeout.current = setTimeout(async () => {
      try {
        const res = await api.get(`/friends/search?q=${encodeURIComponent(search)}`)
        setSearchResults(res.data)
      } catch { setSearchResults([]) }
      finally { setSearchLoading(false) }
    }, 300)
  }, [search])

  const handleAction = async (person, action) => {
    setActionLoading(person.profileId)
    try {
      if (action === 'add') {
        const res = await api.post(`/friends/request/${person.profileId}`)
        setOutgoing(prev => [...prev, res.data])
        setSuggestions(prev => prev.filter(s => s.profileId !== person.profileId))
      } else if (action === 'cancel' || action === 'remove') {
        const fid = friendState(person.profileId)?.friendshipId
        await api.delete(`/friends/${fid}`)
        setFriends(prev => prev.filter(f => f.profileId !== person.profileId))
        setOutgoing(prev => prev.filter(f => f.profileId !== person.profileId))
      } else if (action === 'accept') {
        const fid = friendState(person.profileId)?.friendshipId
        const res = await api.patch(`/friends/${fid}/accept`)
        setFriends(prev => [...prev, res.data])
        setIncoming(prev => prev.filter(f => f.profileId !== person.profileId))
      } else if (action === 'decline') {
        const fid = friendState(person.profileId)?.friendshipId
        await api.patch(`/friends/${fid}/decline`)
        setIncoming(prev => prev.filter(f => f.profileId !== person.profileId))
      }
      setSelected(null)
    } finally { setActionLoading(null) }
  }

  const tabs = [
    { id: 'friends',     label: 'Friends',            count: friends.length },
    { id: 'requests',    label: 'Requests',            count: incoming.length },
    { id: 'sent',        label: 'Sent',                count: outgoing.length },
    { id: 'suggestions', label: 'People You May Know', count: suggestions.length },
  ]

  const empty = (msg, sub) => (
    <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm px-6 py-14 text-center">
      <p className="text-wsu-navy dark:text-white font-semibold mb-1">{msg}</p>
      {sub && <p className="text-wsu-slate dark:text-gray-400 text-sm">{sub}</p>}
    </div>
  )

  const listData = activeTab === 'friends'     ? friends
                 : activeTab === 'requests'    ? incoming
                 : activeTab === 'sent'        ? outgoing
                 : suggestions

  return (
    <div
      className="flex flex-col min-h-screen bg-cover bg-center bg-fixed transition-colors duration-300"
      style={{ backgroundImage: `url(${campusPhoto})` }}
    >
      <AppHeader />

      <main className="flex-1 pt-16">

        {/* ── Hero Banner ── */}
        <div className="relative bg-gradient-to-br from-wsu-navy/75 via-blue-900/75 to-blue-800/75 text-white overflow-hidden">
          <div className="relative max-w-5xl mx-auto px-6 py-16">
            <div className="flex flex-col md:flex-row items-start md:items-end justify-between gap-6">
              <div>
                <h1 className="font-display text-3xl md:text-4xl font-bold leading-tight">Friends</h1>
                <p className="text-blue-200 mt-1 text-sm">Connect with classmates from your courses and study groups.</p>
              </div>
              <div className="bg-white/10 border border-white/20 backdrop-blur-sm rounded-2xl px-6 py-4 min-w-[200px] text-center flex-shrink-0">
                <p className="text-xs text-blue-200 font-semibold uppercase tracking-wider mb-1">Friends</p>
                <p className="font-display text-4xl font-bold text-white">{friends.length}</p>
                <p className="text-xs text-blue-200 mt-1">connected</p>
                <span className="inline-block mt-2 text-xs font-semibold px-3 py-1 rounded-full bg-white/20 text-white">
                  {incoming.length > 0 ? `${incoming.length} pending` : 'no requests'}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="max-w-5xl mx-auto px-6 pt-8 pb-6">

          {/* Search */}
          <div className="relative mb-6">
            <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Search by name…"
              className="form-input !pl-10 !py-2.5"
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
            {search && (
              <button
                onClick={() => setSearch('')}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 text-lg leading-none"
              >×</button>
            )}
          </div>

          {searchResults !== null ? (
            <>
              <p className="text-sm text-wsu-slate dark:text-gray-400 mb-4">
                {searchLoading ? 'Searching…' : `${searchResults.length} result${searchResults.length !== 1 ? 's' : ''} for "${search}"`}
              </p>
              {searchLoading ? (
                <div className="flex justify-center py-12">
                  <div className="animate-spin w-7 h-7 border-4 border-blue-700 border-t-transparent rounded-full" />
                </div>
              ) : searchResults.length === 0 ? (
                empty('No users found', `No one matched "${search}".`)
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {searchResults.map(person => (
                    <PersonCard
                      key={person.profileId}
                      person={person}
                      onTap={setSelected}
                      badge={person.sharedGroupName ? `In ${person.sharedGroupName}` : null}
                    />
                  ))}
                </div>
              )}
            </>
          ) : (
            <>
              <div className="flex gap-1 mb-5 flex-wrap">
                {tabs.map(tab => (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-200 flex items-center gap-1.5 ${
                      activeTab === tab.id
                        ? 'bg-blue-700 text-white shadow-sm'
                        : 'bg-white dark:bg-gray-800 text-wsu-slate dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:bg-wsu-mist dark:hover:bg-gray-700'
                    }`}
                  >
                    {tab.label}
                    {tab.count > 0 && (
                      <span className={`text-xs font-bold px-1.5 py-0.5 rounded-full ${
                        activeTab === tab.id
                          ? 'bg-white/25 text-white'
                          : tab.id === 'requests'
                            ? 'bg-red-500 text-white'
                            : 'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300'
                      }`}>
                        {tab.count}
                      </span>
                    )}
                  </button>
                ))}
              </div>

              {loading ? (
                <div className="flex justify-center py-24">
                  <div className="animate-spin w-8 h-8 border-4 border-blue-700 border-t-transparent rounded-full" />
                </div>
              ) : listData.length === 0 ? (
                <>
                  {activeTab === 'friends'     && empty('No friends yet', 'Browse your course rosters or search by name above to connect with classmates.')}
                  {activeTab === 'requests'    && empty('No pending requests', "You're all caught up.")}
                  {activeTab === 'sent'        && empty('No sent requests', 'Send friend requests from your course rosters.')}
                  {activeTab === 'suggestions' && empty('No suggestions yet', 'Join study groups to discover classmates.')}
                </>
              ) : (
                <div className={activeTab === 'requests' || activeTab === 'sent' ? 'space-y-3' : 'grid grid-cols-1 sm:grid-cols-2 gap-3'}>
                  {activeTab === 'friends' && friends.map(f => (
                    <PersonCard key={f.friendshipId} person={f} onTap={setSelected} />
                  ))}

                  {activeTab === 'requests' && incoming.map(f => (
                    <PersonCard
                      key={f.friendshipId}
                      person={f}
                      onTap={setSelected}
                      actionSlot={
                        <div className="flex gap-2">
                          <button disabled={actionLoading === f.profileId} onClick={() => handleAction(f, 'accept')}
                            className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-blue-700 text-white hover:bg-blue-800 transition-colors disabled:opacity-60">
                            Accept
                          </button>
                          <button disabled={actionLoading === f.profileId} onClick={() => handleAction(f, 'decline')}
                            className="text-xs font-semibold px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 text-wsu-slate dark:text-gray-300 hover:bg-wsu-mist dark:hover:bg-gray-700 transition-colors disabled:opacity-60">
                            Decline
                          </button>
                        </div>
                      }
                    />
                  ))}

                  {activeTab === 'sent' && outgoing.map(f => (
                    <PersonCard
                      key={f.friendshipId}
                      person={f}
                      onTap={setSelected}
                      actionSlot={
                        <span className="text-xs font-semibold text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20 px-2.5 py-1 rounded-full whitespace-nowrap">
                          Pending
                        </span>
                      }
                    />
                  ))}

                  {activeTab === 'suggestions' && suggestions.map(s => (
                    <PersonCard
                      key={s.profileId}
                      person={s}
                      onTap={setSelected}
                      badge={s.sharedGroupName ? `In ${s.sharedGroupName}` : null}
                      actionSlot={
                        <button disabled={actionLoading === s.profileId} onClick={() => handleAction(s, 'add')}
                          className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-blue-700 text-white hover:bg-blue-800 transition-colors disabled:opacity-60 whitespace-nowrap">
                          + Add
                        </button>
                      }
                    />
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </main>

      {selected && (
        <ProfileModal
          person={selected}
          onClose={() => setSelected(null)}
          onAction={handleAction}
          friendState={friendState(selected.profileId)}
        />
      )}
    </div>
  )
}

export default Friends
