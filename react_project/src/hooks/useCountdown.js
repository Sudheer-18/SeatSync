import { useEffect, useState } from 'react'

function remainingSeconds(expiresAt) {
  const expiry = new Date(expiresAt).getTime()
  return Number.isNaN(expiry) ? 0 : Math.max(0, Math.ceil((expiry - Date.now()) / 1000))
}

export default function useCountdown(expiresAt, onExpire) {
  const [seconds, setSeconds] = useState(() => remainingSeconds(expiresAt))

  useEffect(() => {
    let completed = false
    const update = () => {
      const next = remainingSeconds(expiresAt)
      setSeconds(next)
      if (next === 0 && !completed) {
        completed = true
        onExpire()
      }
    }

    update()
    const timer = window.setInterval(update, 1000)
    return () => window.clearInterval(timer)
  }, [expiresAt, onExpire])

  return seconds
}
