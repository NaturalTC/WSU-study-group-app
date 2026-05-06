import { useState, useEffect } from 'react'
import AppHeader from '../components/AppHeader'
import StudyGroupCard from '../components/StudyGroupCard'
import api from '../api/axios'
import campusPhoto from '../assets/WSUCampusStock2013_033-L.jpg'
import { useNotifications } from '../context/NotificationContext'

function StudyGroups() {
  const { refresh: refreshNotifications } = useNotifications()
  const [groups, setGroups]               = useState([])
  const [courses, setCourses]             = useState([])
  const [joinedGroupIds, setJoinedGroupIds] = useState(new Set())
  const [loading, setLoading]             = useState(true)
  const [search, setSearch]               = useState('')
  const [activeFilter, setActiveFilter]   = useState('all')
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [selectedGroup, setSelectedGroup] = useState(null)
  const [createForm, setCreateForm]       = useState({ name: '', courseId: '', password: '' })
  const [createLoading, setCreateLoading] = useState(false)
  const [createError, setCreateError]     = useState('')
  const [createShowPassword, setCreateShowPassword] = useState(false)
  const [joinLoading, setJoinLoading]     = useState(null)
  const [passwordPromptGroup, setPasswordPromptGroup] = useState(null)
  const [groupPassword, setGroupPassword] = useState('')
  const [passwordError, setPasswordError] = useState('')
  const [passwordLoading, setPasswordLoading] = useState(false)
  const [showPasswordText, setShowPasswordText] = useState(false)

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const [coursesRes, groupsRes, myGroupsRes] = await Promise.all([
          api.get('/courses'),
          api.get('/groups'),
          api.get('/groups/my').catch(() => ({ data: [] })),
        ])

        setCourses(coursesRes.data)
        setGroups(groupsRes.data)
        setJoinedGroupIds(new Set(myGroupsRes.data.map(g => g.id)))
      } catch (err) {
        console.error('Failed to load study groups:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchAll()
  }, [])

  const coursesWithGroups = courses.filter(c => groups.some(g => g.course?.id === c.id))

  const filteredGroups = groups.filter(g => {
    const matchesCourse = activeFilter === 'all' || g.course?.id === Number(activeFilter)
    const matchesSearch =
      g.name?.toLowerCase().includes(search.toLowerCase()) ||
      g.course?.courseName?.toLowerCase().includes(search.toLowerCase()) ||
      g.course?.courseCode?.toLowerCase().includes(search.toLowerCase())
    return matchesCourse && matchesSearch
  })

  const handleJoin = async (group) => {
    if (joinedGroupIds.has(group.id)) return
    setJoinLoading(group.id)
    try {
      await api.post(`/groups/${group.id}/join`, {})
      setJoinedGroupIds(prev => new Set([...prev, group.id]))
      refreshNotifications()
    } catch (err) {
      if (err.response?.status === 403) {
        setPasswordPromptGroup(group)
        setGroupPassword('')
        setPasswordError('')
        setShowPasswordText(false)
      } else {
        alert(err.response?.data?.message || 'Failed to join group.')
      }
    } finally {
      setJoinLoading(null)
    }
  }

  const handleJoinWithPassword = async (e) => {
    e.preventDefault()
    setPasswordLoading(true)
    setPasswordError('')
    try {
      await api.post(`/groups/${passwordPromptGroup.id}/join`, { password: groupPassword })
      setJoinedGroupIds(prev => new Set([...prev, passwordPromptGroup.id]))
      refreshNotifications()
      setPasswordPromptGroup(null)
      setGroupPassword('')
    } catch (err) {
      if (err.response?.status === 403) {
        setPasswordError('Incorrect password. Please try again.')
      } else {
        setPasswordError(err.response?.data?.message || 'Failed to join group.')
      }
    } finally {
      setPasswordLoading(false)
    }
  }

  const closePasswordPrompt = () => {
    setPasswordPromptGroup(null)
    setGroupPassword('')
    setPasswordError('')
    setShowPasswordText(false)
  }

  const handleLeave = async (group) => {
    setJoinLoading(group.id)
    try {
      await api.delete(`/groups/${group.id}/leave`)
      setJoinedGroupIds(prev => { const next = new Set(prev); next.delete(group.id); return next })
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to leave group.')
    } finally {
      setJoinLoading(null)
    }
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    setCreateLoading(true)
    setCreateError('')
    try {
      const res = await api.post('/groups', {
        name: createForm.name,
        courseId: Number(createForm.courseId),
        password: createForm.password || null,
      })
      const newGroup = res.data
      setGroups(prev => [...prev, newGroup])
      setJoinedGroupIds(prev => new Set([...prev, newGroup.id]))
      setShowCreateModal(false)
      setCreateForm({ name: '', courseId: '', password: '' })
      setCreateShowPassword(false)
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Failed to create group.')
    } finally {
      setCreateLoading(false)
    }
  }

  return (
    <div
      className="flex flex-col min-h-screen bg-cover bg-center bg-fixed transition-colors duration-300"
      style={{ backgroundImage: `url(${campusPhoto})` }}
    >
      <AppHeader />

      <main className="flex-1 pt-16 pb-16">

        {/* ── Hero Banner ── */}
        <div className="relative bg-gradient-to-br from-wsu-navy/75 via-blue-900/75 to-blue-800/75 text-white overflow-hidden">
          <div className="relative max-w-5xl mx-auto px-6 py-16">
            <div className="flex flex-col md:flex-row items-end justify-between gap-6">
              <div>
                <h1 className="font-display text-3xl md:text-4xl font-bold leading-tight">Study Groups</h1>
                <p className="text-blue-200 mt-1 text-sm">Find a group for your courses or start your own.</p>
              </div>
              <div className="bg-white/10 border border-white/20 backdrop-blur-sm rounded-2xl px-6 py-4 min-w-[200px] text-center flex-shrink-0">
                <p className="text-xs text-blue-200 font-semibold uppercase tracking-wider mb-1">Ready to study?</p>
                <p className="font-display text-4xl font-bold text-white">+</p>
                <p className="text-xs mt-1 invisible">placeholder</p>
                <button
                  onClick={() => setShowCreateModal(true)}
                  className="inline-block mt-2 bg-white/20 hover:bg-white/30 text-white text-xs font-semibold px-3 py-1 rounded-full transition-all duration-200"
                >
                  Create New Group
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="max-w-5xl mx-auto px-6 pt-8 pb-0">

          {/* Search */}
          <div className="relative mb-3">
            <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Search by group name or course..."
              className="form-input !pl-12"
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>

          {/* Course Filter Tabs */}
          {!loading && coursesWithGroups.length > 0 && (
            <div className="flex flex-wrap gap-2 mb-4">
              <button
                onClick={() => setActiveFilter('all')}
                className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-200 ${
                  activeFilter === 'all'
                    ? 'bg-blue-700 text-white shadow-sm'
                    : 'bg-white dark:bg-gray-800 text-wsu-slate dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:bg-wsu-mist dark:hover:bg-gray-700'
                }`}
              >
                All
              </button>
              {coursesWithGroups.map(c => (
                <button
                  key={c.id}
                  onClick={() => setActiveFilter(String(c.id))}
                  className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-200 ${
                    activeFilter === String(c.id)
                      ? 'bg-blue-700 text-white shadow-sm'
                      : 'bg-white dark:bg-gray-800 text-wsu-slate dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:bg-wsu-mist dark:hover:bg-gray-700'
                  }`}
                >
                  {c.courseCode}
                </button>
              ))}
            </div>
          )}

          {/* Groups Grid */}
          {loading ? (
            <div className="flex justify-center py-24">
              <div className="animate-spin w-8 h-8 border-4 border-blue-700 border-t-transparent rounded-full" />
            </div>
          ) : filteredGroups.length > 0 ? (
            <>
              <p className="text-sm text-white/80 mb-6">
                Showing <span className="font-semibold text-white">{filteredGroups.length}</span> group{filteredGroups.length !== 1 ? 's' : ''}
              </p>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredGroups.map(group => (
                  <StudyGroupCard
                    key={group.id}
                    group={group}
                    joined={joinedGroupIds.has(group.id)}
                    joinLoading={joinLoading === group.id}
                    onJoin={handleJoin}
                    onLeave={handleLeave}
                    onViewDetails={setSelectedGroup}
                  />
                ))}
              </div>
            </>
          ) : (
            <div className="text-center py-24">
              <h3 className="font-display text-2xl text-wsu-navy dark:text-white mb-2">No groups found</h3>
              <p className="text-wsu-slate dark:text-gray-400 mb-6">
                {groups.length === 0
                  ? 'No study groups exist yet. Be the first to create one!'
                  : 'Try a different search or create your own group.'}
              </p>
              <button
                onClick={() => setShowCreateModal(true)}
                className="bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200"
              >
                + Create New Group
              </button>
            </div>
          )}
        </div>
      </main>

      {/* View Details Modal */}
      {selectedGroup && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl max-w-lg w-full p-8 animate-fade-up">
            <div className="flex items-center justify-between mb-6">
              <span className="bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 text-xs font-semibold px-3 py-1 rounded-full">
                {selectedGroup.course?.courseCode}
              </span>
              <button onClick={() => setSelectedGroup(null)} className="text-gray-400 hover:text-wsu-navy dark:hover:text-white text-2xl leading-none">×</button>
            </div>
            <h2 className="font-display text-2xl text-wsu-navy dark:text-white mb-2">{selectedGroup.name}</h2>
            <p className="text-wsu-slate dark:text-gray-400 text-sm mb-6">{selectedGroup.course?.courseName}</p>
            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="bg-wsu-chalk dark:bg-gray-800 rounded-xl p-4">
                <div className="text-xs text-wsu-slate dark:text-gray-400 font-medium mb-0.5">Course</div>
                <div className="text-sm text-wsu-navy dark:text-white font-semibold">{selectedGroup.course?.courseName}</div>
              </div>
              <div className="bg-wsu-chalk dark:bg-gray-800 rounded-xl p-4">
                <div className="text-xs text-wsu-slate dark:text-gray-400 font-medium mb-0.5">Members</div>
                <div className="text-sm text-wsu-navy dark:text-white font-semibold">{selectedGroup.members?.length ?? 0}</div>
              </div>
            </div>
            <div className="mb-6">
              <p className="text-xs font-semibold text-wsu-slate dark:text-gray-400 uppercase tracking-widest mb-3">Members</p>
              <div className="flex flex-wrap gap-2">
                {(selectedGroup.members ?? []).map((m, i) => (
                  <span key={i} className="bg-wsu-mist dark:bg-gray-800 text-wsu-navy dark:text-gray-200 text-xs font-medium px-3 py-1 rounded-full">{m.name}</span>
                ))}
              </div>
            </div>
            <div className="flex gap-3">
              <button onClick={() => setSelectedGroup(null)} className="btn-secondary flex-1 py-3">Close</button>
              {joinedGroupIds.has(selectedGroup.id) ? (
                <button
                  onClick={() => { handleLeave(selectedGroup); setSelectedGroup(null) }}
                  className="flex-1 py-3 bg-red-500 hover:bg-red-600 text-white font-semibold rounded-lg transition-all"
                >
                  Leave Group
                </button>
              ) : (
                <button
                  onClick={() => { handleJoin(selectedGroup); setSelectedGroup(null) }}
                  className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white font-semibold rounded-lg transition-all"
                >
                  Join Group
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Password Prompt Modal */}
      {passwordPromptGroup && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl max-w-md w-full p-8 animate-fade-up">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center flex-shrink-0">
                  <svg className="w-5 h-5 text-blue-700 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <h2 className="font-display text-xl text-wsu-navy dark:text-white">Password Required</h2>
              </div>
              <button onClick={closePasswordPrompt} className="text-gray-400 hover:text-wsu-navy dark:hover:text-white text-2xl leading-none">×</button>
            </div>

            <p className="text-sm text-wsu-slate dark:text-gray-400 mb-6">
              <span className="font-semibold text-wsu-navy dark:text-white">{passwordPromptGroup.name}</span> is a password-protected group. Enter the password to join.
            </p>

            {passwordError && (
              <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">
                {passwordError}
              </div>
            )}

            <form onSubmit={handleJoinWithPassword} className="space-y-4">
              <div>
                <label className="form-label">Group Password</label>
                <div className="relative">
                  <input
                    type={showPasswordText ? 'text' : 'password'}
                    required
                    autoFocus
                    placeholder="Enter group password"
                    className="form-input !pr-12"
                    value={groupPassword}
                    onChange={e => setGroupPassword(e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPasswordText(p => !p)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-wsu-navy dark:hover:text-white"
                  >
                    {showPasswordText ? (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 4.411m0 0L21 21" />
                      </svg>
                    ) : (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    )}
                  </button>
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={closePasswordPrompt} className="btn-secondary flex-1 py-3">Cancel</button>
                <button
                  type="submit"
                  disabled={passwordLoading || !groupPassword}
                  className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white font-semibold rounded-lg transition-all disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {passwordLoading ? 'Joining...' : 'Join Group'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Create Group Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl max-w-lg w-full p-8 animate-fade-up">
            <div className="flex items-center justify-between mb-6">
              <h2 className="font-display text-2xl text-wsu-navy dark:text-white">Create a Study Group</h2>
              <button
                onClick={() => { setShowCreateModal(false); setCreateError(''); setCreateShowPassword(false) }}
                className="text-gray-400 hover:text-wsu-navy dark:hover:text-white text-2xl leading-none"
              >
                ×
              </button>
            </div>

            {createError && (
              <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">
                {createError}
              </div>
            )}

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="form-label">Group Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. CAIS 0236 Study Crew"
                  className="form-input"
                  value={createForm.name}
                  onChange={e => setCreateForm(p => ({ ...p, name: e.target.value }))}
                />
              </div>
              <div>
                <label className="form-label">Course</label>
                <select
                  required
                  className="form-input"
                  value={createForm.courseId}
                  onChange={e => setCreateForm(p => ({ ...p, courseId: e.target.value }))}
                >
                  <option value="">Select a course</option>
                  {courses.map(c => (
                    <option key={c.id} value={c.id}>{c.courseCode} — {c.courseName}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="form-label">
                  Password
                  <span className="ml-1 text-wsu-slate dark:text-gray-400 font-normal text-xs">(optional)</span>
                </label>
                <div className="relative">
                  <input
                    type={createShowPassword ? 'text' : 'password'}
                    placeholder="Leave blank for an open group"
                    className="form-input !pr-12"
                    value={createForm.password}
                    onChange={e => setCreateForm(p => ({ ...p, password: e.target.value }))}
                  />
                  {createForm.password && (
                    <button
                      type="button"
                      onClick={() => setCreateShowPassword(p => !p)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-wsu-navy dark:hover:text-white"
                    >
                      {createShowPassword ? (
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 4.411m0 0L21 21" />
                        </svg>
                      ) : (
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                        </svg>
                      )}
                    </button>
                  )}
                </div>
                <p className="text-xs text-wsu-slate dark:text-gray-400 mt-1">Members will need this password to join.</p>
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => { setShowCreateModal(false); setCreateError(''); setCreateShowPassword(false) }}
                  className="btn-secondary flex-1 py-3"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createLoading}
                  className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white font-semibold rounded-lg transition-all disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {createLoading ? 'Creating...' : 'Create Group'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  )
}

export default StudyGroups
