import { useState } from 'react'

function todayString() {
  const date = new Date()
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
  return date.toISOString().slice(0, 10)
}

function initialSearch(initialValues) {
  const saved = JSON.parse(sessionStorage.getItem('seatsync_last_search') || '{}')
  return { from: '', to: '', date: '', ...saved, ...initialValues }
}

export default function SearchForm({ initialValues, onSubmit }) {
  const [values, setValues] = useState(() => initialSearch(initialValues))
  const [errors, setErrors] = useState({})

  const update = (event) => {
    setValues((current) => ({ ...current, [event.target.name]: event.target.value }))
    setErrors((current) => ({ ...current, [event.target.name]: '' }))
  }

  const submit = (event) => {
    event.preventDefault()
    const nextErrors = {}
    if (!values.from.trim()) nextErrors.from = 'Enter a departure city.'
    if (!values.to.trim()) nextErrors.to = 'Enter a destination city.'
    if (values.from.trim().toLowerCase() === values.to.trim().toLowerCase()) {
      nextErrors.to = 'Departure and destination must be different.'
    }
    if (!values.date || values.date < todayString()) nextErrors.date = 'Choose today or a future date.'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length) return

    sessionStorage.setItem('seatsync_last_search', JSON.stringify(values))
    onSubmit({
      from: values.from.trim().toLowerCase(),
      to: values.to.trim().toLowerCase(),
      date: values.date,
    })
  }

  return (
    <form onSubmit={submit} className="grid gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm md:grid-cols-[1fr_1fr_1fr_auto] md:items-end">
      {[
        { name: 'from', label: 'From', placeholder: 'Departure city', type: 'text' },
        { name: 'to', label: 'To', placeholder: 'Destination city', type: 'text' },
        { name: 'date', label: 'Travel date', placeholder: '', type: 'date' },
      ].map((field) => (
        <div key={field.name}>
          <label htmlFor={`search-${field.name}`} className="mb-1.5 block text-sm font-semibold text-slate-700">{field.label}</label>
          <input
            id={`search-${field.name}`}
            name={field.name}
            type={field.type}
            placeholder={field.placeholder}
            min={field.type === 'date' ? todayString() : undefined}
            maxLength={field.type === 'text' ? 100 : undefined}
            value={values[field.name]}
            onChange={update}
            aria-invalid={Boolean(errors[field.name])}
            className="w-full rounded-xl border border-slate-300 px-3 py-3 outline-none focus:border-emerald-600 focus:ring-2 focus:ring-emerald-100"
          />
          {errors[field.name] && <p className="mt-1 text-xs text-red-700">{errors[field.name]}</p>}
        </div>
      ))}
      <button type="submit" className="rounded-xl bg-emerald-600 px-6 py-3 font-bold text-white hover:bg-emerald-700">
        Search buses
      </button>
    </form>
  )
}
