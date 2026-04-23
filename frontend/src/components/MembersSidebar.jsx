import { useState } from 'react'
import { Link } from 'react-router-dom'

function MembersSidebar({ activeGroupId, members, myGroups }) {
  const [tab, setTab] = useState('members')

  return (
    <aside className="w-60 flex-shrink-0 h-full">
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 h-full flex flex-col overflow-hidden">

        {/* Tabs */}
        <div className="flex border-b border-gray-100">
          <button
            onClick={() => setTab('members')}
            className={`flex-1 py-3 text-xs font-semibold transition-colors duration-150 ${
              tab === 'members'
                ? 'text-blue-700 border-b-2 border-blue-700'
                : 'text-wsu-slate hover:text-wsu-navy'
            }`}
          >
            Members ({members.length})
          </button>
          <button
            onClick={() => setTab('groups')}
            className={`flex-1 py-3 text-xs font-semibold transition-colors duration-150 ${
              tab === 'groups'
                ? 'text-blue-700 border-b-2 border-blue-700'
                : 'text-wsu-slate hover:text-wsu-navy'
            }`}
          >
            My Groups ({myGroups.length})
          </button>
        </div>

        {/* Tab content */}
        <div className="flex-1 overflow-y-auto p-4">

          {tab === 'members' && (
            <div className="space-y-3">
              {members.length === 0 ? (
                <p className="text-xs text-wsu-slate text-center py-8">No members yet</p>
              ) : members.map((member) => (
                <div key={member.id} className="flex items-center gap-3">
                  <div className="relative flex-shrink-0">
                    <div className="w-8 h-8 rounded-full bg-wsu-navy text-white text-xs font-bold flex items-center justify-center">
                      {member.name?.charAt(0).toUpperCase() ?? '?'}
                    </div>
                    <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 rounded-full border-2 border-white" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-semibold text-wsu-navy truncate">{member.name}</p>
                    <p className="text-xs text-wsu-slate truncate">{member.major ?? ''}</p>
                  </div>
                </div>
              ))}
            </div>
          )}

          {tab === 'groups' && (
            <div className="space-y-1">
              {myGroups.length === 0 ? (
                <p className="text-xs text-wsu-slate text-center py-8">No groups joined yet</p>
              ) : myGroups.map((group) => {
                const isActive = group.id === activeGroupId
                const letter = group.course?.courseCode?.split(' ')[0]?.charAt(0) ?? 'G'
                return (
                  <Link
                    key={group.id}
                    to={`/group-chat/${group.id}`}
                    className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 ${
                      isActive
                        ? 'bg-blue-700 text-white shadow-sm'
                        : 'hover:bg-wsu-mist text-wsu-navy'
                    }`}
                  >
                    <div className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold flex-shrink-0 ${
                      isActive ? 'bg-white/20 text-white' : 'bg-wsu-mist text-wsu-navy'
                    }`}>
                      {letter}
                    </div>
                    <div className="min-w-0">
                      <p className={`text-xs font-semibold truncate ${isActive ? 'text-white' : 'text-wsu-navy'}`}>
                        {group.name}
                      </p>
                      <p className={`text-xs truncate ${isActive ? 'text-white/70' : 'text-wsu-slate'}`}>
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
                className="flex items-center justify-center gap-1 mt-3 pt-3 border-t border-gray-100 text-xs text-blue-700 font-semibold hover:underline"
              >
                Browse all groups →
              </Link>
            </div>
          )}

        </div>
      </div>
    </aside>
  )
}

export default MembersSidebar
