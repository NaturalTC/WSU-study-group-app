import { createContext, useContext, useState, useEffect, useRef } from 'react'
import { useToast } from './ToastContext'

export const BADGE_DEFINITIONS = [
  // ── Logins ──────────────────────────────────────────────────────────────────
  { id: 'login_1',    category: 'logins',   tier: 1, name: 'First Flight',      description: 'Logged in for the first time',          threshold: 1,    icon: '🦉' },
  { id: 'login_10',   category: 'logins',   tier: 2, name: 'Regular Owl',       description: 'Logged in 10 times',                    threshold: 10,   icon: '🦉' },
  { id: 'login_25',   category: 'logins',   tier: 3, name: 'Nest Dweller',      description: 'Logged in 25 times',                    threshold: 25,   icon: '🦉' },
  { id: 'login_50',   category: 'logins',   tier: 4, name: 'Night Owl',         description: 'Logged in 50 times',                    threshold: 50,   icon: '🦉' },
  { id: 'login_100',  category: 'logins',   tier: 5, name: 'Legendary Owl',     description: 'Logged in 100 times',                   threshold: 100,  icon: '🦉' },
  // ── Streak ──────────────────────────────────────────────────────────────────
  { id: 'streak_3',   category: 'streak',   tier: 1, name: 'On a Roll',         description: '3-day consecutive login streak',        threshold: 3,    icon: '🔥' },
  { id: 'streak_7',   category: 'streak',   tier: 2, name: 'Week Warrior',      description: '7-day consecutive login streak',        threshold: 7,    icon: '🔥' },
  { id: 'streak_14',  category: 'streak',   tier: 3, name: 'Fortnight Flame',   description: '14-day consecutive login streak',       threshold: 14,   icon: '🔥' },
  { id: 'streak_30',  category: 'streak',   tier: 4, name: 'Monthly Blaze',     description: '30-day consecutive login streak',       threshold: 30,   icon: '🔥' },
  // ── Messages ────────────────────────────────────────────────────────────────
  { id: 'msg_1',      category: 'messages', tier: 1, name: 'First Word',        description: 'Sent your first message',               threshold: 1,    icon: '💬' },
  { id: 'msg_25',     category: 'messages', tier: 2, name: 'Conversationalist', description: 'Sent 25 messages',                      threshold: 25,   icon: '💬' },
  { id: 'msg_100',    category: 'messages', tier: 3, name: 'Chatterbox',        description: 'Sent 100 messages',                     threshold: 100,  icon: '💬' },
  { id: 'msg_500',    category: 'messages', tier: 4, name: 'Voice of the Nest', description: 'Sent 500 messages',                     threshold: 500,  icon: '💬' },
  // ── Emojis ──────────────────────────────────────────────────────────────────
  { id: 'emoji_1',    category: 'emojis',   tier: 1, name: 'Expressive',        description: 'Sent your first emoji',                 threshold: 1,    icon: '😊' },
  { id: 'emoji_10',   category: 'emojis',   tier: 2, name: 'Emoji Fan',         description: 'Sent 10 emojis',                        threshold: 10,   icon: '😊' },
  { id: 'emoji_50',   category: 'emojis',   tier: 3, name: 'Emoji Master',      description: 'Sent 50 emojis',                        threshold: 50,   icon: '😊' },
  // ── Groups ──────────────────────────────────────────────────────────────────
  { id: 'group_1',    category: 'groups',   tier: 1, name: 'Team Player',       description: 'Joined your first study group',         threshold: 1,    icon: '👥' },
  { id: 'group_3',    category: 'groups',   tier: 2, name: 'Collaborator',      description: 'Joined 3 study groups',                 threshold: 3,    icon: '👥' },
  { id: 'group_5',    category: 'groups',   tier: 3, name: 'Network Builder',   description: 'Joined 5 study groups',                 threshold: 5,    icon: '👥' },
  // ── Sessions ────────────────────────────────────────────────────────────────
  { id: 'session_1',  category: 'sessions', tier: 1, name: 'First Session',     description: 'Attended your first study session',     threshold: 1,    icon: '📅' },
  { id: 'session_5',  category: 'sessions', tier: 2, name: 'Regular Studier',   description: 'Attended 5 study sessions',             threshold: 5,    icon: '📅' },
  { id: 'session_10', category: 'sessions', tier: 3, name: 'Dedicated Learner', description: 'Attended 10 study sessions',            threshold: 10,   icon: '📅' },
  { id: 'session_25', category: 'sessions', tier: 4, name: 'Study Champion',    description: 'Attended 25 study sessions',            threshold: 25,   icon: '📅' },
  // ── Points ──────────────────────────────────────────────────────────────────
  { id: 'pts_100',    category: 'points',   tier: 1, name: 'Point Earner',      description: 'Earned 100 points',                     threshold: 100,  icon: '⭐' },
  { id: 'pts_500',    category: 'points',   tier: 2, name: 'High Achiever',     description: 'Earned 500 points',                     threshold: 500,  icon: '⭐' },
  { id: 'pts_1000',   category: 'points',   tier: 3, name: 'Wise Owl',          description: 'Earned 1,000 points',                   threshold: 1000, icon: '⭐' },
  { id: 'pts_5000',   category: 'points',   tier: 4, name: 'Nest Guardian',     description: 'Earned 5,000 points',                   threshold: 5000, icon: '⭐' },
  // ── Helper ──────────────────────────────────────────────────────────────────
  { id: 'help_1',     category: 'helper',   tier: 1, name: 'Helpful Hand',      description: 'Helped a classmate for the first time', threshold: 1,    icon: '🤝' },
  { id: 'help_5',     category: 'helper',   tier: 2, name: 'Mentor',            description: 'Helped 5 classmates',                   threshold: 5,    icon: '🤝' },
  { id: 'help_10',    category: 'helper',   tier: 3, name: 'Campus Guide',      description: 'Helped 10 classmates',                  threshold: 10,   icon: '🤝' },
]

