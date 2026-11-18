import FormField from './FormField'
import { BUS_TYPES } from '../../utils/constants'

const inputClass = 'w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-emerald-600 focus:outline-none'

export default function BusFields({ values, errors, onChange, editing, firstField }) {
  return (
    <>
      <FormField id="bus-name" label="Bus name" error={errors.name}>
        <input ref={firstField} id="bus-name" value={values.name} onChange={(event) => onChange('name', event.target.value)} maxLength={100} className={inputClass} />
      </FormField>
      <FormField id="bus-number" label="Bus number" error={errors.busNumber}>
        <input id="bus-number" value={values.busNumber} onChange={(event) => onChange('busNumber', event.target.value)} maxLength={30} className={inputClass} />
      </FormField>
      <FormField id="bus-type" label="Bus type" error={errors.type}>
        <select id="bus-type" value={values.type} onChange={(event) => onChange('type', event.target.value)} className={inputClass}>
          {BUS_TYPES.map((type) => <option key={type.value} value={type.value}>{type.label}</option>)}
        </select>
      </FormField>
      {!editing && (
        <>
          <FormField id="bus-seats" label="Total seats (10–60)" error={errors.totalSeats}>
            <input id="bus-seats" type="number" min="10" max="60" step="1" value={values.totalSeats} onChange={(event) => onChange('totalSeats', event.target.value)} className={inputClass} />
          </FormField>
          <FormField id="ladies-seats" label="Ladies-only seats (comma-separated)" error={errors.ladiesOnlySeats}>
            <input id="ladies-seats" value={values.ladiesOnlySeats} onChange={(event) => onChange('ladiesOnlySeats', event.target.value)} placeholder="A1, A2" className={inputClass} />
          </FormField>
        </>
      )}
    </>
  )
}
