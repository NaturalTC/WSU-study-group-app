import { useState } from 'react'
import Header from '../components/Header'
import Footer from '../components/Footer'
import StudyGroupCard from '../components/StudyGroupCard'

// ── Mock Data ──────────────────────────────────────────────────────
// TODO: Replace with real API call:
//       GET /api/study-groups
//       Expected response: [{ id, name, description, courseCode, courseName,
//                             schedule, location, maxMembers, currentMembers, members }]
const MOCK_GROUPS = [
  {
    id: 1,
    name: 'CS 201 Weekend Grind',
    description: 'We meet every weekend to work through problem sets and prep for exams together.',
    courseCode: 'CS 201',
    courseName: 'Data Structures',
    schedule: 'Sat & Sun 2–4 PM',
    location: 'Ely Library Rm 204',
    maxMembers: 999,
    currentMembers: 4,
    members: ['Alex', 'Jordan', 'Sam', 'Taylor'],
  },
  {
    id: 2,
    name: 'Algorithms Study Squad',
    description: 'Focused on mastering algorithm design. We do practice problems and whiteboard sessions.',
    courseCode: 'CS 301',
    courseName: 'Algorithms',
    schedule: 'Mon & Wed 5–7 PM',
    location: 'Zoom (online)',
    maxMembers: 999,
    currentMembers: 5,
    members: ['Morgan', 'Casey', 'Riley', 'Drew', 'Quinn'],
  },
  {
    id: 3,
    name: 'Calc II Crew',
    description: 'Working through integration techniques and series. All skill levels welcome!',
    courseCode: 'MATH 261',
    courseName: 'Calculus II',
    schedule: 'Tue & Thu 3–5 PM',
    location: 'Parenzo Hall Rm 101',
    maxMembers: 999,
    currentMembers: 3,
    members: ['Jamie', 'Blake', 'Avery'],
  },
  {
    id: 4,
    name: 'Software Eng. Capstone Group',
    description: 'Collaborating on our capstone project. Weekly syncs to check progress and blockers.',
    courseCode: 'CS 350',
    courseName: 'Software Engineering',
    schedule: 'Fri 1–3 PM',
    location: 'Ely Library Rm 310',
    maxMembers: 999,
    currentMembers: 4,
    members: ['Chris', 'Dana', 'Lee', 'Pat'],
  },
  {
    id: 5,
    name: 'Stats & Probability Pals',
    description: 'Going through homework problems and reviewing for quizzes every week.',
    courseCode: 'STAT 110',
    courseName: 'Introduction to Statistics',
    schedule: 'Wed 4–6 PM',
    location: 'Zoom (online)',
    maxMembers: 999,
    currentMembers: 2,
    members: ['Skyler', 'Reese'],
  },
  {
    id: 6,
    name: 'Bio 101 Exam Preppers',
    description: 'Focused on exam prep. We make flashcards, quiz each other, and review lecture notes.',
    courseCode: 'BIOL 101',
    courseName: 'General Biology',
    schedule: 'Sun 1–3 PM',
    location: 'Science Hall Rm 202',
    maxMembers: 999,
    currentMembers: 6,
    members: ['Frankie', 'Harley', 'Sage', 'River', 'Phoenix', 'Rowan'],
  },
]

// ── Unique courses for filter tabs ────────────────────────────────
const ALL_COURSES = ['All', ...new Set(MOCK_GROUPS.map((g) => g.courseCode))]

