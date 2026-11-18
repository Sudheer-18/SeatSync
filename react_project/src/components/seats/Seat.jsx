const styles = {
  AVAILABLE: 'border-emerald-300 bg-emerald-50 text-emerald-900 hover:bg-emerald-100',
  SELECTED: 'border-blue-600 bg-blue-600 text-white',
  HELD: 'border-blue-600 bg-blue-50 text-blue-900',
  OTHER_HELD: 'border-amber-300 bg-amber-100 text-amber-900',
  BOOKED: 'border-slate-300 bg-slate-200 text-slate-500',
}

export default function Seat({ seat, selected, onToggle, className = '', disabled: interactionDisabled = false }) {
  const state = seat.status === 'BOOKED' ? 'BOOKED'
      : seat.status === 'HELD' ? (seat.heldByMe ? 'HELD' : 'OTHER_HELD')
        : selected ? 'SELECTED' : 'AVAILABLE'
  const label = {
    SELECTED: 'selected',
    AVAILABLE: 'available',
    HELD: 'held by you',
    OTHER_HELD: 'held by another traveler',
    BOOKED: 'booked',
  }[state]
  const disabled = interactionDisabled || (state !== 'AVAILABLE' && state !== 'SELECTED')

  return (
    <button
      type="button"
      disabled={disabled}
      onClick={() => onToggle(seat)}
      aria-label={`Seat ${seat.seatNumber}, ${label}${seat.ladiesOnly ? ', ladies only' : ''}`}
      className={`relative flex h-12 w-12 items-center justify-center rounded-xl border-2 text-sm font-bold focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-700 disabled:cursor-not-allowed ${styles[state]} ${seat.ladiesOnly ? 'ring-2 ring-pink-400 ring-offset-1' : ''} ${className}`}
    >
      {seat.seatNumber}
      {seat.ladiesOnly && <span className="absolute -right-1 -top-2 rounded-full bg-pink-100 px-1 text-[9px] text-pink-900">F</span>}
    </button>
  )
}
