import { useEffect, useRef } from 'react'

export default function ConfirmModal({
  open, title, message, confirmLabel = 'Confirm', cancelLabel = 'Cancel', loading = false, error, onClose, onConfirm,
}) {
  const dialogRef = useRef(null)

  useEffect(() => {
    if (!open) return undefined
    const previousFocus = document.activeElement
    dialogRef.current?.querySelector('button')?.focus()
    return () => previousFocus?.focus()
  }, [open])

  const handleKeyDown = (event) => {
    if (event.key === 'Escape' && !loading) onClose()
    if (event.key !== 'Tab') return
    const buttons = dialogRef.current?.querySelectorAll('button:not([disabled])')
    if (!buttons?.length) return
    const first = buttons[0]
    const last = buttons[buttons.length - 1]
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
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-950/60 p-4" onKeyDown={handleKeyDown}>
      <section ref={dialogRef} role="dialog" aria-modal="true" aria-labelledby="confirm-title" className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h2 id="confirm-title" className="text-xl font-black text-slate-900">{title}</h2>
        <p className="mt-3 text-sm leading-6 text-slate-600">{message}</p>
        {error && <p className="mt-3 text-sm text-red-700" role="alert">{error}</p>}
        <div className="mt-6 flex justify-end gap-3">
          <button type="button" disabled={loading} onClick={onClose} className="rounded-lg border border-slate-300 px-4 py-2 font-semibold disabled:opacity-50">
            {cancelLabel}
          </button>
          <button type="button" disabled={loading} onClick={onConfirm} className="rounded-lg bg-red-700 px-4 py-2 font-bold text-white disabled:opacity-50">
            {loading ? 'Working…' : confirmLabel}
          </button>
        </div>
      </section>
    </div>
  )
}
