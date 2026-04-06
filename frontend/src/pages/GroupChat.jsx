import { useState, useEffect, useRef } from 'react'
import { useParams } from 'react-router-dom'
import Header from '../components/Header'
import ChatMessage from '../components/ChatMessage'
import MembersSidebar from '../components/MembersSidebar'

// ── Mock Data ──────────────────────────────────────────────────────
// TODO: Replace with real API call:
//       GET /api/study-groups/{id}
//       Expected response: { id, name, courseCode, courseName, members }
const MOCK_GROUP_DATA = {
  1: { id: 1, name: 'CS 201 Weekend Grind',        courseCode: 'CS 201',   courseName: 'Data Structures'    },
  3: { id: 3, name: 'Calc II Crew',                courseCode: 'MATH 261', courseName: 'Calculus II'        },
  4: { id: 4, name: 'Software Eng. Capstone Group', courseCode: 'CS 350',  courseName: 'Software Engineering'},
}

// TODO: Replace with real presence data from WebSocket:
//       WebSocket event: { type: 'PRESENCE', members: [...] }
const MOCK_ONLINE_MEMBERS = [
  { name: 'Alex Johnson',  isYou: true,  status: 'Active'   },
  { name: 'Jordan Smith',  isYou: false, status: 'Viewing'  },
  { name: 'Sam Williams',  isYou: false, status: 'Typing...' },
]

// ── Mock AI responses ──────────────────────────────────────────────
// TODO: Replace with real AI API call:
//       POST /api/ai/chat
//       Request:  { sessionId, groupId, userId, message }
//       Response: { reply: string }
//       Consider using WebSocket to broadcast AI reply to all members
const AI_RESPONSES = [
  "Great question! Let me break that down for you. In data structures, a binary search tree maintains the property that left child nodes are smaller than the parent, and right child nodes are larger.",
  "To solve this problem, you'll want to think about the time complexity first. What's the size of your input? That will help determine the best approach.",
  "That's a common concept in this course! Think of it like organizing a library — you want to be able to find any book quickly without checking every single one.",
  "For your exam, focus on understanding the core concepts rather than memorizing. Can you explain why this algorithm works, not just how it works?",
  "Good thinking! You're on the right track. The key insight here is that we can reduce the problem to a smaller subproblem at each step.",
]