// TODO: replace with api.get('/profiles/badges') when backend endpoint is ready.
// Expected response shape: [{ id: string, earnedAt: ISO string }]
const MOCK_EARNED = [
  // logins — tiers 1 through 5
  { id: 'login_1',    earnedAt: '2026-03-15T10:00:00Z' },
  { id: 'login_10',   earnedAt: '2026-04-01T09:00:00Z' },
  { id: 'login_25',   earnedAt: '2026-04-15T09:00:00Z' },
  { id: 'login_50',   earnedAt: '2026-04-28T09:00:00Z' },
  { id: 'login_100',  earnedAt: '2026-05-01T09:00:00Z' },
  // streak — tiers 1 through 4
  { id: 'streak_3',   earnedAt: '2026-03-18T08:00:00Z' },
  { id: 'streak_7',   earnedAt: '2026-04-29T08:00:00Z' },
  { id: 'streak_14',  earnedAt: '2026-04-22T08:00:00Z' },
  { id: 'streak_30',  earnedAt: '2026-05-02T08:00:00Z' },
  // messages — tiers 1 through 3
  { id: 'msg_1',      earnedAt: '2026-03-15T11:00:00Z' },
  { id: 'msg_25',     earnedAt: '2026-04-10T14:00:00Z' },
  { id: 'msg_100',    earnedAt: '2026-04-28T14:00:00Z' },
  // emojis — tiers 1 through 3
  { id: 'emoji_1',    earnedAt: '2026-04-30T15:00:00Z' },
  { id: 'emoji_10',   earnedAt: '2026-05-01T15:00:00Z' },
  { id: 'emoji_50',   earnedAt: '2026-05-02T15:00:00Z' },
  // groups — tiers 1 through 3
  { id: 'group_1',    earnedAt: '2026-03-15T12:00:00Z' },
  { id: 'group_3',    earnedAt: '2026-04-20T10:00:00Z' },
  { id: 'group_5',    earnedAt: '2026-05-01T10:00:00Z' },
  // sessions — tiers 1 through 4
  { id: 'session_1',  earnedAt: '2026-04-28T10:00:00Z' },
  { id: 'session_5',  earnedAt: '2026-04-30T10:00:00Z' },
  { id: 'session_10', earnedAt: '2026-05-02T10:00:00Z' },
  { id: 'session_25', earnedAt: '2026-05-03T10:00:00Z' },
  // points — tiers 1 through 4
  { id: 'pts_100',    earnedAt: '2026-03-20T14:00:00Z' },
  { id: 'pts_500',    earnedAt: '2026-04-15T16:00:00Z' },
  { id: 'pts_1000',   earnedAt: '2026-04-29T16:00:00Z' },
  { id: 'pts_5000',   earnedAt: '2026-05-03T16:00:00Z' },
  // helper — tiers 1 through 3
  { id: 'help_1',     earnedAt: '2026-04-25T13:00:00Z' },
  { id: 'help_5',     earnedAt: '2026-05-01T13:00:00Z' },
  { id: 'help_10',    earnedAt: '2026-05-03T13:00:00Z' },
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
    // TODO: api.get('/profiles/badges').then(res => setRaw(res.data)).catch(() => setRaw(MOCK_EARNED))
    setRaw(MOCK_EARNED)
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
