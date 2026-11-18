import { BUS_TYPES } from './constants'

export function blankAdminForm(tab) {
  if (tab === 'buses') return { name: '', busNumber: '', type: 'AC_SEATER', totalSeats: '40', ladiesOnlySeats: '' }
  if (tab === 'routes') return { source: '', destination: '' }
  return { busId: '', routeId: '', departureTime: '', arrivalTime: '', price: '' }
}

export function adminRowValues(tab, row) {
  if (tab === 'buses') return { ...row, totalSeats: String(row.totalSeats), ladiesOnlySeats: '' }
  if (tab === 'routes') return { source: row.source, destination: row.destination }
  return {
    busId: String(row.busId),
    routeId: String(row.routeId),
    departureTime: row.departureTime?.slice(0, 16) || '',
    arrivalTime: row.arrivalTime?.slice(0, 16) || '',
    price: String(row.price),
  }
}

export function validateAdminForm(tab, values, editing) {
  const errors = {}
  if (tab === 'buses') {
    if (values.name.trim().length < 2 || values.name.trim().length > 100) errors.name = 'Name must be between 2 and 100 characters.'
    if (!values.busNumber.trim()) errors.busNumber = 'Bus number is required.'
    if (!BUS_TYPES.some((type) => type.value === values.type)) errors.type = 'Choose a bus type.'
    const count = Number(values.totalSeats)
    if (!editing && (!Number.isInteger(count) || count < 10 || count > 60)) errors.totalSeats = 'Choose 10 to 60 seats.'
    const ladies = values.ladiesOnlySeats.split(',').map((seat) => seat.trim()).filter(Boolean)
    if (new Set(ladies).size !== ladies.length) errors.ladiesOnlySeats = 'Seat numbers must not repeat.'
    if (!editing && ladies.some((seat) => !Array.from({ length: count }, (_, index) => `${String.fromCharCode(65 + Math.floor(index / 4))}${(index % 4) + 1}`).includes(seat))) {
      errors.ladiesOnlySeats = 'Choose seat numbers generated for this bus.'
    }
  }
  if (tab === 'routes') {
    if (!values.source.trim() || values.source.length > 100) errors.source = 'Source is required (100 characters max).'
    if (!values.destination.trim() || values.destination.length > 100) errors.destination = 'Destination is required (100 characters max).'
    if (values.source.trim().toLowerCase() === values.destination.trim().toLowerCase()) {
      errors.destination = 'Source and destination must differ.'
    }
  }
  if (tab === 'trips') {
    if (!values.busId) errors.busId = 'Select a bus.'
    if (!values.routeId) errors.routeId = 'Select a route.'
    const departure = new Date(values.departureTime).getTime()
    const arrival = new Date(values.arrivalTime).getTime()
    if (!Number.isFinite(departure) || departure <= Date.now()) errors.departureTime = 'Departure must be in the future.'
    if (!Number.isFinite(arrival) || arrival <= departure) errors.arrivalTime = 'Arrival must be after departure.'
    if (!Number.isFinite(Number(values.price)) || Number(values.price) <= 0) errors.price = 'Price must be greater than zero.'
  }
  return errors
}

export function adminPayload(tab, values, editing) {
  if (tab === 'buses') {
    const common = { name: values.name.trim(), busNumber: values.busNumber.trim(), type: values.type }
    return editing ? common : {
      ...common,
      totalSeats: Number(values.totalSeats),
      ladiesOnlySeats: values.ladiesOnlySeats.split(',').map((seat) => seat.trim()).filter(Boolean),
    }
  }
  if (tab === 'routes') return { source: values.source.trim(), destination: values.destination.trim() }
  const schedule = {
    departureTime: values.departureTime,
    arrivalTime: values.arrivalTime,
    price: Number(values.price),
  }
  return editing ? schedule : { busId: Number(values.busId), routeId: Number(values.routeId), ...schedule }
}

export function adminRecordLabel(tab, record) {
  if (tab === 'buses') return record?.name
  if (tab === 'routes') return `${record?.source} to ${record?.destination}`
  return `trip #${record?.id}`
}
