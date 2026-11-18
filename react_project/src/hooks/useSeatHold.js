import { useCallback, useEffect, useRef, useState } from 'react'
import holdApi from '../api/holdApi'
import useToast from './useToast'

const selectionKey = (tripId) => `seatsync_selected_${tripId}`

function restoreSelection(tripId) {
  const saved = sessionStorage.getItem(selectionKey(tripId))
  return saved ? JSON.parse(saved) : []
}

export default function useSeatHold(tripId, refreshSeats) {
  const [selectedIds, setSelectedIds] = useState(() => restoreSelection(tripId))
  const [hold, setHold] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const holdRef = useRef(null)
  const preserveRef = useRef(false)
  const releaseRef = useRef(false)
  const addToast = useToast()

  useEffect(() => {
    if (hold) sessionStorage.removeItem(selectionKey(tripId))
    else if (selectedIds.length) sessionStorage.setItem(selectionKey(tripId), JSON.stringify(selectedIds))
    else sessionStorage.removeItem(selectionKey(tripId))
  }, [hold, selectedIds, tripId])

  useEffect(() => () => {
    if (holdRef.current && !preserveRef.current && !releaseRef.current) {
      holdApi.release(tripId, holdRef.current.seatIds)
        .catch((requestError) => addToast(requestError.message, 'error'))
    }
  }, [addToast, tripId])

  const toggleSeat = useCallback((seat) => {
    if (holdRef.current || seat.status !== 'AVAILABLE') return
    setSelectedIds((current) => {
      if (current.includes(seat.seatId)) return current.filter((id) => id !== seat.seatId)
      if (current.length >= 6) {
        addToast('You can select up to 6 seats.', 'error')
        return current
      }
      return [...current, seat.seatId]
    })
  }, [addToast])

  const holdSeats = async () => {
    if (!selectedIds.length) return
    setBusy(true)
    setError('')
    try {
      const result = await holdApi.hold(tripId, selectedIds)
      if (!result.expiresAt || !Array.isArray(result.seatIds)) throw new Error('The hold response was incomplete.')
      holdRef.current = result
      setHold(result)
      setSelectedIds(result.seatIds)
    } catch (requestError) {
      const message = requestError.message || 'Unable to hold these seats.'
      if (requestError.status === 409) {
        addToast(message, 'error')
        setSelectedIds([])
        refreshSeats().catch((refreshError) => setError(refreshError.message))
      } else {
        setError(message)
        addToast(message, 'error')
      }
    } finally {
      setBusy(false)
    }
  }

  const release = async () => {
    if (!holdRef.current || releaseRef.current) return
    releaseRef.current = true
    setBusy(true)
    setError('')
    try {
      await holdApi.release(tripId, holdRef.current.seatIds)
      holdRef.current = null
      setHold(null)
      setSelectedIds([])
      addToast('Your seat hold has been released.')
      await refreshSeats()
    } catch (requestError) {
      setError(requestError.message)
      addToast(requestError.message, 'error')
    } finally {
      releaseRef.current = false
      setBusy(false)
    }
  }

  const expireHold = useCallback(() => {
    if (!holdRef.current) return
    holdRef.current = null
    setHold(null)
    setSelectedIds([])
    addToast('Your hold expired.', 'error')
    refreshSeats().catch((requestError) => setError(requestError.message))
  }, [addToast, refreshSeats])

  const preserveHold = () => { preserveRef.current = true }

  return { selectedIds, hold, busy, error, setError, toggleSeat, holdSeats, release, expireHold, preserveHold }
}
