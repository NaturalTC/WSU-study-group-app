import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'
import { resendVerification } from '../api/auth'

function VerifyPending() {
  const { state } = useLocation()
  const [email, setEmail]         = useState(state?.email ?? '')
  const [loading, setLoading]     = useState(false)
  const [status, setStatus]       = useState(null) // 'success' | 'error'
  const [errorMsg, setErrorMsg]   = useState('')

  const handleResend = async (e) => {
    e.preventDefault()
    if (!email) return
    setLoading(true)
    setStatus(null)
    setErrorMsg('')
    try {
      await resendVerification(email)
      setStatus('success')
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to resend. Please try again.'
      setErrorMsg(msg)
      setStatus('error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="flex-1 bg-wsu-chalk flex items-center justify-center px-6 pt-24 pb-12">
        <div className="w-full max-w-md">
          <div className="card animate-fade-up text-center">

            <div className="w-12 h-12 bg-blue-700 rounded-xl flex items-center justify-center mx-auto mb-4 shadow">
              <span className="text-white font-display text-xl font-bold">W</span>
            </div>

            <h1 className="font-display text-3xl text-wsu-navy mb-2">Check your email</h1>
            <p className="text-wsu-slate text-sm mb-6">
              We sent a verification link to your WSU email. Click the link to activate your account before logging in.
            </p>

            {/* Resend section */}
            <div className="border-t border-gray-100 pt-6 mt-2">
              <p className="text-sm text-wsu-slate mb-4">Didn't get it? Enter your email and we'll send a new link.</p>

              {status === 'success' ? (
                <div className="bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg px-4 py-3">
                  Verification email sent! Check your inbox.
                </div>
              ) : (
                <form onSubmit={handleResend} className="space-y-3">
                  <input
                    type="email"
                    required
                    placeholder="you@westfield.ma.edu"
                    className="form-input"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                  />
                  {status === 'error' && (
                    <p className="text-red-600 text-sm">{errorMsg}</p>
                  )}
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-2.5 rounded-lg transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Sending...' : 'Resend verification email'}
                  </button>
                </form>
              )}
            </div>

            <Link to="/login" className="block mt-6 text-blue-700 font-semibold hover:underline text-sm">
              Back to Login
            </Link>

          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

export default VerifyPending
