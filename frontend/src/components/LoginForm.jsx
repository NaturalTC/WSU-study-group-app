import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

function LoginForm() {
  const navigate = useNavigate()

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  })

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Handle input changes
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  // ─────────────────────────────────────────────────────────────────
  // handleLogin — Backend Integration Point
  //
  // ENDPOINT:  POST /api/auth/login
  //
  // REQUEST PAYLOAD:
  //   {
  //     email:    string  (e.g. "student@westfield.ma.edu"),
  //     password: string
  //   }
  //
  // EXPECTED RESPONSE (success):
  //   {
  //     token:   string   (JWT token),
  //     user: {
  //       id:        number,
  //       firstName: string,
  //       lastName:  string,
  //       email:     string,
  //       role:      string  (e.g. "STUDENT")
  //     }
  //   }
  //
  // EXPECTED RESPONSE (error):
  //   { message: "Invalid email or password" }
  //
  // TODO:
  //   1. Store JWT token in localStorage or httpOnly cookie
  //   2. Store user info in global auth context / Redux store
  //   3. Redirect to /dashboard after successful login
  // ─────────────────────────────────────────────────────────────────
  const handleLogin = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      // TODO: Replace this block with real API call
      // const response = await fetch('http://localhost:8080/api/auth/login', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify(formData),
      // })
      // const data = await response.json()
      // if (!response.ok) throw new Error(data.message)
      // localStorage.setItem('token', data.token)
      // navigate('/dashboard')

      // ── PLACEHOLDER: Simulate API delay ──
      await new Promise((res) => setTimeout(res, 1000))
      console.log('Login payload (placeholder):', formData)
      alert('Login placeholder triggered! See console for payload.')

    } catch (err) {
      setError(err.message || 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-wsu-chalk flex items-center justify-center px-6 pt-24 pb-12">
      <div className="w-full max-w-md">

        {/* Card */}
        <div className="card animate-fade-up">

          {/* Header */}
          <div className="text-center mb-8">
            <div className="w-12 h-12 bg-blue-700 rounded-xl flex items-center justify-center mx-auto mb-4 shadow">
              <span className="text-white font-display text-xl font-bold">W</span>
            </div>
            <h1 className="font-display text-3xl text-wsu-navy mb-1">Welcome back</h1>
            <p className="text-wsu-slate text-sm">Sign in to your WSU StudyGroup account</p>
          </div>

          {/* Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-6">
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleLogin} className="space-y-5">

            <div>
              <label className="form-label" htmlFor="email">
                WSU Email
              </label>
              <input
                id="email"
                name="email"
                type="email"
                required
                placeholder="you@westfield.ma.edu"
                className="form-input"
                value={formData.email}
                onChange={handleChange}
              />
            </div>

            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="form-label" htmlFor="password">
                  Password
                </label>
                {/* TODO: Link to /forgot-password when that page is built */}
                <a href="#" className="text-blue-700 font-semibold hover:underline">
                  Forgot password?
                </a>
              </div>
              <input
                id="password"
                name="password"
                type="password"
                required
                placeholder="••••••••"
                className="form-input"
                value={formData.password}
                onChange={handleChange}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                  </svg>
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="flex items-center gap-4 my-6">
            <hr className="flex-1 border-gray-200" />
            <span className="text-gray-400 text-xs">or</span>
            <hr className="flex-1 border-gray-200" />
          </div>

          {/* Register Link */}
          <p className="text-center text-sm text-wsu-slate">
            Don't have an account?{' '}
            <Link to="/register" className="text-blue-700 font-semibold hover:underline">
              Create one free
            </Link>
          </p>
        </div>

        {/* Back to home */}
        <p className="text-center mt-6 text-sm text-wsu-slate">
          <Link to="/" className="hover:text-wsu-crimson transition-colors">
            ← Back to home
          </Link>
        </p>

      </div>
    </div>
  )
}

export default LoginForm