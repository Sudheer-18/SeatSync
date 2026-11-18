import { useState } from 'react'
import bookingApi from '../../api/bookingApi'
import ErrorBanner from '../common/ErrorBanner'
import { formatINR } from '../../utils/formatters'

export default function UpiPaymentPanel({ booking, instructions }) {
  const [reference, setReference] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [submitted, setSubmitted] = useState(Boolean(booking.payment.paymentReference))

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      await bookingApi.submitPaymentReference(booking.bookingId, reference.trim())
      setSubmitted(true)
    } catch (requestError) {
      setError(requestError.message || 'Unable to submit the transaction reference.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="space-y-3 rounded-2xl border border-amber-300 bg-amber-50 p-5">
      <h2 className="text-lg font-bold text-amber-950">Pay by UPI</h2>
      <p className="text-sm text-amber-900">Pay {formatINR(instructions.amount)} and enter the UPI transaction reference. Your ticket is confirmed only after the admin verifies payment.</p>
      {instructions.upiId ? (
        <div className="space-y-2">
          <p>UPI ID: <strong>{instructions.upiId}</strong></p>
          <a href={instructions.paymentUri} className="inline-block rounded-xl bg-emerald-700 px-4 py-2 font-bold text-white">Open UPI app</a>
        </div>
      ) : <ErrorBanner message="UPI payment is not configured yet. Ask the administrator to set the merchant UPI ID." />}
      <p className="text-sm">Payment window ends: {new Date(instructions.expiresAt).toLocaleString()}</p>
      {submitted ? <p className="font-semibold text-emerald-800">Transaction reference submitted. Waiting for admin verification.</p> : (
        <form onSubmit={submit} className="flex flex-wrap gap-2">
          <label className="sr-only" htmlFor="upi-reference">UPI transaction reference</label>
          <input id="upi-reference" value={reference} onChange={(event) => setReference(event.target.value)}
            minLength={8} maxLength={100} pattern="[A-Za-z0-9-]{8,100}" required
            placeholder="UPI transaction ID / UTR" className="min-w-60 flex-1 rounded-lg border border-slate-300 px-3 py-2" />
          <button type="submit" disabled={busy || !instructions.upiId}
            className="rounded-lg bg-slate-900 px-4 py-2 font-bold text-white disabled:opacity-50">
            {busy ? 'Submitting…' : 'Submit reference'}
          </button>
        </form>
      )}
      {error && <ErrorBanner message={error} />}
    </section>
  )
}
