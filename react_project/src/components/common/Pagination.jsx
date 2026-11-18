export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null

  return (
    <nav className="flex items-center justify-center gap-4 py-6" aria-label="Pagination">
      <button
        type="button"
        className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold disabled:opacity-40"
        disabled={page <= 0}
        onClick={() => onChange(page - 1)}
      >
        Previous
      </button>
      <span className="text-sm text-slate-600">Page {page + 1} of {totalPages}</span>
      <button
        type="button"
        className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold disabled:opacity-40"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Next
      </button>
    </nav>
  )
}
