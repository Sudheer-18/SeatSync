import { useEffect, useRef } from 'react'

export default function FormModal({ open, title, loading, error, onClose, onSubmit, children }) {
  const firstField = useRef(null)
  const formRef = useRef(null)

  useEffect(() => {
    if (!open) return undefined
    const previousFocus = document.activeElement
    const firstAvailable = formRef.current?.querySelector('input:not([disabled]), select:not([disabled])')
    const target = firstField.current?.disabled ? firstAvailable : firstField.current
    target?.focus()
    return () => previousFocus?.focus()
  }, [open])

  const handleKeyDown = (event) => {
    if (event.key === 'Escape' && !loading) {
      event.preventDefault()
      onClose()
    }
    if (event.key !== 'Tab') return
    const items = formRef.current?.querySelectorAll('input:not([disabled]), select:not([disabled]), button:not([disabled])')
    if (!items?.length) return
    const first = items[0]
    const last = items[items.length - 1]
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault()
      last.focus()
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault()
      first.focus()
    }
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-30 flex items-center justify-center overflow-y-auto bg-slate-950/60 p-4">
      <form
        ref={formRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="form-modal-title"
        onSubmit={onSubmit}
        onKeyDown={handleKeyDown}
        className="my-auto w-full max-w-xl rounded-2xl bg-white p-6 shadow-xl"
      >
        <h2 id="form-modal-title" className="text-xl font-black text-slate-900">{title}</h2>
        <div className="mt-5 space-y-4">{children(firstField)}</div>
        {error && <p className="mt-4 text-sm text-red-700" role="alert">{error}</p>}
        <div className="mt-6 flex justify-end gap-3">
          <button type="button" disabled={loading} onClick={onClose} className="rounded-lg border border-slate-300 px-4 py-2 font-semibold">Close</button>
          <button type="submit" disabled={loading} className="rounded-lg bg-emerald-600 px-4 py-2 font-bold text-white disabled:opacity-50">
            {loading ? 'Saving…' : 'Save'}
          </button>
        </div>
      </form>
    </div>
  )
}
