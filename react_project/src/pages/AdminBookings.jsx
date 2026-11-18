import { useEffect, useState } from 'react'
import adminApi from '../api/adminApi'
import DataTable from '../components/common/DataTable'
import EmptyState from '../components/common/EmptyState'
import ErrorBanner from '../components/common/ErrorBanner'
import Loader from '../components/common/Loader'
import Pagination from '../components/common/Pagination'
import { formatDateTime, formatINR } from '../utils/formatters'

const pageSize = 10

export default function AdminBookings() {
  const [filters, setFilters] = useState({ tripId: '', status: '', from: '', to: '' })
  const [applied, setApplied] = useState({})
  const [page, setPage] = useState(0)
  const [rows, setRows] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  const [reviewingId, setReviewingId] = useState(null)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    const params = Object.fromEntries(Object.entries(applied).filter(([, value]) => value))
    adminApi.listBookings({ ...params, page, size: pageSize })
      .then((result) => {
        if (!Array.isArray(result.content)) throw new Error('Admin bookings response was invalid.')
        if (active) {
          setRows(result.content)
          setTotalPages(result.totalPages)
        }
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [applied, page, attempt])

  const updateFilter = (event) => {
    setFilters((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  const applyFilters = (event) => {
    event.preventDefault()
    setPage(0)
    setApplied(filters)
  }

  const reviewPayment = async (bookingId, approve) => {
    setReviewingId(bookingId)
    setError('')
    try {
      if (approve) await adminApi.verifyPayment(bookingId)
      else await adminApi.rejectPayment(bookingId)
      setAttempt((value) => value + 1)
    } catch (requestError) {
      setError(requestError.message || 'Unable to update payment status.')
    } finally {
      setReviewingId(null)
    }
  }

  const columns = [
    { key: 'bookingId', label: 'Booking ID' },
    { key: 'userName', label: 'User' },
    { key: 'userEmail', label: 'Email' },
    { key: 'route', label: 'Route' },
    { key: 'status', label: 'Status' },
    { key: 'paymentReference', label: 'UPI reference', render: (row) => row.paymentReference || 'Not submitted' },
    { key: 'totalAmount', label: 'Amount', render: (row) => formatINR(row.totalAmount) },
    { key: 'createdAt', label: 'Created', render: (row) => formatDateTime(row.createdAt) },
    { key: 'review', label: 'Review', render: (row) => row.status !== 'PENDING_PAYMENT' || !row.paymentReference
      ? row.paymentStatus
      : <div className="flex gap-2">
        <button type="button" disabled={reviewingId === row.bookingId}
          onClick={() => reviewPayment(row.bookingId, true)} className="font-bold text-emerald-700">Verify</button>
        <button type="button" disabled={reviewingId === row.bookingId}
          onClick={() => reviewPayment(row.bookingId, false)} className="font-bold text-red-700">Reject</button>
      </div> },
  ]

  return (
    <main className="mx-auto max-w-7xl space-y-5 px-5 py-10">
      <header><p className="text-sm font-bold uppercase tracking-wider text-emerald-700">Administration</p><h1 className="mt-1 text-3xl font-black">All bookings</h1></header>
      <form onSubmit={applyFilters} className="grid gap-3 rounded-2xl border border-slate-200 bg-white p-4 sm:grid-cols-2 lg:grid-cols-5">
        <label className="text-sm font-semibold">Trip ID<input name="tripId" type="number" min="1" value={filters.tripId} onChange={updateFilter} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2" /></label>
        <label className="text-sm font-semibold">Status<select name="status" value={filters.status} onChange={updateFilter} className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2"><option value="">All</option><option value="PENDING_PAYMENT">Pending payment</option><option value="CONFIRMED">Confirmed</option><option value="CANCELLED">Cancelled</option><option value="EXPIRED">Expired</option></select></label>
        <label className="text-sm font-semibold">From<input name="from" type="date" value={filters.from} onChange={updateFilter} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2" /></label>
        <label className="text-sm font-semibold">To<input name="to" type="date" value={filters.to} onChange={updateFilter} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2" /></label>
        <button type="submit" className="self-end rounded-lg bg-slate-900 px-4 py-2 font-bold text-white">Apply filters</button>
      </form>
      {error && <ErrorBanner message={error} onRetry={() => setAttempt((value) => value + 1)} />}
      {loading ? <Loader label="Loading bookings…" /> : rows.length
        ? <DataTable columns={columns} rows={rows} />
        : !error && <EmptyState title="No bookings match these filters" />}
      {!loading && !error && <Pagination page={page} totalPages={totalPages} onChange={setPage} />}
    </main>
  )
}
