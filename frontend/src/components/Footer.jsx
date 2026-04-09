import { Link } from 'react-router-dom'

function Footer() {
  return (
    <footer className="bg-wsu-navy text-white/70">

      {/* Main Footer Content */}
      <div className="max-w-7xl mx-auto px-6 py-16 grid grid-cols-1 md:grid-cols-4 gap-10">

        {/* Brand Column */}
        <div className="md:col-span-1">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-9 h-9 bg-blue-700 rounded-lg flex items-center justify-center shadow">
              <span className="text-white font-display text-lg font-bold">W</span>
            </div>
            <span className="font-display text-xl text-white">WSU StudyGroup</span>
          </div>
          <p className="text-sm leading-relaxed text-white/50">
            The social study platform built exclusively for Westfield State University students.
          </p>
        </div>

        {/* Product Links */}
        <div>
          <h4 className="text-white font-semibold text-sm uppercase tracking-widest mb-4">
            Product
          </h4>
          <ul className="space-y-3 text-sm">
            <li><a href="#features" className="hover:text-white transition-colors">Features</a></li>
            <li><a href="#how-it-works" className="hover:text-white transition-colors">How It Works</a></li>
            <li><a href="#about" className="hover:text-white transition-colors">About</a></li>
            {/* TODO: Add link to AI assistant page when built */}
            <li><span className="text-white/30 cursor-not-allowed">AI Assistant (coming soon)</span></li>
          </ul>
        </div>

        {/* Account Links */}
        <div>
          <h4 className="text-white font-semibold text-sm uppercase tracking-widest mb-4">
            Account
          </h4>
          <ul className="space-y-3 text-sm">
            <li>
              <Link to="/register" className="hover:text-white transition-colors">
                Create Account
              </Link>
            </li>
            <li>
              <Link to="/login" className="hover:text-white transition-colors">
                Log In
              </Link>
            </li>
            {/* TODO: Add /forgot-password route when backend auth is ready */}
            <li><span className="text-white/30 cursor-not-allowed">Forgot Password (coming soon)</span></li>
          </ul>
        </div>

        {/* Legal & Contact Links */}
        <div>
          <h4 className="text-white font-semibold text-sm uppercase tracking-widest mb-4">
            Company
          </h4>
          <ul className="space-y-3 text-sm">
            {/* TODO: Create /contact page */}
            <li><a href="#" className="hover:text-white transition-colors">Contact Us</a></li>
            {/* TODO: Create /privacy page */}
            <li><a href="#" className="hover:text-white transition-colors">Privacy Policy</a></li>
            {/* TODO: Create /terms page */}
            <li><a href="#" className="hover:text-white transition-colors">Terms of Service</a></li>
            {/* TODO: Create /accessibility page */}
            <li><a href="#" className="hover:text-white transition-colors">Accessibility</a></li>
          </ul>
        </div>
      </div>

      {/* Bottom Bar */}
      <div className="border-t border-white/10">
        <div className="max-w-7xl mx-auto px-6 py-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-white/40">
          <p>Built by WSU Software Engineering students</p>
        </div>
      </div>

    </footer>
  )
}

export default Footer