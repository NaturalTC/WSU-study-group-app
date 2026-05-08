import { useEffect, useRef, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import api from '../api/axios'
import Header from '../components/Header'
import Footer from '../components/Footer'

function VerifyEmail() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [status, setStatus] = useState('verifying') // 'verifying' | 'error'
  const [errorMsg, setErrorMsg] = useState('')
  const called = useRef(false)

  useEffect(() => {
    // Strict Mode double-invocation guard
    if (called.current) return
    called.current = true

    const token = searchParams.get('token')
    if (!token) {
      setErrorMsg('Verification link is missing a token.')
      setStatus('error')
      return
    }

    api.post(`/auth/verify?token=${token}`)
      .then(() => navigate('/verify-success', { replace: true }))
      .catch(err => {
        const msg = err.response?.data?.message || err.response?.data || 'This link is invalid or has already been used.'
        setErrorMsg(typeof msg === 'string' ? msg : 'Verification failed.')
        setStatus('error')
      })
  }, [])

  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="flex-1 bg-wsu-chalk flex items-center justify-center px-6 pt-24 pb-12">
        <div className="w-full max-w-md">
          <div className="card animate-fade-up text-center">

            {status === 'verifying' && (
              <>
                <div className="w-12 h-12 bg-blue-700 rounded-xl flex items-center justify-center mx-auto mb-4 shadow">
                  <svg className="animate-spin w-6 h-6 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                  </svg>
                </div>
                <h1 className="font-display text-2xl text-wsu-navy mb-2">Verifying your email...</h1>
                <p className="text-wsu-slate text-sm">Just a moment.</p>
              </>
            )}

            {status === 'error' && (
              <>
                <div className="w-12 h-12 bg-red-600 rounded-xl flex items-center justify-center mx-auto mb-4 shadow">
                  <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </div>
                <h1 className="font-display text-2xl text-wsu-navy mb-2">Verification Failed</h1>
                <p className="text-wsu-slate text-sm mb-6">{errorMsg}</p>
                <a href="/verify-pending" className="inline-block bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md">
                  Resend verification email
                </a>
              </>
            )}

          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

export default VerifyEmail
