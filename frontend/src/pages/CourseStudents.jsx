import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import AppHeader from '../components/AppHeader'
import api from '../api/axios'

function initials(name = '') {
  const parts = name.trim().split(' ')
  return parts.length >= 2
    ? `${parts[0].charAt(0)}${parts[parts.length - 1].charAt(0)}`
    : name.charAt(0)?.toUpperCase() ?? '?'
}

function FriendButton({ student, onAction }) {
  const [loading, setLoading] = useState(false)

  const handle = async (action) => {
    setLoading(true)
    try {
      await onAction(student, action)
    } finally {
      setLoading(false)
    }
  }

  const { friendshipStatus, friendshipDirection, friendshipId, profileId } = student

  if (friendshipStatus === 'ACCEPTED') {
    return (
      <button
        disabled={loading}
        onClick={() => handle('remove')}
        className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 border border-green-200 dark:border-green-800 hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-600 dark:hover:text-red-400 hover:border-red-200 dark:hover:border-red-800 transition-all group"
      >
        <span className="group-hover:hidden">Friends</span>
        <span className="hidden group-hover:inline">Remove</span>
      </button>
    )
  }

  if (friendshipStatus === 'PENDING' && friendshipDirection === 'SENT') {
    return (
      <button
        disabled={loading}
        onClick={() => handle('cancel')}
        className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-700 hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-500 dark:hover:text-red-400 hover:border-red-200 dark:hover:border-red-800 transition-all group"
      >
        <span className="group-hover:hidden">Pending</span>
        <span className="hidden group-hover:inline">Cancel</span>
      </button>
    )
  }

  if (friendshipStatus === 'PENDING' && friendshipDirection === 'RECEIVED') {
    return (
      <div className="flex gap-1.5">
        <button
          disabled={loading}
          onClick={() => handle('accept')}
          className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-blue-700 text-white hover:bg-blue-800 transition-colors disabled:opacity-60"
        >
          Accept
        </button>
        <button
          disabled={loading}
          onClick={() => handle('decline')}
          className="text-xs font-semibold px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 text-wsu-slate dark:text-gray-300 hover:bg-wsu-mist dark:hover:bg-gray-700 transition-colors disabled:opacity-60"
        >
          Decline
        </button>
      </div>
    )
  }

  // No existing relationship
  return (
    <button
      disabled={loading}
      onClick={() => handle('add')}
      className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-blue-700 hover:bg-blue-800 text-white transition-colors disabled:opacity-60"
    >
      {loading ? '...' : '+ Add Friend'}
    </button>
  )
}