function getTimestamp() {
  return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function GroupChat() {
  const { groupId } = useParams()
  const parsedGroupId = parseInt(groupId) || 1
  const currentGroup = MOCK_GROUP_DATA[parsedGroupId] || MOCK_GROUP_DATA[1]

  const [messages, setMessages]   = useState([])
  const [input, setInput]         = useState('')
  const [isAITyping, setIsAITyping] = useState(false)
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const messagesEndRef = useRef(null)
  const inputRef = useRef(null)

  // Auto scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // Welcome message on load
  useEffect(() => {
    setMessages([
      {
        id: 1,
        sender: 'system',
        text: `Welcome to the ${currentGroup.name} study session!`,
        timestamp: getTimestamp(),
      },
      {
        id: 2,
        sender: 'AI',
        text: `Hi everyone! 👋 I'm your AI Study Assistant for ${currentGroup.courseName}. Ask me anything about the course material and I'll help the whole group understand it!`,
        timestamp: getTimestamp(),
      },
      {
        id: 3,
        sender: 'system',
        text: 'Jordan Smith joined the session',
        timestamp: getTimestamp(),
      },
      {
        id: 4,
        sender: 'Jordan Smith',
        text: 'Hey everyone! Ready to study. Should we start with last week\'s lecture?',
        timestamp: getTimestamp(),
      },
    ])
  }, [parsedGroupId])

  // ── handleSendMessage ──────────────────────────────────────────
  // Sends a student message and triggers an AI response.
  //
  // TODO: Replace with real WebSocket broadcast:
  //       socket.emit('message', { groupId, userId, text: input })
  //
  // TODO: Replace AI response with real API call:
  //       POST /api/ai/chat
  //       Request:  { sessionId, groupId, userId, message: input }
  //       Response: { reply: string }
  //       Then broadcast AI reply via WebSocket to all group members:
  //       socket.on('ai-response', (data) => addMessage(data))
  // ──────────────────────────────────────────────────────────────
  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!input.trim()) return

    const userMessage = {
      id: Date.now(),
      sender: 'Alex Johnson', // TODO: Replace with real logged-in user's full name
      text: input.trim(),
      timestamp: getTimestamp(),
    }

    setMessages((prev) => [...prev, userMessage])
    setInput('')
    inputRef.current?.focus()

    // Show AI typing indicator
    setIsAITyping(true)
    const typingMessage = {
      id: 'typing',
      sender: 'AI',
      text: '',
      isTyping: true,
      timestamp: getTimestamp(),
    }
    setMessages((prev) => [...prev, typingMessage])

    // Simulate AI response delay
    // TODO: Remove this simulation when real WebSocket + AI API is connected
    await new Promise((res) => setTimeout(res, 1500 + Math.random() * 1000))

    const aiReply = AI_RESPONSES[Math.floor(Math.random() * AI_RESPONSES.length)]

    setMessages((prev) => [
      ...prev.filter((m) => m.id !== 'typing'),
      {
        id: Date.now() + 1,
        sender: 'AI',
        text: aiReply,
        timestamp: getTimestamp(),
      },
    ])
    setIsAITyping(false)
  }

  // Handle Enter key to send
  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      handleSendMessage(e)
    }
  }

  return (
    <div className="flex flex-col min-h-screen bg-wsu-chalk">
      <Header />

      <main className="flex-1 pt-20 pb-0 max-w-7xl mx-auto w-full px-4 md:px-6">
        <div className="flex gap-6 h-[calc(100vh-5rem)] py-4">

          {/* ── Chat Area ─────────────────────────────────────── */}
          <div className="flex-1 flex flex-col bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden min-w-0">

            {/* Chat Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-white">
              <div className="flex items-center gap-4">
                {/* Mobile sidebar toggle */}
                <button
                  onClick={() => setSidebarOpen(!sidebarOpen)}
                  className="lg:hidden w-8 h-8 flex items-center justify-center rounded-lg hover:bg-wsu-mist transition-colors"
                >
                  <svg className="w-5 h-5 text-wsu-navy" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                </button>

                <div className="w-10 h-10 bg-blue-700 rounded-xl flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                  {currentGroup.courseCode.split(' ')[0].charAt(0)}
                </div>
                <div>
                  <h1 className="font-display text-lg text-wsu-navy leading-tight">{currentGroup.name}</h1>
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 bg-green-500 rounded-full" />
                    <span className="text-xs text-wsu-slate">
                      {MOCK_ONLINE_MEMBERS.length} members online · {currentGroup.courseCode}
                    </span>
                  </div>
                </div>
              </div>

              {/* AI Status Badge */}
              <div className="flex items-center gap-2 bg-blue-50 border border-blue-100 px-3 py-1.5 rounded-full">
                <span className="text-sm">🤖</span>
                <span className="text-xs font-semibold text-blue-700">AI Assistant Active</span>
              </div>
            </div>

            {/* Messages Area */}
            <div className="flex-1 overflow-y-auto px-6 py-4 space-y-1">
              {messages.map((message) => (
                <ChatMessage key={message.id} message={message} />
              ))}
              <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div className="px-6 py-4 border-t border-gray-100 bg-white">
              {/* AI typing notice */}
              {isAITyping && (
                <p className="text-xs text-blue-600 mb-2 animate-pulse2">
                  🤖 AI Assistant is generating a response for everyone...
                </p>
              )}

              <form onSubmit={handleSendMessage} className="flex gap-3">
                <div className="flex-1 relative">
                  <textarea
                    ref={inputRef}
                    rows={1}
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={handleKeyDown}
                    placeholder="Ask the AI a question or message your group..."
                    className="form-input resize-none pr-12 py-3 leading-relaxed"
                    style={{ minHeight: '48px', maxHeight: '120px' }}
                  />
                </div>
                <button
                  type="submit"
                  disabled={!input.trim() || isAITyping}
                  className="bg-blue-700 hover:bg-blue-800 text-white px-5 py-3 rounded-lg font-semibold transition-all duration-200 shadow-md disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 flex-shrink-0"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                  </svg>
                  <span className="hidden sm:inline">Send</span>
                </button>
              </form>

              <p className="text-xs text-gray-400 mt-2 text-center">
                Press <kbd className="bg-gray-100 px-1 rounded text-gray-500">Enter</kbd> to send · 
                <kbd className="bg-gray-100 px-1 rounded text-gray-500 ml-1">Shift+Enter</kbd> for new line
              </p>
            </div>
          </div>

          {/* ── Desktop Sidebar ────────────────────────────────── */}
          <div className="hidden lg:block">
            <MembersSidebar
              activeGroupId={parsedGroupId}
              onlineMembers={MOCK_ONLINE_MEMBERS}
              currentGroup={currentGroup}
            />
          </div>

          {/* ── Mobile Sidebar Overlay ─────────────────────────── */}
          {sidebarOpen && (
            <div className="lg:hidden fixed inset-0 z-40 flex">
              <div
                className="absolute inset-0 bg-black/40 backdrop-blur-sm"
                onClick={() => setSidebarOpen(false)}
              />
              <div className="relative ml-auto w-80 h-full bg-wsu-chalk p-4 overflow-y-auto shadow-2xl animate-fade-in">
                <button
                  onClick={() => setSidebarOpen(false)}
                  className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-wsu-mist transition-colors text-wsu-slate"
                >
                  ×
                </button>
                <div className="mt-8">
                  <MembersSidebar
                    activeGroupId={parsedGroupId}
                    onlineMembers={MOCK_ONLINE_MEMBERS}
                    currentGroup={currentGroup}
                  />
                </div>
              </div>
            </div>
          )}

        </div>
      </main>
    </div>
  )
}

export default GroupChat