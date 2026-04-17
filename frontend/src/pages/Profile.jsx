import {Link} from 'react-router-dom'
import AppHeader from '../components/AppHeader'

// ─────────────────────────────────────────────────────────────────
// MOCK DATA — Backend Integration Points
//
// 1. MY STUDY GROUPS
//    ENDPOINT: GET /api/study-groups/my-groups
//    TODO: Replace MOCK_MY_GROUPS with real API call
//
// 2. MY COURSES
//    ENDPOINT: GET /courses/my
//    TODO: Replace MOCK_MY_COURSES with real API call
// ─────────────────────────────────────────────────────────────────

const MOCK_MY_GROUPS = [
    {
        id: 1,
        name: 'CS 201 Weekend Grind',
        courseCode: 'CS 201',
        courseName: 'Data Structures',
        schedule: 'Sat & Sun 2–4 PM',
        location: 'Ely Library Rm 204',
        members: ['Alex', 'Jordan', 'Sam', 'Taylor'],
        isLive: true,
    },
    {
        id: 4,
        name: 'Software Eng. Capstone Group',
        courseCode: 'CS 350',
        courseName: 'Software Engineering',
        schedule: 'Fri 1–3 PM',
        location: 'Ely Library Rm 310',
        members: ['Chris', 'Dana', 'Lee', 'Pat'],
        isLive: false,
    },
    {
        id: 3,
        name: 'Calc II Crew',
        courseCode: 'MATH 261',
        courseName: 'Calculus II',
        schedule: 'Tue & Thu 3–5 PM',
        location: 'Parenzo Hall Rm 101',
        members: ['Jamie', 'Blake', 'Avery'],
        isLive: false,
    },
]


const MOCK_MY_COURSES = [
    {
        id: 1,
        course: {courseCode: 'CAIS 0236', courseName: 'Computer Organization and Architecture', departmentCode: 'CAIS'},
        section: '001',
        semester: 'Spring 2026',
    },
    {
        id: 2,
        course: {courseCode: 'MATH 0261', courseName: 'Calculus II', departmentCode: 'MATH'},
        section: '003',
        semester: 'Spring 2026',
    },
    {
        id: 3,
        course: {courseCode: 'CAIS 0350', courseName: 'Software Engineering', departmentCode: 'CAIS'},
        section: '002',
        semester: 'Spring 2026',
    },
]

function Profile() {
    const user = {firstName: 'Student', lastName: 'User', major: 'Computer Science', year: 'Junior'}
    return (
        <div className="flex flex-col min-h-screen bg-wsu-chalk">

            <AppHeader/>

            {/* ── Main Content ─────────────────────────────────────── */}
            <main className="flex-1 pt-24 pb-12 px-6">
                <div className="max-w-7xl mx-auto">

                    {/* Welcome */}
                    <div className="mb-8">
                        <h1 className="font-display text-3xl text-wsu-navy font-bold">
                            Welcome back, {user.firstName}
                        </h1>
                        <p className="text-wsu-slate text-sm mt-1">
                            {user.year} · {user.major}
                        </p>
                    </div>

                    {/* ── Dashboard Tiles ── */}
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

                        {/* ── My Study Groups (double-width, left) ── */}
                        <div className="lg:col-span-2 card flex flex-col gap-4">
                            <div className="flex items-center justify-between">
                                <h2 className="font-display text-xl text-wsu-navy font-bold">
                                    My Study Groups
                                </h2>
                                <Link
                                    to="/study-groups"
                                    className="text-xs text-blue-700 font-semibold hover:underline"
                                >
                                    Browse all →
                                </Link>
                            </div>

                            <div className="space-y-3">
                                {MOCK_MY_GROUPS.map((group) => (
                                    <div
                                        key={group.id}
                                        className="flex items-center gap-4 bg-gray-50 rounded-xl px-4 py-3"
                                    >
                                        {/* Course badge */}
                                        <div
                                            className="w-11 h-11 bg-blue-700 rounded-xl flex items-center justify-center text-white font-display font-bold text-xs flex-shrink-0">
                                            {group.courseCode.split(' ')[0].charAt(0)}
                                        </div>

                                        {/* Info */}
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2 mb-0.5">
                                                <p className="text-sm font-semibold text-wsu-navy truncate">
                                                    {group.name}
                                                </p>
                                                {group.isLive && (
                                                    <span
                                                        className="flex items-center gap-1 bg-green-50 text-green-700 text-xs font-semibold px-2 py-0.5 rounded-full flex-shrink-0">
                                                        <span
                                                            className="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"/>
                                                        Live
                                                    </span>
                                                )}
                                            </div>
                                            <p className="text-xs text-wsu-slate">
                                                {group.courseCode} · {group.schedule}
                                            </p>
                                            <div className="flex items-center gap-2 mt-1.5">
                                                <div className="flex -space-x-1.5">
                                                    {group.members.slice(0, 4).map((member, i) => (
                                                        <div
                                                            key={i}
                                                            className="w-5 h-5 rounded-full bg-wsu-navy text-white text-xs flex items-center justify-center border border-white font-semibold"
                                                        >
                                                            {member.charAt(0)}
                                                        </div>
                                                    ))}
                                                </div>
                                                <span className="text-xs text-wsu-slate">
                                                    {group.members.length} members
                                                </span>
                                            </div>
                                        </div>

                                        {/* Go to Chat */}
                                        <Link
                                            to={`/group-chat/${group.id}`}
                                            className="flex-shrink-0 text-xs font-semibold px-3 py-1.5 bg-blue-700 hover:bg-blue-800 text-white rounded-lg transition-all duration-200"
                                        >
                                            Go to Chat
                                        </Link>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* ── Right column: two tiles stacked ── */}
                        <div className="flex flex-col gap-6">

                            {/* ── My Courses ── */}
                            <div className="card flex flex-col gap-4">
                                <div className="flex items-center justify-between">
                                    <h2 className="font-display text-xl text-wsu-navy font-bold">
                                        My Courses
                                    </h2>
                                    <span className="text-xs text-wsu-slate font-medium">
                                        {MOCK_MY_COURSES.length} enrolled
                                    </span>
                                </div>

                                <div className="space-y-3">
                                    {MOCK_MY_COURSES.map((enrollment) => (
                                        <div
                                            key={enrollment.id}
                                            className="flex items-center gap-3 bg-gray-50 rounded-xl px-4 py-3"
                                        >
                                            <div
                                                className="w-9 h-9 bg-blue-700 rounded-xl flex items-center justify-center text-white font-display font-bold text-xs flex-shrink-0">
                                                {enrollment.course.departmentCode.charAt(0)}
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <p className="text-xs font-semibold text-wsu-navy truncate">
                                                    {enrollment.course.courseCode} · {enrollment.course.courseName}
                                                </p>
                                                <p className="text-xs text-wsu-slate mt-0.5">
                                                    Section {enrollment.section} · {enrollment.semester}
                                                </p>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </main>

        </div>
    )
}

export default Profile
