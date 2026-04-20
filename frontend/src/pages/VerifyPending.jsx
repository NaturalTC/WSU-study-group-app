import { Link } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'

function VerifyPending() {
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
              We sent a verification link to your WSU email address. Click the link to activate your account before logging in.
            </p>

            <div className="bg-blue-50 border border-blue-200 text-blue-700 text-sm rounded-lg px-4 py-3 mb-6">
              Didn't get it? Check your spam folder or wait a minute and try registering again.
            </div>

            <Link
              to="/login"
              className="text-blue-700 font-semibold hover:underline text-sm"
            >
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
