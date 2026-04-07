// ── ChatMessage ────────────────────────────────────────────────────
// Renders a single chat message — either from a student or the AI.
// TODO: In production, messages will come from a WebSocket connection.
//       Backend should broadcast messages to all clients in the session.

function ChatMessage({ message }) {
  const isAI = message.sender === 'AI'
  const isSystem = message.sender === 'system'

  // System messages (e.g. "Alex joined the session")
  if (isSystem) {
    return (
      <div className="flex justify-center my-2">
        <span className="text-xs text-wsu-slate bg-wsu-mist px-4 py-1 rounded-full">
          {message.text}
        </span>
      </div>
    )
  }

  return (
    <div className={`flex gap-3 mb-4 ${isAI ? 'flex-row' : 'flex-row'}`}>

      {/* Avatar */}
      <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0 shadow-sm
        ${isAI ? 'bg-blue-700 text-white' : 'bg-wsu-navy text-white'}`}
      >
        {isAI ? '🤖' : message.sender.charAt(0).toUpperCase()}
      </div>

      {/* Bubble */}
      <div className="flex flex-col max-w-[75%]">
        {/* Sender name + timestamp */}
        <div className="flex items-center gap-2 mb-1">
          <span className={`text-xs font-semibold ${isAI ? 'text-blue-700' : 'text-wsu-navy'}`}>
            {isAI ? 'AI Study Assistant' : message.sender}
          </span>
          <span className="text-xs text-gray-400">{message.timestamp}</span>
        </div>

        {/* Message text */}
        <div className={`px-4 py-3 rounded-2xl text-sm leading-relaxed
          ${isAI
            ? 'bg-blue-50 text-wsu-navy border border-blue-100 rounded-tl-none'
            : 'bg-white text-wsu-navy border border-gray-100 shadow-sm rounded-tl-none'}`}
        >
          {/* Loading animation for AI typing */}
          {message.isTyping ? (
            <div className="flex gap-1 items-center h-5">
              <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
              <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
              <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
            </div>
          ) : (
            message.text
          )}
        </div>
      </div>
    </div>
  )
}

export default ChatMessage