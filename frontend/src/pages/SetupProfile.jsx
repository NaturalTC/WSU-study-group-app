import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'

const YEAR_OPTIONS = ['Freshman', 'Sophomore', 'Junior', 'Senior']

export default function SetupProfile() {
  const navigate = useNavigate()
  const { setProfile } = useAuth()

  const [form, setForm] = useState({ name: '', major: '', year: '', bio: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleChange = (e) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const res = await api.post('/profiles', form)
      setProfile(res.data)
      navigate('/profile')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save profile. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-wsu-chalk flex items-center justify-center px-6 py-12">
      <div className="w-full max-w-lg">

        {/* Card */}
        <div className="card animate-fade-up">

          {/* Header */}
          <div className="text-center mb-8">
            <div className="w-12 h-12 bg-blue-700 rounded-xl flex items-center justify-center mx-auto mb-4 shadow">
              <span className="text-white font-display text-xl font-bold">W</span>
            </div>
            <h1 className="font-display text-3xl text-wsu-navy mb-1">Set up your profile</h1>
            <p className="text-wsu-slate text-sm">
              Let your classmates know who you are. You can edit this any time.
            </p>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-6">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">

            <div>
              <label className="form-label" htmlFor="name">Full Name</label>
              <input
                id="name"
                name="name"
                type="text"
                required
                placeholder="e.g. Jose Jimenez"
                className="form-input"
                value={form.name}
                onChange={handleChange}
              />
            </div>

            <div>
              <label className="form-label" htmlFor="major">Major</label>
              <input
                id="major"
                name="major"
                type="text"
                required
                placeholder="e.g. Computer Science"
                className="form-input"
                value={form.major}
                onChange={handleChange}
              />
            </div>

            <div>
              <label className="form-label" htmlFor="year">Academic Year</label>
              <select
                id="year"
                name="year"
                required
                className="form-input"
                value={form.year}
                onChange={handleChange}
              >
                <option value="">Select your year</option>
                {YEAR_OPTIONS.map(y => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="form-label" htmlFor="bio">
                Bio <span className="text-wsu-slate font-normal">(optional)</span>
              </label>
              <textarea
                id="bio"
                name="bio"
                rows={3}
                placeholder="Tell your classmates a little about yourself..."
                className="form-input resize-none"
                value={form.bio}
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
                  Saving...
                </>
              ) : (
                'Save Profile'
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
