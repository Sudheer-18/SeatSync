const items = [
  ['bg-emerald-50 border-emerald-300', 'Available'],
  ['bg-blue-600 border-blue-600', 'Selected'],
  ['bg-blue-50 border-blue-600', 'Held by you'],
  ['bg-amber-100 border-amber-300', 'Held'],
  ['bg-slate-200 border-slate-300', 'Booked'],
]

export default function SeatLegend() {
  return (
    <ul className="flex flex-wrap gap-x-5 gap-y-3 text-xs text-slate-700" aria-label="Seat status legend">
      {items.map(([color, label]) => (
        <li key={label} className="flex items-center gap-2">
          <span className={`h-4 w-4 rounded border ${color}`} aria-hidden="true" />
          {label}
        </li>
      ))}
      <li className="flex items-center gap-2">
        <span className="h-4 w-4 rounded border-2 border-pink-400" aria-hidden="true" />
        Ladies only
      </li>
    </ul>
  )
}
