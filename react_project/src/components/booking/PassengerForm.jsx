const genders = ['MALE', 'FEMALE', 'OTHER']

export default function PassengerForm({ seat, value, errors, onChange }) {
  return (
    <fieldset className="space-y-4 rounded-2xl border border-slate-200 bg-white p-5">
      <legend className="px-2 text-lg font-bold text-slate-900">Seat {seat.seatNumber}</legend>
      {seat.ladiesOnly && <p className="rounded-lg bg-pink-50 p-3 text-sm text-pink-900">Ladies-only seat: passenger gender must be FEMALE.</p>}
      <div>
        <label htmlFor={`passenger-${seat.seatId}-name`} className="mb-1 block text-sm font-semibold">Passenger name</label>
        <input
          id={`passenger-${seat.seatId}-name`}
          value={value.name}
          maxLength={100}
          onChange={(event) => onChange('name', event.target.value)}
          className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-emerald-600 focus:outline-none"
          aria-invalid={Boolean(errors.name)}
        />
        {errors.name && <p className="mt-1 text-sm text-red-700">{errors.name}</p>}
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label htmlFor={`passenger-${seat.seatId}-age`} className="mb-1 block text-sm font-semibold">Age</label>
          <input
            id={`passenger-${seat.seatId}-age`}
            type="number"
            min="1"
            max="100"
            step="1"
            value={value.age}
            onChange={(event) => onChange('age', event.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-emerald-600 focus:outline-none"
            aria-invalid={Boolean(errors.age)}
          />
          {errors.age && <p className="mt-1 text-sm text-red-700">{errors.age}</p>}
        </div>
        <div>
          <label htmlFor={`passenger-${seat.seatId}-gender`} className="mb-1 block text-sm font-semibold">Gender</label>
          <select
            id={`passenger-${seat.seatId}-gender`}
            value={value.gender}
            onChange={(event) => onChange('gender', event.target.value)}
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 focus:border-emerald-600 focus:outline-none"
            aria-invalid={Boolean(errors.gender)}
          >
            <option value="">Select gender</option>
            {genders.map((gender) => <option key={gender} value={gender}>{gender}</option>)}
          </select>
          {errors.gender && <p className="mt-1 text-sm text-red-700">{errors.gender}</p>}
        </div>
      </div>
    </fieldset>
  )
}
