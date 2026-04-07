// ── StudyGroupCard ─────────────────────────────────────────────────
// Displays a single study group's info.
// TODO: Props will eventually come from GET /api/study-groups

function StudyGroupCard({ group, onJoin, onViewDetails }) {
  const spotsLeft = group.maxMembers === 999 ? 999 : group.maxMembers - group.currentMembers

  return (
    <div className="card flex flex-col gap-4 hover:scale-[1.01] transition-transform duration-200">

      {/* Top Row: Course Tag + Status */}
      <div className="flex items-center justify-between">
        <span className="inline-block bg-blue-50 text-blue-700 text-xs font-semibold px-3 py-1 rounded-full">
          {group.courseCode}
        </span>
        <span className="text-xs font-semibold px-3 py-1 rounded-full bg-green-50 text-green-600">
          Open
        </span>
      </div>

      {/* Group Name */}
      <div>
        <h3 className="font-display text-xl text-wsu-navy mb-1">{group.name}</h3>
        <p className="text-wsu-slate text-sm leading-relaxed">{group.description}</p>
      </div>

      {/* Details Grid */}
      <div className="grid grid-cols-2 gap-3 text-sm">
        <div className="flex items-center gap-2 text-wsu-slate">
          <span>📚</span>
          <span>{group.courseName}</span>
        </div>
        <div className="flex items-center gap-2 text-wsu-slate">
          <span>📅</span>
          <span>{group.schedule}</span>
        </div>
        <div className="flex items-center gap-2 text-wsu-slate">
          <span>📍</span>
          <span>{group.location}</span>
        </div>
        <div className="flex items-center gap-2 text-wsu-slate">
          <span>👥</span>
          <span>{group.currentMembers}/{group.maxMembers} members</span>
        </div>
      </div>

      {/* Members Avatars */}
      <div className="flex items-center gap-2">
        <div className="flex -space-x-2">
          {group.members.slice(0, 4).map((member, i) => (
            <div
              key={i}
              className="w-7 h-7 rounded-full bg-wsu-navy text-white text-xs flex items-center justify-center border-2 border-white font-semibold"
            >
              {member.charAt(0).toUpperCase()}
            </div>
          ))}
          {group.members.length > 4 && (
            <div className="w-7 h-7 rounded-full bg-gray-200 text-gray-500 text-xs flex items-center justify-center border-2 border-white font-semibold">
              +{group.members.length - 4}
            </div>
          )}
        </div>
        <span className="text-xs text-wsu-slate ml-1">
          {group.members.slice(0, 2).join(', ')}
          {group.members.length > 2 ? ` +${group.members.length - 2} more` : ''}
        </span>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3 mt-auto pt-2 border-t border-gray-100">
        <button
          onClick={() => onViewDetails(group)}
          className="btn-secondary flex-1 text-sm py-2"
        >
          View Details
        </button>
        <button
          onClick={() => onJoin(group)}
          disabled={spotsLeft === 0}
          className="flex-1 text-sm py-2 bg-blue-700 hover:bg-blue-800 text-white font-semibold rounded-lg transition-all duration-200 shadow-md disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {spotsLeft === 0 ? 'Full' : 'Join Group'}
        </button>
      </div>
    </div>
  )
}

export default StudyGroupCard