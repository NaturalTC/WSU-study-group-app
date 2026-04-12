import { Link } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'

function VerifySuccess() {
  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="flex-1 bg-wsu-chalk flex items-center justify-center px-6 pt-24 pb-12">
        <div className="w-full max-w-md">
          <div className="card animate-fade-up text-center">

            <div className="w-12 h-12 bg-green-600 rounded-xl flex items-center justify-center mx-auto mb-4 shadow">
              <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>

            <h1 className="font-display text-3xl text-wsu-navy mb-2">Email Verified</h1>
            <p className="text-wsu-slate text-sm mb-6">
              Your WSU account is active. You can now sign in and start finding study groups.
            </p>

            <Link
              to="/login"
              className="inline-block bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md"
            >
              Sign In
            </Link>

          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

export default VerifySuccess
