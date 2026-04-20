import { useState } from 'react'
import { Link } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'

function ForgotPassword() {
  const [email, setEmail]     = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError]     = useState(null)
  const [sent, setSent]       = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const res = await fetch('http://localhost:8080/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      })
      const text = await res.text()
      if (!res.ok) throw new Error(text)
      setSent(true)
    } catch (err) {
      setError(err.message || 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="flex-1 bg-wsu-chalk flex items-center justify-center px-6 pt-24 pb-12">
        <div className="w-full max-w-md">
          <div className="card animate-fade-up">

            <div className="text-center mb-8">
              <div className="w-12 h-12 bg-blue-700 rounded-xl flex items-center justify-center mx-auto mb-4 shadow">
                <span className="text-white font-display text-xl font-bold">W</span>
              </div>
              <h1 className="font-display text-3xl text-wsu-navy mb-1">Forgot Password</h1>
              <p className="text-wsu-slate text-sm">Enter your WSU email and we'll send you a reset link</p>
            </div>

            {sent ? (
              <div className="text-center">
                <div className="bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg px-4 py-3 mb-6">
                  Reset link sent! Check your WSU email inbox.
                </div>
                <Link to="/login" className="text-blue-700 font-semibold hover:underline text-sm">
                  Back to Login
                </Link>
              </div>
            ) : (
              <>
                {error && (
                  <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-6">
                    {error}
                  </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-5">
                  <div>
                    <label className="form-label" htmlFor="email">WSU Email</label>
                    <input
                      id="email"
                      type="email"
                      required
                      placeholder="you@westfield.ma.edu"
                      className="form-input"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                    />
                  </div>
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md disabled:opacity-60 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Sending...' : 'Send Reset Link'}
                  </button>
                </form>
              </>
            )}

          </div>

          <p className="text-center mt-6 text-sm text-wsu-slate">
            <Link to="/login" className="hover:text-wsu-crimson transition-colors">
              ← Back to login
            </Link>
          </p>
        </div>
      </main>
      <Footer />
    </div>
  )
}

export default ForgotPassword
