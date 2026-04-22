function ChatMessage({ message, isOwn }) {
  const isSystem = message.sender === 'system'

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
    <div className={`flex gap-3 mb-4 ${isOwn ? 'flex-row-reverse' : 'flex-row'}`}>

      {/* Avatar — hidden for own messages */}
      {!isOwn && (
        <div className="w-8 h-8 rounded-full bg-wsu-navy text-white flex items-center justify-center text-sm font-bold flex-shrink-0 shadow-sm">
          {message.sender.charAt(0).toUpperCase()}
        </div>
      )}

      {/* Bubble */}
      <div className={`flex flex-col max-w-[75%] ${isOwn ? 'items-end' : 'items-start'}`}>
        <div className={`flex items-center gap-2 mb-1 ${isOwn ? 'flex-row-reverse' : 'flex-row'}`}>
          <span className="text-xs font-semibold text-wsu-navy">
            {isOwn ? 'You' : message.sender}
          </span>
          <span className="text-xs text-gray-400">{message.timestamp}</span>
        </div>

        <div className={`px-4 py-3 rounded-2xl text-sm leading-relaxed
          ${isOwn
            ? 'bg-blue-700 text-white rounded-tr-none'
            : 'bg-white text-wsu-navy border border-gray-100 shadow-sm rounded-tl-none'}`}
        >
          {message.text}
        </div>
      </div>
    </div>
  )
}

export default ChatMessage