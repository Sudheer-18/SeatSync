import { Link } from 'react-router-dom'
import { formatBusType, formatDateTime, formatINR, getDuration } from '../../utils/formatters'

export default function TripCard({ trip }) {
  return (
    <article className="grid gap-5 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm md:grid-cols-[1fr_auto] md:items-center">
      <div>
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-lg font-bold text-slate-900">{trip.busName}</h2>
          <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-bold text-emerald-800">
            {formatBusType(trip.busType)}
          </span>
        </div>
        <div className="mt-4 grid gap-3 sm:grid-cols-[1fr_auto_1fr] sm:items-center">
          <div>
            <p className="font-bold text-slate-900">{formatDateTime(trip.departureTime)}</p>
            <p className="text-sm text-slate-500">{trip.source}</p>
          </div>
          <p className="text-sm text-slate-500">{getDuration(trip.departureTime, trip.arrivalTime)}</p>
          <div className="sm:text-right">
            <p className="font-bold text-slate-900">{formatDateTime(trip.arrivalTime)}</p>
            <p className="text-sm text-slate-500">{trip.destination}</p>
          </div>
        </div>
      </div>
      <div className="flex items-center justify-between gap-5 border-t border-slate-100 pt-4 md:min-w-48 md:flex-col md:items-end md:border-l md:border-t-0 md:pl-5 md:pt-0">
        <div>
          <p className="text-xs text-slate-500">Starting from</p>
          <p className="text-2xl font-black text-slate-900">{formatINR(trip.price)}</p>
          <p className="text-xs text-slate-500">{trip.availableSeats} seats left</p>
        </div>
        <Link
          to={`/trips/${trip.tripId}/seats`}
          className="rounded-xl bg-slate-900 px-4 py-3 text-center text-sm font-bold text-white hover:bg-slate-700"
        >
          Select seats
        </Link>
      </div>
    </article>
  )
}
