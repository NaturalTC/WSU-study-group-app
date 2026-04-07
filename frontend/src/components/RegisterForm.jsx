import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

// ── Mock course data ───────────────────────────────────────────────
// TODO: Replace this with a real API call to fetch available courses:
//       GET /api/courses
//       Expected response: [{ id, code, name, department }]
const MOCK_COURSES = [
  { id: 1, code: 'CS 101',  name: 'Intro to Computer Science' },
  { id: 2, code: 'CS 201',  name: 'Data Structures' },
  { id: 3, code: 'CS 301',  name: 'Algorithms' },
  { id: 4, code: 'CS 350',  name: 'Software Engineering' },
  { id: 5, code: 'MATH 160',name: 'Calculus I' },
  { id: 6, code: 'MATH 261',name: 'Calculus II' },
  { id: 7, code: 'STAT 110',name: 'Introduction to Statistics' },
  { id: 8, code: 'BIOL 101',name: 'General Biology' },
  { id: 9, code: 'PHYS 211',name: 'General Physics I' },
  { id: 10,code: 'ENGL 111',name: 'College Writing' },
]

function RegisterForm() {
  const navigate = useNavigate()

  const [step, setStep] = useState(1) // Multi-step form: step 1 = account info, step 2 = courses
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const [formData, setFormData] = useState({
    firstName:       '',
    lastName:        '',
    email:           '',
    password:        '',
    confirmPassword: '',
    major:           '',
    year:            '',
    selectedCourses: [],
  })

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  // Toggle course selection
  const toggleCourse = (courseId) => {
    const already = formData.selectedCourses.includes(courseId)
    setFormData({
      ...formData,
      selectedCourses: already
        ? formData.selectedCourses.filter((id) => id !== courseId)
        : [...formData.selectedCourses, courseId],
    })
  }

  // Step 1 validation
  const handleNextStep = (e) => {
    e.preventDefault()
    setError(null)

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }
    setStep(2)
  }

  // ─────────────────────────────────────────────────────────────────
  // handleRegister — Backend Integration Point
  //
  // ENDPOINT:  POST /api/auth/register
  //
  // REQUEST PAYLOAD:
  //   {
  //     firstName:  string,
  //     lastName:   string,
  //     email:      string   (must be @westfield.ma.edu),
  //     password:   string,
  //     major:      string,
  //     year:       string   (e.g. "Sophomore"),
  //     courseIds:  number[] (array of selected course IDs)
  //   }
  //
  // EXPECTED RESPONSE (success):
  //   {
  //     token: string  (JWT token),
  //     user: {
  //       id:        number,
  //       firstName: string,
  //       lastName:  string,
  //       email:     string,
  //       role:      string
  //     }
  //   }
  //
  // EXPECTED RESPONSE (error):
  //   { message: "Email already in use" }
  //
  // TODO:
  //   1. Store JWT token in localStorage or httpOnly cookie
  //   2. Store user info in global auth context / Redux store
  //   3. Redirect to /dashboard after successful registration
  //   4. Consider sending a verification email via backend
  // ─────────────────────────────────────────────────────────────────
  const handleRegister = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      const payload = {
        firstName:  formData.firstName,
        lastName:   formData.lastName,
        email:      formData.email,
        password:   formData.password,
        major:      formData.major,
        year:       formData.year,
        courseIds:  formData.selectedCourses,
      }

      // TODO: Replace this block with real API call
      // const response = await fetch('http://localhost:8080/api/auth/register', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify(payload),
      // })
      // const data = await response.json()
      // if (!response.ok) throw new Error(data.message)
      // localStorage.setItem('token', data.token)
      // navigate('/dashboard')

      // ── PLACEHOLDER: Simulate API delay ──
      await new Promise((res) => setTimeout(res, 1200))
      console.log('Register payload (placeholder):', payload)
      alert('Register placeholder triggered! See console for payload.')

    } catch (err) {
      setError(err.message || 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-wsu-chalk flex items-center justify-center px-6 pt-24 pb-12">
      <div className="w-full max-w-lg">

        {/* Card */}
        <div className="card animate-fade-up">

          {/* Header */}
          <div className="text-center mb-8">
            <div className="w-12 h-12 bg-blue-700 rounded-xl flex items-center justify-center mx-auto mb-4 shadow">
              <span className="text-white font-display text-xl font-bold">W</span>
            </div>
            <h1 className="font-display text-3xl text-wsu-navy mb-1">
              {step === 1 ? 'Create your account' : 'Select your courses'}
            </h1>
            <p className="text-wsu-slate text-sm">
              {step === 1
                ? 'Join thousands of WSU students studying smarter'
                : 'Choose the courses you are currently enrolled in'}
            </p>
          </div>

          {/* Step Indicator */}
          <div className="flex items-center justify-center gap-3 mb-8">
            {[1, 2].map((s) => (
              <div key={s} className="flex items-center gap-3">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold transition-all duration-300
                  ${step >= s
                    ? 'bg-blue-700 text-white shadow'
                    : 'bg-gray-200 text-gray-400'}`}
                >
                  {s}
                </div>
                {s < 2 && (
                  <div className={`w-12 h-0.5 transition-all duration-300 ${step > s ? 'bg-blue-700' : 'bg-gray-200'}`} />
                )}
              </div>
            ))}
          </div>

          {/* Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-6">
              {error}
            </div>
          )}

          {/* ── Step 1: Account Info ── */}
          {step === 1 && (
            <form onSubmit={handleNextStep} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="form-label" htmlFor="firstName">First Name</label>
                  <input
                    id="firstName"
                    name="firstName"
                    type="text"
                    required
                    placeholder="Jane"
                    className="form-input"
                    value={formData.firstName}
                    onChange={handleChange}
                  />
                </div>
                <div>
                  <label className="form-label" htmlFor="lastName">Last Name</label>
                  <input
                    id="lastName"
                    name="lastName"
                    type="text"
                    required
                    placeholder="Doe"
                    className="form-input"
                    value={formData.lastName}
                    onChange={handleChange}
                  />
                </div>
              </div>

              <div>
                <label className="form-label" htmlFor="email">WSU Email</label>
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

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="form-label" htmlFor="major">Major</label>
                  <input
                    id="major"
                    name="major"
                    type="text"
                    placeholder="Computer Science"
                    className="form-input"
                    value={formData.major}
                    onChange={handleChange}
                  />
                </div>
                <div>
                  <label className="form-label" htmlFor="year">Year</label>
                  <select
                    id="year"
                    name="year"
                    className="form-input"
                    value={formData.year}
                    onChange={handleChange}
                  >
                    <option value="">Select year</option>
                    <option>Freshman</option>
                    <option>Sophomore</option>
                    <option>Junior</option>
                    <option>Senior</option>
                    <option>Graduate</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="form-label" htmlFor="password">Password</label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  required
                  placeholder="Min. 8 characters"
                  className="form-input"
                  value={formData.password}
                  onChange={handleChange}
                />
              </div>

              <div>
                <label className="form-label" htmlFor="confirmPassword">Confirm Password</label>
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

            <button type="submit" className="w-full mt-2 bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md">
                Continue →
            </button>
            </form>
          )}

          {/* ── Step 2: Course Selection ── */}
          {step === 2 && (
            <form onSubmit={handleRegister} className="space-y-4">
              <div className="grid grid-cols-1 gap-2 max-h-72 overflow-y-auto pr-1">
                {/* TODO: Replace MOCK_COURSES with data from GET /api/courses */}
                {MOCK_COURSES.map((course) => {
                  const selected = formData.selectedCourses.includes(course.id)
                  return (
                    <button
                      key={course.id}
                      type="button"
                      onClick={() => toggleCourse(course.id)}
                      className={`flex items-center justify-between px-4 py-3 rounded-lg border-2 text-left transition-all duration-200
                        ${selected
                          ? 'border-blue-700 bg-blue-50 text-blue-700'
                          : 'border-gray-200 bg-white text-wsu-navy hover:border-wsu-slate'}`}
                    >
                      <div>
                        <span className="font-semibold text-sm">{course.code}</span>
                        <span className="text-xs text-wsu-slate ml-2">{course.name}</span>
                      </div>
                      {selected && (
                        <svg className="w-4 h-4 text-blue-700 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                      )}
                    </button>
                  )
                })}
              </div>

              <p className="text-xs text-wsu-slate text-center">
                {formData.selectedCourses.length} course{formData.selectedCourses.length !== 1 ? 's' : ''} selected
              </p>

              <div className="flex gap-3 mt-2">
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  className="flex-1 border-2 border-blue-700 text-blue-700 font-semibold px-6 py-3 rounded-lg hover:bg-blue-700 hover:text-white transition-all duration-200"
                >
                  ← Back
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 bg-blue-700 hover:bg-blue-800 text-white font-semibold px-6 py-3 rounded-lg transition-all duration-200 shadow-md flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    <>
                      <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                      </svg>
                      Creating account...
                    </>
                  ) : (
                    'Create Account'
                  )}
                </button>
              </div>
            </form>
          )}

          {/* Divider */}
          <div className="flex items-center gap-4 my-6">
            <hr className="flex-1 border-gray-200" />
            <span className="text-gray-400 text-xs">or</span>
            <hr className="flex-1 border-gray-200" />
          </div>

          {/* Login Link */}
          <p className="text-center text-sm text-wsu-slate">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-700 font-semibold hover:underline">
              Sign in
            </Link>
          </p>
        </div>

        {/* Back to home */}
        <p className="text-center mt-6 text-sm text-wsu-slate">
          <Link to="/" className="hover:text-blue-700 transition-colors">
            ← Back to home
          </Link>
        </p>

      </div>
    </div>
  )
}

export default RegisterForm