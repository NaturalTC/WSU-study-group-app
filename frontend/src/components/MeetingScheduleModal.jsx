import { useState } from 'react'
import { useMeetings } from '../context/MeetingsContext'

const DURATION_OPTIONS = [15, 30, 45, 60, 75, 90, 120, 150, 180]

function defaultDatetime() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  d.setHours(12, 0, 0, 0)
  return d.toISOString().slice(0, 16)
}

// Converts an ISO/LocalDateTime string into the value format that <input type="datetime-local">
// expects (YYYY-MM-DDTHH:mm in local time).
function toLocalInputValue(isoStr) {
  const d = new Date(isoStr)
  if (isNaN(d.getTime())) return defaultDatetime()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// Schedules or edits a real meeting against the /meetings backend.
//
// Modes (controlled by props):
//   • create     — no editingSession. Creates a new session with all fields.
//   • reschedule — editingSession provided, mode='reschedule' (default). ONLY changes the date/time.
//   • details    — editingSession provided, mode='details'. ONLY changes duration / location / notes.
function MeetingScheduleModal({ groups, defaultGroupId, editingSession, mode = 'reschedule', onClose, onScheduled }) {
  const { scheduleSession, rescheduleSession } = useMeetings()
  const isEditing       = Boolean(editingSession)
  const isDetailsOnly   = isEditing && mode === 'details'
  const isRescheduling  = isEditing && mode === 'reschedule'

  const [groupId,  setGroupId]  = useState(
    editingSession?.studyGroup?.id ?? defaultGroupId ?? groups?.[0]?.id ?? ''
  )
  const [datetime, setDatetime] = useState(
    editingSession ? toLocalInputValue(editingSession.scheduledAt) : defaultDatetime()
  )
  const [duration, setDuration] = useState(editingSession?.durationMinutes ?? 60)
  const [location, setLocation] = useState(editingSession?.location ?? '')
  const [notes,    setNotes]    = useState(editingSession?.notes ?? '')
  const [loading,  setLoading]  = useState(false)
  const [error,    setError]    = useState(null)
  const [done,     setDone]     = useState(false)

  const setClientError = (message) =>
    setError({ message, code: 'CLIENT_VALIDATION', reference: null })

  const setServerError = (err) => {
    const data = err?.response?.data ?? {}
    const status = err?.response?.status
    const isNetwork = !err?.response
    const fallback = isEditing
      ? (isDetailsOnly ? 'Failed to update details.' : 'Failed to reschedule.')
      : 'Failed to schedule session.'
    const ref = `LOCAL-${Date.now().toString(36).toUpperCase()}`
    console.error(`[${data.reference ?? ref}]`, err)
    setError({
      message:   data.message ?? (isNetwork ? 'Network error — check your connection and try again.' : fallback),
      code:      data.code ?? (isNetwork ? 'NETWORK' : status ? `HTTP_${status}` : 'UNKNOWN'),
      reference: data.reference ?? ref,
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)

    if (!isEditing && !groupId) {
      setClientError('Please choose a group before scheduling.')
      return
    }

    // Time validation only matters when actually setting a time
    let localScheduledAt = null
    if (!isDetailsOnly) {
      const when = new Date(datetime)
      if (isNaN(when.getTime())) {
        setClientError('Please pick a valid date and time.')
        return
      }
      if (when.getTime() <= Date.now()) {
        setClientError('Session must be scheduled for a future time — meetings cannot start in the past.')
        return
      }
      // Send the input value as-is (local time, no timezone). Going through Date.toISOString()
      // converts to UTC and shifts the wall-clock hours, which the backend then stores as
      // LocalDateTime — losing the offset and showing up wrong on read-back.
      localScheduledAt = datetime.length === 16 ? `${datetime}:00` : datetime
    }

    setLoading(true)
    try {
      // Mode-specific payloads. Backend treats null/missing fields on PATCH as "leave alone",
      // so sending only the fields that belong to the current mode ensures other fields stay put.
      if (isRescheduling) {
        await rescheduleSession(editingSession.id, { scheduledAt: localScheduledAt })
      } else if (isDetailsOnly) {
        await rescheduleSession(editingSession.id, {
          scheduledAt: null,
          durationMinutes: duration ? Number(duration) : null,
          location,
          notes,
        })
      } else {
        await scheduleSession(Number(groupId), {
          scheduledAt: localScheduledAt,
          durationMinutes: duration ? Number(duration) : null,
          location,
          notes,
        })
      }
      setDone(true)
      if (onScheduled) onScheduled()
      setTimeout(onClose, 1500)
    } catch (err) {
      setServerError(err)
    } finally {
      setLoading(false)
    }
  }

  const heading =
    !isEditing      ? 'Schedule Meeting'
    : isDetailsOnly ? 'Edit Details'
    :                 'Reschedule Meeting'

  const subheading =
    !isEditing      ? 'Notifies all group members'
    : isDetailsOnly ? `${editingSession.studyGroup?.name ?? 'Study group'} — time will not change`
    :                 `${editingSession.studyGroup?.name ?? 'Study group'} — members will be notified`

  const successText =
    !isEditing      ? 'Meeting scheduled!'
    : isDetailsOnly ? 'Details updated!'
    :                 'Meeting rescheduled!'

  const submitText =
    !isEditing      ? 'Schedule Meeting'
    : isDetailsOnly ? 'Save Details'
    :                 'Save Changes'

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl w-full max-w-md animate-fade-up">
        <div className="px-8 pt-8 pb-6">
          <div className="flex items-start justify-between mb-6">
            <div>
              <h2 className="font-display text-2xl text-wsu-navy dark:text-white">{heading}</h2>
              <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5">{subheading}</p>
            </div>
            <button
              onClick={onClose}
              className="w-8 h-8 flex items-center justify-center rounded-xl hover:bg-wsu-mist dark:hover:bg-gray-800 text-wsu-slate dark:text-gray-400 text-xl leading-none transition-colors"
            >
              ×
            </button>
          </div>

          {done ? (
            <div className="text-center py-8">
              <div className="text-5xl mb-4">📅</div>
              <p className="font-semibold text-wsu-navy dark:text-white text-lg">{successText}</p>
              <p className="text-sm text-wsu-slate dark:text-gray-400 mt-1">
                {isDetailsOnly ? 'Your changes have been saved.' : 'All group members have been notified.'}
              </p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <div className="bg-red-50 dark:bg-red-900/20 rounded-lg px-3 py-2">
                  <p className="text-sm text-red-700 dark:text-red-400">{error.message}</p>
                  <p className="mt-1 font-mono text-[10px] text-red-500/80 dark:text-red-400/70 select-all">
                    code: {error.code}{error.reference ? ` · ref: ${error.reference}` : ''}
                  </p>
                </div>
              )}

              {!isEditing && (
                <div>
                  <label className="form-label dark:text-gray-200">Study Group</label>
                  <select
                    required
                    className="form-input dark:bg-gray-800 dark:border-gray-700 dark:text-white"
                    value={groupId}
                    onChange={e => setGroupId(e.target.value)}
                  >
                    <option value="" disabled>Select a group...</option>
                    {(groups ?? []).map(g => (
                      <option key={g.id} value={g.id}>
                        {g.name}{g.course?.courseCode ? ` · ${g.course.courseCode}` : ''}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {!isDetailsOnly && (
                <div>
                  <label className="form-label dark:text-gray-200">Date & Time</label>
                  <input
                    type="datetime-local"
                    required
                    className="form-input dark:bg-gray-800 dark:border-gray-700 dark:text-white"
                    value={datetime}
                    onChange={e => setDatetime(e.target.value)}
                  />
                </div>
              )}

              {!isRescheduling && (
                <>
                  <div>
                    <label className="form-label dark:text-gray-200">
                      Duration <span className="font-normal text-wsu-slate dark:text-gray-500">(minutes)</span>
                    </label>
                    <select
                      className="form-input dark:bg-gray-800 dark:border-gray-700 dark:text-white"
                      value={duration ?? ''}
                      onChange={e => setDuration(e.target.value ? Number(e.target.value) : null)}
                    >
                      <option value="">Unspecified</option>
                      {DURATION_OPTIONS.map(m => (
                        <option key={m} value={m}>
                          {m < 60 ? `${m} min` : m % 60 === 0 ? `${m / 60} hr` : `${Math.floor(m / 60)} hr ${m % 60} min`}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="form-label dark:text-gray-200">
                      Location <span className="font-normal text-wsu-slate dark:text-gray-500">(optional)</span>
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Ely Library Rm 204 or Zoom"
                      className="form-input dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:placeholder-gray-500"
                      value={location}
                      onChange={e => setLocation(e.target.value)}
                    />
                  </div>

                  <div>
                    <label className="form-label dark:text-gray-200">
                      Notes <span className="font-normal text-wsu-slate dark:text-gray-500">(optional)</span>
                    </label>
                    <textarea
                      rows={3}
                      placeholder="Agenda, chapters to review, things to bring..."
                      className="form-input resize-none dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:placeholder-gray-500"
                      value={notes}
                      onChange={e => setNotes(e.target.value)}
                    />
                  </div>
                </>
              )}

              <div className="flex gap-3 pt-1">
                <button
                  type="button"
                  onClick={onClose}
                  className="flex-1 py-3 rounded-xl border border-gray-200 dark:border-gray-700 text-wsu-navy dark:text-gray-200 text-sm font-semibold hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white text-sm font-semibold rounded-xl transition-all disabled:opacity-60"
                >
                  {loading ? 'Saving...' : submitText}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}

export default MeetingScheduleModal
