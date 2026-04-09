import { Link } from 'react-router-dom'
import { useEffect, useRef } from 'react'

function HeroSection() {
  const heroRef = useRef(null)

  // Reveal animation on mount
  useEffect(() => {
    const elements = heroRef.current?.querySelectorAll('.reveal')
    elements?.forEach((el, i) => {
      setTimeout(() => {
        el.classList.add('visible')
      }, i * 150)
    })
  }, [])

  return (
    <section
      ref={heroRef}
      className="relative min-h-screen flex items-center justify-center overflow-hidden bg-wsu-navy"
    >
      {/* Background decorative circles */}
      <div className="absolute top-[-80px] right-[-80px] w-[400px] h-[400px] bg-wsu-crimson opacity-10 rounded-full blur-3xl" />
      <div className="absolute bottom-[-60px] left-[-60px] w-[300px] h-[300px] bg-wsu-gold opacity-10 rounded-full blur-3xl" />

      {/* Grid pattern overlay */}
      <div
        className="absolute inset-0 opacity-5"
        style={{
          backgroundImage: `linear-gradient(#fff 1px, transparent 1px), linear-gradient(90deg, #fff 1px, transparent 1px)`,
          backgroundSize: '40px 40px',
        }}
      />

      {/* Hero Content */}
      <div className="relative z-10 max-w-5xl mx-auto px-6 text-center pt-24 pb-16">

        {/* Badge */}
        <div className="reveal inline-flex items-center gap-2 bg-white/10 border border-white/20 text-white text-sm font-medium px-4 py-2 rounded-full mb-8 backdrop-blur-sm">
          <span className="w-2 h-2 bg-wsu-gold rounded-full animate-pulse2" />
          Now available for WSU students
        </div>

        {/* Headline */}
        <h1 className="reveal font-display text-5xl md:text-7xl text-white leading-tight mb-6">
          Study Smarter,{' '}
          <span className="text-wsu-gold">Together.</span>
        </h1>

        {/* Subheadline */}
        <p className="reveal text-lg md:text-xl text-white/70 max-w-2xl mx-auto mb-10 leading-relaxed">
          Connect with classmates, form study groups, get AI-powered help,
          and earn rewards — all in one place built for WSU students.
        </p>

        {/* CTA Buttons */}
        <div className="reveal flex flex-col sm:flex-row items-center justify-center gap-4 mb-16">
          <Link to="/register" className="btn-gold text-base px-8 py-4 w-full sm:w-auto text-center rounded-xl">
            Create Free Account
          </Link>
          <Link to="/login" className="border-2 border-white/30 text-white font-semibold px-8 py-4 rounded-xl hover:bg-white/10 transition-all duration-200 w-full sm:w-auto text-center">
            Log In
          </Link>
        </div>

        {/* Stats Row */}
        <div className="reveal grid grid-cols-3 gap-6 max-w-lg mx-auto">
          {[
            { value: '2,400+', label: 'Students' },
            { value: '180+',   label: 'Study Groups' },
            { value: '50+',    label: 'Courses' },
          ].map((stat) => (
            <div key={stat.label} className="text-center">
              <div className="font-display text-3xl text-wsu-gold">{stat.value}</div>
              <div className="text-white/60 text-sm mt-1">{stat.label}</div>
            </div>
          ))}
        </div>

        {/* TODO: Replace stat values above with real data from:
            GET /api/stats
            Expected response: { studentCount, groupCount, courseCount }
        */}
      </div>

      {/* Scroll indicator */}
      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex flex-col items-center gap-2 text-white/40 text-xs animate-float">
        <span>Scroll to explore</span>
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </div>
    </section>
  )
}

export default HeroSection