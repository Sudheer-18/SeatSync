import { formatDateTime, formatINR } from '../../utils/formatters'
import { Link } from 'react-router-dom'

export default function BookingCard({ booking, onCancel }) {
  const confirmed = booking.status === 'CONFIRMED'
  const pending = booking.status === 'PENDING_PAYMENT'

  return (
    <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-lg font-bold text-slate-900">{booking.source} to {booking.destination}</h2>
          <p className="mt-1 text-sm text-slate-600">{formatDateTime(booking.departureTime)}</p>
        </div>
        <span className={`rounded-full px-3 py-1 text-xs font-bold ${confirmed ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-700'}`}>
          {booking.status}
        </span>
      </div>
      <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-4">
        <div>
          <p className="text-sm text-slate-600">Seats: {booking.seatNumbers.join(', ')}</p>
          <p className="mt-1 font-bold text-slate-900">{formatINR(booking.totalAmount)}</p>
        </div>
        {pending && <Link to={`/booking-success/${booking.bookingId}`} className="font-bold text-emerald-700">Continue payment</Link>}
        {(confirmed || pending) && (
          <button type="button" onClick={() => onCancel(booking)} className="rounded-lg border border-red-200 px-4 py-2 text-sm font-bold text-red-700 hover:bg-red-50">
            Cancel booking
          </button>
        )}
      </div>
    </article>
  )
}
