import { formatDateTime, formatINR } from '../../utils/formatters'

export default function BookingSummary({ trip, seats }) {
  const total = seats.length * Number(trip.price)

  return (
    <aside className="rounded-2xl border border-slate-200 bg-white p-5">
      <h2 className="text-lg font-bold text-slate-900">Booking summary</h2>
      <p className="mt-2 text-sm text-slate-600">{trip.route.source} to {trip.route.destination}</p>
      <p className="text-sm text-slate-600">{formatDateTime(trip.departureTime)}</p>
      <div className="my-4 border-t border-slate-200 pt-4">
        <p className="text-sm font-semibold text-slate-800">Seats: {seats.map((seat) => seat.seatNumber).join(', ')}</p>
        <p className="mt-1 text-sm text-slate-600">{formatINR(trip.price)} per seat</p>
      </div>
      <div className="flex items-center justify-between border-t border-slate-200 pt-4 font-bold">
        <span>Total</span>
        <span>{formatINR(total)}</span>
      </div>
    </aside>
  )
}
