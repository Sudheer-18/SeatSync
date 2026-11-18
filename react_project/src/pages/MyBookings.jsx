import { useEffect, useState } from 'react'
import bookingApi from '../api/bookingApi'
import BookingCard from '../components/booking/BookingCard'
import ConfirmModal from '../components/common/ConfirmModal'
import EmptyState from '../components/common/EmptyState'
import ErrorBanner from '../components/common/ErrorBanner'
import Loader from '../components/common/Loader'
import Pagination from '../components/common/Pagination'
import useToast from '../hooks/useToast'
import { formatINR } from '../utils/formatters'

const PAGE_SIZE = 10

export default function MyBookings() {
  const [bookings, setBookings] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [status, setStatus] = useState('')
  const [selected, setSelected] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [cancelError, setCancelError] = useState('')
  const [canceling, setCanceling] = useState(false)
  const [attempt, setAttempt] = useState(0)
  const addToast = useToast()

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    bookingApi.mine({ page, size: PAGE_SIZE, ...(status ? { status } : {}) })
      .then((result) => {
        if (!Array.isArray(result.content)) throw new Error('Booking list response was invalid.')
        if (!active) return
        setBookings(result.content)
        setTotalPages(result.totalPages)
      })
      .catch((requestError) => {
        if (active) setError(requestError.message || 'Unable to load your bookings.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [page, status, attempt])

  const changeStatus = (event) => {
    setStatus(event.target.value)
    setPage(0)
  }

  const cancelBooking = async () => {
    if (!selected) return
    setCanceling(true)
    setCancelError('')
    try {
      const result = await bookingApi.cancel(selected.bookingId)
      addToast(selected.status === 'PENDING_PAYMENT'
        ? 'Pending payment booking cancelled.'
        : `Booking cancelled. Refund: ${formatINR(result.refundAmount)}.`)
      setSelected(null)
      setAttempt((value) => value + 1)
    } catch (requestError) {
      setCancelError(requestError.message || 'Unable to cancel this booking.')
    } finally {
      setCanceling(false)
    }
  }

  return (
    <main className="mx-auto max-w-7xl space-y-5 px-5 py-10">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-bold uppercase tracking-wider text-emerald-700">Your trips</p>
          <h1 className="mt-1 text-3xl font-black text-slate-950">My bookings</h1>
        </div>
        <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
          Status
          <select value={status} onChange={changeStatus} className="rounded-lg border border-slate-300 bg-white px-3 py-2">
            <option value="">All bookings</option>
            <option value="PENDING_PAYMENT">Pending payment</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="EXPIRED">Expired</option>
          </select>
        </label>
      </header>
      {error && <ErrorBanner message={error} onRetry={() => setAttempt((value) => value + 1)} />}
      {loading ? <Loader label="Loading your bookings…" /> : bookings.length
        ? <div className="space-y-4">{bookings.map((booking) => (
          <BookingCard key={booking.bookingId} booking={booking} onCancel={(item) => { setSelected(item); setCancelError('') }} />
        ))}</div>
        : !error && <EmptyState title="No bookings found" message="Trips you book will appear here." />}
      {!loading && !error && <Pagination page={page} totalPages={totalPages} onChange={setPage} />}
      <ConfirmModal
        open={Boolean(selected)}
        title="Cancel this booking?"
        message={selected ? selected.status === 'PENDING_PAYMENT'
          ? `Cancel your unpaid booking from ${selected.source} to ${selected.destination}?`
          : `Cancel the trip from ${selected.source} to ${selected.destination}?` : ''}
        confirmLabel="Cancel booking"
        cancelLabel="Keep booking"
        loading={canceling}
        error={cancelError}
        onClose={() => setSelected(null)}
        onConfirm={cancelBooking}
      />
    </main>
  )
}
