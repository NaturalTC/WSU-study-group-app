import { useState, useEffect, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import AppHeader from '../components/AppHeader'
import ChatMessage from '../components/ChatMessage'
import MembersSidebar from '../components/MembersSidebar'
import ScheduleEventModal from '../components/ScheduleEventModal'
import { useAuth } from '../context/AuthContext'
import { useNotifications } from '../context/NotificationContext'
import api from '../api/axios'
import campusPhoto from '../assets/WSUCampusStock2013_063-L.jpg'

function getCourseGradient(courseCode) {
  const prefix = (courseCode ?? '').split(' ')[0].toUpperCase()
  if (prefix.startsWith('CAIS') || prefix.startsWith('CIS') || prefix.startsWith('CS'))
    return 'from-blue-500 to-indigo-700'
  if (prefix.startsWith('MATH') || prefix.startsWith('STAT'))
    return 'from-violet-500 to-purple-700'
  if (prefix.startsWith('BIOL') || prefix.startsWith('CHEM') || prefix.startsWith('PHYS') || prefix.startsWith('ENVS'))
    return 'from-emerald-500 to-teal-700'
  if (prefix.startsWith('PSYC') || prefix.startsWith('SOCI') || prefix.startsWith('ANTH'))
    return 'from-orange-500 to-amber-600'
  if (prefix.startsWith('HIST') || prefix.startsWith('ENGL') || prefix.startsWith('PHIL') || prefix.startsWith('LITR'))
    return 'from-rose-500 to-red-700'
  if (prefix.startsWith('BUSN') || prefix.startsWith('ACCT') || prefix.startsWith('MGMT') || prefix.startsWith('MKTG'))
    return 'from-cyan-600 to-blue-700'
  if (prefix.startsWith('NURS') || prefix.startsWith('HLTH'))
    return 'from-teal-500 to-cyan-600'
  if (prefix.startsWith('CRJU') || prefix.startsWith('POLI'))
    return 'from-slate-500 to-gray-700'
  if (prefix.startsWith('COMM') || prefix.startsWith('JOUR'))
    return 'from-pink-500 to-rose-600'
  if (prefix.startsWith('EDUC'))
    return 'from-amber-500 to-yellow-600'
  return 'from-wsu-navy to-blue-900'
}

function formatTime(isoStr) {
  const d    = isoStr ? new Date(isoStr) : new Date()
  const time = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

  const today     = new Date(); today.setHours(0, 0, 0, 0)
  const yesterday = new Date(today); yesterday.setDate(yesterday.getDate() - 1)
  const dDay      = new Date(d);    dDay.setHours(0, 0, 0, 0)

  if (dDay.getTime() === today.getTime())     return `Today · ${time}`
  if (dDay.getTime() === yesterday.getTime()) return `Yesterday · ${time}`
  return d.toLocaleDateString([], { month: 'short', day: 'numeric' }) + ` · ${time}`
}

function EmojiPickerIcon() {
  return (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <circle cx="11" cy="13" r="8" strokeWidth={1.5} />
      <circle cx="8.5" cy="11.5" r="0.75" fill="currentColor" stroke="none" />
      <circle cx="13.5" cy="11.5" r="0.75" fill="currentColor" stroke="none" />
      <path strokeLinecap="round" strokeWidth={1.5} d="M8.5 15.5c.5 1 1.5 1.5 2.5 1.5s2-.5 2.5-1.5" />
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M20 3v4M18 5h4" />
    </svg>
  )
}

const QUICK_EMOJIS = ['👍', '😂', '🔥']

function GroupChat() {
  const { groupId } = useParams()
  const parsedGroupId = parseInt(groupId)
  const { profile, refreshProfile } = useAuth()
  const { refresh: refreshNotifications } = useNotifications()

  const [group, setGroup]         = useState(null)
  const [myGroups, setMyGroups]   = useState([])
  const [messages, setMessages]   = useState([])
  const [input, setInput]         = useState('')
  const [loading, setLoading]     = useState(true)
  const [connected, setConnected] = useState(false)
  const [sidebarOpen,   setSidebarOpen]   = useState(false)
  const [scheduleOpen,  setScheduleOpen]  = useState(false)
  const [emojiOpen,     setEmojiOpen]     = useState(false)
  const [aiOpen,        setAiOpen]        = useState(false)
  const [aiInput,       setAiInput]       = useState('')
  const [aiLoading,     setAiLoading]     = useState(false)

  const stompClientRef = useRef(null)
  const messagesEndRef = useRef(null)
  const inputRef       = useRef(null)
  const emojiRef       = useRef(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // Load group info, chat history, and the user's group list
  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const [groupRes, historyRes, myGroupsRes] = await Promise.all([
          api.get(`/groups/${parsedGroupId}`),
          api.get(`/groups/${parsedGroupId}/messages`),
          api.get('/groups/my').catch(() => ({ data: [] })),
        ])

        setGroup(groupRes.data)
        setMyGroups(myGroupsRes.data)

        const history = historyRes.data.map(msg => ({
          id: msg.id,
          sender: msg.sender?.name ?? msg.senderName ?? 'Unknown',
          text: msg.content,
          timestamp: formatTime(msg.sentAt),
        }))

        setMessages([
          {
            id: 'welcome',
            sender: 'system',
            text: `Welcome to ${groupRes.data.name}`,
            timestamp: formatTime(),
          },
          ...history,
        ])
      } catch (err) {
        console.error('Failed to load group chat:', err)
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [parsedGroupId])

  // WebSocket connection
  useEffect(() => {
    const token = localStorage.getItem('token')

    const wsUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8080')
      .replace(/^http/, 'ws') + '/ws'

    const client = new Client({
      brokerURL: wsUrl,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/topic/chat/${parsedGroupId}`, (frame) => {
          const dto = JSON.parse(frame.body)
          setMessages(prev => [...prev, {
            id: `ws-${Date.now()}-${Math.random()}`,
            sender: dto.senderName,
            text: dto.content,
            timestamp: formatTime(dto.sentAt),
          }])
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => console.error('STOMP error:', frame),
    })

    client.activate()
    stompClientRef.current = client

    return () => {
      client.deactivate()
      setConnected(false)
    }
  }, [parsedGroupId])

  useEffect(() => {
    const handler = (e) => { if (emojiRef.current && !emojiRef.current.contains(e.target)) setEmojiOpen(false) }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const sendMessage = (content) => {
    if (!content.trim() || !connected) return
    stompClientRef.current.publish({
      destination: `/app/chat/${parsedGroupId}`,
      body: JSON.stringify({
        studyGroupId: parsedGroupId,
        senderName: profile?.name ?? 'Unknown',
        content: content.trim(),
      }),
    })
  }

  const handleSendMessage = (e) => {
    e.preventDefault()
    if (!input.trim() || !connected) return
    sendMessage(input)
    refreshProfile()
    setInput('')
    if (inputRef.current) {
      inputRef.current.style.height = ''
      inputRef.current.style.overflowY = 'hidden'
    }
    inputRef.current?.focus()
  }

  const handleEmojiSend = (emoji) => {
    sendMessage(emoji)
    refreshProfile()
    setEmojiOpen(false)
    inputRef.current?.focus()
  }

  const handleAskAi = async (e) => {
    e.preventDefault()
    const question = aiInput.trim()
    if (!question || aiLoading || !connected) return
    setAiInput('')
    setAiLoading(true)
    try {
      await api.post('/ai/chat', {
        groupId: parsedGroupId,
        profileId: profile?.id,
        message: question,
      })
      // Backend broadcasts the question + AI reply via WebSocket — they arrive through
      // the existing /topic/chat/{groupId} subscription, so no local state update needed.
    } catch {
      setMessages(prev => [...prev, {
        id: `ai-error-${Date.now()}`,
        sender: 'AI Assistant',
        text: 'Sorry, I had trouble answering that. Please try again.',
        timestamp: formatTime(),
      }])
    } finally {
      setAiLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      handleSendMessage(e)
    }
  }

  const courseCode   = group?.course?.courseCode ?? ''
  const courseLetter = courseCode.split(' ')[0]?.charAt(0) ?? 'G'
  const memberCount  = group?.members?.length ?? 0

  return (
    <div
      className="flex flex-col min-h-screen bg-cover bg-center bg-fixed transition-colors duration-300"
      style={{ backgroundImage: `url(${campusPhoto})` }}
    >
      <AppHeader />

      <main className="flex-1 pt-20 pb-0 max-w-7xl mx-auto w-full px-4 md:px-6">
        <div className="flex gap-6 h-[calc(100vh-5rem)] py-4">

          {/* ── Chat Area ─────────────────────────────────────── */}
          <div className="flex-1 flex flex-col bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden min-w-0">

            {/* Chat Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800">
              <div className="flex items-center gap-4">
                <button
                  onClick={() => setSidebarOpen(!sidebarOpen)}
                  className="lg:hidden w-8 h-8 flex items-center justify-center rounded-lg hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                >
                  <svg className="w-5 h-5 text-wsu-navy dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                </button>

                <div className={`w-10 h-10 bg-gradient-to-br ${getCourseGradient(courseCode)} rounded-xl flex items-center justify-center text-white font-bold text-sm flex-shrink-0`}>
                  {courseLetter}
                </div>
                <div>
                  <h1 className="font-display text-lg text-wsu-navy dark:text-white leading-tight">
                    {group?.name ?? 'Loading...'}
                  </h1>
                  <div className="flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500' : 'bg-gray-400'}`} />
                    <span className="text-xs text-wsu-slate dark:text-gray-400">
                      {memberCount} member{memberCount !== 1 ? 's' : ''} · {courseCode}
                    </span>
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => setScheduleOpen(true)}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-full border border-blue-200 dark:border-blue-800 bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 text-xs font-semibold hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors"
                >
                  <span>📅</span>
                  <span className="hidden sm:inline">Schedule</span>
                </button>

                <button
                  onClick={() => setAiOpen(p => !p)}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full border text-xs font-semibold transition-colors ${
                    aiOpen
                      ? 'border-violet-300 dark:border-violet-700 bg-violet-100 dark:bg-violet-900/40 text-violet-700 dark:text-violet-300'
                      : 'border-violet-200 dark:border-violet-800 bg-violet-50 dark:bg-violet-900/20 text-violet-700 dark:text-violet-400 hover:bg-violet-100 dark:hover:bg-violet-900/40'
                  }`}
                >
                  <span className="hidden sm:inline">Ask AI</span>
                </button>

                <div className={`flex items-center gap-2 px-3 py-1.5 rounded-full border ${
                  connected
                    ? 'bg-green-50 dark:bg-green-900/20 border-green-100 dark:border-green-800'
                    : 'bg-gray-50 dark:bg-gray-800 border-gray-200 dark:border-gray-700'
                }`}>
                  <span className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500 animate-pulse' : 'bg-gray-400'}`} />
                  <span className={`text-xs font-semibold ${connected ? 'text-green-700 dark:text-green-400' : 'text-gray-500 dark:text-gray-400'}`}>
                    {connected ? 'Live' : 'Connecting...'}
                  </span>
                </div>
              </div>
            </div>

            {/* Messages Area */}
            <div className="flex-1 overflow-y-auto px-6 py-4 space-y-1 bg-wsu-chalk dark:bg-gray-900">
              {loading ? (
                <div className="flex justify-center py-12">
                  <div className="animate-spin w-6 h-6 border-4 border-blue-700 border-t-transparent rounded-full" />
                </div>
              ) : (
                messages.map(message => (
                  <ChatMessage
                    key={message.id}
                    message={message}
                    isOwn={message.sender === profile?.name}
                  />
                ))
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div className="px-6 py-4 border-t border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800">
              <form onSubmit={handleSendMessage} className="flex gap-3 items-end">
                <textarea
                  ref={inputRef}
                  rows={1}
                  value={input}
                  onChange={e => {
                    setInput(e.target.value)
                    e.target.style.height = 'auto'
                    const next = e.target.scrollHeight
                    e.target.style.height = `${Math.min(next, 160)}px`
                    e.target.style.overflowY = next > 160 ? 'auto' : 'hidden'
                  }}
                  onKeyDown={handleKeyDown}
                  placeholder={connected ? 'Message your group...' : 'Connecting to chat...'}
                  disabled={!connected}
                  className="form-input resize-none disabled:opacity-60 disabled:cursor-not-allowed flex-1 min-w-0 h-12 !py-2.5 overflow-hidden"
                />
                <div className="relative flex-shrink-0 h-12" ref={emojiRef}>
                  <button
                    type="button"
                    onClick={() => setEmojiOpen(p => !p)}
                    disabled={!connected}
                    className="px-3 h-12 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:bg-wsu-mist dark:hover:bg-gray-700 text-wsu-slate dark:text-gray-300 transition-all duration-200 shadow-sm disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
                    aria-label="Send emoji"
                  >
                    <EmojiPickerIcon />
                  </button>
                  {emojiOpen && (
                    <div className="absolute bottom-full mb-2 right-0 flex gap-1 bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-xl shadow-lg p-2">
                      {QUICK_EMOJIS.map(emoji => (
                        <button
                          key={emoji}
                          type="button"
                          onClick={() => handleEmojiSend(emoji)}
                          className="text-xl p-1.5 rounded-lg hover:bg-wsu-mist dark:hover:bg-gray-700 transition-colors"
                        >
                          {emoji}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
                <button
                  type="submit"
                  disabled={!input.trim() || !connected}
                  className="bg-blue-700 hover:bg-blue-800 text-white px-5 h-12 rounded-lg font-semibold transition-all duration-200 shadow-md disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 flex-shrink-0"
                >
                  <span className="flex items-center gap-2 -translate-x-1">
                    <svg className="w-4 h-4 -rotate-45 -translate-y-px" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                    </svg>
                    <span className="hidden sm:inline leading-none">Send</span>
                  </span>
                </button>
              </form>

              <p className="text-xs text-gray-400 dark:text-gray-500 mt-2 text-center">
                Press <kbd className="bg-gray-100 dark:bg-gray-700 px-1 rounded text-gray-500 dark:text-gray-400">Enter</kbd> to send ·{' '}
                <kbd className="bg-gray-100 dark:bg-gray-700 px-1 rounded text-gray-500 dark:text-gray-400">Shift+Enter</kbd> for new line
              </p>
            </div>

            {/* AI Input Panel */}
            {aiOpen && (
              <div className="px-6 py-3 border-t border-violet-100 dark:border-violet-900/30 bg-violet-50/60 dark:bg-violet-950/20">
                <form onSubmit={handleAskAi} className="flex gap-3 items-center">
                  <input
                    type="text"
                    value={aiInput}
                    onChange={e => setAiInput(e.target.value)}
                    placeholder="Ask the AI a study question..."
                    disabled={aiLoading || !connected}
                    className="form-input flex-1 min-w-0 h-10 !py-2 text-sm disabled:opacity-60 disabled:cursor-not-allowed"
                  />
                  <button
                    type="submit"
                    disabled={!aiInput.trim() || aiLoading || !connected}
                    className="flex-shrink-0 flex items-center justify-center gap-1.5 px-4 h-10 bg-violet-600 hover:bg-violet-700 text-white rounded-lg text-sm font-semibold transition-all disabled:opacity-50 disabled:cursor-not-allowed min-w-[72px]"
                  >
                    {aiLoading
                      ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                      : 'Ask AI'}
                  </button>
                </form>
              </div>
            )}
          </div>

          {/* ── Desktop Sidebar ────────────────────────────────── */}
          <div className="hidden lg:block">
            <MembersSidebar
              activeGroupId={parsedGroupId}
              currentGroup={group}
              members={group?.members ?? []}
              myGroups={myGroups}
              onSchedule={() => setScheduleOpen(true)}
            />
          </div>

          {/* ── Mobile Sidebar Overlay ─────────────────────────── */}
          {sidebarOpen && (
            <div className="lg:hidden fixed inset-0 z-40 flex">
              <div
                className="absolute inset-0 bg-black/40 backdrop-blur-sm"
                onClick={() => setSidebarOpen(false)}
              />
              <div className="relative ml-auto w-80 h-full bg-wsu-chalk dark:bg-gray-800 p-4 overflow-y-auto shadow-2xl animate-fade-in">
                <button
                  onClick={() => setSidebarOpen(false)}
                  className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-wsu-mist transition-colors text-wsu-slate"
                >
                  ×
                </button>
                <div className="mt-8">
                  <MembersSidebar
                    activeGroupId={parsedGroupId}
                    currentGroup={group}
                    members={group?.members ?? []}
                    myGroups={myGroups}
                    onSchedule={() => { setSidebarOpen(false); setScheduleOpen(true) }}
                  />
                </div>
              </div>
            </div>
          )}

        </div>
      </main>

      {scheduleOpen && group && (
        <ScheduleEventModal
          groupId={parsedGroupId}
          groupName={group.name}
          onClose={() => { setScheduleOpen(false); refreshNotifications() }}
        />
      )}
    </div>
  )
}

export default GroupChat
