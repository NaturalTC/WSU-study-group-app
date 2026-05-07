/**
 * /meetings page — shows the logged-in student's upcoming sessions across every group
 * they belong to, grouped into Today / Tomorrow / This Week / Later. The session creator
 * sees three actions per row: Edit details, Reschedule, and Cancel; everyone else only
 * sees the "Open chat" link. Schedule and edit flows both reuse MeetingScheduleModal —
 * the mode prop drives which fields are shown.
 */
import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import AppHeader from '../components/AppHeader'
import MeetingScheduleModal from '../components/MeetingScheduleModal'
import { useAuth } from '../context/AuthContext'
import { useMeetings } from '../context/MeetingsContext'
import api from '../api/axios'
import campusPhoto from '../assets/CampusStock_Apr2023_025-X4.jpg'

// "Tue, May 5, 2026 · 03:00 PM – 04:00 PM" when duration is set,
// "Tue, May 5, 2026 · 03:00 PM" when it isn't.
function formatLong(isoStr, durationMinutes) {
  const d = new Date(isoStr)
  const date = d.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })
  const time = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  if (durationMinutes && Number.isFinite(durationMinutes)) {
    const end = new Date(d.getTime() + durationMinutes * 60_000)
    const endTime = end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    return `${date} · ${time} – ${endTime}`
  }
  return `${date} · ${time}`
}

// "45 min", "1 hr", "1 hr 30 min". Returns null for missing/invalid input so the caller
// can render nothing instead of "0 min" or "NaN min".
function formatDuration(minutes) {
  if (!minutes || !Number.isFinite(minutes)) return null
  if (minutes < 60) return `${minutes} min`
  const hrs = Math.floor(minutes / 60)
  const rem = minutes % 60
  return rem === 0 ? `${hrs} hr` : `${hrs} hr ${rem} min`
}

// Maps a session's scheduledAt to one of four labels driving the section headers.
// Uses local-midnight boundaries so a 1 AM meeting still counts as "Today" and an
// 11 PM meeting today doesn't slip into "Tomorrow".
function relativeBucket(isoStr) {
  const d = new Date(isoStr)
  const now = new Date()
  const today = new Date(now); today.setHours(0, 0, 0, 0)
  const tom   = new Date(today); tom.setDate(tom.getDate() + 1)
  const week  = new Date(today); week.setDate(week.getDate() + 7)

  if (d < tom)  return 'Today'
  if (d < new Date(tom.getTime() + 86400000)) return 'Tomorrow'
  if (d < week) return 'This Week'
  return 'Later'
}

