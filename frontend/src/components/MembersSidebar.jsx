import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useEvents } from '../context/EventsContext'

function formatEventDate(isoStr) {
  const d    = new Date(isoStr)
  const now  = new Date(); now.setHours(0, 0, 0, 0)
  const tom  = new Date(now); tom.setDate(tom.getDate() + 1)
  const time = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  if (d.toDateString() === now.toDateString())  return `Today · ${time}`
  if (d.toDateString() === tom.toDateString())  return `Tomorrow · ${time}`
  return d.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' }) + ` · ${time}`
}

function MembersSidebar({ activeGroupId, currentGroup, members, myGroups, onSchedule }) {
  const [tab, setTab] = useState('members')
  const { getGroupEvents, removeEvent } = useEvents()

  const events = getGroupEvents(activeGroupId)

  return (
    <aside className="w-64 flex-shrink-0 h-full">
      <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 h-full flex flex-col overflow-hidden transition-colors duration-300">

        {/* Tabs */}
        <div className="flex border-b border-gray-100 dark:border-gray-800">
          {[
            { key: 'members', label: `Members (${members.length})` },
            { key: 'groups',  label: `Groups (${myGroups.length})` },
            { key: 'events',  label: `Events${events.length > 0 ? ` (${events.length})` : ''}` },
          ].map(t => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`flex-1 py-2.5 text-xs font-semibold transition-colors duration-150 ${
                tab === t.key
                  ? 'text-blue-700 border-b-2 border-blue-700'
                  : 'text-wsu-slate dark:text-gray-400 hover:text-wsu-navy dark:hover:text-white'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* Tab content */}
        <div className="flex-1 overflow-y-auto p-4">

          {/* Members tab */}
          {tab === 'members' && (
            <div className="space-y-3">
              {members.length === 0 ? (
                <p className="text-xs text-wsu-slate dark:text-gray-500 text-center py-8">No members yet</p>
              ) : members.map((member) => (
                <div key={member.id} className="flex items-center gap-3">
                  <div className="relative flex-shrink-0">
                    {member.profilePicURL ? (
                      <img src={member.profilePicURL} alt={member.name} className="w-8 h-8 rounded-full object-cover" />
                    ) : (
                      <div className="w-8 h-8 rounded-full bg-wsu-navy dark:bg-blue-800 text-white text-xs font-bold flex items-center justify-center">
                        {member.name?.charAt(0).toUpperCase() ?? '?'}
                      </div>
                    )}
                    <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 rounded-full border-2 border-white dark:border-gray-900" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-semibold text-wsu-navy dark:text-white truncate">{member.name}</p>
                    <p className="text-xs text-wsu-slate dark:text-gray-400 truncate">{member.major ?? ''}</p>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* My Groups tab */}
          {tab === 'groups' && (
            <div className="space-y-1">
              {myGroups.length === 0 ? (
                <p className="text-xs text-wsu-slate dark:text-gray-500 text-center py-8">No groups joined yet</p>
              ) : myGroups.map((group) => {
                const isActive = group.id === activeGroupId
                const letter   = group.course?.courseCode?.split(' ')[0]?.charAt(0) ?? 'G'
                return (
                  <Link
                    key={group.id}
                    to={`/group-chat/${group.id}`}
                    className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 ${
                      isActive
                        ? 'bg-blue-700 text-white shadow-sm'
                        : 'hover:bg-wsu-mist dark:hover:bg-gray-800 text-wsu-navy dark:text-gray-200'
                    }`}
                  >
                    <div className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold flex-shrink-0 ${
                      isActive ? 'bg-white/20 text-white' : 'bg-wsu-mist dark:bg-gray-700 text-wsu-navy dark:text-gray-200'
                    }`}>
                      {letter}
                    </div>
                    <div className="min-w-0">
                      <p className={`text-xs font-semibold truncate ${isActive ? 'text-white' : 'text-wsu-navy dark:text-white'}`}>
                        {group.name}
                      </p>
                      <p className={`text-xs truncate ${isActive ? 'text-white/70' : 'text-wsu-slate dark:text-gray-400'}`}>
                        {group.course?.courseCode ?? ''}
                      </p>
                    </div>
                    {isActive && (
                      <span className="ml-auto w-1.5 h-1.5 bg-green-400 rounded-full flex-shrink-0" />
                    )}
                  </Link>
                )
              })}

              <Link
                to="/study-groups"
                className="flex items-center justify-center gap-1 mt-3 pt-3 border-t border-gray-100 dark:border-gray-800 text-xs text-blue-700 dark:text-blue-400 font-semibold hover:underline"
              >
                Browse all groups →
              </Link>
            </div>
          )}

          {/* Events tab */}
          {tab === 'events' && (
            <div>
              {events.length === 0 ? (
                <div className="text-center py-8">
                  <div className="text-3xl mb-2">📅</div>
                  <p className="text-xs text-wsu-slate dark:text-gray-400">No upcoming events for this group.</p>
                </div>
              ) : (
                <div className="space-y-2 mb-4">
                  {events.map(ev => (
                    <div
                      key={ev.id}
                      className="bg-wsu-mist dark:bg-gray-800 rounded-xl p-3 group"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0">
                          <p className="text-sm font-semibold text-wsu-navy dark:text-white leading-snug truncate">{ev.title}</p>
                          <p className="text-xs text-blue-600 dark:text-blue-400 font-medium mt-0.5">{formatEventDate(ev.eventDate)}</p>
                          {ev.notes && (
                            <p className="text-xs text-wsu-slate dark:text-gray-400 mt-1 line-clamp-2">{ev.notes}</p>
                          )}
                        </div>
                        <button
                          onClick={() => removeEvent(activeGroupId, ev.id)}
                          className="text-gray-300 dark:text-gray-600 hover:text-red-400 dark:hover:text-red-400 text-lg leading-none opacity-0 group-hover:opacity-100 transition-all flex-shrink-0"
                          title="Remove event"
                        >
                          ×
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {onSchedule && (
                <button
                  onClick={onSchedule}
                  className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl border-2 border-dashed border-gray-200 dark:border-gray-700 text-wsu-slate dark:text-gray-400 text-xs font-semibold hover:border-blue-300 hover:text-blue-600 dark:hover:border-blue-700 dark:hover:text-blue-400 transition-colors"
                >
                  <span>📅</span>
                  Schedule New Event
                </button>
              )}
            </div>
          )}

        </div>
      </div>
    </aside>
  )
}

export default MembersSidebar
