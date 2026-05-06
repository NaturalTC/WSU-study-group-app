import ReactMarkdown from 'react-markdown'

function ChatMessage({ message, isOwn }) {
  const isSystem = message.sender === 'system'
  const isAI     = message.sender === 'AI Assistant'

  if (isSystem) {
    return (
      <div className="flex justify-center my-2">
        <span className="text-xs text-wsu-slate bg-wsu-mist px-4 py-1 rounded-full">
          {message.text}
        </span>
      </div>
    )
  }

  if (isAI) {
    return (
      <div className="flex gap-3 mb-4">
        <div className="w-8 h-8 rounded-full bg-violet-100 dark:bg-violet-900/40 border border-violet-200 dark:border-violet-700 flex items-center justify-center text-xs font-bold text-violet-600 dark:text-violet-400 flex-shrink-0 mt-1">
          AI
        </div>
        <div className="flex flex-col max-w-[85%]">
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-semibold text-violet-700 dark:text-violet-400">AI Assistant</span>
            <span className="text-xs text-gray-400">{message.timestamp}</span>
          </div>
          <div className="px-4 py-3 rounded-2xl rounded-tl-none text-sm leading-relaxed bg-violet-50 dark:bg-violet-900/20 text-gray-800 dark:text-gray-100 border border-violet-100 dark:border-violet-800">
            <ReactMarkdown
              components={{
                strong: ({ children }) => (
                  <strong className="font-semibold text-violet-900 dark:text-violet-200">{children}</strong>
                ),
                ul: ({ children }) => (
                  <ul className="list-disc pl-4 mt-1 mb-2 space-y-0.5">{children}</ul>
                ),
                ol: ({ children }) => (
                  <ol className="list-decimal pl-4 mt-1 mb-2 space-y-1">{children}</ol>
                ),
                li: ({ children }) => (
                  <li className="leading-snug">{children}</li>
                ),
                p: ({ children }) => (
                  <p className="mb-2 last:mb-0">{children}</p>
                ),
                h1: ({ children }) => (
                  <h1 className="text-base font-bold mb-1 text-violet-900 dark:text-violet-200">{children}</h1>
                ),
                h2: ({ children }) => (
                  <h2 className="text-sm font-semibold mb-1 text-violet-900 dark:text-violet-200">{children}</h2>
                ),
                h3: ({ children }) => (
                  <h3 className="text-sm font-semibold mb-1 text-violet-800 dark:text-violet-300">{children}</h3>
                ),
                code: ({ children }) => (
                  <code className="bg-violet-100 dark:bg-violet-900/50 px-1 py-0.5 rounded text-xs font-mono">{children}</code>
                ),
                hr: () => (
                  <hr className="my-2 border-violet-200 dark:border-violet-700" />
                ),
              }}
            >
              {message.text}
            </ReactMarkdown>
          </div>
        </div>
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
            : 'bg-white dark:bg-gray-700 text-wsu-navy dark:text-gray-100 border border-gray-100 dark:border-gray-600 shadow-sm rounded-tl-none'}`}
        >
          {message.text}
        </div>
      </div>
    </div>
  )
}

export default ChatMessage
