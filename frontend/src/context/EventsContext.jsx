import { createContext, useContext, useState } from 'react'
import api from '../api/axios'

const EventsContext = createContext(null)
const STORAGE_KEY = 'wsu-events'

function load() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY)) ?? {} }
  catch { return {} }
}

export function EventsProvider({ children }) {
  const [byGroup, setByGroup] = useState(load)

  const persist = (data) => {
    setByGroup(data)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  }

  const addEvent = async (groupId, groupName, { title, eventDate, notes }) => {
    const ev = {
      id:        `local-${Date.now()}`,
      title,
      eventDate,
      notes:     notes || '',
      groupId,
      groupName,
    }
    try {
      const res = await api.post(`/groups/${groupId}/events`, { title, eventDate, notes })
      if (res.data?.id) ev.id = res.data.id
    } catch {}
    persist({ ...byGroup, [groupId]: [...(byGroup[groupId] ?? []), ev] })
    return ev
  }

  const removeEvent = (groupId, eventId) => {
    persist({ ...byGroup, [groupId]: (byGroup[groupId] ?? []).filter(e => e.id !== eventId) })
    api.delete(`/groups/${groupId}/events/${eventId}`).catch(() => {})
  }

  const getGroupEvents = (groupId) => {
    const now = new Date(); now.setHours(0, 0, 0, 0)
    return (byGroup[groupId] ?? [])
      .filter(e => new Date(e.eventDate) >= now)
      .sort((a, b) => new Date(a.eventDate) - new Date(b.eventDate))
  }

  const getUpcoming = () => {
    const now = new Date(); now.setHours(0, 0, 0, 0)
    return Object.values(byGroup)
      .flat()
      .filter(e => new Date(e.eventDate) >= now)
      .sort((a, b) => new Date(a.eventDate) - new Date(b.eventDate))
      .slice(0, 20)
  }

  return (
    <EventsContext.Provider value={{ addEvent, removeEvent, getGroupEvents, getUpcoming }}>
      {children}
    </EventsContext.Provider>
  )
}

export const useEvents = () => useContext(EventsContext)