function CourseStudents() {
  const { courseId } = useParams()
  const navigate     = useNavigate()

  const [students, setStudents]       = useState([])
  const [courseName, setCourseName]   = useState('')
  const [courseCode, setCourseCode]   = useState('')
  const [sections, setSections]       = useState([])
  const [activeSection, setActiveSection] = useState('all')
  const [loading, setLoading]         = useState(true)
  const [error, setError]             = useState('')

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      setError('')
      try {
        const [studentsRes, coursesRes] = await Promise.all([
          api.get(`/courses/${courseId}/students`),
          api.get('/courses'),
        ])

        const course = coursesRes.data.find(c => c.id === Number(courseId))
        if (course) {
          setCourseName(course.courseName)
          setCourseCode(course.courseCode)
        }

        setStudents(studentsRes.data)

        const uniqueSections = [...new Set(studentsRes.data.map(s => s.section))].sort()
        setSections(uniqueSections)
      } catch (err) {
        setError('Failed to load students.')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [courseId])

  const visible = activeSection === 'all'
    ? students
    : students.filter(s => s.section === activeSection)

  const handleFriendAction = async (student, action) => {
    if (action === 'add') {
      const res = await api.post(`/friends/request/${student.profileId}`)
      setStudents(prev => prev.map(s =>
        s.profileId === student.profileId
          ? { ...s, friendshipStatus: 'PENDING', friendshipDirection: 'SENT', friendshipId: res.data.friendshipId }
          : s
      ))
    } else if (action === 'cancel' || action === 'remove') {
      await api.delete(`/friends/${student.friendshipId}`)
      setStudents(prev => prev.map(s =>
        s.profileId === student.profileId
          ? { ...s, friendshipStatus: null, friendshipDirection: null, friendshipId: null }
          : s
      ))
    } else if (action === 'accept') {
      const res = await api.patch(`/friends/${student.friendshipId}/accept`)
      setStudents(prev => prev.map(s =>
        s.profileId === student.profileId
          ? { ...s, friendshipStatus: 'ACCEPTED', friendshipDirection: null, friendshipId: res.data.friendshipId }
          : s
      ))
    } else if (action === 'decline') {
      await api.patch(`/friends/${student.friendshipId}/decline`)
      setStudents(prev => prev.map(s =>
        s.profileId === student.profileId
          ? { ...s, friendshipStatus: null, friendshipDirection: null, friendshipId: null }
          : s
      ))
    }
  }

  return (
    <div className="flex flex-col min-h-screen bg-wsu-chalk dark:bg-gray-900 transition-colors duration-300">
      <AppHeader />

      <main className="flex-1 pt-16">

        {/* Hero */}
        <div className="bg-white dark:bg-gray-800 border-b border-gray-100 dark:border-gray-700">
          <div className="max-w-4xl mx-auto px-6 py-8">
            <button
              onClick={() => navigate(-1)}
              className="flex items-center gap-1.5 text-sm text-wsu-slate dark:text-gray-400 hover:text-wsu-navy dark:hover:text-white transition-colors mb-4"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
              Back
            </button>
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-2xl bg-blue-700 flex items-center justify-center text-white font-display font-bold text-lg flex-shrink-0">
                {courseCode?.split(' ')[0]?.charAt(0) ?? 'C'}
              </div>
              <div>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-xs font-semibold bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 px-2.5 py-1 rounded-full">
                    {courseCode}
                  </span>
                </div>
                <h1 className="font-display text-2xl text-wsu-navy dark:text-white font-bold mt-1">
                  {courseName || 'Course Roster'}
                </h1>
                {!loading && (
                  <p className="text-sm text-wsu-slate dark:text-gray-400 mt-0.5">
                    {students.length} student{students.length !== 1 ? 's' : ''} enrolled
                    {activeSection !== 'all' && ` · ${visible.length} in section ${activeSection}`}
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="max-w-4xl mx-auto px-6 py-6">

          {/* Section filter */}
          {!loading && sections.length > 0 && (
            <div className="flex flex-wrap items-center gap-2 mb-5">
              <span className="text-xs font-semibold text-wsu-slate dark:text-gray-400 uppercase tracking-wider mr-1">
                Section:
              </span>
              <button
                onClick={() => setActiveSection('all')}
                className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-all duration-200 ${
                  activeSection === 'all'
                    ? 'bg-blue-700 text-white shadow-sm'
                    : 'bg-white dark:bg-gray-800 text-wsu-slate dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:bg-wsu-mist dark:hover:bg-gray-700'
                }`}
              >
                All sections
              </button>
              {sections.map(sec => (
                <button
                  key={sec}
                  onClick={() => setActiveSection(sec)}
                  className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-all duration-200 ${
                    activeSection === sec
                      ? 'bg-blue-700 text-white shadow-sm'
                      : 'bg-white dark:bg-gray-800 text-wsu-slate dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:bg-wsu-mist dark:hover:bg-gray-700'
                  }`}
                >
                  {sec}
                </button>
              ))}
            </div>
          )}

          {/* Content */}
          {loading ? (
            <div className="flex justify-center py-24">
              <div className="animate-spin w-8 h-8 border-4 border-blue-700 border-t-transparent rounded-full" />
            </div>
          ) : error ? (
            <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 rounded-xl px-4 py-3 text-sm">
              {error}
            </div>
          ) : visible.length === 0 ? (
            <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm px-6 py-14 text-center">
              <p className="text-wsu-navy dark:text-white font-semibold mb-1">No students found</p>
              <p className="text-wsu-slate dark:text-gray-400 text-sm">
                {activeSection !== 'all'
                  ? `No one else is in section ${activeSection}.`
                  : 'No other students are enrolled in this course.'}
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {visible.map(student => (
                <div
                  key={`${student.profileId}-${student.section}`}
                  className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-5 flex flex-col gap-3 hover:shadow-md transition-shadow duration-200"
                >
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-xl bg-blue-700 flex items-center justify-center text-white font-display font-bold text-sm flex-shrink-0">
                      {initials(student.name)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-wsu-navy dark:text-white text-sm leading-snug truncate">
                        {student.name}
                      </p>
                      {student.major && (
                        <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5 truncate">
                          {student.major}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-2 flex-wrap">
                    {student.year && (
                      <span className="text-xs font-semibold bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 px-2 py-0.5 rounded-full">
                        {student.year}
                      </span>
                    )}
                    <span className="text-xs font-semibold bg-gray-50 dark:bg-gray-700 text-wsu-slate dark:text-gray-300 px-2 py-0.5 rounded-full">
                      Sec {student.section}
                    </span>
                  </div>

                  <div className="mt-auto pt-3 border-t border-gray-50 dark:border-gray-700 flex justify-end">
                    <FriendButton student={student} onAction={handleFriendAction} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  )
}

export default CourseStudents
