export const TOKEN_STORAGE_KEY = 'seatsync_token'

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const BUS_TYPES = [
  { value: 'AC_SLEEPER', label: 'AC Sleeper' },
  { value: 'AC_SEATER', label: 'AC Seater' },
  { value: 'NON_AC_SLEEPER', label: 'Non-AC Sleeper' },
  { value: 'NON_AC_SEATER', label: 'Non-AC Seater' },
]
