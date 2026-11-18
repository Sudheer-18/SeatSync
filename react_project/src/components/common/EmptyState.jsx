export default function EmptyState({ title, message }) {
  return (
    <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-12 text-center">
      <h2 className="text-lg font-bold text-slate-900">{title}</h2>
      {message && <p className="mt-2 text-sm text-slate-600">{message}</p>}
    </div>
  )
}
