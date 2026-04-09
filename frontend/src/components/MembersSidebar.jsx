import { Link } from 'react-router-dom'

// ── MembersSidebar ─────────────────────────────────────────────────
// Shows online members in the current session and allows switching
// between study groups the student is enrolled in.
//
// TODO: Replace mock data with real API calls:
//       GET /api/study-groups/my-groups     → groups the user is in
//       GET /api/study-groups/{id}/members  → online members in session
//       WebSocket presence events           → real-time online status

// Mock groups the logged-in student is currently in
// TODO: Replace with data from GET /api/study-groups/my-groups
const MY_GROUPS = [
  { id: 1, name: 'CS 201 Weekend Grind',       courseCode: 'CS 201'   },
  { id: 4, name: 'Software Eng. Capstone Group', courseCode: 'CS 350' },
  { id: 3, name: 'Calc II Crew',               courseCode: 'MATH 261' },
]

function MembersSidebar({ activeGroupId, onlineMembers, currentGroup }) {
  return (
    <aside className="w-72 flex-shrink-0 flex flex-col gap-6 h-full overflow-y-auto">

      {/* Current Group Info */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
        <p className="text-xs font-semibold text-wsu-slate uppercase tracking-widest mb-3">
          Current Session
        </p>
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-700 rounded-xl flex items-center justify-center text-white font-display font-bold text-sm flex-shrink-0">
            {currentGroup?.courseCode?.split(' ')[0] || 'GRP'}
          </div>
          <div>
            <p className="font-semibold text-wsu-navy text-sm leading-tight">{currentGroup?.name}</p>
            <p className="text-xs text-wsu-slate mt-0.5">{currentGroup?.courseCode}</p>
          </div>
        </div>

        {/* Live indicator */}
        <div className="flex items-center gap-2 mt-4 bg-green-50 rounded-lg px-3 py-2">
          <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
          <span className="text-xs text-green-700 font-semibold">Session Live</span>
          {/* TODO: Show real session duration from backend */}
          <span className="text-xs text-green-600 ml-auto">12:34</span>
        </div>
      </div>

      {/* Online Members */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 flex-1">
        <div className="flex items-center justify-between mb-4">
          <p className="text-xs font-semibold text-wsu-slate uppercase tracking-widest">
            Online Members
          </p>
          <span className="bg-green-100 text-green-700 text-xs font-bold px-2 py-0.5 rounded-full">
            {onlineMembers.length} online
          </span>
        </div>

        <div className="space-y-3">
          {onlineMembers.map((member, i) => (
            <div key={i} className="flex items-center gap-3">
              {/* Avatar */}
              <div className="relative">
                <div className="w-8 h-8 rounded-full bg-wsu-navy text-white text-xs font-bold flex items-center justify-center">
                  {member.name.charAt(0).toUpperCase()}
                </div>
                <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 rounded-full border-2 border-white" />
              </div>

              {/* Name + status */}
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-wsu-navy truncate">
                  {member.name}
                  {member.isYou && (
                    <span className="ml-1 text-xs text-blue-700 font-normal">(you)</span>
                  )}
                </p>
                <p className="text-xs text-wsu-slate truncate">{member.status}</p>
              </div>
            </div>
          ))}
        </div>

        {/* TODO: Add real-time typing indicator when a member is typing */}
      </div>

      {/* Switch Study Group */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
        <p className="text-xs font-semibold text-wsu-slate uppercase tracking-widest mb-4">
          My Study Groups
        </p>

        <div className="space-y-2">
          {MY_GROUPS.map((group) => {
            const isActive = group.id === activeGroupId
            return (
              <Link
                key={group.id}
                to={`/group-chat/${group.id}`}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200
                  ${isActive
                    ? 'bg-blue-700 text-white shadow'
                    : 'hover:bg-wsu-mist text-wsu-navy'}`}
              >
                <div className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold flex-shrink-0
                  ${isActive ? 'bg-white/20 text-white' : 'bg-wsu-mist text-wsu-navy'}`}>
                  {group.courseCode.split(' ')[0].charAt(0)}
                </div>
                <div className="min-w-0">
                  <p className={`text-xs font-semibold truncate ${isActive ? 'text-white' : 'text-wsu-navy'}`}>
                    {group.name}
                  </p>
                  <p className={`text-xs truncate ${isActive ? 'text-white/70' : 'text-wsu-slate'}`}>
                    {group.courseCode}
                  </p>
                </div>
                {isActive && (
                  <span className="ml-auto w-1.5 h-1.5 bg-green-400 rounded-full flex-shrink-0" />
                )}
              </Link>
            )
          })}
        </div>

        {/* Link to all study groups */}
        <Link
          to="/study-groups"
          className="flex items-center justify-center gap-2 mt-4 text-xs text-blue-700 font-semibold hover:underline"
        >
          Browse all groups →
        </Link>
      </div>

      {/* Quick Navigation */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
        <p className="text-xs font-semibold text-wsu-slate uppercase tracking-widest mb-4">
          Quick Navigation
        </p>
        <div className="space-y-1">
          {[
            { label: 'Home',         to: '/',              icon: '🏠' },
            { label: 'Study Groups', to: '/study-groups',  icon: '👥' },
            { label: 'My Profile',   to: '/profile',       icon: '👤' },
            { label: 'Leaderboard',  to: '/leaderboard',   icon: '🏆' },
          ].map((item) => (
            <Link
              key={item.to}
              to={item.to}
              className="flex items-center gap-3 px-3 py-2 rounded-xl text-wsu-slate hover:bg-wsu-mist hover:text-wsu-navy transition-all duration-200 text-sm"
            >
              <span>{item.icon}</span>
              <span className="font-medium">{item.label}</span>
            </Link>
          ))}
        </div>
      </div>

    </aside>
  )
}

export default MembersSidebar