function Meetings() {
  const { profile } = useAuth()
  const { upcoming, loadingUpcoming, fetchUpcoming, cancelSession } = useMeetings()

  const [groups, setGroups]         = useState([])
  const [groupsLoading, setGroupsLoading] = useState(true)
  const [showModal, setShowModal]   = useState(false)
  // editing = { session, mode } where mode is 'reschedule' or 'details', or null when not editing
  const [editing,   setEditing]     = useState(null)
  const [cancelError, setCancelError] = useState(null)

  useEffect(() => {
    fetchUpcoming()
  }, [fetchUpcoming])

  useEffect(() => {
    setGroupsLoading(true)
    api.get('/groups/my')
      .then(res => setGroups(Array.isArray(res.data) ? res.data : []))
      .catch(() => setGroups([]))
      .finally(() => setGroupsLoading(false))
  }, [])

  const buckets = useMemo(() => {
    const order = ['Today', 'Tomorrow', 'This Week', 'Later']
    const map = Object.fromEntries(order.map(k => [k, []]))
    for (const s of upcoming) map[relativeBucket(s.scheduledAt)].push(s)
    return order.map(label => ({ label, sessions: map[label] }))
                .filter(b => b.sessions.length > 0)
  }, [upcoming])

  const handleCancel = async (session) => {
    setCancelError(null)
    if (!window.confirm('Cancel this meeting? All members will lose this on their list.')) return
    try {
      await cancelSession(session.id, session.studyGroup?.id)
    } catch (err) {
      const data = err?.response?.data ?? {}
      const status = err?.response?.status
      const isNetwork = !err?.response
      const ref = `LOCAL-${Date.now().toString(36).toUpperCase()}`
      console.error(`[${data.reference ?? ref}]`, err)
      setCancelError({
        message:   data.message ?? (isNetwork ? 'Network error — check your connection and try again.' : 'Failed to cancel meeting.'),
        code:      data.code ?? (isNetwork ? 'NETWORK' : status ? `HTTP_${status}` : 'UNKNOWN'),
        reference: data.reference ?? ref,
      })
    }
  }

  const isOwner = (session) => session.scheduledBy?.id === profile?.id

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
            <div className="flex flex-col md:flex-row items-end justify-between gap-6">
              <div>
                <h1 className="font-display text-3xl md:text-4xl font-bold leading-tight">Meetings</h1>
                <p className="text-blue-200 mt-1 text-sm">Upcoming study sessions across all your groups.</p>
              </div>
              <div className="bg-white/10 border border-white/20 backdrop-blur-sm rounded-2xl px-6 py-4 min-w-[200px] text-center flex-shrink-0">
                <p className="text-xs text-blue-200 font-semibold uppercase tracking-wider mb-1">Ready to meet?</p>
                <p className="font-display text-4xl font-bold text-white">📅</p>
                <p className="text-xs mt-1 invisible">placeholder</p>
                <button
                  onClick={() => setShowModal(true)}
                  disabled={groupsLoading || groups.length === 0}
                  className="inline-block mt-2 bg-white/20 hover:bg-white/30 text-white text-xs font-semibold px-3 py-1 rounded-full transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Schedule Meeting
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="max-w-5xl mx-auto px-6 pt-8 pb-12">

        {cancelError && (
          <div className="mb-4 p-3 rounded-xl bg-red-50 dark:bg-red-900/20">
            <p className="text-sm text-red-700 dark:text-red-400">{cancelError.message}</p>
            <p className="mt-1 font-mono text-[10px] text-red-500/80 dark:text-red-400/70 select-all">
              code: {cancelError.code}{cancelError.reference ? ` · ref: ${cancelError.reference}` : ''}
            </p>
          </div>
        )}

        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 p-6 md:p-8">
          {loadingUpcoming ? (
            <div className="flex justify-center py-16">
              <div className="animate-spin w-8 h-8 border-4 border-blue-700 border-t-transparent rounded-full" />
            </div>
          ) : upcoming.length === 0 ? (
            <div className="text-center py-16">
              <div className="text-5xl mb-3">📅</div>
              <p className="font-semibold text-wsu-navy dark:text-white text-lg">No upcoming meetings</p>
              <p className="text-sm text-wsu-slate dark:text-gray-400 mt-1">
                {groups.length === 0
                  ? 'Join a study group to start scheduling.'
                  : 'Schedule a meeting to see it here.'}
              </p>
              {groups.length === 0 && (
                <Link
                  to="/study-groups"
                  className="inline-block mt-4 text-sm text-blue-700 dark:text-blue-400 font-semibold hover:underline"
                >
                  Browse study groups →
                </Link>
              )}
            </div>
          ) : (
            <div className="space-y-8">
              {buckets.map(bucket => (
                <section key={bucket.label}>
                  <h2 className="text-xs font-bold uppercase tracking-wider text-wsu-slate dark:text-gray-400 mb-3">
                    {bucket.label}
                  </h2>
                  <ul className="space-y-3">
                    {bucket.sessions.map(s => (
                      <li
                        key={s.id}
                        className="flex items-start gap-4 p-4 rounded-xl border border-gray-100 dark:border-gray-800 bg-wsu-mist/40 dark:bg-gray-800/40 hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                      >
                        <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900/40 rounded-xl flex items-center justify-center flex-shrink-0 mt-0.5">
                          <span className="text-lg">📅</span>
                        </div>

                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <p className="text-sm font-semibold text-wsu-navy dark:text-white">
                              {s.studyGroup?.name ?? 'Study Session'}
                            </p>
                            {s.studyGroup?.course?.courseCode && (
                              <span className="text-xs px-2 py-0.5 rounded-full bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 font-medium">
                                {s.studyGroup.course.courseCode}
                              </span>
                            )}
                          </div>
                          <p className="text-xs text-blue-600 dark:text-blue-400 font-medium mt-0.5">
                            {formatLong(s.scheduledAt, s.durationMinutes)}
                            {s.durationMinutes && (
                              <span className="text-wsu-slate dark:text-gray-400 font-normal ml-2">
                                · {formatDuration(s.durationMinutes)}
                              </span>
                            )}
                          </p>
                          {s.location && (
                            <p className="text-xs text-wsu-slate dark:text-gray-400 mt-1">
                              📍 {s.location}
                            </p>
                          )}
                          {s.notes && (
                            <p className="text-xs text-wsu-slate dark:text-gray-400 mt-1 line-clamp-2">
                              {s.notes}
                            </p>
                          )}
                          <p className="text-xs text-wsu-slate dark:text-gray-500 mt-1">
                            Scheduled by {s.scheduledBy?.name ?? 'someone'}
                          </p>
                        </div>

                        <div className="flex flex-col items-end gap-2 flex-shrink-0">
                          {s.studyGroup?.id && (
                            <Link
                              to={`/group-chat/${s.studyGroup.id}`}
                              className="text-xs px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 text-wsu-navy dark:text-gray-200 font-semibold hover:bg-white dark:hover:bg-gray-700 transition-colors"
                            >
                              Open chat
                            </Link>
                          )}
                          {isOwner(s) && (
                            <>
                              <button
                                onClick={() => setEditing({ session: s, mode: 'details' })}
                                className="text-xs px-3 py-1.5 rounded-lg text-wsu-navy dark:text-gray-200 hover:bg-wsu-mist dark:hover:bg-gray-700 font-semibold transition-colors"
                              >
                                Edit details
                              </button>
                              <button
                                onClick={() => setEditing({ session: s, mode: 'reschedule' })}
                                className="text-xs px-3 py-1.5 rounded-lg text-blue-700 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 font-semibold transition-colors"
                              >
                                Reschedule
                              </button>
                              <button
                                onClick={() => handleCancel(s)}
                                className="text-xs px-3 py-1.5 rounded-lg text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 font-semibold transition-colors"
                              >
                                Cancel
                              </button>
                            </>
                          )}
                        </div>
                      </li>
                    ))}
                  </ul>
                </section>
              ))}
            </div>
          )}
        </div>
        </div>
      </main>

      {showModal && (
        <MeetingScheduleModal
          groups={groups}
          onClose={() => setShowModal(false)}
          onScheduled={fetchUpcoming}
        />
      )}

      {editing && (
        <MeetingScheduleModal
          groups={groups}
          editingSession={editing.session}
          mode={editing.mode}
          onClose={() => setEditing(null)}
          onScheduled={fetchUpcoming}
        />
      )}
    </div>
  )
}

export default Meetings
