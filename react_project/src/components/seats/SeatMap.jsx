import Seat from './Seat'

export default function SeatMap({ seats, selectedIds, onToggle, disabled }) {
  const sortedSeats = [...seats].sort((a, b) => a.seatNumber.localeCompare(b.seatNumber, undefined, { numeric: true }))
  const rows = []
  for (let index = 0; index < sortedSeats.length; index += 4) {
    rows.push(sortedSeats.slice(index, index + 4))
  }
  const seatRows = ['row-start-1', 'row-start-2', 'row-start-4', 'row-start-5']

  return (
    <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white p-5">
      <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-500">Front of bus ← · Scroll horizontally for more seats</p>
      <div role="group" aria-label="Bus seat map, front to back from left to right" className="flex w-max items-start gap-3">
        {rows.map((row, rowIndex) => (
          <div key={row[0]?.seatId || rowIndex} aria-label={`Seat row ${rowIndex + 1}`}
            className="grid grid-rows-[3rem_3rem_1.5rem_3rem_3rem] items-center gap-y-2">
            {row.map((seat, index) => (
              <Seat
                key={seat.seatId}
                seat={seat}
                selected={selectedIds.includes(seat.seatId)}
                onToggle={onToggle}
                className={seatRows[index]}
                disabled={disabled}
              />
            ))}
          </div>
        ))}
      </div>
    </div>
  )
}
