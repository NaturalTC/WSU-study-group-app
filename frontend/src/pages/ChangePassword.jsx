import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Header from '../components/Header'
import Footer from '../components/Footer'

function ChangePassword() {
  const navigate = useNavigate()

  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError]     = useState(null)
  const [success, setSuccess] = useState(false)

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)

    if (formData.newPassword !== formData.confirmPassword) {
      setError('New passwords do not match.')
      return
    }
    if (formData.newPassword.length < 8) {
      setError('New password must be at least 8 characters.')
      return
    }
    if (formData.currentPassword === formData.newPassword) {
      setError('New password must be different from your current password.')
      return
    }

    setLoading(true)
    try {
      const token = localStorage.getItem('token')
      const res = await fetch('http://localhost:8080/auth/update-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({
          currentPassword: formData.currentPassword,
          newPassword: formData.newPassword,
        }),
      })
      const text = await res.text()
      if (!res.ok) throw new Error(text)
      setSuccess(true)
      setTimeout(() => navigate('/study-groups'), 3000)
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
              <h1 className="font-display text-3xl text-wsu-navy mb-1">Change Password</h1>
              <p className="text-wsu-slate text-sm">Update your account password</p>
            </div>

            {success ? (
              <div className="text-center">
                <div className="bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg px-4 py-3 mb-6">
                  Password updated successfully! Redirecting...
                </div>
                <Link to="/study-groups" className="text-blue-700 font-semibold hover:underline text-sm">
                  Go to Study Groups
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
                    <label className="form-label" htmlFor="currentPassword">Current Password</label>
                    <input
                      id="currentPassword"
                      name="currentPassword"
                      type="password"
                      required
                      placeholder="Your current password"
                      className="form-input"
                      value={formData.currentPassword}
                      onChange={handleChange}
                    />
                  </div>

                  <div>
                    <label className="form-label" htmlFor="newPassword">New Password</label>
                    <input
                      id="newPassword"
                      name="newPassword"
                      type="password"
                      required
                      placeholder="Min. 8 characters"
                      className="form-input"
                      value={formData.newPassword}
                      onChange={handleChange}
                    />
                  </div>

                  <div>
                    <label className="form-label" htmlFor="confirmPassword">Confirm New Password</label>
                    <input
                      id="confirmPassword"
                      name="confirmPassword"
                      type="password"
                      required
                      placeholder="••••••••"
                      className="form-input"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                    />
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md disabled:opacity-60 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Updating...' : 'Update Password'}
                  </button>
                </form>
              </>
            )}

          </div>

          <p className="text-center mt-6 text-sm text-wsu-slate">
            <Link to="/study-groups" className="hover:text-wsu-crimson transition-colors">
              ← Back to Study Groups
            </Link>
          </p>
        </div>
      </main>
      <Footer />
    </div>
  )
}

export default ChangePassword
