import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import tripApi from '../api/tripApi'
import CountdownTimer from '../components/seats/CountdownTimer'
import SeatLegend from '../components/seats/SeatLegend'
import SeatMap from '../components/seats/SeatMap'
import EmptyState from '../components/common/EmptyState'
import ErrorBanner from '../components/common/ErrorBanner'
import Loader from '../components/common/Loader'
import useSeatHold from '../hooks/useSeatHold'
import useAuth from '../hooks/useAuth'
import { formatDateTime, formatINR } from '../utils/formatters'

export default function SeatSelection() {
  const { tripId } = useParams()
  const [trip, setTrip] = useState(null)
  const [seats, setSeats] = useState([])
  const [loading, setLoading] = useState(true)
  const [pageError, setPageError] = useState('')
  const [attempt, setAttempt] = useState(0)
  const navigate = useNavigate()
  const location = useLocation()
  const { user } = useAuth()

  const refreshSeats = useCallback(async () => {
    const result = await tripApi.getSeats(tripId)
    if (!Array.isArray(result.seats)) throw new Error('Seat map response was invalid.')
    setSeats(result.seats)
    return result
  }, [tripId])

  const holdState = useSeatHold(tripId, refreshSeats)

  useEffect(() => {
    let active = true
    setLoading(true)
    setPageError('')
    Promise.all([tripApi.getById(tripId), tripApi.getSeats(tripId)])
      .then(([tripResult, seatResult]) => {
        if (!Array.isArray(seatResult.seats)) throw new Error('Seat map response was invalid.')
        if (!active) return
        setTrip(tripResult)
        setSeats(seatResult.seats)
      })
      .catch((error) => {
        if (active) setPageError(error.message || 'Unable to load trip details.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [tripId, attempt])

  useEffect(() => {
    const timer = window.setInterval(() => {
      refreshSeats().catch((error) => setPageError(error.message))
    }, 10000)
    return () => window.clearInterval(timer)
  }, [refreshSeats])

  const goToCheckout = () => {
    holdState.preserveHold()
    navigate(`/checkout/${tripId}`, {
      state: {
        tripId,
        seatIds: holdState.hold.seatIds,
        expiresAt: holdState.hold.expiresAt,
        trip,
        seats: seats.filter((seat) => holdState.hold.seatIds.includes(seat.seatId)),
      },
    })
  }

  const holdSelectedSeats = () => {
    if (!user) {
      navigate('/login', { state: { from: location } })
      return
    }
    holdState.holdSeats()
  }

  if (loading) return <Loader label="Loading trip and seats…" />
  if (pageError && !trip) {
    return <main className="mx-auto max-w-7xl px-5 py-10"><ErrorBanner message={pageError} onRetry={() => setAttempt((value) => value + 1)} /></main>
  }
  if (!trip) return <main className="mx-auto max-w-7xl px-5 py-10"><EmptyState title="Trip not found" message="This trip may no longer be available." /></main>

  return (
    <main className="mx-auto max-w-7xl space-y-6 px-5 py-8 pb-32">
      <Link to="/" className="text-sm font-bold text-emerald-700">← Back to search</Link>
      <header className="rounded-2xl bg-slate-950 p-6 text-white">
        <p className="text-sm font-bold uppercase tracking-wider text-emerald-400">{trip.bus.name}</p>
        <h1 className="mt-2 text-2xl font-black">{trip.route.source} to {trip.route.destination}</h1>
        <p className="mt-2 text-sm text-slate-300">{formatDateTime(trip.departureTime)} · {formatINR(trip.price)} per seat</p>
      </header>
      {pageError && <ErrorBanner message={pageError} onRetry={() => refreshSeats().catch((error) => setPageError(error.message))} />}
      {holdState.error && <ErrorBanner message={holdState.error} />}
      {seats.length
        ? <section className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-xl font-black text-slate-900">Choose your seats</h2>
            <SeatLegend />
          </div>
          <SeatMap seats={seats} selectedIds={holdState.selectedIds} onToggle={holdState.toggleSeat} disabled={holdState.busy || Boolean(holdState.hold)} />
        </section>
        : <EmptyState title="No seats are available" message="Please check another trip." />}
      {holdState.hold && (
        <section className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-amber-300 bg-amber-50 p-4">
          <CountdownTimer expiresAt={holdState.hold.expiresAt} onExpire={holdState.expireHold} />
          <button type="button" disabled={holdState.busy} onClick={holdState.release} className="font-bold text-red-700 disabled:opacity-50">
            Release seats
          </button>
        </section>
      )}
      <section className="fixed inset-x-0 bottom-0 z-10 border-t border-slate-200 bg-white/95 p-4 shadow-lg backdrop-blur">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm text-slate-600">{holdState.selectedIds.length} seat(s) selected</p>
            <p className="text-lg font-black text-slate-950">{formatINR(holdState.selectedIds.length * Number(trip.price))}</p>
          </div>
          {holdState.hold ? (
            <button type="button" onClick={goToCheckout} className="rounded-xl bg-emerald-600 px-5 py-3 font-bold text-white hover:bg-emerald-700">
              Continue to checkout
            </button>
          ) : (
            <button
              type="button"
              onClick={holdSelectedSeats}
              disabled={!holdState.selectedIds.length || holdState.busy}
              className="rounded-xl bg-emerald-600 px-5 py-3 font-bold text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {holdState.busy ? 'Holding seats…' : user ? 'Hold seats' : 'Sign in to hold seats'}
            </button>
          )}
        </div>
      </section>
    </main>
  )
}
