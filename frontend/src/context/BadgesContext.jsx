import { createContext, useContext, useState, useEffect, useRef } from 'react'
import { useToast } from './ToastContext'
import api from '../api/axios'

export const BADGE_DEFINITIONS = [
  // ── Groups ──────────────────────────────────────────────────────────────────
  { id: 'group_1',       category: 'groups',   tier: 1, name: 'Team Player',    description: 'Joined your first study group',      threshold: 1,   icon: '👥' },
  { id: 'group_starter', category: 'groups',   tier: 2, name: 'Group Starter',  description: 'Created your first study group',     threshold: 1,   icon: '🚀' },
  // ── Messages ────────────────────────────────────────────────────────────────
  { id: 'msg_10',        category: 'messages', tier: 1, name: 'Active Chatter', description: 'Sent 10 messages in group chats',    threshold: 10,  icon: '💬' },
  // ── Sessions ────────────────────────────────────────────────────────────────
  { id: 'session_1',     category: 'sessions', tier: 1, name: 'First Session',  description: 'Scheduled your first study session', threshold: 1,   icon: '📅' },
  // ── Points ──────────────────────────────────────────────────────────────────
  { id: 'pts_100',       category: 'points',   tier: 1, name: 'Point Earner',   description: 'Earned 100 points',                  threshold: 100, icon: '⭐' },
]


const BadgesContext = createContext(null)

export function BadgesProvider({ children }) {
  const { addToast }          = useToast()
  const hasToasted            = useRef(false)
  const [raw, setRaw]         = useState([])
  const [seenIds, setSeenIds] = useState(() => {
    try { return new Set(JSON.parse(localStorage.getItem('wsu-seen-badges') ?? '[]')) }
    catch { return new Set() }
  })

  useEffect(() => {
    api.get('/profiles/badges')
      .then(res => setRaw(res.data))
      .catch(() => setRaw([]))
  }, [])

  // Fire toasts once per session for newly earned badges
  useEffect(() => {
    if (raw.length === 0 || hasToasted.current) return
    hasToasted.current = true

    const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
    const fresh = raw
      .map(e => {
        const def = BADGE_DEFINITIONS.find(b => b.id === e.id)
        return def ? { ...def, earnedAt: e.earnedAt } : null
      })
      .filter(b => b && new Date(b.earnedAt) > sevenDaysAgo && !seenIds.has(b.id))
      .sort((a, b) => b.tier - a.tier)

    if (fresh.length === 0) return

    const show = fresh.slice(0, 3)
    show.forEach((badge, i) => {
      setTimeout(() => {
        addToast({
          title: 'New Badge Earned!',
          description: badge.name,
          type: 'badge',
          duration: 5000,
        })
      }, i * 600)
    })

    if (fresh.length > 3) {
      setTimeout(() => {
        addToast({
          title: `+${fresh.length - 3} more badges earned`,
          description: 'Check your profile to see them all.',
          type: 'badge',
          duration: 5000,
        })
      }, show.length * 600)
    }

    // Mark all as seen so toasts don't re-fire on next load
    const next = new Set([...seenIds, ...fresh.map(b => b.id)])
    setSeenIds(next)
    localStorage.setItem('wsu-seen-badges', JSON.stringify([...next]))
  }, [raw])

  const earned = raw
    .map(e => {
      const def = BADGE_DEFINITIONS.find(b => b.id === e.id)
      return def ? { ...def, earnedAt: e.earnedAt } : null
    })
    .filter(Boolean)

  const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
  const newBadges = earned.filter(b => new Date(b.earnedAt) > sevenDaysAgo && !seenIds.has(b.id))

  const markAllSeen = () => {
    const next = new Set([...seenIds, ...earned.map(b => b.id)])
    setSeenIds(next)
    localStorage.setItem('wsu-seen-badges', JSON.stringify([...next]))
  }

  return (
    <BadgesContext.Provider value={{ earned, newBadges, markAllSeen }}>
      {children}
    </BadgesContext.Provider>
  )
}

export const useBadges = () => useContext(BadgesContext)
