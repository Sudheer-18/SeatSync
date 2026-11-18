import FormField from './FormField'

const inputClass = 'w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-emerald-600 focus:outline-none'

export default function RouteFields({ values, errors, onChange, firstField }) {
  return (
    <>
      <FormField id="route-source" label="Source city" error={errors.source}>
        <input ref={firstField} id="route-source" value={values.source} maxLength={100} onChange={(event) => onChange('source', event.target.value)} className={inputClass} />
      </FormField>
      <FormField id="route-destination" label="Destination city" error={errors.destination}>
        <input id="route-destination" value={values.destination} maxLength={100} onChange={(event) => onChange('destination', event.target.value)} className={inputClass} />
      </FormField>
    </>
  )
}
