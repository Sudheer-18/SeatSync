import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import tripApi from '../api/tripApi'
import EmptyState from '../components/common/EmptyState'
import ErrorBanner from '../components/common/ErrorBanner'
import Loader from '../components/common/Loader'
import Pagination from '../components/common/Pagination'
import FilterBar from '../components/trips/FilterBar'
import SearchForm from '../components/trips/SearchForm'
import TripCard from '../components/trips/TripCard'
import { formatDate } from '../utils/formatters'

const PAGE_SIZE = 10

export default function SearchResults() {
  const [params, setParams] = useSearchParams()
  const query = params.toString()
  const criteria = new URLSearchParams(query)
  const from = criteria.get('from') || ''
  const to = criteria.get('to') || ''
  const date = criteria.get('date') || ''
  const [trips, setTrips] = useState([])
  const [pagination, setPagination] = useState({ page: 0, totalPages: 0, totalElements: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(false)
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    let active = true
    const search = new URLSearchParams(query)
    const searchFrom = search.get('from')
    const searchTo = search.get('to')
    const searchDate = search.get('date')

    if (!searchFrom || !searchTo || !searchDate) {
      setLoading(false)
      setError('Enter a route and date to search for buses.')
      return () => { active = false }
    }

    setLoading(true)
    setError('')
    tripApi.search({
      from: searchFrom,
      to: searchTo,
      date: searchDate,
      type: search.getAll('type'),
      sort: search.get('sort') || '',
      page: Number(search.get('page') || 0),
      size: PAGE_SIZE,
    })
      .then((result) => {
        if (!Array.isArray(result.content)) throw new Error('Trip search returned an invalid response.')
        if (!active) return
        setTrips(result.content)
        setPagination({
          page: result.page,
          totalPages: result.totalPages,
          totalElements: result.totalElements,
        })
      })
      .catch((requestError) => {
        if (active) setError(requestError.message || 'Unable to load trips.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [query, attempt])

  const updateFilter = (key, value) => {
    const next = new URLSearchParams(query)
    next.set('page', '0')
    if (key === 'type') {
      next.delete('type')
      value.forEach((type) => next.append('type', type))
    } else if (value) next.set(key, value)
    else next.delete(key)
    setParams(next)
  }

  const updatePage = (page) => {
    const next = new URLSearchParams(query)
    next.set('page', String(page))
    setParams(next)
  }

  const editSearch = (values) => {
    setParams(values)
    setEditing(false)
  }

  if (!from || !to || !date) {
    return (
      <main className="mx-auto max-w-7xl px-5 py-12">
        <ErrorBanner message={error} />
        <Link to="/" className="mt-4 inline-block font-bold text-emerald-700">Search for a trip</Link>
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-7xl space-y-5 px-5 py-10">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-bold uppercase tracking-wider text-emerald-700">Available trips</p>
          <h1 className="mt-1 text-3xl font-black text-slate-950">{from} to {to}</h1>
          <p className="mt-1 text-sm text-slate-600">{formatDate(date)} · {pagination.totalElements} trips found</p>
        </div>
        <button type="button" className="font-bold text-emerald-700" onClick={() => setEditing((value) => !value)}>
          {editing ? 'Close search editor' : 'Edit search'}
        </button>
      </header>
      {editing && <SearchForm initialValues={{ from, to, date }} onSubmit={editSearch} />}
      <FilterBar selectedTypes={params.getAll('type')} sort={params.get('sort') || ''} onChange={updateFilter} />
      {error && <ErrorBanner message={error} onRetry={() => setAttempt((value) => value + 1)} />}
      {loading ? <Loader label="Finding available trips…" /> : (
        trips.length
          ? <div className="space-y-4">{trips.map((trip) => <TripCard key={trip.tripId} trip={trip} />)}</div>
          : !error && <EmptyState title="No buses found for this route and date" message="Try another date or adjust your filters." />
      )}
      {!loading && !error && <Pagination page={pagination.page} totalPages={pagination.totalPages} onChange={updatePage} />}
    </main>
  )
}
