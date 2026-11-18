import FormField from './FormField'

const inputClass = 'w-full rounded-lg border border-slate-300 bg-white px-3 py-2 focus:border-emerald-600 focus:outline-none'

export default function TripFields({ values, errors, onChange, buses, routes, firstField, editing }) {
  return (
    <>
      <FormField id="trip-bus" label="Bus" error={errors.busId}>
        <select ref={firstField} id="trip-bus" disabled={editing} value={values.busId} onChange={(event) => onChange('busId', event.target.value)} className={inputClass}>
          <option value="">Select a bus</option>
          {buses.map((bus) => <option key={bus.id} value={bus.id}>{bus.name} · {bus.busNumber}</option>)}
        </select>
      </FormField>
      <FormField id="trip-route" label="Route" error={errors.routeId}>
        <select id="trip-route" disabled={editing} value={values.routeId} onChange={(event) => onChange('routeId', event.target.value)} className={inputClass}>
          <option value="">Select a route</option>
          {routes.map((route) => <option key={route.id} value={route.id}>{route.source} to {route.destination}</option>)}
        </select>
      </FormField>
      <FormField id="trip-departure" label="Departure" error={errors.departureTime}>
        <input id="trip-departure" type="datetime-local" value={values.departureTime} onChange={(event) => onChange('departureTime', event.target.value)} className={inputClass} />
      </FormField>
      <FormField id="trip-arrival" label="Arrival" error={errors.arrivalTime}>
        <input id="trip-arrival" type="datetime-local" value={values.arrivalTime} onChange={(event) => onChange('arrivalTime', event.target.value)} className={inputClass} />
      </FormField>
      <FormField id="trip-price" label="Price per seat (INR)" error={errors.price}>
        <input id="trip-price" type="number" min="0.01" step="0.01" value={values.price} onChange={(event) => onChange('price', event.target.value)} className={inputClass} />
      </FormField>
    </>
  )
}
