import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import authApi from '../../api/authApi'
import useAuth from '../../hooks/useAuth'
import useToast from '../../hooks/useToast'

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate(values, mode) {
  const errors = {}
  if (mode === 'register' && (values.name.trim().length < 2 || values.name.trim().length > 100)) {
    errors.name = 'Name must be between 2 and 100 characters.'
  }
  if (!emailPattern.test(values.email.trim())) errors.email = 'Enter a valid email address.'
  if (values.password.length < 8) errors.password = 'Password must be at least 8 characters.'
  if (mode === 'register' && values.confirmPassword !== values.password) {
    errors.confirmPassword = 'Passwords do not match.'
  }
  return errors
}

export default function AuthForm({ mode }) {
  const isRegister = mode === 'register'
  const [values, setValues] = useState({ name: '', email: '', password: '', confirmPassword: '' })
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { login } = useAuth()
  const addToast = useToast()
  const navigate = useNavigate()
  const location = useLocation()

  const update = (event) => {
    setValues((current) => ({ ...current, [event.target.name]: event.target.value }))
    setErrors((current) => ({ ...current, [event.target.name]: '' }))
  }

  const submit = async (event) => {
    event.preventDefault()
    const validationErrors = validate(values, mode)
    setErrors(validationErrors)
    setFormError('')
    if (Object.keys(validationErrors).length) return

    setSubmitting(true)
    try {
      if (isRegister) {
        await authApi.register({ name: values.name.trim(), email: values.email.trim(), password: values.password })
        addToast('Account created. You can now sign in.')
        navigate('/login', { replace: true })
      } else {
        const user = await login({ email: values.email.trim(), password: values.password })
        const returnPath = location.state?.from?.pathname || '/'
        navigate(user.role === 'ADMIN' ? '/admin' : returnPath, { replace: true })
      }
    } catch (error) {
      setFormError(error.message || 'Unable to complete your request.')
    } finally {
      setSubmitting(false)
    }
  }

  const fields = [
    ...(isRegister ? [{ name: 'name', label: 'Full name', type: 'text', autoComplete: 'name' }] : []),
    { name: 'email', label: 'Email address', type: 'email', autoComplete: 'email' },
    { name: 'password', label: 'Password', type: 'password', autoComplete: isRegister ? 'new-password' : 'current-password' },
    ...(isRegister ? [{ name: 'confirmPassword', label: 'Confirm password', type: 'password', autoComplete: 'new-password' }] : []),
  ]

  return (
    <section className="mx-auto w-full max-w-md rounded-3xl border border-slate-200 bg-white p-7 shadow-sm sm:p-9">
      <p className="text-sm font-bold uppercase tracking-[0.18em] text-emerald-700">SeatSync account</p>
      <h1 className="mt-2 text-3xl font-black text-slate-900">{isRegister ? 'Create your account' : 'Welcome back'}</h1>
      <p className="mt-2 text-sm text-slate-600">{isRegister ? 'Book your next bus trip with confidence.' : 'Sign in to continue to your trip.'}</p>
      <form className="mt-7 space-y-4" onSubmit={submit} noValidate>
        {fields.map((field) => (
          <div key={field.name}>
            <label htmlFor={field.name} className="mb-1.5 block text-sm font-semibold text-slate-700">{field.label}</label>
            <input
              id={field.name}
              name={field.name}
              type={field.type}
              autoComplete={field.autoComplete}
              value={values[field.name]}
              onChange={update}
              aria-invalid={Boolean(errors[field.name])}
              aria-describedby={errors[field.name] ? `${field.name}-error` : undefined}
              className="w-full rounded-xl border border-slate-300 px-3.5 py-3 outline-none transition focus:border-emerald-600 focus:ring-2 focus:ring-emerald-100"
            />
            {errors[field.name] && <p id={`${field.name}-error`} className="mt-1 text-sm text-red-700">{errors[field.name]}</p>}
          </div>
        ))}
        {formError && <p className="rounded-xl bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">{formError}</p>}
        <button
          type="submit"
          disabled={submitting}
          className="flex w-full items-center justify-center rounded-xl bg-emerald-600 px-4 py-3 font-bold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {submitting ? 'Please wait…' : isRegister ? 'Create account' : 'Sign in'}
        </button>
      </form>
      <p className="mt-6 text-center text-sm text-slate-600">
        {isRegister ? 'Already have an account?' : 'New to SeatSync?'}{' '}
        <Link className="font-bold text-emerald-700 hover:text-emerald-800" to={isRegister ? '/login' : '/register'}>
          {isRegister ? 'Sign in' : 'Create an account'}
        </Link>
      </p>
    </section>
  )
}
