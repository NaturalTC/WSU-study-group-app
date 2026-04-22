import { Link } from 'react-router-dom'

function Header() {
  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100 shadow-sm">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">

        {/* Logo — links back to home */}
        <Link to="/" className="flex items-center gap-3">
          <div className="w-9 h-9 bg-blue-700 rounded-lg flex items-center justify-center shadow">
            <span className="text-white font-display text-lg font-bold">W</span>
          </div>
          <span className="font-display text-xl text-wsu-navy hidden sm:block">
            WSU StudyGroup
          </span>
        </Link>

        {/* Auth buttons */}
        <div className="flex items-center gap-3">
          <Link
            to="/login"
            className="text-sm font-semibold px-4 py-2 rounded-lg text-wsu-navy hover:text-blue-700 transition-colors duration-200"
          >
            Log In
          </Link>
          <Link
            to="/register"
            className="bg-blue-700 hover:bg-blue-800 text-white font-semibold text-sm px-5 py-2 rounded-lg transition-all duration-200 shadow-md"
          >
            Sign Up
          </Link>
        </div>

      </div>
    </header>
  )
}

export default Header
