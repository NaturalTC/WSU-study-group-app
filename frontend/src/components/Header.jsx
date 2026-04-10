import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'

function Header() {
  const [menuOpen, setMenuOpen] = useState(false)
  const location = useLocation()

  const isActive = (path) => location.pathname === path

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100 shadow-sm">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">

        {/* Logo */}
        <Link to="/" className="flex items-center gap-3">
          <div className="w-9 h-9 bg-blue-700 rounded-lg flex items-center justify-center shadow">
            <span className="text-white font-display text-lg font-bold">W</span>
          </div>
          <span className="font-display text-xl text-wsu-navy hidden sm:block">
            WSU StudyGroup
          </span>
        </Link>

        {/* Desktop Nav */}
        <nav className="hidden md:flex items-center gap-8">
          <a href="#features" className="text-wsu-slate hover:text-blue-700 transition-colors duration-200 font-medium text-sm">
            Features
          </a>
          <a href="#how-it-works" className="text-wsu-slate hover:text-blue-700 transition-colors duration-200 font-medium text-sm">
            How It Works
          </a>
          <a href="#about" className="text-wsu-slate hover:text-blue-700 transition-colors duration-200 font-medium text-sm">
            About
          </a>
          <Link
            to="/study-groups"
            className={`font-medium text-sm transition-colors duration-200
              ${isActive('/study-groups')
                ? 'text-blue-700 font-semibold'
                : 'text-wsu-slate hover:text-blue-700'}`}
          >
            Study Groups
          </Link>
          <Link
            to="/group-chat/1"
            className={`font-medium text-sm transition-colors duration-200
              ${location.pathname.startsWith('/group-chat')
                ? 'text-blue-700 font-semibold'
                : 'text-wsu-slate hover:text-blue-700'}`}
          >
            Group Chat
          </Link>
        </nav>

        {/* Desktop CTA Buttons */}
        <div className="hidden md:flex items-center gap-3">
          <Link
            to="/login"
            className={`text-sm font-semibold px-4 py-2 rounded-lg transition-all duration-200
              ${isActive('/login')
                ? 'bg-wsu-mist text-blue-700'
                : 'text-wsu-navy hover:text-blue-700'}`}
          >
            Log In
          </Link>
          <Link
            to="/register"
            className="bg-blue-700 hover:bg-blue-800 text-white font-semibold text-sm px-5 py-2 rounded-lg transition-all duration-200 shadow-md"
          >
            Get Started
          </Link>
        </div>

        {/* Mobile Hamburger */}
        <button
          className="md:hidden flex flex-col gap-1.5 p-2"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="Toggle menu"
        >
          <span className={`block w-6 h-0.5 bg-wsu-navy transition-all duration-300 ${menuOpen ? 'rotate-45 translate-y-2' : ''}`} />
          <span className={`block w-6 h-0.5 bg-wsu-navy transition-all duration-300 ${menuOpen ? 'opacity-0' : ''}`} />
          <span className={`block w-6 h-0.5 bg-wsu-navy transition-all duration-300 ${menuOpen ? '-rotate-45 -translate-y-2' : ''}`} />
        </button>
      </div>

      {/* Mobile Menu */}
      {menuOpen && (
        <div className="md:hidden bg-white border-t border-gray-100 px-6 py-4 flex flex-col gap-4 animate-fade-in">
          <a href="#features" className="text-wsu-slate hover:text-blue-700 font-medium" onClick={() => setMenuOpen(false)}>Features</a>
          <a href="#how-it-works" className="text-wsu-slate hover:text-blue-700 font-medium" onClick={() => setMenuOpen(false)}>How It Works</a>
          <a href="#about" className="text-wsu-slate hover:text-blue-700 font-medium" onClick={() => setMenuOpen(false)}>About</a>
          <Link to="/study-groups" className={`font-medium ${isActive('/study-groups') ? 'text-blue-700' : 'text-wsu-slate hover:text-blue-700'}`} onClick={() => setMenuOpen(false)}>Study Groups</Link>
          <Link to="/group-chat/1" className={`font-medium ${location.pathname.startsWith('/group-chat') ? 'text-blue-700' : 'text-wsu-slate hover:text-blue-700'}`} onClick={() => setMenuOpen(false)}>Group Chat</Link>
          <hr className="border-gray-100" />
          <Link to="/login" className="text-wsu-navy font-semibold" onClick={() => setMenuOpen(false)}>Log In</Link>
          <Link to="/register" className="bg-blue-700 hover:bg-blue-800 text-white font-semibold text-center rounded-lg px-6 py-3 transition-all duration-200" onClick={() => setMenuOpen(false)}>Get Started</Link>
        </div>
      )}
    </header>
  )
}

export default Header