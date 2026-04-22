function StudyGroupCard({ group, joined, joinLoading, onJoin, onLeave, onViewDetails }) {
  const memberCount = group.members?.length ?? 0

  return (
    <div className="card flex flex-col gap-4 hover:scale-[1.01] transition-transform duration-200">

      {/* Course tag + joined badge */}
      <div className="flex items-center justify-between">
        <span className="inline-block bg-blue-50 text-blue-700 text-xs font-semibold px-3 py-1 rounded-full">
          {group.course?.courseCode}
        </span>
        {joined ? (
          <span className="text-xs font-semibold px-3 py-1 rounded-full bg-blue-50 text-blue-700">Joined</span>
        ) : (
          <span className="text-xs font-semibold px-3 py-1 rounded-full bg-green-50 text-green-600">Open</span>
        )}
      </div>

      {/* Name + course */}
      <div>
        <h3 className="font-display text-xl text-wsu-navy mb-1">{group.name}</h3>
        <p className="text-wsu-slate text-sm">{group.course?.courseName}</p>
      </div>

      {/* Member count */}
      <div className="flex items-center gap-2 text-sm text-wsu-slate">
        <span>👥</span>
        <span>{memberCount} {memberCount === 1 ? 'member' : 'members'}</span>
      </div>

      {/* Member avatars */}
      <div className="flex items-center gap-2">
        <div className="flex -space-x-2">
          {(group.members ?? []).slice(0, 4).map((member, i) => (
            <div
              key={i}
              className="w-7 h-7 rounded-full bg-wsu-navy text-white text-xs flex items-center justify-center border-2 border-white font-semibold"
            >
              {member.name?.charAt(0)?.toUpperCase() ?? '?'}
            </div>
          ))}
        </div>
        {memberCount > 0 && (
          <span className="text-xs text-wsu-slate ml-1">
            {(group.members ?? []).slice(0, 2).map(m => m.name).join(', ')}
            {memberCount > 2 ? ` +${memberCount - 2} more` : ''}
          </span>
        )}
      </div>

      {/* Actions */}
      <div className="flex gap-3 mt-auto pt-2 border-t border-gray-100">
        <button
          onClick={() => onViewDetails(group)}
          className="btn-secondary flex-1 text-sm py-2"
        >
          View Details
        </button>
        {joined ? (
          <button
            onClick={() => onLeave(group)}
            disabled={joinLoading}
            className="flex-1 text-sm py-2 bg-red-500 hover:bg-red-600 text-white font-semibold rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {joinLoading ? '...' : 'Leave'}
          </button>
        ) : (
          <button
            onClick={() => onJoin(group)}
            disabled={joinLoading}
            className="flex-1 text-sm py-2 bg-blue-700 hover:bg-blue-800 text-white font-semibold rounded-lg transition-all duration-200 shadow-md disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {joinLoading ? '...' : 'Join Group'}
          </button>
        )}
      </div>
    </div>
  )
}

export default StudyGroupCard
