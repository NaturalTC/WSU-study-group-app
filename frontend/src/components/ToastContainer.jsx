const TYPE_STYLES = {
  system:  { bar: null,            iconBg: null,                                    iconColor: null,                                    icon: null  },
  info:    { bar: 'bg-blue-600',   iconBg: 'bg-blue-50   dark:bg-blue-900/40',   iconColor: 'text-blue-600   dark:text-blue-400',   icon: 'ℹ'  },
  success: { bar: 'bg-green-500',  iconBg: 'bg-green-50  dark:bg-green-900/40',  iconColor: 'text-green-600  dark:text-green-400',  icon: '✓'  },
  badge:   { bar: 'bg-amber-400',  iconBg: 'bg-amber-50  dark:bg-amber-900/40',  iconColor: 'text-amber-600  dark:text-amber-400',  icon: '🏅' },
  warning: { bar: 'bg-orange-400', iconBg: 'bg-orange-50 dark:bg-orange-900/40', iconColor: 'text-orange-600 dark:text-orange-400', icon: '⚠'  },
  error:   { bar: 'bg-red-500',    iconBg: 'bg-red-50    dark:bg-red-900/40',    iconColor: 'text-red-600    dark:text-red-400',    icon: '✕'  },
}

function Toast({ toast, onDismiss }) {
  const s      = TYPE_STYLES[toast.type] ?? TYPE_STYLES.info
  const isSystem = toast.type === 'system'

  return (
    <div
      className={`flex items-start gap-3 bg-white dark:bg-gray-900 rounded-2xl shadow-xl
                  border-2 border-blue-700
                  pr-4 py-3 w-80
                  ${isSystem ? 'px-4' : 'pl-1'}
                  ${toast.removing
                    ? 'opacity-0 transition-opacity duration-500 ease-in'
                    : 'animate-toast-in'}`}
    >
      {/* Colored left bar — hidden for system toasts */}
      {!isSystem && s.bar && (
        <div className={`w-1 self-stretch rounded-full flex-shrink-0 ${s.bar}`} />
      )}

      {/* Icon — hidden for system toasts */}
      {!isSystem && s.icon && (
        <div className={`w-8 h-8 rounded-xl flex items-center justify-center text-sm font-bold flex-shrink-0 ${s.iconBg} ${s.iconColor}`}>
          {s.icon}
        </div>
      )}

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
    <div className="fixed top-20 right-6 z-[100] flex flex-col gap-3 pointer-events-none">
      {toasts.map(toast => (
        <div key={toast.id} className="pointer-events-auto">
          <Toast toast={toast} onDismiss={onDismiss} />
        </div>
      ))}
    </div>
  )
}
