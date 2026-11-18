import BusFields from './BusFields'
import RouteFields from './RouteFields'
import TripFields from './TripFields'

export default function AdminFormFields({ tab, values, errors, onChange, editing, options, firstField }) {
  const props = { values, errors, onChange, editing, firstField }
  if (tab === 'buses') return <BusFields {...props} />
  if (tab === 'routes') return <RouteFields {...props} />
  return <TripFields {...props} buses={options.buses} routes={options.routes} />
}
