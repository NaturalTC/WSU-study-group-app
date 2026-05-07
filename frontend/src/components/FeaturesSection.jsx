import { useEffect, useRef } from 'react'

// ── Mock feature data ──────────────────────────────────────────────
// TODO: These could eventually be fetched from:
//       GET /api/features  (if features become dynamic/admin-managed)
//       For now this is static content.
const features = [
  {
    id: 1,
    icon: '🤖',
    title: 'AI Study Assistant',
    description:
      'Get instant help on any subject with our built-in AI tutor. Ask questions, get explanations, and generate practice problems — available 24/7.',
    color: 'bg-blue-50 dark:bg-blue-900/30',
    accent: 'text-blue-600 dark:text-blue-400',
  },
  {
    id: 2,
    icon: '👥',
    title: 'Study Groups',
    description:
      'Find or create study groups for your courses. Schedule sessions, share notes, and collaborate with classmates in real time.',
    color: 'bg-red-50 dark:bg-red-900/30',
    accent: 'text-wsu-crimson dark:text-red-400',
  },
  {
    id: 3,
    icon: '🏆',
    title: 'Gamification & Rewards',
    description:
      'Earn points for attending sessions, helping others, and hitting study goals. Climb the leaderboard and unlock badges.',
    color: 'bg-yellow-50 dark:bg-yellow-900/30',
    accent: 'text-yellow-600 dark:text-yellow-400',
  },
  {
    id: 4,
    icon: '📅',
    title: 'Smart Scheduling',
    description:
      'Sync your class schedule and find times that work for everyone in your group. No more back-and-forth texting.',
    color: 'bg-green-50 dark:bg-green-900/30',
    accent: 'text-green-600 dark:text-green-400',
  },
  {
    id: 5,
    icon: '📚',
    title: 'Course Resources',
    description:
      'Access shared notes, past exams, and study guides organized by course. Contribute and give back to your community.',
    color: 'bg-purple-50 dark:bg-purple-900/30',
    accent: 'text-purple-600 dark:text-purple-400',
  },
  {
    id: 6,
    icon: '💬',
    title: 'Group Messaging',
    description:
      'Stay connected with your study group through built-in messaging. Share links, files, and updates in one place.',
    color: 'bg-pink-50 dark:bg-pink-900/30',
    accent: 'text-pink-600 dark:text-pink-400',
  },
]

function FeaturesSection() {
  const sectionRef = useRef(null)

  // Scroll-triggered reveal animation
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible')
          }
        })
      },
      { threshold: 0.15 }
    )

    const elements = sectionRef.current?.querySelectorAll('.reveal')
    elements?.forEach((el) => observer.observe(el))

    return () => observer.disconnect()
  }, [])

  return (
    <section
      id="features"
      ref={sectionRef}
      className="py-24 px-6 bg-wsu-chalk dark:bg-gray-900 transition-colors duration-300"
    >
      <div className="max-w-7xl mx-auto">

        {/* Section Header */}
        <div className="text-center mb-16 reveal">
          <span className="inline-block text-wsu-crimson font-semibold text-sm uppercase tracking-widest mb-3">
            Everything You Need
          </span>
          <h2 className="section-title">
            Built for the way students actually study
          </h2>
          <p className="section-subtitle mx-auto">
            From AI-powered tutoring to group coordination and gamified rewards —
            StudyNest has every tool to help you succeed.
          </p>
        </div>

        {/* Feature Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feature, index) => (
            <div
              key={feature.id}
              className="card reveal"
              style={{ transitionDelay: `${index * 80}ms` }}
            >
              {/* Icon */}
              <div className={`w-12 h-12 ${feature.color} rounded-xl flex items-center justify-center text-2xl mb-4`}>
                {feature.icon}
              </div>

              {/* Title */}
              <h3 className={`font-display text-xl font-semibold mb-2 ${feature.accent}`}>
                {feature.title}
              </h3>

              {/* Description */}
              <p className="text-wsu-slate dark:text-gray-400 text-sm leading-relaxed">
                {feature.description}
              </p>
            </div>
          ))}
        </div>

        {/* How It Works Section */}
        <div id="how-it-works" className="mt-28">
          <div className="text-center mb-14 reveal">
            <span className="inline-block text-wsu-crimson font-semibold text-sm uppercase tracking-widest mb-3">
              How It Works
            </span>
            <h2 className="section-title">Get started in minutes</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              {
                step: '01',
                title: 'Create your account',
                desc: 'Sign up with your WSU email and set up your student profile in under 2 minutes.',
              },
              {
                step: '02',
                title: 'Add your courses',
                desc: 'Select the classes you are enrolled in and instantly see active study groups.',
              },
              {
                step: '03',
                title: 'Join or start a group',
                desc: 'Connect with classmates, schedule sessions, and start studying smarter together.',
              },
            ].map((item, i) => (
              <div key={i} className="reveal text-center" style={{ transitionDelay: `${i * 100}ms` }}>
                <div className="w-14 h-14 bg-wsu-navy text-white font-display text-xl rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
                  {item.step}
                </div>
                <h3 className="font-display text-xl text-wsu-navy dark:text-white mb-2">{item.title}</h3>
                <p className="text-wsu-slate dark:text-gray-400 text-sm leading-relaxed">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>

        {/* About Section */}
        <div id="about" className="mt-28 reveal">
          <div className="bg-wsu-navy rounded-3xl p-10 md:p-16 text-center relative overflow-hidden">
            {/* Decorative background */}
            <div className="absolute top-[-40px] right-[-40px] w-64 h-64 bg-wsu-crimson opacity-10 rounded-full blur-3xl" />
            <div className="absolute bottom-[-40px] left-[-40px] w-64 h-64 bg-wsu-gold opacity-10 rounded-full blur-3xl" />

            <div className="relative z-10">
              <span className="inline-block text-wsu-gold font-semibold text-sm uppercase tracking-widest mb-4">
                About StudyNest
              </span>
              <h2 className="font-display text-3xl md:text-4xl text-white mb-6">
                Made by WSU students, for WSU students
              </h2>
              <p className="text-white/70 max-w-2xl mx-auto text-lg leading-relaxed mb-8">
                StudyNest was built as a Software Engineering final project
                to solve a real problem — connecting students who want to study
                together but have no easy way to find each other.
              </p>
              <a href="/register" className="btn-gold inline-block px-8 py-4 rounded-xl text-base">
                Join the Community
              </a>
            </div>
          </div>
        </div>

      </div>
    </section>
  )
}

export default FeaturesSection