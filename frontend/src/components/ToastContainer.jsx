const TYPE_STYLES = {
  info:    { bar: 'bg-blue-600',   iconBg: 'bg-blue-50   dark:bg-blue-900/40',   iconColor: 'text-blue-600   dark:text-blue-400',   icon: 'ℹ' },
  success: { bar: 'bg-green-500',  iconBg: 'bg-green-50  dark:bg-green-900/40',  iconColor: 'text-green-600  dark:text-green-400',  icon: '✓' },
  badge:   { bar: 'bg-amber-400',  iconBg: 'bg-amber-50  dark:bg-amber-900/40',  iconColor: 'text-amber-600  dark:text-amber-400',  icon: '🏅' },
  warning: { bar: 'bg-orange-400', iconBg: 'bg-orange-50 dark:bg-orange-900/40', iconColor: 'text-orange-600 dark:text-orange-400', icon: '⚠' },
  error:   { bar: 'bg-red-500',    iconBg: 'bg-red-50    dark:bg-red-900/40',    iconColor: 'text-red-600    dark:text-red-400',    icon: '✕' },
}

function Toast({ toast, onDismiss }) {
  const s = TYPE_STYLES[toast.type] ?? TYPE_STYLES.info

  return (
    <div
      className={`flex items-start gap-3 bg-white dark:bg-gray-900 rounded-2xl shadow-xl
                  border border-gray-100 dark:border-gray-700 pl-1 pr-4 py-3 w-80
                  transition-all duration-300 ease-in-out
                  ${toast.removing ? 'opacity-0 translate-x-8 scale-95' : 'opacity-100 translate-x-0 scale-100 animate-fade-in'}`}
    >
      {/* Colored left bar */}
      <div className={`w-1 self-stretch rounded-full flex-shrink-0 ${s.bar}`} />

      {/* Icon */}
      <div className={`w-8 h-8 rounded-xl flex items-center justify-center text-sm font-bold flex-shrink-0 ${s.iconBg} ${s.iconColor}`}>
        {s.icon}
      </div>

      {/* Text */}
      <div className="flex-1 min-w-0 pt-0.5">
        <p className="text-sm font-semibold text-wsu-navy dark:text-white leading-snug">
          {toast.title}
        </p>
        {toast.description && (
          <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5 leading-snug">
            {toast.description}
          </p>
        )}
      </div>

      {/* Dismiss */}
      <button
        onClick={() => onDismiss(toast.id)}
        className="text-gray-300 dark:text-gray-600 hover:text-gray-500 dark:hover:text-gray-400 text-xl leading-none flex-shrink-0 transition-colors mt-0.5"
        aria-label="Dismiss"
      >
        ×
      </button>
    </div>
  )
}

export default function ToastContainer({ toasts, onDismiss }) {
  if (toasts.length === 0) return null
  return (
    <div className="fixed bottom-6 right-6 z-[100] flex flex-col gap-3 pointer-events-none">
      {toasts.map(toast => (
        <div key={toast.id} className="pointer-events-auto">
          <Toast toast={toast} onDismiss={onDismiss} />
        </div>
      ))}
    </div>
  )
}