function StudyGroups() {
  const [search, setSearch]               = useState('')
  const [activeFilter, setActiveFilter]   = useState('All')
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [selectedGroup, setSelectedGroup] = useState(null)
  const [joinedGroups, setJoinedGroups]   = useState([])

  // ── TODO: Replace mock auth check with real auth context ──────
  // e.g. const { user } = useAuthContext()
  // if (!user) navigate('/login')
  const mockUser = { firstName: 'Student', lastName: 'User' }

  // Filter + search logic
  const filteredGroups = MOCK_GROUPS.filter((group) => {
    const matchesCourse = activeFilter === 'All' || group.courseCode === activeFilter
    const matchesSearch =
      group.name.toLowerCase().includes(search.toLowerCase()) ||
      group.courseName.toLowerCase().includes(search.toLowerCase()) ||
      group.courseCode.toLowerCase().includes(search.toLowerCase())
    return matchesCourse && matchesSearch
  })

  // ── handleJoin ────────────────────────────────────────────────
  // TODO: Call POST /api/study-groups/{id}/join
  // Request: { userId }
  // Response: { success: true, group: updatedGroup }
  const handleJoin = (group) => {
    if (joinedGroups.includes(group.id)) {
      alert(`You have already joined "${group.name}"!`)
      return
    }
    setJoinedGroups([...joinedGroups, group.id])
    alert(`Successfully joined "${group.name}"! (placeholder)`)
  }

  const handleViewDetails = (group) => {
    setSelectedGroup(group)
  }

  return (
    <div className="flex flex-col min-h-screen bg-wsu-chalk">
      <Header />

      <main className="flex-1 pt-24 pb-16">
        <div className="max-w-7xl mx-auto px-6">

          {/* Page Header */}
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-10">
            <div>
              <h1 className="font-display text-4xl text-wsu-navy mb-1">Study Groups</h1>
              <p className="text-wsu-slate">
                Find a group for your courses or start your own.
              </p>
            </div>
            <button
              onClick={() => setShowCreateModal(true)}
              className="bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md self-start md:self-auto"
            >
              + Create New Group
            </button>
          </div>

          {/* Search Bar */}
          <div className="relative mb-6">
            <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Search by group name or course..."
              className="form-input pl-12"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          {/* Course Filter Tabs */}
          <div className="flex flex-wrap gap-2 mb-8">
            {ALL_COURSES.map((course) => (
              <button
                key={course}
                onClick={() => setActiveFilter(course)}
                className={`px-4 py-2 rounded-full text-sm font-semibold transition-all duration-200
                  ${activeFilter === course
                    ? 'bg-blue-700 text-white shadow'
                    : 'bg-white text-wsu-slate border border-gray-200 hover:border-blue-700 hover:text-blue-700'}`}
              >
                {course}
              </button>
            ))}
          </div>

          {/* Results Count */}
          <p className="text-sm text-wsu-slate mb-6">
            Showing <span className="font-semibold text-wsu-navy">{filteredGroups.length}</span> group{filteredGroups.length !== 1 ? 's' : ''}
            {activeFilter !== 'All' ? ` in ${activeFilter}` : ''}
          </p>

          {/* Groups Grid */}
          {filteredGroups.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredGroups.map((group) => (
                <StudyGroupCard
                  key={group.id}
                  group={group}
                  onJoin={handleJoin}
                  onViewDetails={handleViewDetails}
                />
              ))}
            </div>
          ) : (
            <div className="text-center py-24">
              <div className="text-5xl mb-4">📭</div>
              <h3 className="font-display text-2xl text-wsu-navy mb-2">No groups found</h3>
              <p className="text-wsu-slate mb-6">Try a different search or create your own group.</p>
              <button
                onClick={() => setShowCreateModal(true)}
                className="bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200"
              >
                + Create New Group
              </button>
            </div>
          )}
        </div>
      </main>

      {/* ── View Details Modal ──────────────────────────────────── */}
      {selectedGroup && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full p-8 animate-fade-up">
            <div className="flex items-center justify-between mb-6">
              <span className="bg-blue-50 text-blue-700 text-xs font-semibold px-3 py-1 rounded-full">
                {selectedGroup.courseCode}
              </span>
              <button
                onClick={() => setSelectedGroup(null)}
                className="text-gray-400 hover:text-wsu-navy transition-colors text-2xl leading-none"
              >
                ×
              </button>
            </div>

            <h2 className="font-display text-2xl text-wsu-navy mb-2">{selectedGroup.name}</h2>
            <p className="text-wsu-slate text-sm mb-6">{selectedGroup.description}</p>

            <div className="grid grid-cols-2 gap-4 mb-6">
              {[
                { icon: '📚', label: 'Course',   value: selectedGroup.courseName },
                { icon: '📅', label: 'Schedule', value: selectedGroup.schedule },
                { icon: '📍', label: 'Location', value: selectedGroup.location },
                { icon: '👥', label: 'Members',  value: `${selectedGroup.currentMembers}/${selectedGroup.maxMembers}` },
              ].map((item) => (
                <div key={item.label} className="bg-wsu-chalk rounded-xl p-4">
                  <div className="text-lg mb-1">{item.icon}</div>
                  <div className="text-xs text-wsu-slate font-medium mb-0.5">{item.label}</div>
                  <div className="text-sm text-wsu-navy font-semibold">{item.value}</div>
                </div>
              ))}
            </div>

            {/* Members List */}
            <div className="mb-6">
              <p className="text-xs font-semibold text-wsu-slate uppercase tracking-widest mb-3">Members</p>
              <div className="flex flex-wrap gap-2">
                {selectedGroup.members.map((member, i) => (
                  <span key={i} className="bg-wsu-mist text-wsu-navy text-xs font-medium px-3 py-1 rounded-full">
                    {member}
                  </span>
                ))}
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setSelectedGroup(null)}
                className="btn-secondary flex-1 py-3"
              >
                Close
              </button>
              <button
                onClick={() => { handleJoin(selectedGroup); setSelectedGroup(null) }}
                disabled={selectedGroup.currentMembers >= selectedGroup.maxMembers}
                className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white font-semibold rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {selectedGroup.currentMembers >= selectedGroup.maxMembers ? 'Group Full' : 'Join Group'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Create Group Modal ──────────────────────────────────── */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full p-8 animate-fade-up max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h2 className="font-display text-2xl text-wsu-navy">Create a Study Group</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-gray-400 hover:text-wsu-navy transition-colors text-2xl leading-none"
              >
                ×
              </button>
            </div>

            {/* ── handleCreateGroup ───────────────────────────────
                TODO: Call POST /api/study-groups
                Request payload: {
                  name, description, courseCode, courseName,
                  schedule, location, maxMembers, creatorId
                }
                Response: { id, ...newGroup }
            ─────────────────────────────────────────────────── */}
            <form
              onSubmit={(e) => {
                e.preventDefault()
                alert('Create group placeholder triggered! See comments for API details.')
                setShowCreateModal(false)
              }}
              className="space-y-4"
            >
              <div>
                <label className="form-label">Group Name</label>
                <input type="text" required placeholder="e.g. CS 201 Weekend Grind" className="form-input" />
              </div>

              <div>
                <label className="form-label">Course</label>
                {/* TODO: Populate this dropdown from GET /api/courses */}
                <select className="form-input">
                  <option value="">Select a course</option>
                  <option>CS 101 — Intro to Computer Science</option>
                  <option>CS 201 — Data Structures</option>
                  <option>CS 301 — Algorithms</option>
                  <option>CS 350 — Software Engineering</option>
                  <option>MATH 160 — Calculus I</option>
                  <option>MATH 261 — Calculus II</option>
                  <option>STAT 110 — Introduction to Statistics</option>
                  <option>BIOL 101 — General Biology</option>
                </select>
              </div>

              <div>
                <label className="form-label">Description</label>
                <textarea
                  rows={3}
                  placeholder="What will your group focus on?"
                  className="form-input resize-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="form-label">Schedule</label>
                  <input type="text" placeholder="e.g. Mon & Wed 5–7 PM" className="form-input" />
                </div>
                <div>
                  <label className="form-label">Location</label>
                  <input type="text" placeholder="e.g. Library Rm 204" className="form-input" />
                </div>
              </div>

                <div>
                    <label className="form-label">Max Members</label>
                    <input type="text" className="form-input" value="Unlimited" readOnly />
                </div>

              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowCreateModal(false)} className="btn-secondary flex-1 py-3">
                  Cancel
                </button>
                <button type="submit" className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white font-semibold rounded-lg transition-all duration-200">
                  Create Group
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <Footer />
    </div>
  )
}

export default StudyGroups