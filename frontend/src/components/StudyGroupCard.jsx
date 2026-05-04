function getCourseGradient(courseCode) {
  const prefix = (courseCode ?? '').split(' ')[0].toUpperCase()
  if (prefix.startsWith('CAIS') || prefix.startsWith('CIS') || prefix.startsWith('CS'))
    return 'from-blue-500 to-indigo-700'
  if (prefix.startsWith('MATH') || prefix.startsWith('STAT'))
    return 'from-violet-500 to-purple-700'
  if (prefix.startsWith('BIOL') || prefix.startsWith('CHEM') || prefix.startsWith('PHYS') || prefix.startsWith('ENVS'))
    return 'from-emerald-500 to-teal-700'
  if (prefix.startsWith('PSYC') || prefix.startsWith('SOCI') || prefix.startsWith('ANTH'))
    return 'from-orange-500 to-amber-600'
  if (prefix.startsWith('HIST') || prefix.startsWith('ENGL') || prefix.startsWith('PHIL') || prefix.startsWith('LITR'))
    return 'from-rose-500 to-red-700'
  if (prefix.startsWith('BUSN') || prefix.startsWith('ACCT') || prefix.startsWith('MGMT') || prefix.startsWith('MKTG'))
    return 'from-cyan-600 to-blue-700'
  if (prefix.startsWith('NURS') || prefix.startsWith('HLTH'))
    return 'from-teal-500 to-cyan-600'
  if (prefix.startsWith('CRJU') || prefix.startsWith('POLI'))
    return 'from-slate-500 to-gray-700'
  if (prefix.startsWith('COMM') || prefix.startsWith('JOUR'))
    return 'from-pink-500 to-rose-600'
  if (prefix.startsWith('EDUC'))
    return 'from-amber-500 to-yellow-600'
  return 'from-wsu-navy to-blue-900'
}

function StudyGroupCard({ group, joined, joinLoading, onJoin, onLeave, onViewDetails }) {
  const memberCount = group.members?.length ?? 0
  const courseCode  = group.course?.courseCode ?? ''
  const prefix      = courseCode.split(' ')[0] ?? ''
  const gradient    = getCourseGradient(courseCode)

  return (
    <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-md hover:shadow-xl transition-all duration-300 hover:scale-[1.01] flex flex-col border border-transparent dark:border-gray-700 overflow-hidden">

      {/* Gradient header */}
      <div className={`h-24 bg-gradient-to-br ${gradient} relative overflow-hidden flex-shrink-0`}>
        <span className="absolute -right-2 -bottom-3 text-7xl font-display font-black text-white/15 select-none leading-none">
          {prefix}
        </span>
        <div className="absolute -left-4 -top-4 w-20 h-20 rounded-full bg-white/10" />
        <div className="absolute right-16 top-2 w-10 h-10 rounded-full bg-white/10" />
        <div className="absolute top-3 right-3">
          {joined ? (
            <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-white/20 backdrop-blur-sm text-white">Joined</span>
          ) : (
            <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-white/20 backdrop-blur-sm text-white">Open</span>
          )}
        </div>
        <div className="absolute bottom-3 left-4">
          <span className="text-xs font-bold text-white/80 uppercase tracking-widest">{courseCode}</span>
        </div>
      </div>

      {/* Content */}
      <div className="p-5 flex flex-col gap-3 flex-1">
        <div>
          <h3 className="font-display text-lg text-wsu-navy dark:text-white mb-0.5 leading-snug">{group.name}</h3>
          <p className="text-wsu-slate dark:text-gray-400 text-sm">{group.course?.courseName}</p>
        </div>

        {/* Member avatars + count */}
        <div className="flex items-center gap-2 mt-auto">
          <div className="flex -space-x-2">
            {(group.members ?? []).slice(0, 4).map((member, i) => (
              <div key={i} className="w-7 h-7 rounded-full bg-wsu-navy dark:bg-blue-800 text-white text-xs flex items-center justify-center border-2 border-white dark:border-gray-800 font-semibold">
                {member.name?.charAt(0)?.toUpperCase() ?? '?'}
              </div>
            ))}
          </div>
          <span className="text-xs text-wsu-slate dark:text-gray-400">
            {memberCount} {memberCount === 1 ? 'member' : 'members'}
          </span>
        </div>

        {/* Actions */}
        <div className="flex gap-3 pt-3 border-t border-gray-100 dark:border-gray-700">
          <button
            onClick={() => onViewDetails(group)}
            className="flex-1 text-sm py-2 border-2 border-wsu-navy dark:border-gray-500 text-wsu-navy dark:text-gray-200 font-semibold rounded-lg hover:bg-wsu-navy hover:text-white dark:hover:bg-gray-700 transition-all duration-200"
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
    </div>
  )
}

export default StudyGroupCard
