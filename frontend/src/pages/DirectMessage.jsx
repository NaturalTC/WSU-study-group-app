import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import AppHeader from '../components/AppHeader'
import ChatMessage from '../components/ChatMessage'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'
import campusPhoto from '../assets/WSUCampusStock2013_063-L.jpg'

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

function getInitials(name) {
  const parts = (name ?? '').trim().split(' ')
  return parts.length >= 2
    ? `${parts[0].charAt(0)}${parts[parts.length - 1].charAt(0)}`.toUpperCase()
    : (name?.charAt(0)?.toUpperCase() ?? '?')
}

function BackIcon() {
  return (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
    </svg>
  )
}

function DirectMessage() {
  const { profileId } = useParams()
  const navigate      = useNavigate()
  const { profile }   = useAuth()

  const targetProfileId = parseInt(profileId, 10)
  const myId            = profile?.id

  const dmRoomId = myId && targetProfileId
    ? `dm-${Math.min(myId, targetProfileId)}-${Math.max(myId, targetProfileId)}`
    : null

  const [targetProfile, setTargetProfile] = useState(null)
  const [messages,      setMessages]      = useState([])
  const [input,         setInput]         = useState('')
  const [loading,       setLoading]       = useState(true)
  const [connected,     setConnected]     = useState(false)

  const stompClientRef = useRef(null)
  const messagesEndRef = useRef(null)
  const inputRef       = useRef(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  useEffect(() => {
    if (!dmRoomId) return
    setLoading(true)

    const load = async () => {
      try {
        const [profileRes, historyRes] = await Promise.all([
          api.get(`/profiles/${targetProfileId}`),
          api.get(`/dm/${dmRoomId}/messages`),
        ])
        setTargetProfile(profileRes.data)

        const history = historyRes.data.map(msg => ({
          id:        msg.id,
          sender:    msg.sender?.name ?? msg.senderName ?? 'Unknown',
          text:      msg.content,
          timestamp: formatTime(msg.sentAt),
        }))
        setMessages(history)
      } catch (err) {
        console.error('Failed to load DM:', err)
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [dmRoomId, targetProfileId])

  useEffect(() => {
    if (!dmRoomId) return
    const token = localStorage.getItem('token')

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        client.publish({
          destination: `/app/dm/enter/${dmRoomId}`,
          body: JSON.stringify({ profileId: myId }),
        })
        client.subscribe(`/topic/dm/${dmRoomId}`, (frame) => {
          const dto = JSON.parse(frame.body)
          setMessages(prev => [...prev, {
            id:        `ws-${Date.now()}-${Math.random()}`,
            sender:    dto.senderName,
            text:      dto.content,
            timestamp: formatTime(dto.sentAt),
          }])
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError:  (frame) => console.error('STOMP error:', frame),
    })

    client.activate()
    stompClientRef.current = client

    return () => {
      try {
        client.publish({
          destination: `/app/dm/leave/${dmRoomId}`,
          body: JSON.stringify({}),
        })
      } catch (_) { /* client may have already disconnected */ }
      client.deactivate()
      setConnected(false)
    }
  }, [dmRoomId])

  const sendMessage = (content) => {
    if (!content.trim() || !connected || !dmRoomId) return
    stompClientRef.current.publish({
      destination: `/app/dm/${dmRoomId}`,
      body: JSON.stringify({
        senderName: profile?.name ?? 'Unknown',
        content:    content.trim(),
        dmRoomId,
      }),
    })
  }

  const handleSend = (e) => {
    e.preventDefault()
    if (!input.trim() || !connected) return
    sendMessage(input)
    setInput('')
    if (inputRef.current) {
      inputRef.current.style.height   = ''
      inputRef.current.style.overflowY = 'hidden'
    }
    inputRef.current?.focus()
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) handleSend(e)
  }

  const initials = getInitials(targetProfile?.name)

  return (
    <div
      className="flex flex-col min-h-screen bg-cover bg-center bg-fixed transition-colors duration-300"
      style={{ backgroundImage: `url(${campusPhoto})` }}
    >
      <AppHeader />

      <main className="flex-1 pt-20 pb-0 max-w-4xl mx-auto w-full px-4 md:px-6">
        <div className="flex h-[calc(100vh-5rem)] py-4">

          <div className="flex-1 flex flex-col bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">

            {/* Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800">
              <div className="flex items-center gap-4">
                <button
                  onClick={() => navigate(-1)}
                  className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-wsu-mist dark:hover:bg-gray-700 transition-colors text-wsu-slate dark:text-gray-300"
                  aria-label="Go back"
                >
                  <BackIcon />
                </button>

                {targetProfile?.profilePicURL ? (
                  <img
                    src={targetProfile.profilePicURL}
                    alt={targetProfile.name}
                    className="w-10 h-10 rounded-full object-cover shadow-sm flex-shrink-0"
                  />
                ) : (
                  <div className="w-10 h-10 rounded-full bg-wsu-navy dark:bg-blue-800 text-white font-bold text-sm flex items-center justify-center flex-shrink-0 shadow-sm">
                    {initials}
                  </div>
                )}

                <div>
                  <h1 className="font-display text-lg text-wsu-navy dark:text-white leading-tight">
                    {targetProfile?.name ?? 'Loading...'}
                  </h1>
                  {targetProfile?.major && (
                    <p className="text-xs text-wsu-slate dark:text-gray-400">{targetProfile.major}</p>
                  )}
                </div>
              </div>

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

            {/* Messages */}
            <div className="flex-1 overflow-y-auto px-6 py-4 space-y-1 bg-wsu-chalk dark:bg-gray-900">
              {loading ? (
                <div className="flex justify-center py-12">
                  <div className="animate-spin w-6 h-6 border-4 border-blue-700 border-t-transparent rounded-full" />
                </div>
              ) : messages.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full gap-3 text-center py-16">
                  <div className="w-16 h-16 rounded-full bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center">
                    {targetProfile?.profilePicURL ? (
                      <img src={targetProfile.profilePicURL} alt="" className="w-16 h-16 rounded-full object-cover" />
                    ) : (
                      <span className="text-2xl font-bold text-blue-700 dark:text-blue-400">{initials}</span>
                    )}
                  </div>
                  <div>
                    <p className="font-semibold text-wsu-navy dark:text-white">
                      Start a conversation with {targetProfile?.name ?? 'this user'}
                    </p>
                    <p className="text-sm text-wsu-slate dark:text-gray-400 mt-1">
                      Your messages are private between the two of you.
                    </p>
                  </div>
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

            {/* Input */}
            <div className="px-6 py-4 border-t border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800">
              <form onSubmit={handleSend} className="flex gap-3 items-end">
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
                  placeholder={connected
                    ? `Message ${targetProfile?.name ?? ''}...`
                    : 'Connecting to chat...'
                  }
                  disabled={!connected}
                  className="form-input resize-none disabled:opacity-60 disabled:cursor-not-allowed flex-1 min-w-0 h-12 !py-2.5 overflow-hidden"
                />
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

          </div>
        </div>
      </main>
    </div>
  )
}

export default DirectMessage
