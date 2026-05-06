import { createContext, useContext, useState, useCallback } from 'react'
import api from '../api/axios'
import { useAuth } from './AuthContext'

const MeetingsContext = createContext(null)

/**
 * Wraps the /meetings REST API and caches the results in React state so multiple
 * components can share a consistent view without each one re-fetching. Cancellations,
 * reschedules, and edits update both the per-group cache and the cross-group "upcoming"
 * list optimistically — no extra refetch is needed after a mutation succeeds.
 *
 * Exposed via `useMeetings()`:
 *   • upcoming, loadingUpcoming   — the cross-group future-sessions list and its loading flag
 *   • fetchUpcoming()             — refresh the upcoming list (call once on page mount)
 *   • fetchGroupSessions(groupId) — refresh sessions for one group; returns the array
 *   • getGroupSessions(groupId)   — read the cached array (no fetch)
 *   • scheduleSession(groupId, fields)            — POST  /meetings
 *   • rescheduleSession(sessionId, fields)        — PATCH /meetings/{id}; pass scheduledAt:null
 *                                                   to leave the time alone (details-only edit)
 *   • cancelSession(sessionId, groupId)           — DELETE /meetings/{id}
 */
export function MeetingsProvider({ children }) {
  const { refreshProfile } = useAuth()
  const [sessionsByGroup, setSessionsByGroup] = useState({})
  const [upcoming, setUpcoming]               = useState([])
  const [loadingUpcoming, setLoadingUpcoming] = useState(false)

  // Loads the per-group cache. Swallows errors so the UI just shows an empty list
  // instead of crashing — the page is read-only and a 403/404 just means "nothing to show."
  const fetchGroupSessions = useCallback(async (groupId) => {
    if (!groupId) return []
    try {
      const res = await api.get(`/meetings/group/${groupId}`)
      setSessionsByGroup(prev => ({ ...prev, [groupId]: res.data }))
      return res.data
    } catch {
      return []
    }
  }, [])

  const fetchUpcoming = useCallback(async () => {
    setLoadingUpcoming(true)
    try {
      const res = await api.get('/meetings/upcoming')
      setUpcoming(res.data)
      return res.data
    } catch {
      setUpcoming([])
      return []
    } finally {
      setLoadingUpcoming(false)
    }
  }, [])

  const scheduleSession = async (groupId, { scheduledAt, location, notes, durationMinutes }) => {
    const res = await api.post('/meetings', {
      groupId,
      scheduledAt,
      durationMinutes: durationMinutes ?? null,
      location: location || null,
      notes:    notes    || null,
    })
    const session = res.data
    setSessionsByGroup(prev => {
      const list = [...(prev[groupId] ?? []), session]
        .sort((a, b) => new Date(a.scheduledAt) - new Date(b.scheduledAt))
      return { ...prev, [groupId]: list }
    })
    setUpcoming(prev => [...prev, session]
      .sort((a, b) => new Date(a.scheduledAt) - new Date(b.scheduledAt)))
    refreshProfile()
    return session
  }

  const rescheduleSession = async (sessionId, { scheduledAt, location, notes, durationMinutes }) => {
    // scheduledAt may be null = details-only edit; backend leaves the existing time alone.
    const res = await api.patch(`/meetings/${sessionId}`, {
      scheduledAt: scheduledAt ?? null,
      durationMinutes: durationMinutes ?? null,
      location: location ?? null,
      notes:    notes    ?? null,
    })
    const updated = res.data
    const updateInList = (list) => (list ?? [])
      .map(s => s.id === sessionId ? updated : s)
      .sort((a, b) => new Date(a.scheduledAt) - new Date(b.scheduledAt))
    setUpcoming(prev => updateInList(prev))
    setSessionsByGroup(prev => {
      const next = {}
      for (const [gid, list] of Object.entries(prev)) next[gid] = updateInList(list)
      return next
    })
    return updated
  }

  const cancelSession = async (sessionId, groupId) => {
    await api.delete(`/meetings/${sessionId}`)
    setUpcoming(prev => prev.filter(s => s.id !== sessionId))
    if (groupId != null) {
      setSessionsByGroup(prev => ({
        ...prev,
        [groupId]: (prev[groupId] ?? []).filter(s => s.id !== sessionId),
      }))
    } else {
      // groupId may be missing if the caller didn't have it handy — fall back to
      // sweeping every cached group, since the cancelled session is in at most one.
      setSessionsByGroup(prev => {
        const next = {}
        for (const [gid, list] of Object.entries(prev)) {
          next[gid] = list.filter(s => s.id !== sessionId)
        }
        return next
      })
    }
  }

  const getGroupSessions = (groupId) => sessionsByGroup[groupId] ?? []

  return (
    <MeetingsContext.Provider value={{
      upcoming,
      loadingUpcoming,
      fetchUpcoming,
      fetchGroupSessions,
      getGroupSessions,
      scheduleSession,
      rescheduleSession,
      cancelSession,
    }}>
      {children}
    </MeetingsContext.Provider>
  )
}

export const useMeetings = () => useContext(MeetingsContext)
