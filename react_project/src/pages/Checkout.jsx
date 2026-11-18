import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import bookingApi from '../api/bookingApi'
import BookingSummary from '../components/booking/BookingSummary'
import CountdownTimer from '../components/seats/CountdownTimer'
import ErrorBanner from '../components/common/ErrorBanner'
import Loader from '../components/common/Loader'
import PassengerForm from '../components/booking/PassengerForm'
import useToast from '../hooks/useToast'
import { validatePassenger } from '../utils/validators'

function isValidHold(state, tripId) {
  return state?.tripId === tripId
    && state.trip
    && Array.isArray(state.seats)
    && state.seats.length > 0
    && Array.isArray(state.seatIds)
    && state.seatIds.length === state.seats.length
    && new Date(state.expiresAt).getTime() > Date.now()
}

export default function Checkout() {
  const { tripId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const addToast = useToast()
  const state = location.state
  const validHold = isValidHold(state, tripId)
  const keyRef = useRef(null)
  if (!keyRef.current) keyRef.current = crypto.randomUUID()
  const [passengers, setPassengers] = useState(() => (
    Array.isArray(state?.seats) ? state.seats.map(() => ({ name: '', age: '', gender: '' })) : []
  ))
  const [contactPhone, setContactPhone] = useState('')
  const [contactPhoneError, setContactPhoneError] = useState('')
  const [errors, setErrors] = useState({})
  const [requestError, setRequestError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const returnToSeats = useCallback(() => {
    addToast('Your hold expired. Please choose seats again.', 'error')
    navigate(`/trips/${tripId}/seats`, { replace: true })
  }, [addToast, navigate, tripId])

  useEffect(() => {
    if (!validHold) returnToSeats()
  }, [validHold, returnToSeats])

  const updatePassenger = (index, field, value) => {
    setPassengers((current) => current.map((passenger, item) => (
      item === index ? { ...passenger, [field]: value } : passenger
    )))
    setErrors((current) => ({
      ...current,
      [index]: { ...current[index], [field]: '' },
    }))
  }

  const submit = async (event) => {
    event.preventDefault()
    const nextErrors = Object.fromEntries(
      state.seats.map((seat, index) => [index, validatePassenger(passengers[index], seat)]),
    )
    setErrors(nextErrors)
    const phone = contactPhone.trim()
    const validPhone = /^\+[1-9][0-9]{7,14}$/.test(phone)
    setContactPhoneError(validPhone ? '' : 'Enter a valid phone number starting with + and country code (8–15 digits).')
    if (!validPhone || Object.values(nextErrors).some((item) => Object.keys(item).length)) return
    if (Date.now() >= new Date(state.expiresAt).getTime()) return returnToSeats()

    setSubmitting(true)
    setRequestError('')
    try {
      const booking = await bookingApi.create({
        tripId: Number(tripId),
        contactPhone: phone,
        passengers: state.seats.map((seat, index) => ({
          seatId: seat.seatId,
          name: passengers[index].name.trim(),
          age: Number(passengers[index].age),
          gender: passengers[index].gender,
        })),
      }, keyRef.current)
      if (!booking.bookingId) throw new Error('Booking response did not include a booking ID.')
      navigate(`/booking-success/${booking.bookingId}`, { replace: true })
    } catch (error) {
      setRequestError(error.message || 'Unable to complete this booking.')
    } finally {
      setSubmitting(false)
    }
  }

  if (!validHold) return <Loader label="Returning to seat selection…" />

  return (
    <main className="mx-auto max-w-7xl space-y-6 px-5 py-8">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-sm font-bold uppercase tracking-wider text-emerald-700">Passenger details</p>
          <h1 className="mt-1 text-3xl font-black text-slate-950">Complete your booking</h1>
        </div>
        <CountdownTimer expiresAt={state.expiresAt} onExpire={returnToSeats} />
      </header>
      {requestError && <ErrorBanner message={requestError} />}
      <div className="grid gap-6 lg:grid-cols-[1fr_20rem]">
        <form onSubmit={submit} className="space-y-4">
          <div className="rounded-2xl border border-slate-200 bg-white p-5">
            <label htmlFor="booking-contact-phone" className="mb-1 block text-sm font-semibold">Contact phone number</label>
            <input
              id="booking-contact-phone"
              type="tel"
              autoComplete="tel"
              value={contactPhone}
              onChange={(event) => {
                setContactPhone(event.target.value)
                setContactPhoneError('')
              }}
              maxLength={16}
              placeholder="+919876543210"
              required
              aria-invalid={Boolean(contactPhoneError)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-emerald-600 focus:outline-none"
            />
            <p className="mt-1 text-sm text-slate-600">Booking and payment updates will be sent to this number and your registered email.</p>
            {contactPhoneError && <p className="mt-1 text-sm text-red-700">{contactPhoneError}</p>}
          </div>
          {state.seats.map((seat, index) => (
            <PassengerForm
              key={seat.seatId}
              seat={seat}
              value={passengers[index]}
              errors={errors[index] || {}}
              onChange={(field, value) => updatePassenger(index, field, value)}
            />
          ))}
          {requestError && (
            <Link to={`/trips/${tripId}/seats`} className="inline-block font-bold text-emerald-700">
              Return to seat selection
            </Link>
          )}
          <button type="submit" disabled={submitting} className="w-full rounded-xl bg-emerald-600 px-5 py-3 font-bold text-white hover:bg-emerald-700 disabled:opacity-50">
            {submitting ? 'Preparing payment…' : 'Continue to UPI payment'}
          </button>
        </form>
        <BookingSummary trip={state.trip} seats={state.seats} />
      </div>
    </main>
  )
}
