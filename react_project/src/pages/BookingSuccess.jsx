import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import bookingApi from '../api/bookingApi'
import EmptyState from '../components/common/EmptyState'
import ErrorBanner from '../components/common/ErrorBanner'
import Loader from '../components/common/Loader'
import { formatDateTime, formatINR } from '../utils/formatters'
import UpiPaymentPanel from '../components/booking/UpiPaymentPanel'

export default function BookingSuccess() {
  const { bookingId } = useParams()
  const [booking, setBooking] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  const [paymentInstructions, setPaymentInstructions] = useState(null)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    setBooking(null)
    setPaymentInstructions(null)
    bookingApi.getById(bookingId)
      .then((result) => {
        if (!Array.isArray(result.seats)) throw new Error('Booking details response was invalid.')
        if (!active) return
        setBooking(result)
        if (result.status === 'PENDING_PAYMENT') {
          bookingApi.paymentInstructions(bookingId)
            .then((instructions) => { if (active) setPaymentInstructions(instructions) })
            .catch((requestError) => { if (active) setError(requestError.message || 'Unable to load payment instructions.') })
        }
      })
      .catch((requestError) => {
        if (active) setError(requestError.message || 'Unable to load booking details.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [bookingId, attempt])

  if (loading) return <Loader label="Loading your ticket…" />

  return (
    <main className="mx-auto max-w-3xl space-y-5 px-5 py-12">
      {error && <ErrorBanner message={error} onRetry={() => setAttempt((value) => value + 1)} />}
      {!error && !booking && <EmptyState title="Booking not found" />}
      {booking && (
        <section className="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
          <div className="bg-emerald-700 p-7 text-white">
            <p className="text-sm font-bold uppercase tracking-wider text-emerald-100">
              {booking.status === 'CONFIRMED' ? 'Booking confirmed' : booking.status === 'PENDING_PAYMENT' ? 'Payment pending verification' : `Booking ${booking.status.toLowerCase()}`}
            </p>
            <h1 className="mt-2 text-3xl font-black">
              {booking.status === 'CONFIRMED' ? 'Your ticket is ready' : booking.status === 'PENDING_PAYMENT' ? 'Complete UPI payment' : 'Booking status'}
            </h1>
            <p className="mt-2 text-sm text-emerald-100">Booking #{booking.bookingId}</p>
          </div>
          <div className="space-y-5 p-7">
            {booking.status === 'PENDING_PAYMENT' && paymentInstructions && (
              <UpiPaymentPanel booking={booking} instructions={paymentInstructions} />
            )}
            <div>
              <p className="text-xl font-black text-slate-900">
                {booking.route.source} to {booking.route.destination}
              </p>
              <p className="mt-1 text-sm text-slate-600">{formatDateTime(booking.departureTime)}</p>
            </div>
            <div className="divide-y divide-slate-100 rounded-xl border border-slate-200">
              {booking.seats.map((seat) => (
                <div key={seat.seatNumber} className="flex flex-wrap justify-between gap-2 p-4 text-sm">
                  <span className="font-bold text-slate-900">Seat {seat.seatNumber}</span>
                  <span className="text-slate-600">{seat.passengerName}</span>
                  <span className="text-slate-500">{seat.gender}, age {seat.age}</span>
                </div>
              ))}
            </div>
            <div className="flex flex-wrap justify-between gap-3 border-t border-slate-200 pt-4">
              <span className="font-semibold text-slate-700">Status: {booking.status}</span>
              <span className="text-xl font-black text-slate-950">{formatINR(booking.totalAmount)}</span>
            </div>
            <div className="flex flex-wrap gap-3">
              <Link to="/my-bookings" className="rounded-xl bg-slate-900 px-5 py-3 font-bold text-white">My bookings</Link>
              <Link to="/" className="rounded-xl border border-slate-300 px-5 py-3 font-bold text-slate-800">Book another trip</Link>
            </div>
          </div>
        </section>
      )}
    </main>
  )
}
