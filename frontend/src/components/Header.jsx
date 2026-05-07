import { Link } from 'react-router-dom'
import OwlLogo from './OwlLogo'
import { useAuth } from '../context/AuthContext'

function Header() {
  const { profile } = useAuth()

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100 shadow-sm">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">

        <Link to="/" className="flex items-center gap-3">
          <div className="w-9 h-9 bg-wsu-navy rounded-xl flex items-center justify-center shadow">
            <OwlLogo size={26} />
          </div>
          <span className="font-display text-xl text-wsu-navy hidden sm:block">
            StudyNest
          </span>
        </Link>

        <div className="flex items-center gap-3">
          <Link
            to="/leaderboard"
            className="text-sm font-semibold px-4 py-2 rounded-lg text-wsu-navy hover:text-blue-700 transition-colors duration-200"
          >
            Leaderboard 🏆
          </Link>
          {profile ? (
            <Link
              to="/profile"
              className="bg-blue-700 hover:bg-blue-800 text-white font-semibold text-sm px-5 py-2 rounded-lg transition-all duration-200 shadow-md"
            >
              Go to App
            </Link>
          ) : (
            <>
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
            </>
          )}
        </div>

      </div>
    </header>
  )
}

export default Header
