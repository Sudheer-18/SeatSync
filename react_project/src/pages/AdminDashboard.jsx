import { useEffect, useState } from 'react'
import adminApi from '../api/adminApi'
import StatsCard from '../components/admin/StatsCard'
import EmptyState from '../components/common/EmptyState'
import ErrorBanner from '../components/common/ErrorBanner'
import Loader from '../components/common/Loader'
import { formatINR } from '../utils/formatters'

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    adminApi.stats()
      .then((result) => {
        if (!Array.isArray(result.topRoutes)) throw new Error('Dashboard response was invalid.')
        if (active) setStats(result)
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [attempt])

  const cards = stats ? [
    ['Total bookings', stats.totalBookings],
    ['Confirmed', stats.confirmedBookings],
    ['Cancelled', stats.cancelledBookings],
    ['Revenue', formatINR(stats.totalRevenue)],
    ['Total trips', stats.totalTrips],
    ['Total users', stats.totalUsers],
  ] : []

  return (
    <main className="mx-auto max-w-7xl space-y-6 px-5 py-10">
      <header>
        <p className="text-sm font-bold uppercase tracking-wider text-emerald-700">Overview</p>
        <h1 className="mt-1 text-3xl font-black text-slate-950">Admin dashboard</h1>
      </header>
      {error && <ErrorBanner message={error} onRetry={() => setAttempt((value) => value + 1)} />}
      {loading ? <Loader label="Loading dashboard…" /> : stats && (
        <>
          <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {cards.map(([label, value]) => <StatsCard key={label} label={label} value={value} />)}
          </section>
          <section className="space-y-3">
            <h2 className="text-xl font-black text-slate-900">Top routes</h2>
            {stats.topRoutes.length
              ? <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white">
                <table className="min-w-full text-left text-sm">
                  <thead className="bg-slate-50 text-slate-600"><tr><th scope="col" className="px-4 py-3">Route</th><th scope="col" className="px-4 py-3">Bookings</th></tr></thead>
                  <tbody className="divide-y divide-slate-100">{stats.topRoutes.map((route) => (
                    <tr key={route.route}><td className="px-4 py-3 font-semibold">{route.route}</td><td className="px-4 py-3">{route.bookings}</td></tr>
                  ))}</tbody>
                </table>
              </div>
              : <EmptyState title="No route activity yet" />}
          </section>
        </>
      )}
    </main>
  )
}
