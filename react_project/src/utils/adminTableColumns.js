import { formatDateTime, formatINR } from './formatters'

export default function adminTableColumns(tab) {
  if (tab === 'buses') return [
    { key: 'name', label: 'Bus name' }, { key: 'busNumber', label: 'Bus number' },
    { key: 'type', label: 'Type' }, { key: 'totalSeats', label: 'Seats' },
  ]
  if (tab === 'routes') return [
    { key: 'source', label: 'Source' }, { key: 'destination', label: 'Destination' },
  ]
  return [
    { key: 'busName', label: 'Bus' },
    { key: 'routeName', label: 'Route', render: (row) => row.routeName || `${row.source} to ${row.destination}` },
    { key: 'departureTime', label: 'Departure', render: (row) => formatDateTime(row.departureTime) },
    { key: 'price', label: 'Price', render: (row) => formatINR(row.price) },
  ]
}
