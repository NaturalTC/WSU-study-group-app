import { useState } from 'react'
import { useEvents } from '../context/EventsContext'

function defaultDatetime() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  d.setHours(12, 0, 0, 0)
  return d.toISOString().slice(0, 16)
}

function ScheduleEventModal({ groupId, groupName, onClose }) {
  const { addEvent } = useEvents()
  const [form, setForm]       = useState({ title: '', datetime: defaultDatetime(), notes: '' })
  const [loading, setLoading] = useState(false)
  const [done, setDone]       = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await addEvent(groupId, groupName, {
        title:     form.title,
        eventDate: new Date(form.datetime).toISOString(),
        notes:     form.notes,
      })
      setDone(true)
      setTimeout(onClose, 1800)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-6 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl w-full max-w-md animate-fade-up">

        <div className="px-8 pt-8 pb-6">
          <div className="flex items-start justify-between mb-6">
            <div>
              <h2 className="font-display text-2xl text-wsu-navy dark:text-white">Schedule Event</h2>
              <p className="text-xs text-wsu-slate dark:text-gray-400 mt-0.5">{groupName}</p>
            </div>
            <button
              onClick={onClose}
              className="w-8 h-8 flex items-center justify-center rounded-xl hover:bg-wsu-mist dark:hover:bg-gray-800 text-wsu-slate dark:text-gray-400 text-xl leading-none transition-colors"
            >
              ×
            </button>
          </div>

          {done ? (
            <div className="text-center py-8">
              <div className="text-5xl mb-4">📅</div>
              <p className="font-semibold text-wsu-navy dark:text-white text-lg">Event scheduled!</p>
              <p className="text-sm text-wsu-slate dark:text-gray-400 mt-1">It will appear in your reminders.</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="form-label dark:text-gray-200">Event Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Midterm Review Session"
                  className="form-input dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:placeholder-gray-500"
                  value={form.title}
                  onChange={e => setForm(p => ({ ...p, title: e.target.value }))}
                />
              </div>

              <div>
                <label className="form-label dark:text-gray-200">Date & Time</label>
                <input
                  type="datetime-local"
                  required
                  className="form-input dark:bg-gray-800 dark:border-gray-700 dark:text-white"
                  value={form.datetime}
                  onChange={e => setForm(p => ({ ...p, datetime: e.target.value }))}
                />
              </div>

              <div>
                <label className="form-label dark:text-gray-200">
                  Notes <span className="font-normal text-wsu-slate dark:text-gray-500">(optional)</span>
                </label>
                <textarea
                  rows={3}
                  placeholder="Any details about the session..."
                  className="form-input resize-none dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:placeholder-gray-500"
                  value={form.notes}
                  onChange={e => setForm(p => ({ ...p, notes: e.target.value }))}
                />
              </div>

              <div className="flex gap-3 pt-1">
                <button
                  type="button"
                  onClick={onClose}
                  className="flex-1 py-3 rounded-xl border border-gray-200 dark:border-gray-700 text-wsu-navy dark:text-gray-200 text-sm font-semibold hover:bg-wsu-mist dark:hover:bg-gray-800 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white text-sm font-semibold rounded-xl transition-all disabled:opacity-60"
                >
                  {loading ? 'Saving...' : 'Schedule Event'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}

export default ScheduleEventModal
