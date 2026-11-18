import { BUS_TYPES } from '../../utils/constants'

export default function FilterBar({ selectedTypes, sort, onChange }) {
  const toggleType = (type) => {
    const nextTypes = selectedTypes.includes(type)
      ? selectedTypes.filter((value) => value !== type)
      : [...selectedTypes, type]
    onChange('type', nextTypes)
  }

  return (
    <section className="flex flex-col gap-5 rounded-2xl border border-slate-200 bg-white p-4 sm:flex-row sm:items-center sm:justify-between">
      <fieldset>
        <legend className="mb-2 text-sm font-bold text-slate-800">Bus type</legend>
        <div className="flex flex-wrap gap-x-4 gap-y-2">
          {BUS_TYPES.map((type) => (
            <label key={type.value} className="flex items-center gap-2 text-sm text-slate-700">
              <input
                type="checkbox"
                checked={selectedTypes.includes(type.value)}
                onChange={() => toggleType(type.value)}
                className="h-4 w-4 accent-emerald-600"
              />
              {type.label}
            </label>
          ))}
        </div>
      </fieldset>
      <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
        Sort by
        <select
          value={sort}
          onChange={(event) => onChange('sort', event.target.value)}
          className="rounded-lg border border-slate-300 bg-white px-3 py-2"
        >
          <option value="">Recommended</option>
          <option value="price">Price: low to high</option>
          <option value="departure">Departure: earliest</option>
        </select>
      </label>
    </section>
  )
}
