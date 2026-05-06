import { useRef } from 'react'

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

function StudyGroupCard({ group, joined, joinLoading, picLoading, onJoin, onLeave, onDelete, onUploadPic, isCreator, onViewDetails }) {
  const memberCount = group.members?.length ?? 0
  const courseCode  = group.course?.courseCode ?? ''
  const prefix      = courseCode.split(' ')[0] ?? ''
  const gradient    = getCourseGradient(courseCode)
  const fileInputRef = useRef(null)

  const handleFileChange = (e) => {
    const file = e.target.files?.[0]
    if (file) onUploadPic(group, file)
    e.target.value = ''
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-md hover:shadow-xl transition-all duration-300 hover:scale-[1.01] flex flex-col border border-transparent dark:border-gray-700 overflow-hidden">

      {/* Header — group pic if set, otherwise gradient */}
      <div className={`h-24 relative overflow-hidden flex-shrink-0 group/header ${group.groupPicURL ? '' : `bg-gradient-to-br ${gradient}`}`}>
        {group.groupPicURL ? (
          <img src={group.groupPicURL} alt={group.name} className="w-full h-full object-cover" />
        ) : (
          <>
            <span className="absolute -right-2 -bottom-3 text-7xl font-display font-black text-white/15 select-none leading-none">{prefix}</span>
            <div className="absolute -left-4 -top-4 w-20 h-20 rounded-full bg-white/10" />
            <div className="absolute right-16 top-2 w-10 h-10 rounded-full bg-white/10" />
          </>
        )}

        {/* Camera upload overlay — creator only, appears on hover */}
        {isCreator && (
          <>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleFileChange}
              disabled={picLoading}
            />
            <div
              onClick={(e) => { e.stopPropagation(); fileInputRef.current?.click() }}
              className="absolute inset-0 bg-black/0 group-hover/header:bg-black/30 transition-all duration-200 cursor-pointer flex items-center justify-center"
            >
              <div className="opacity-0 group-hover/header:opacity-100 transition-opacity duration-200 flex flex-col items-center gap-1">
                {picLoading ? (
                  <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  <>
                    <svg className="w-6 h-6 text-white drop-shadow" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <span className="text-white text-[10px] font-semibold drop-shadow">Change photo</span>
                  </>
                )}
              </div>
            </div>
          </>
        )}

        <div className="absolute top-3 right-3 flex items-center gap-1.5">
          {isCreator && (
            <button
              onClick={(e) => { e.stopPropagation(); onDelete(group) }}
              className="w-7 h-7 flex items-center justify-center rounded-full bg-black/30 backdrop-blur-sm hover:bg-red-500/80 text-white transition-colors"
              title="Delete group"
            >
              <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          )}
          {joined ? (
            <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-black/30 backdrop-blur-sm text-white">Joined</span>
          ) : (
            <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-black/30 backdrop-blur-sm text-white">Open</span>
          )}
        </div>
        <div className="absolute bottom-3 left-4">
          <span className="text-xs font-bold text-white drop-shadow uppercase tracking-widest">{courseCode}</span>
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
              member.profilePicURL ? (
                <img key={i} src={member.profilePicURL} alt={member.name} className="w-7 h-7 rounded-full object-cover border-2 border-white dark:border-gray-800" />
              ) : (
                <div key={i} className="w-7 h-7 rounded-full bg-wsu-navy dark:bg-blue-800 text-white text-xs flex items-center justify-center border-2 border-white dark:border-gray-800 font-semibold">
                  {member.name?.charAt(0)?.toUpperCase() ?? '?'}
                </div>
              )
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
