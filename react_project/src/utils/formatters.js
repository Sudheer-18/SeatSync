import { BUS_TYPES } from './constants'

export function formatDateTime(value) {
  if (!value || Number.isNaN(new Date(value).getTime())) return 'Time unavailable'
  return new Intl.DateTimeFormat('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value))
}

export function formatDate(value) {
  const date = new Date(`${value}T00:00:00`)
  if (!value || Number.isNaN(date.getTime())) return 'Date unavailable'
  return new Intl.DateTimeFormat('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date)
}

export function formatINR(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(Number(value))
}

export function formatBusType(value) {
  return BUS_TYPES.find((type) => type.value === value)?.label || value
}

export function getDuration(start, end) {
  const minutes = Math.round((new Date(end) - new Date(start)) / 60000)
  if (!Number.isFinite(minutes) || minutes < 0) return 'Duration unavailable'
  return `${Math.floor(minutes / 60)}h ${minutes % 60}m`
}
