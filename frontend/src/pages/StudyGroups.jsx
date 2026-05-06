import { useState, useEffect } from 'react'
import AppHeader from '../components/AppHeader'
import StudyGroupCard from '../components/StudyGroupCard'
import api from '../api/axios'
import campusPhoto from '../assets/WSUCampusStock2013_033-L.jpg'
import { useNotifications } from '../context/NotificationContext'
import { useAuth } from '../context/AuthContext'

function StudyGroups() {
  const { refresh: refreshNotifications } = useNotifications()
  const { profile, refreshProfile } = useAuth()
  const [groups, setGroups]               = useState([])
  const [courses, setCourses]             = useState([])
  const [joinedGroupIds, setJoinedGroupIds] = useState(new Set())
  const [loading, setLoading]             = useState(true)
  const [search, setSearch]               = useState('')
  const [activeFilter, setActiveFilter]   = useState('all')
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [selectedGroup, setSelectedGroup] = useState(null)
  const [createForm, setCreateForm]       = useState({ name: '', courseId: '' })
  const [createLoading, setCreateLoading] = useState(false)
  const [createError, setCreateError]     = useState('')
  const [joinLoading, setJoinLoading]     = useState(null)
  const [picLoading, setPicLoading]       = useState(null) // holds groupId while uploading
  const [picError, setPicError]           = useState('')

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
      await api.post(`/groups/${group.id}/join`)
      setJoinedGroupIds(prev => new Set([...prev, group.id]))
      refreshNotifications()
      refreshProfile()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to join group.')
    } finally {
      setJoinLoading(null)
    }
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

  const handleDelete = async (group) => {
    if (!window.confirm(`Delete "${group.name}"? This cannot be undone.`)) return
    try {
      await api.delete(`/groups/${group.id}`)
      setGroups(prev => prev.filter(g => g.id !== group.id))
      setJoinedGroupIds(prev => { const next = new Set(prev); next.delete(group.id); return next })
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete group.')
    }
  }

  const handleGroupPicUpload = async (group, file) => {
    if (!file) return
    setPicLoading(group.id)
    setPicError('')
    try {
      const form = new FormData()
      form.append('file', file)
      await api.post(`/groups/${group.id}/picture`, form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      // Re-fetch all groups so every card gets the fresh URL
      const res = await api.get('/groups')
      setGroups(res.data)
      if (selectedGroup?.id === group.id) {
        setSelectedGroup(res.data.find(g => g.id === group.id) ?? null)
      }
    } catch (err) {
      setPicError(err.response?.data?.message || 'Failed to upload picture.')
    } finally {
      setPicLoading(null)
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
      })
      const newGroup = res.data
      setGroups(prev => [...prev, newGroup])
      setJoinedGroupIds(prev => new Set([...prev, newGroup.id]))
      refreshProfile()
      setShowCreateModal(false)
      setCreateForm({ name: '', courseId: '' })
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
                    picLoading={picLoading === group.id}
                    onJoin={handleJoin}
                    onLeave={handleLeave}
                    onDelete={handleDelete}
                    onUploadPic={handleGroupPicUpload}
                    isCreator={profile?.id === group.createdBy?.id}
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
          <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden animate-fade-up">

            {/* Group picture header */}
            <div className="relative h-40 bg-gradient-to-br from-wsu-navy to-blue-800 flex-shrink-0">
              {selectedGroup.groupPicURL ? (
                <img src={selectedGroup.groupPicURL} alt={selectedGroup.name} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center">
                  <span className="text-6xl font-display font-black text-white/20 select-none">
                    {selectedGroup.course?.courseCode?.split(' ')[0]}
                  </span>
                </div>
              )}
              {/* Camera upload — creator only */}
              {profile?.id === selectedGroup.createdBy?.id && (
                <label className="absolute bottom-3 right-3 cursor-pointer">
                  <input
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={e => { const f = e.target.files?.[0]; if (f) handleGroupPicUpload(selectedGroup, f); e.target.value = '' }}
                    disabled={picLoading === selectedGroup.id}
                  />
                  <div className="w-9 h-9 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center text-white hover:bg-black/60 transition-all">
                    {picLoading === selectedGroup.id ? (
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                    )}
                  </div>
                </label>
              )}
              <button onClick={() => { setSelectedGroup(null); setPicError('') }} className="absolute top-3 right-3 w-8 h-8 rounded-full bg-black/40 backdrop-blur-sm text-white flex items-center justify-center hover:bg-black/60 transition-all text-lg leading-none">×</button>
            </div>

            <div className="p-8">
              {picError && (
                <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-2 mb-4">{picError}</div>
              )}
              <div className="flex items-center justify-between mb-4">
                <span className="bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 text-xs font-semibold px-3 py-1 rounded-full">
                  {selectedGroup.course?.courseCode}
                </span>
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
                  <div key={i} className="flex items-center gap-1.5 bg-wsu-mist dark:bg-gray-800 text-wsu-navy dark:text-gray-200 text-xs font-medium px-2 py-1 rounded-full">
                    {m.profilePicURL ? (
                      <img src={m.profilePicURL} alt={m.name} className="w-5 h-5 rounded-full object-cover" />
                    ) : (
                      <div className="w-5 h-5 rounded-full bg-wsu-navy dark:bg-blue-800 text-white flex items-center justify-center text-[10px] font-bold">
                        {m.name?.charAt(0)?.toUpperCase() ?? '?'}
                      </div>
                    )}
                    {m.name}
                  </div>
                ))}
              </div>
            </div>
            <div className="flex gap-3">
              <button onClick={() => { setSelectedGroup(null); setPicError('') }} className="btn-secondary flex-1 py-3">Close</button>
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
            </div>{/* end p-8 */}
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
                onClick={() => { setShowCreateModal(false); setCreateError('') }}
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
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => { setShowCreateModal(false); setCreateError('') }}
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
