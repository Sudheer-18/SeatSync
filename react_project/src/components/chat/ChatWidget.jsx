import { useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import chatApi from '../../api/chatApi'
import useAuth from '../../hooks/useAuth'

const unavailable = 'Assistant is unavailable. Please use search.'

export default function ChatWidget({ open, setOpen }) {
  const { user } = useAuth()
  const location = useLocation()
  const [messages, setMessages] = useState([])
  const [draft, setDraft] = useState('')
  const [pendingAction, setPendingAction] = useState(null)
  const [historyLoaded, setHistoryLoaded] = useState(false)
  const [sending, setSending] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    if (!open || !user || historyLoaded) return
    let active = true
    chatApi.history()
      .then((history) => {
        if (!Array.isArray(history)) throw new Error('Chat history response was invalid.')
        if (active) setMessages(history)
      })
      .catch(() => { if (active) setMessages([{ role: 'ASSISTANT', content: unavailable }]) })
      .finally(() => { if (active) setHistoryLoaded(true) })
    return () => { active = false }
  }, [open, user, historyLoaded])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, sending])

  const send = async (message) => {
    const text = message.trim()
    if (!text || text.length > 500 || sending) return
    setMessages((current) => [...current, { role: 'USER', content: text }])
    setDraft('')
    setPendingAction(null)
    setSending(true)
    try {
      const response = await chatApi.send(text)
      setMessages((current) => [...current, { role: 'ASSISTANT', content: response.reply }])
      setPendingAction(response.pendingAction?.requiresConfirmation ? response.pendingAction : null)
    } catch {
      setMessages((current) => [...current, { role: 'ASSISTANT', content: unavailable }])
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="fixed bottom-5 right-5 z-20">
      {open && (
        <section className="mb-3 flex h-[min(34rem,calc(100vh-7rem))] w-[min(23rem,calc(100vw-2rem))] flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xl" aria-label="SeatSync assistant">
          <header className="flex items-center justify-between bg-slate-950 px-4 py-3 text-white">
            <div><p className="font-bold">SeatSync assistant</p><p className="text-xs text-slate-300">Trip help, anytime</p></div>
            <button type="button" onClick={() => setOpen(false)} aria-label="Close chat" className="rounded px-2 py-1 text-slate-200 hover:bg-slate-800">Close</button>
          </header>
          <div className="flex-1 space-y-3 overflow-y-auto p-4" aria-live="polite">
            {!user ? (
              <p className="text-sm text-slate-600">Sign in to chat with the assistant. <Link to="/login" state={{ from: location }} className="font-bold text-emerald-700">Sign in</Link></p>
            ) : messages.map((message, index) => (
              <div key={`${message.createdAt || 'message'}-${index}`} className={`max-w-[88%] rounded-2xl px-3 py-2 text-sm ${message.role === 'USER' ? 'ml-auto bg-emerald-100 text-slate-900' : 'bg-slate-100 text-slate-800'}`}>
                {message.content}
              </div>
            ))}
            {sending && <p className="text-sm text-slate-500" role="status">Assistant is typing…</p>}
            {pendingAction && user && (
              <div className="rounded-xl border border-amber-300 bg-amber-50 p-3 text-sm text-amber-950">
                <p>Confirm this action for booking #{pendingAction.bookingId}?</p>
                <div className="mt-3 flex gap-2">
                  <button type="button" disabled={sending} onClick={() => send('Yes, confirm')} className="rounded-lg bg-emerald-700 px-3 py-2 font-bold text-white disabled:opacity-50">Confirm</button>
                  <button type="button" disabled={sending} onClick={() => send('No, dismiss')} className="rounded-lg border border-slate-300 px-3 py-2 font-bold disabled:opacity-50">Dismiss</button>
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>
          {user && (
            <form onSubmit={(event) => { event.preventDefault(); send(draft) }} className="flex gap-2 border-t border-slate-200 p-3">
              <input value={draft} maxLength={500} onChange={(event) => setDraft(event.target.value)} aria-label="Message assistant" placeholder="Ask about your trip…" className="min-w-0 flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-600 focus:outline-none" />
              <button type="submit" disabled={sending || !draft.trim()} className="rounded-lg bg-emerald-600 px-3 py-2 text-sm font-bold text-white disabled:opacity-50">Send</button>
            </form>
          )}
        </section>
      )}
    </div>
  )
}
